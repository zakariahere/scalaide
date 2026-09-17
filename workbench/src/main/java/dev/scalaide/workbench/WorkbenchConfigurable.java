package dev.scalaide.workbench;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.FormBuilder;
import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WorkbenchConfigurable implements Configurable {
    private final Project project;
    private JTextField javaHome, millExecutable, millModule, scalaCliExecutable;

    public WorkbenchConfigurable(Project project) { this.project = project; }
    @Override public String getDisplayName() { return "Scala Workbench"; }

    @Override public JComponent createComponent() {
        javaHome = new JTextField(36);
        millExecutable = new JTextField(36);
        millModule = new JTextField(36);
        scalaCliExecutable = new JTextField(36);
        var panel = FormBuilder.createFormBuilder()
                .addLabeledComponent("Build JDK home (blank: project JDK, then IDE runtime):", javaHome)
                .addLabeledComponent("Mill executable:", millExecutable)
                .addLabeledComponent("Mill module (e.g. app):", millModule)
                .addLabeledComponent("Scala CLI executable:", scalaCliExecutable)
                .addComponent(new JLabel("Executable fields accept a path or PATH command, without arguments."))
                .addComponent(new JLabel("sbt uses the launcher supplied by the Scala plugin."))
                .addComponentFillVertically(new JPanel(), 0).getPanel();
        reset();
        return panel;
    }

    @Override public boolean isModified() {
        if (javaHome == null) return false;
        var s = WorkbenchSettings.get(project).getState();
        return !javaHome.getText().trim().equals(s.javaHome) || !millExecutable.getText().trim().equals(s.millExecutable)
                || !millModule.getText().trim().equals(s.millModule)
                || !scalaCliExecutable.getText().trim().equals(s.scalaCliExecutable);
    }

    @Override public void apply() throws ConfigurationException {
        String home = javaHome.getText().trim();
        if (!home.isEmpty()) {
            try {
                if (!Files.isRegularFile(Path.of(home, "bin", com.intellij.openapi.util.SystemInfo.isWindows ? "java.exe" : "java")))
                    throw new ConfigurationException("The build JDK home must contain bin/java.");
            } catch (java.nio.file.InvalidPathException e) { throw new ConfigurationException("Invalid JDK path."); }
        }
        if (millExecutable.getText().isBlank() || scalaCliExecutable.getText().isBlank())
            throw new ConfigurationException("Build tool executables cannot be empty.");
        var s = WorkbenchSettings.get(project).getState();
        s.javaHome = home;
        s.millExecutable = millExecutable.getText().trim();
        s.millModule = millModule.getText().trim();
        s.scalaCliExecutable = scalaCliExecutable.getText().trim();
    }

    @Override public void reset() {
        var s = WorkbenchSettings.get(project).getState();
        javaHome.setText(s.javaHome); millExecutable.setText(s.millExecutable);
        millModule.setText(s.millModule); scalaCliExecutable.setText(s.scalaCliExecutable);
    }
    @Override public void disposeUIResources() { javaHome = millExecutable = millModule = scalaCliExecutable = null; }
}
