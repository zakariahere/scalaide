package dev.scalaide.workbench;

import com.intellij.openapi.application.impl.LaterInvocator;
import com.intellij.testFramework.PlatformTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;

public class LearningSettingsDialogTest extends BasePlatformTestCase {
    public void testSaveClosesWhileDialogIsModal() {
        var original = LearningSettings.get().getState();
        var dialog = new LearningSettingsDialog(getProject());
        // Exercise the real save handler while IntelliJ has an active modal dialog.
        // A blank key preserves credentials and avoids touching Password Safe in tests.
        LaterInvocator.enterModal(dialog);
        try {
            dialog.doOKAction();
            PlatformTestUtil.waitWithEventsDispatching("Save must finish without first closing the modal dialog",
                    dialog::isDisposed, 5);
            assertTrue(dialog.isOK());
        } finally {
            LaterInvocator.leaveModal(dialog);
            if (!dialog.isDisposed()) dialog.close(1);
            PlatformTestUtil.dispatchAllInvocationEventsInIdeEventQueue();
            LearningSettings.get().loadState(original);
        }
    }
}
