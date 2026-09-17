package dev.scalaide.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Bounded root inspection. Never reads or executes build definitions. */
public final class ProjectDetector {
    public ProjectLayout detect(Path root) throws IOException {
        if (!Files.isDirectory(root)) throw new IOException("Project directory does not exist: " + root);
        var tools = new ArrayList<BuildTool>();
        if (Files.isRegularFile(root.resolve("build.sbt")) ||
            Files.isRegularFile(root.resolve("project/build.properties"))) tools.add(BuildTool.SBT);
        if (List.of("build.mill", "build.mill.scala", "build.sc").stream()
                .anyMatch(name -> Files.isRegularFile(root.resolve(name)))) tools.add(BuildTool.MILL);
        if (Files.isRegularFile(root.resolve("project.scala")) ||
            Files.isDirectory(root.resolve(".scala-build"))) tools.add(BuildTool.SCALA_CLI);
        List<Path> connections = List.of();
        if (Files.isDirectory(root.resolve(".bsp"))) {
            try (var entries = Files.list(root.resolve(".bsp"))) {
                connections = entries.filter(Files::isRegularFile)
                        .filter(p -> p.getFileName().toString().endsWith(".json"))
                        .sorted().limit(64).toList();
            }
            if (!connections.isEmpty()) tools.add(BuildTool.BSP);
        }
        return new ProjectLayout(root, tools, connections);
    }
}
