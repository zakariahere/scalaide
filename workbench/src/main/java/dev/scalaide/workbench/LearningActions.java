package dev.scalaide.workbench;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

public final class LearningActions {
    private LearningActions() {}
    public static final class Show extends DumbAwareAction {
        @Override public void actionPerformed(@NotNull AnActionEvent event) {
            if (event.getProject() == null) return;
            var window = ToolWindowManager.getInstance(event.getProject()).getToolWindow(LearningToolWindow.ID);
            if (window != null) window.activate(null);
        }
        @Override public void update(@NotNull AnActionEvent event) { event.getPresentation().setEnabled(event.getProject() != null); }
        @Override public @NotNull ActionUpdateThread getActionUpdateThread() { return ActionUpdateThread.BGT; }
    }
    public static final class Summarize extends DumbAwareAction {
        @Override public void actionPerformed(@NotNull AnActionEvent event) {
            if (event.getProject() == null) return;
            var window = ToolWindowManager.getInstance(event.getProject()).getToolWindow(LearningToolWindow.ID);
            if (window != null) window.activate(() -> {
                var content = window.getContentManager().getContent(0);
                if (content != null && content.getComponent() instanceof LearningPanel panel) panel.summarizeCurrent();
            });
        }
        @Override public void update(@NotNull AnActionEvent event) {
            var file = event.getData(CommonDataKeys.VIRTUAL_FILE);
            event.getPresentation().setEnabled(event.getProject() != null && file != null
                    && "Scala".equals(file.getFileType().getName()) && "scala".equals(file.getExtension()));
        }
        @Override public @NotNull ActionUpdateThread getActionUpdateThread() { return ActionUpdateThread.BGT; }
    }
}
