package dev.scalaide.workbench;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import dev.scalaide.core.LearningPrompt;

/** Capture on EDT: immutable request data plus a local-only document reference for freshness. */
record LearningSnapshot(String fileName, String source, Document document, long stamp) {
    static LearningSnapshot capture(Project project) {
        var editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) throw new IllegalArgumentException("Open a Scala (.scala) source file first.");
        var document = editor.getDocument();
        var file = FileDocumentManager.getInstance().getFile(document);
        if (file == null || !"Scala".equals(file.getFileType().getName()))
            throw new IllegalArgumentException("The active editor is not a Scala source file.");
        String source = document.getText();
        LearningPrompt.validateSource(file.getName(), source);
        return new LearningSnapshot(file.getName(), source, document, document.getModificationStamp());
    }
    boolean changed() { return document.getModificationStamp() != stamp; }
    @Override public String toString() { return "Scala file snapshot (source omitted)"; }
}
