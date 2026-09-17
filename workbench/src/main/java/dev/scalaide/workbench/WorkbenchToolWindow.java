package dev.scalaide.workbench;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.components.*;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.ui.JBUI;
import dev.scalaide.core.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

public final class WorkbenchToolWindow implements ToolWindowFactory, DumbAware {
    @Override public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow window) {
        JPanel panel = new WorkbenchPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(JBUI.Borders.empty(16));
        var alive = new AtomicBoolean(true);
        var loading = new AtomicBoolean(false);

        add(panel, new BrandHeader());
        add(panel, Box.createVerticalStrut(12));
        add(panel, actionButton("Apply Zakaria Graphite theme", "ScalaWorkbench.ApplyBrandTheme", panel));
        add(panel, Box.createVerticalStrut(20));
        add(panel, heading("01 / YOUR PROJECT"));
        var projectName = new JBLabel(project.getName());
        projectName.setFont(BrandIdentity.display(18));
        projectName.setForeground(BrandIdentity.TEXT);
        add(panel, projectName);
        var rootLabel = new JBTextArea(project.getBasePath() == null ? "No project directory" : project.getBasePath());
        rootLabel.setEditable(false); rootLabel.setLineWrap(true); rootLabel.setWrapStyleWord(true);
        rootLabel.setFont(BrandIdentity.body(12));
        rootLabel.setForeground(BrandIdentity.MUTED);
        rootLabel.setOpaque(false); rootLabel.setRows(2);
        add(panel, rootLabel);
        add(panel, Box.createVerticalStrut(8));
        var detected = new JBLabel("Inspecting build files…");
        var imported = new JBLabel();
        add(panel, detected); add(panel, imported);
        var scala = PluginManagerCore.getPlugin(PluginId.getId("org.intellij.scala"));
        add(panel, new JBLabel("Scala engine: " + (scala == null ? "unavailable" : scala.getVersion())));
        add(panel, Box.createVerticalStrut(14));
        add(panel, heading("02 / BUILD & TEST"));
        var choices = new JComboBox<>(new String[]{"Detect automatically", "sbt", "Mill", "Scala CLI", "BSP (native integration)"});
        String saved = WorkbenchSettings.get(project).getState().buildTool;
        choices.setSelectedIndex(switch (saved) { case "SBT" -> 1; case "MILL" -> 2; case "SCALA_CLI" -> 3; case "BSP" -> 4; default -> 0; });
        choices.addActionListener(e -> WorkbenchSettings.get(project).getState().buildTool =
                switch (choices.getSelectedIndex()) { case 1 -> "SBT"; case 2 -> "MILL"; case 3 -> "SCALA_CLI"; case 4 -> "BSP"; default -> ""; });
        choices.setMaximumSize(new Dimension(Integer.MAX_VALUE, choices.getPreferredSize().height));
        choices.getAccessibleContext().setAccessibleName("Project build tool");
        add(panel, choices);
        add(panel, actionButton("Compile project", "ScalaWorkbench.Compile", panel));
        add(panel, actionButton("Run project tests", "ScalaWorkbench.Test", panel));
        add(panel, actionButton("Open terminal", "ScalaWorkbench.Terminal", panel));
        add(panel, actionButton("Configure build tools…", "ScalaWorkbench.Settings", panel));
        add(panel, Box.createVerticalStrut(16));
        add(panel, heading("03 / NAVIGATE & INSPECT"));
        add(panel, actionButton("Search Everywhere", "SearchEverywhere", panel));
        add(panel, actionButton("Find Action", "GotoAction", panel));
        add(panel, actionButton("Project structure / JDK…", "ShowProjectStructureSettings", panel));

        var hint = new JBTextArea("Open an sbt project through File → Open and import its build. " +
                "For Mill or Scala CLI, generate a BSP connection with that tool and open it using the Scala plugin's BSP import. " +
                "Compiler-backed features become available after import and indexing.");
        hint.setEditable(false); hint.setOpaque(false); hint.setLineWrap(true); hint.setWrapStyleWord(true);
        hint.setFont(BrandIdentity.body(12));
        hint.setForeground(BrandIdentity.MUTED);
        hint.setRows(6); hint.setBorder(JBUI.Borders.emptyTop(16));
        add(panel, hint);
        var refresh = new JButton("Refresh project status");
        add(panel, refresh);
        panel.add(Box.createVerticalGlue());
        Runnable inspect = () -> {
            if (!loading.compareAndSet(false, true)) return;
            refresh.setEnabled(false);
            imported.setText("IDE modules: " + ModuleManager.getInstance(project).getModules().length + " (see Project Structure)");
            ApplicationManager.getApplication().executeOnPooledThread(() -> {
                String result;
                try {
                    if (project.getBasePath() == null) result = "No project directory";
                    else {
                        var layout = new ProjectDetector().detect(Path.of(project.getBasePath()));
                        result = layout.buildTools().isEmpty() ? "No recognized build marker" :
                                "Build markers: " + String.join(", ", layout.buildTools().stream().map(BuildTool::label).toList());
                    }
                } catch (Exception failure) { result = "Cannot inspect project: " + failure.getMessage(); }
                String status = result;
                ApplicationManager.getApplication().invokeLater(() -> {
                    loading.set(false);
                    if (!alive.get() || project.isDisposed()) return;
                    detected.setText(status);
                    detected.setToolTipText(status);
                    refresh.setEnabled(true);
                });
            });
        };
        refresh.addActionListener(e -> inspect.run());
        var scroll = new JBScrollPane(panel);
        scroll.setBorder(JBUI.Borders.empty());
        var content = ContentFactory.getInstance().createContent(scroll, "Overview", false);
        content.setDisposer(() -> alive.set(false));
        window.getContentManager().addContent(content);
        inspect.run();
    }

    private static JBLabel heading(String text) {
        var label = new JBLabel(text);
        label.setFont(BrandIdentity.body(11).deriveFont(Font.BOLD));
        label.setForeground(BrandIdentity.ACCENT);
        label.setBorder(JBUI.Borders.emptyBottom(8));
        return label;
    }
    private static void add(JPanel panel, Component component) {
        if (component instanceof JComponent jc) {
            jc.setAlignmentX(Component.LEFT_ALIGNMENT);
            jc.setMaximumSize(new Dimension(Integer.MAX_VALUE, jc.getPreferredSize().height));
        }
        panel.add(component);
    }
    private static JButton actionButton(String text, String actionId, JPanel context) {
        var button = new JButton(text);
        var action = ActionManager.getInstance().getAction(actionId);
        button.setEnabled(action != null);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> ActionManager.getInstance().tryToExecute(action, null, context, "ScalaWorkbench", true));
        return button;
    }

    private static final class WorkbenchPanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return JBUI.scale(16); }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) { return Math.max(JBUI.scale(16), visible.height - JBUI.scale(24)); }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
