package dev.scalaide.workbench;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

public final class WorkbenchActions {
    private WorkbenchActions() {}
    public static final class Show extends DumbAwareAction {
        @Override public void actionPerformed(@NotNull AnActionEvent e) {
            if (e.getProject() == null) return;
            var window = ToolWindowManager.getInstance(e.getProject()).getToolWindow("Scala Workbench");
            if (window != null) window.activate(null);
        }
        @Override public void update(@NotNull AnActionEvent e) { e.getPresentation().setEnabled(e.getProject() != null); }
        @Override public @NotNull ActionUpdateThread getActionUpdateThread() { return ActionUpdateThread.BGT; }
    }
    public static final class Settings extends DumbAwareAction {
        @Override public void actionPerformed(@NotNull AnActionEvent e) {
            if (e.getProject() != null)
                ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), WorkbenchConfigurable.class);
        }
        @Override public void update(@NotNull AnActionEvent e) { e.getPresentation().setEnabled(e.getProject() != null); }
        @Override public @NotNull ActionUpdateThread getActionUpdateThread() { return ActionUpdateThread.BGT; }
    }
    public static final class Terminal extends DumbAwareAction {
        @Override public void actionPerformed(@NotNull AnActionEvent e) {
            if (e.getProject() == null) return;
            // Resolve at invocation, after project tool-window registration. Activation actions
            // may not exist yet while restored tool-window content is being constructed.
            var terminal = ToolWindowManager.getInstance(e.getProject()).getToolWindow("Terminal");
            if (terminal != null) terminal.activate(null);
            else com.intellij.notification.NotificationGroupManager.getInstance()
                    .getNotificationGroup("Scala Workbench").createNotification(
                            "The terminal is still initializing. Try again after project startup.",
                            com.intellij.notification.NotificationType.INFORMATION).notify(e.getProject());
        }
        @Override public void update(@NotNull AnActionEvent e) { e.getPresentation().setEnabled(e.getProject() != null); }
        @Override public @NotNull ActionUpdateThread getActionUpdateThread() { return ActionUpdateThread.BGT; }
    }
}
