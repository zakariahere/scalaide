package dev.scalaide.workbench;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/** Machine-specific executable paths belong in the local workspace, not shared project files. */
@Service(Service.Level.PROJECT)
@State(name = "ScalaWorkbench", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
public final class WorkbenchSettings implements PersistentStateComponent<WorkbenchSettings.Values> {
    public static final class Values {
        public String buildTool = "";
        public String javaHome = "";
        public String millExecutable = "mill";
        public String millModule = "";
        public String scalaCliExecutable = "scala-cli";
    }

    private Values state = new Values();
    public static WorkbenchSettings get(Project project) { return project.getService(WorkbenchSettings.class); }
    @Override public @NotNull Values getState() { return state; }
    @Override public void loadState(@NotNull Values state) { this.state = state; }
}
