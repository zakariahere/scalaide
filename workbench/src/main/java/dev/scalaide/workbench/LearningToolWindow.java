package dev.scalaide.workbench;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.*;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public final class LearningToolWindow implements ToolWindowFactory, DumbAware {
    public static final String ID = "Scala Learning";
    @Override public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow window) {
        var panel = new LearningPanel(project);
        var content = ContentFactory.getInstance().createContent(panel, "Learning notes", false);
        content.setDisposer(panel); window.getContentManager().addContent(content);
    }
}
