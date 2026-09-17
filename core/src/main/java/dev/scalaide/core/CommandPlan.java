package dev.scalaide.core;

import java.nio.file.Path;
import java.util.List;

/** Arguments remain separate all the way to the process API. */
public record CommandPlan(Path directory, List<String> argv, String label) {
    public CommandPlan {
        directory = directory.toAbsolutePath().normalize();
        argv = List.copyOf(argv);
        if (argv.isEmpty() || argv.stream().anyMatch(s -> s == null || s.isBlank() || s.indexOf('\0') >= 0))
            throw new IllegalArgumentException("Command contains an empty or invalid argument");
    }
}
