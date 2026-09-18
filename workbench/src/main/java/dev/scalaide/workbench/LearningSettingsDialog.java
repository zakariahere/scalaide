package dev.scalaide.workbench;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

/** Key entry is masked and never populated with an existing secret. Storage is off the EDT. */
final class LearningSettingsDialog extends DialogWrapper {
    private final JTextField model = new JTextField(28);
    private final JComboBox<String> language = new JComboBox<>(new String[]{"English", "French"});
    private final JCheckBox java = new JCheckBox("Include useful Java comparisons");
    private final JPasswordField key = new JPasswordField(28);
    private final JCheckBox forget = new JCheckBox("Forget the stored API key");
    private final JLabel status = new JLabel(" ");
    private boolean saving;

    LearningSettingsDialog(Project project) {
        super(project, true);
        setTitle("Scala Learning · DeepSeek"); setOKButtonText("Save settings");
        var values = LearningSettings.get().getState();
        model.setText(values.model); language.setSelectedItem(values.language); java.setSelected(values.compareJava);
        key.getAccessibleContext().setAccessibleName("DeepSeek API key");
        init();
    }
    @Override protected @Nullable JComponent createCenterPanel() {
        var panel = FormBuilder.createFormBuilder()
                .addComponent(new JBLabel("Explain the active Scala file with DeepSeek."))
                .addLabeledComponent("Model:", model).addLabeledComponent("Summary language:", language)
                .addComponent(java).addLabeledComponent("New API key:", key)
                .addComponent(new JBLabel("Leave blank to keep the existing key. Stored in IntelliJ Password Safe."))
                .addComponent(new JBLabel("DEEPSEEK_API_KEY is used if no key is stored in Password Safe."))
                .addComponent(forget).addComponent(status).getPanel();
        panel.setPreferredSize(new Dimension(580, panel.getPreferredSize().height));
        return panel;
    }
    @Override protected @Nullable ValidationInfo doValidate() {
        if (!model.getText().trim().matches("[a-zA-Z0-9][a-zA-Z0-9._-]{0,79}"))
            return new ValidationInfo("Enter a valid DeepSeek model name.", model);
        char[] chars = key.getPassword();
        try {
            if (chars.length > 0 && forget.isSelected()) return new ValidationInfo("Either replace the key or forget it.", key);
            for (char c : chars) if (c < 33 || c > 126) return new ValidationInfo("Remove whitespace from the API key.", key);
        } finally { Arrays.fill(chars, '\0'); }
        return null;
    }
    @Override protected void doOKAction() {
        if (saving || doValidate() != null) return;
        var values = new LearningSettings.Values();
        values.model = model.getText().trim(); values.language = (String) language.getSelectedItem(); values.compareJava = java.isSelected();
        char[] chars = key.getPassword(); key.setText("");
        boolean clear = forget.isSelected();
        // Complete within this dialog's modality, without waiting for it to close first.
        var modality = ModalityState.current();
        saving = true; setOKActionEnabled(false); setCancelButtonText("Close"); status.setText("Saving settings…");
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            boolean success = false;
            try {
                if (clear) DeepSeekCredentials.save(null);
                else if (chars.length > 0) DeepSeekCredentials.save(new String(chars));
                success = true;
            } catch (Exception ignored) {
                // Password-provider exceptions can contain sensitive data; do not log/display them.
            } finally { Arrays.fill(chars, '\0'); }
            boolean saved = success;
            ApplicationManager.getApplication().invokeLater(() -> {
                if (isDisposed()) return;
                saving = false;
                if (saved) { LearningSettings.get().loadState(values); close(OK_EXIT_CODE); }
                else { setOKActionEnabled(true); status.setText("Could not save the key. Check IntelliJ Password Safe and try again."); }
            }, modality);
        });
    }
    @Override protected void dispose() { key.setText(""); super.dispose(); }
}
