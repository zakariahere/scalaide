package dev.scalaide.workbench;

import com.intellij.execution.process.CapturingProcessHandler;
import com.intellij.execution.process.KillableColoredProcessHandler;
import com.intellij.execution.process.ProcessListener;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.openapi.util.SystemInfo;
import dev.scalaide.core.CommandPlan;
import org.junit.rules.TemporaryFolder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class BuildProcessCommandTest extends com.intellij.testFramework.fixtures.BasePlatformTestCase {
    private final TemporaryFolder temp = new TemporaryFolder();

    @Override protected void setUp() throws Exception { super.setUp(); temp.create(); }
    @Override protected void tearDown() throws Exception {
        try { temp.delete(); } finally { super.tearDown(); }
    }

    public void testRunsNativeExecutableWithSpaceInWorkingDirectory() throws Exception {
        var root = temp.newFolder("working directory").toPath();
        var java = Path.of(System.getProperty("java.home"), "bin", SystemInfo.isWindows ? "java.exe" : "java");
        var output = new CapturingProcessHandler(BuildProcessCommand.create(
                new CommandPlan(root, List.of(java.toString(), "-version"), "Java"))).runProcess(10000);
        assertEquals(0, output.getExitCode());
        assertFalse(output.isTimeout());
        assertTrue(output.getStderr().contains("version"));
    }

    public void testExecutesWindowsBatchLaunchersWithoutHandBuiltShellText() throws Exception {
        assumeTrue(SystemInfo.isWindows);
        var root = temp.newFolder("working directory").toPath();
        var launcher = root.resolve("test launcher.cmd");
        Files.writeString(launcher, "@echo off\r\necho %~1\r\nexit /b 7\r\n");
        var output = new CapturingProcessHandler(BuildProcessCommand.create(
                new CommandPlan(root, List.of(launcher.toString(), "argument with spaces"), "Fixture"))).runProcess(10000);
        assertEquals(output.getStderr(), 7, output.getExitCode());
        assertTrue(output.getStdout().contains("argument with spaces"));
    }

    public void testRunningProcessCanBeStopped() throws Exception {
        var root = temp.getRoot().toPath();
        var java = Path.of(System.getProperty("java.home"), "bin", SystemInfo.isWindows ? "java.exe" : "java");
        var fixtureResource = SlowFixture.class.getResource("/dev/scalaide/workbench/BuildProcessCommandTest$SlowFixture.class");
        assertNotNull(fixtureResource);
        String classes = Path.of(fixtureResource.toURI()).getParent().getParent().getParent().getParent().toString();
        var handler = new KillableColoredProcessHandler(BuildProcessCommand.create(new CommandPlan(root,
                List.of(java.toString(), "-cp", classes, SlowFixture.class.getName()), "Cancellation fixture")));
        var ready = new CountDownLatch(1);
        handler.addProcessListener(new ProcessListener() {
            @Override public void onTextAvailable(ProcessEvent event, com.intellij.openapi.util.Key outputType) {
                if (event.getText().contains("READY")) ready.countDown();
            }
        });
        try {
            handler.startNotify();
            assertTrue("Fixture must start before cancellation", ready.await(10, TimeUnit.SECONDS));
            handler.destroyProcess();
            assertTrue("Cancellation must terminate the process", handler.waitFor(10000));
            assertTrue(handler.isProcessTerminated());
        } finally {
            if (!handler.isProcessTerminated()) handler.killProcess();
        }
    }

    public static final class SlowFixture {
        public static void main(String[] args) throws Exception {
            System.out.println("READY");
            System.out.flush();
            Thread.sleep(60000);
        }
    }
}
