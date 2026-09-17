package dev.scalaide.workbench;

import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.*;
import com.intellij.execution.ui.*;
import com.intellij.ide.trustedProjects.TrustedProjects;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.VirtualFileManager;
import dev.scalaide.core.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.BorderLayout;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

/** Owns build processes for one project. No file-system or process work on the EDT. */
@Service(Service.Level.PROJECT)
public final class BuildService implements Disposable {
    private static final Logger LOG = Logger.getInstance(BuildService.class);
    private final Project project;
    private final AtomicBoolean busy = new AtomicBoolean();
    private volatile KillableColoredProcessHandler running;
    private volatile boolean disposed;

    public BuildService(Project project) { this.project = project; }
    public boolean isBusy() { return busy.get(); }

    public void run(BuildOperation operation) {
        if (disposed || project.isDisposed() || project.getBasePath() == null || !busy.compareAndSet(false, true)) return;
        // Follow the host's project-trust state. Opening a folder must never execute a build implicitly.
        if (!TrustedProjects.isProjectTrusted(project)) {
            busy.set(false);
            notifyUser("Trust this project in IntelliJ before running its build scripts.", NotificationType.WARNING);
            return;
        }
        FileDocumentManager.getInstance().saveAllDocuments();
        // Snapshot project settings and SDK while on the UI thread; workers receive immutable values.
        var settings = WorkbenchSettings.get(project).getState();
        String selected = settings.buildTool;
        String mill = settings.millExecutable;
        String module = settings.millModule;
        String cli = settings.scalaCliExecutable;
        var sdk = ProjectRootManager.getInstance(project).getProjectSdk();
        String javaHome = !settings.javaHome.isBlank() ? settings.javaHome :
                sdk != null && sdk.getHomePath() != null ? sdk.getHomePath() : System.getProperty("java.home");
        Path root = Path.of(project.getBasePath());

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                var layout = new ProjectDetector().detect(root);
                BuildTool tool;
                if (!selected.isBlank()) tool = BuildTool.valueOf(selected);
                else {
                    var candidates = layout.buildTools().stream().filter(t -> t != BuildTool.BSP).toList();
                    if (candidates.size() != 1)
                        throw new IllegalStateException("Select a build tool in Scala Workbench before compiling or testing.");
                    tool = candidates.getFirst();
                }
                CommandPlan plan;
                if (tool == BuildTool.SBT) {
                    var scala = PluginManagerCore.getPlugin(PluginId.getId("org.intellij.scala"));
                    if (scala == null) throw new IllegalStateException("The Scala plugin is unavailable.");
                    Path launcher = scala.getPluginPath().resolve("launcher/sbt-launch.jar");
                    if (!Files.isRegularFile(launcher)) throw new IllegalStateException("Scala plugin sbt launcher is missing: " + launcher);
                    String java = Path.of(javaHome, "bin", SystemInfo.isWindows ? "java.exe" : "java").toString();
                    plan = BuildCommands.sbt(root, java, launcher, operation);
                } else {
                    plan = BuildCommands.external(root, tool, tool == BuildTool.MILL ? mill : cli, module, operation);
                }
                if (disposed || project.isDisposed()) { busy.set(false); return; }
                var command = BuildProcessCommand.create(plan);
                var handler = new KillableColoredProcessHandler(command);
                running = handler;
                if (disposed || project.isDisposed()) { handler.destroyProcess(); busy.set(false); return; }
                ApplicationManager.getApplication().invokeLater(() -> attachConsole(plan, handler));
            } catch (Exception failure) {
                busy.set(false);
                LOG.warn("Scala build could not start", failure);
                notifyUser("Build could not start: " + failure.getMessage(), NotificationType.ERROR);
            }
        });
    }

    private void attachConsole(CommandPlan plan, KillableColoredProcessHandler handler) {
        if (disposed || project.isDisposed()) { handler.destroyProcess(); busy.set(false); return; }
        try {
            ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
            console.attachToProcess(handler);
            console.print(plan.label() + "\nWorking directory: " + plan.directory() + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(console.getComponent(), BorderLayout.CENTER);
            var actions = new DefaultActionGroup();
            actions.add(new AnAction("Stop", "Stop this build and its child processes", com.intellij.icons.AllIcons.Actions.Suspend) {
                @Override public void actionPerformed(@NotNull AnActionEvent e) {
                    if (handler.isProcessTerminating() && handler.canKillProcess()) handler.killProcess();
                    else handler.destroyProcess();
                }
                @Override public void update(@NotNull AnActionEvent e) { e.getPresentation().setEnabled(!handler.isProcessTerminated()); }
                @Override public @NotNull ActionUpdateThread getActionUpdateThread() { return ActionUpdateThread.BGT; }
            });
            for (AnAction action : console.createConsoleActions()) actions.add(action);
            var toolbar = ActionManager.getInstance().createActionToolbar("ScalaWorkbench.Build", actions, false);
            toolbar.setTargetComponent(panel);
            panel.add(toolbar.getComponent(), BorderLayout.WEST);
            var descriptor = new RunContentDescriptor(console, handler, panel, plan.label());
            Disposer.register(descriptor, () -> terminateOnDispose(handler));
            handler.addProcessListener(new ProcessListener() {
                @Override public void processTerminated(@NotNull ProcessEvent event) {
                    running = null;
                    busy.set(false);
                    console.print("\nProcess finished with exit code " + event.getExitCode() + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
                    LOG.info(plan.label() + " exited " + event.getExitCode());
                    notifyUser(plan.label() + (event.getExitCode() == 0 ? " succeeded" : " exited with code " + event.getExitCode()),
                            event.getExitCode() == 0 ? NotificationType.INFORMATION : NotificationType.WARNING);
                    if (!project.isDisposed()) VirtualFileManager.getInstance().asyncRefresh(null);
                }
            });
            RunContentManager.getInstance(project).showRunContent(DefaultRunExecutor.getRunExecutorInstance(), descriptor);
            handler.startNotify();
        } catch (Exception failure) {
            handler.destroyProcess();
            running = null;
            busy.set(false);
            LOG.warn("Could not attach build console", failure);
            notifyUser("Could not attach build console: " + failure.getMessage(), NotificationType.ERROR);
        }
    }

    private void notifyUser(String message, NotificationType type) {
        ApplicationManager.getApplication().invokeLater(() -> {
            if (!disposed && !project.isDisposed())
                NotificationGroupManager.getInstance().getNotificationGroup("Scala Workbench")
                        .createNotification(message, type).notify(project);
        });
    }

    @Override public void dispose() {
        disposed = true;
        var handler = running;
        if (handler != null) terminateOnDispose(handler);
        busy.set(false);
    }

    private static void terminateOnDispose(KillableColoredProcessHandler handler) {
        if (handler.isProcessTerminated()) return;
        if (handler.canKillProcess()) handler.killProcess();
        else handler.destroyProcess();
    }
}
