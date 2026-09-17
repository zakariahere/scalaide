package dev.scalaide.core;

import java.nio.file.Path;
import java.util.List;

/** Immutable observations, not a claim that a build has imported successfully. */
public record ProjectLayout(Path root, List<BuildTool> buildTools, List<Path> bspConnections) {
    public ProjectLayout {
        root = root.toAbsolutePath().normalize();
        buildTools = List.copyOf(buildTools);
        bspConnections = List.copyOf(bspConnections);
    }
}
