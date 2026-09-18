package dev.scalaide.workbench;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class LearningPlatformTest extends BasePlatformTestCase {
    public void testSnapshotUsesUnsavedDocumentAndDetectsLaterChanges() {
        myFixture.configureByText("Lesson.scala", "class Before");
        WriteCommandAction.runWriteCommandAction(getProject(), () -> myFixture.getEditor().getDocument().setText("case class After(name: String)"));
        var snapshot = LearningSnapshot.capture(getProject());
        assertEquals("case class After(name: String)", snapshot.source());
        assertEquals("Lesson.scala", snapshot.fileName()); assertFalse(snapshot.changed());
        WriteCommandAction.runWriteCommandAction(getProject(), () -> myFixture.getEditor().getDocument().setText("class Changed"));
        assertTrue(snapshot.changed()); assertEquals("case class After(name: String)", snapshot.source());
    }
    public void testNonScalaEditorIsRejectedAndActionIsDisabled() {
        var file = myFixture.configureByText("notes.txt", "class User");
        try { LearningSnapshot.capture(getProject()); fail("Non-Scala source accepted"); }
        catch (IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("not a Scala")); }
        var action = ActionManager.getInstance().getAction("ScalaLearning.Summarize");
        assertNotNull(action); assertNotNull(ActionManager.getInstance().getAction("ScalaLearning.Show"));
        DataContext context = id -> CommonDataKeys.PROJECT.is(id) ? getProject() : CommonDataKeys.VIRTUAL_FILE.is(id) ? file.getVirtualFile() : null;
        var event = AnActionEvent.createFromAnAction(action, null, "test", context);
        action.update(event); assertFalse(event.getPresentation().isEnabled());
    }
    public void testScalaEditorEnablesAction() {
        var file = myFixture.configureByText("Lesson.scala", "object Lesson");
        var action = ActionManager.getInstance().getAction("ScalaLearning.Summarize");
        DataContext context = id -> CommonDataKeys.PROJECT.is(id) ? getProject() : CommonDataKeys.VIRTUAL_FILE.is(id) ? file.getVirtualFile() : null;
        var event = AnActionEvent.createFromAnAction(action, null, "test", context);
        action.update(event); assertTrue(event.getPresentation().isEnabled());
    }
    public void testRendererEscapesHtmlAndDoesNotCreateRemoteResources() {
        String html = LearningMarkdown.body("## Concepts\n- **case class** and `copy`\n<img src=\"https://bad.invalid/pixel\">\n```scala\nval x = \"<script>\"\n```\n[link](https://bad.invalid)");
        assertTrue(html.contains("<h2>Concepts</h2>")); assertTrue(html.contains("<b>case class</b>"));
        assertTrue(html.contains("&lt;img")); assertTrue(html.contains("&lt;script&gt;"));
        assertFalse(html.contains("<img")); assertFalse(html.contains("<script")); assertFalse(html.contains("<a "));
        assertTrue(html.contains("<pre>"));
    }
}
