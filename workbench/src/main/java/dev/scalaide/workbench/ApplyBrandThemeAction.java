package dev.scalaide.workbench;

import com.intellij.ide.actions.QuickChangeLookAndFeel;
import com.intellij.ide.ui.LafManager;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;

/** An explicit user action, never a startup override of their chosen theme. */
public final class ApplyBrandThemeAction extends DumbAwareAction {
    @Override public void actionPerformed(@NotNull AnActionEvent event) {
        var manager = LafManager.getInstance();
        var themes = manager.getInstalledThemes().iterator();
        while (themes.hasNext()) {
            var theme = themes.next();
            if (theme.getId().equals("dev.scalaide.zakaria.graphite")) {
                QuickChangeLookAndFeel.switchLafAndUpdateUI(manager, theme, true);
                return;
            }
        }
        NotificationGroupManager.getInstance().getNotificationGroup("Scala Workbench")
                .createNotification("Zakaria Graphite is unavailable. Restart after updating Scala Workbench.",
                        NotificationType.WARNING).notify(event.getProject());
    }
}
