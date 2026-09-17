package dev.scalaide.workbench;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAwareAction;
import dev.scalaide.core.BuildOperation;
import org.jetbrains.annotations.NotNull;

public abstract class BuildAction extends DumbAwareAction {
    private final BuildOperation operation;
    protected BuildAction(BuildOperation operation) { this.operation = operation; }
    @Override public void actionPerformed(@NotNull AnActionEvent e) {
        if (e.getProject() != null) e.getProject().getService(BuildService.class).run(operation);
    }
    @Override public void update(@NotNull AnActionEvent e) {
        var p = e.getProject();
        e.getPresentation().setEnabled(p != null && p.getBasePath() != null && !p.getService(BuildService.class).isBusy());
    }
    @Override public @NotNull ActionUpdateThread getActionUpdateThread() { return ActionUpdateThread.BGT; }
    public static final class Compile extends BuildAction { public Compile() { super(BuildOperation.COMPILE); } }
    public static final class Test extends BuildAction { public Test() { super(BuildOperation.TEST); } }
}
