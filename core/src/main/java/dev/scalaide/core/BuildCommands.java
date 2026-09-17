package dev.scalaide.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BuildCommands {
    private BuildCommands() {}

    public static CommandPlan sbt(Path root, String java, Path launcher, BuildOperation operation) {
        return new CommandPlan(root, List.of(java, "-Dsbt.supershell=false", "-Dsbt.color=false",
                "-Dsbt.log.noformat=true", "-jar", launcher.toString(),
                operation == BuildOperation.COMPILE ? "compile" : "test"), "sbt " + operation.name().toLowerCase());
    }

    public static CommandPlan external(Path root, BuildTool tool, String executable,
                                        String millModule, BuildOperation operation) {
        var argv = new ArrayList<String>();
        argv.add(executable);
        switch (tool) {
            case SCALA_CLI -> argv.addAll(List.of(operation == BuildOperation.COMPILE ? "compile" : "test", "."));
            case MILL -> {
                // A module is a Mill selector, never shell text. Limit this first UI to one named module.
                if (!millModule.matches("[A-Za-z_][A-Za-z0-9_-]*(\\.[A-Za-z_][A-Za-z0-9_-]*)*"))
                    throw new IllegalArgumentException("Set a Mill module such as app or services.api in Scala Workbench settings.");
                argv.add("--no-server");
                argv.add(millModule + (operation == BuildOperation.COMPILE ? ".compile" : ".test"));
            }
            default -> throw new IllegalArgumentException("Use the native sbt/BSP integration for " + tool.label());
        }
        return new CommandPlan(root, argv, tool.label() + " " + operation.name().toLowerCase());
    }
}
