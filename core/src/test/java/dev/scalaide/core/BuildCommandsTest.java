package dev.scalaide.core;

import org.junit.Test;
import java.nio.file.Path;
import java.util.List;
import static org.junit.Assert.*;

public class BuildCommandsTest {
    private final Path root = Path.of("project with spaces");

    @Test public void preservesPathsWithSpacesAsSingleArguments() {
        var plan = BuildCommands.sbt(root, "C:/JDK with spaces/bin/java.exe",
                Path.of("C:/Scala plugin/launcher/sbt-launch.jar"), BuildOperation.TEST);
        assertEquals("C:/JDK with spaces/bin/java.exe", plan.argv().getFirst());
        assertEquals("test", plan.argv().getLast());
        assertEquals(7, plan.argv().size());
        assertEquals(root.toAbsolutePath(), plan.directory());
    }

    @Test public void millUsesExplicitModuleAndOperation() {
        assertEquals(List.of("mill", "--no-server", "services.api.test"),
                BuildCommands.external(root, BuildTool.MILL, "mill", "services.api", BuildOperation.TEST).argv());
    }

    @Test(expected = IllegalArgumentException.class) public void rejectsShellTextInMillSelector() {
        BuildCommands.external(root, BuildTool.MILL, "mill", "app; echo bad", BuildOperation.TEST);
    }

    @Test(expected = IllegalArgumentException.class) public void doesNotGuessMillModule() {
        BuildCommands.external(root, BuildTool.MILL, "mill", "", BuildOperation.COMPILE);
    }

    @Test public void scalaCliTargetsRootDirectory() {
        assertEquals(List.of("scala-cli", "compile", "."), BuildCommands.external(
                root, BuildTool.SCALA_CLI, "scala-cli", "", BuildOperation.COMPILE).argv());
    }

    @Test(expected = IllegalArgumentException.class) public void bspCannotBeTreatedAsAnExecutable() {
        BuildCommands.external(root, BuildTool.BSP, "bsp", "", BuildOperation.TEST);
    }
}
