package dev.scalaide.core;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.Assert.*;

public class ProjectDetectorTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();

    @Test public void detectsSbtWithBspWithoutChoosingForTheUser() throws Exception {
        Path root = temp.getRoot().toPath();
        Files.writeString(root.resolve("build.sbt"), "this is never evaluated");
        Files.createDirectory(root.resolve(".bsp"));
        Files.writeString(root.resolve(".bsp/sbt.json"), "{}");
        Files.writeString(root.resolve(".bsp/readme.txt"), "ignored");
        var layout = new ProjectDetector().detect(root);
        assertEquals(List.of(BuildTool.SBT, BuildTool.BSP), layout.buildTools());
        assertEquals(1, layout.bspConnections().size());
    }

    @Test public void recognizesAllMillNames() throws Exception {
        for (String name : List.of("build.mill", "build.mill.scala", "build.sc")) {
            Path root = temp.newFolder().toPath();
            Files.writeString(root.resolve(name), "");
            assertEquals(List.of(BuildTool.MILL), new ProjectDetector().detect(root).buildTools());
        }
    }

    @Test public void plainScalaFileDoesNotPretendToBeImported() throws Exception {
        Path root = temp.getRoot().toPath();
        Files.writeString(root.resolve("Hello.scala"), "object Hello");
        assertTrue(new ProjectDetector().detect(root).buildTools().isEmpty());
    }

    @Test public void detectsScalaCliAndDoesNotScanNestedBuilds() throws Exception {
        Path root = temp.getRoot().toPath();
        Files.writeString(root.resolve("project.scala"), "//> using scala 3.3.6");
        Files.createDirectory(root.resolve("nested"));
        Files.writeString(root.resolve("nested/build.sbt"), "");
        assertEquals(List.of(BuildTool.SCALA_CLI), new ProjectDetector().detect(root).buildTools());
    }

    @Test(expected = java.io.IOException.class) public void reportsMissingRoot() throws Exception {
        new ProjectDetector().detect(temp.getRoot().toPath().resolve("missing"));
    }

    @Test public void emptyBspFolderIsNotABuildConnection() throws Exception {
        Files.createDirectory(temp.getRoot().toPath().resolve(".bsp"));
        assertTrue(new ProjectDetector().detect(temp.getRoot().toPath()).buildTools().isEmpty());
    }
}
