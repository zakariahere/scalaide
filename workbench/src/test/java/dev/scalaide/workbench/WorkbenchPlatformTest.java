package dev.scalaide.workbench;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.psi.PsiNamedElement;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

/** Executes inside the real platform with the real Scala plugin, not mocked IntelliJ APIs. */
public class WorkbenchPlatformTest extends BasePlatformTestCase {
    public void testRequiredPluginAndActionsAreLoaded() {
        var scala = PluginManagerCore.getPlugin(PluginId.getId("org.intellij.scala"));
        assertNotNull(scala);
        assertNotNull(PluginManagerCore.getPlugin(PluginId.getId("org.jetbrains.plugins.terminal")));
        for (String id : new String[]{"ScalaWorkbench.Compile", "ScalaWorkbench.Test", "ScalaWorkbench.Show",
                "ScalaWorkbench.Settings", "ScalaWorkbench.Terminal", "SearchEverywhere", "GotoAction"}) {
            assertNotNull("Missing action: " + id, ActionManager.getInstance().getAction(id));
        }
    }

    public void testSettingsRoundTripAndProjectScopedService() {
        var settings = WorkbenchSettings.get(getProject());
        var values = new WorkbenchSettings.Values();
        values.buildTool = "MILL";
        values.millModule = "services.api";
        settings.loadState(values);
        assertEquals("services.api", settings.getState().millModule);
        assertSame(settings, WorkbenchSettings.get(getProject()));
        assertFalse(getProject().getService(BuildService.class).isBusy());
    }

    public void testScalaFileTypeAndDefinitionResolution() {
        var file = myFixture.configureByText("Main.scala", """
                class User(val age: Int)
                object Main { val user = new Us<caret>er(30) }
                """);
        assertEquals("Scala", file.getLanguage().getID());
        var reference = file.findReferenceAt(myFixture.getCaretOffset());
        assertNotNull("The Scala engine must provide a PSI reference", reference);
        var definition = reference.resolve();
        assertNotNull("User should resolve to its class definition", definition);
        assertEquals("User", ((PsiNamedElement) definition).getName());
    }

    public void testScalaMemberCompletion() {
        myFixture.configureByText("Completion.scala", """
                class User { def greeting = 42 }
                object Main { val user = new User; user.gre<caret> }
                """);
        var items = myFixture.completeBasic();
        // IntelliJ inserts a unique match directly; otherwise it returns the lookup choices.
        if (items == null) assertTrue(myFixture.getFile().getText().contains("user.greeting"));
        else assertTrue(java.util.Arrays.stream(items).anyMatch(i -> i.getLookupString().equals("greeting")));
    }

    public void testScalaSyntaxDiagnosticsAreProvidedByTheParser() {
        var file = myFixture.configureByText("Broken.scala", "object Broken { val = }");
        assertNotNull(com.intellij.psi.util.PsiTreeUtil.findChildOfType(file, com.intellij.psi.PsiErrorElement.class));
    }
}
