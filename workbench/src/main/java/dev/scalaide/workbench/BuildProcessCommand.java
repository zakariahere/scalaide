package dev.scalaide.workbench;

import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.PathEnvironmentVariableUtil;
import dev.scalaide.core.CommandPlan;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;

final class BuildProcessCommand {
    private BuildProcessCommand() {}
    static GeneralCommandLine create(CommandPlan plan) {
        var args = new ArrayList<>(plan.argv());
        var executable = Path.of(args.getFirst());
        if (!executable.isAbsolute() && executable.getNameCount() == 1) {
            var resolved = PathEnvironmentVariableUtil.findExecutableInPathOnAnyOS(args.getFirst());
            if (resolved != null) args.set(0, resolved.getAbsolutePath());
        } else if (!executable.isAbsolute()) {
            args.set(0, plan.directory().resolve(executable).normalize().toString());
        }
        return new GeneralCommandLine(args).withWorkDirectory(plan.directory().toFile()).withCharset(StandardCharsets.UTF_8);
    }
}
