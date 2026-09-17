# ScalaIDE · Scala Workbench

A Scala-first IDE project built on the real IntelliJ Platform. The first
development milestone is a **runnable, isolated IntelliJ instance plus our Scala
Workbench plugin**. It is not yet a standalone branded distribution or a
production release. The long-term requirements are in [the product brief](docs/product-brief.md).

The [architecture decision](docs/adr/0001-platform.md) compares IntelliJ,
Metals/protocol-based and hybrid platforms, including licensing, compatibility
and isolation. Read it before changing platform direction.

The IDE includes **Zakaria Graphite**, matching [zakaria.lu](https://zakaria.lu),
with graphite surfaces, ice/electric-blue accents, the original hoodie mascot,
ZB monogram and bundled brand fonts. Apply it from the Workbench button or
Find Action → **Apply Zakaria Graphite Theme**. [Branding details](docs/branding.md).

## Run on Windows

Install a **JDK 25** and make `java` available on PATH (or set `JAVA_HOME`).
From this directory:

```powershell
.\scripts\start.ps1
```

This opens the included Scala 3 / sbt / MUnit project. To open your own project:

```powershell
.\scripts\start.ps1 -ProjectPath 'C:\workspace\my-scala-project'
```

The launcher reuses a local IntelliJ IDEA **2026.2.1** installation when found,
without modifying it. You can supply `-IdeHome` or `SCALAIDE_IDEA_HOME` explicitly.
Otherwise Gradle downloads the pinned host. Initial downloads can be large.
The Scala plugin **2026.2.18** is provisioned into the development instance.
No API key, AI account or IntelliJ Ultimate feature is needed for this slice.
The unified host may show optional paid features; it is not redistributed here.

The Gradle entry point also works on other supported desktop operating systems,
but this repository has only been validated on Windows:

```sh
./gradlew :workbench:runIde -PscalaProject=/absolute/path/to/project
```

On first open, let the native Scala plugin import sbt and finish indexing. Choose
the project's JDK if prompted. Use **Scala → Scala Workbench** for project status,
build selection, compile/test, terminal and settings. Opening a project alone
does not trigger our build actions. The host's normal import/trust workflow applies.

## Working capabilities

- Native project tree, Scala editor, syntax highlighting, completion, definition
  navigation and diagnostics through the upstream Scala plugin.
- Native IntelliJ actions and configurable keymap, Search Everywhere, Find Action,
  local history, Git, run/debug and terminal. These are inherited capabilities;
  see [validation](docs/validation.md) for what was actually exercised here.
- Scala Workbench tool window with bounded root detection for sbt, Mill, Scala
  CLI and BSP metadata. A detected marker is not reported as a successful import.
- Compile/test commands with a real Run console, exit status, stop action, project
  process cleanup, and one Workbench build at a time per project.
- sbt through the Scala plugin's bundled launcher, using your build's sbt version.
  The build JDK uses the configured override, project SDK, then IDE runtime.
- Mill and Scala CLI command planning. Mill requires an explicit module in
  settings; executable paths are configurable. These end-to-end integrations
  have not yet been validated against full sample projects.
- For BSP, use native Scala-plugin import and run/test workflows. There is no
  pretend `bsp` command. Workbench explains this when selected.

Workbench sbt commands currently start a fresh process and do not read all options
from wrapper-specific `.sbtopts`/`.jvmopts` files. Use the native sbt shell/run
configurations for builds that depend on them. The Workbench console reports
test output; the native test runner provides structured test trees.

## Familiar controls

The host's default Windows IntelliJ keymap is kept, with no forced remapping.
Custom Workbench actions can be assigned shortcuts in Settings → Keymap.

| Workflow | Default Windows shortcut |
| --- | --- |
| Search Everywhere | Shift twice |
| Find Action | Ctrl+Shift+A |
| Go to file / class | Ctrl+Shift+N / Ctrl+N |
| Go to definition / usages | Ctrl+B / Alt+F7 |
| Rename / intentions | Shift+F6 / Alt+Enter |
| Completion / parameter info | Ctrl+Space / Ctrl+P |
| Format | Ctrl+Alt+L |
| Terminal | Alt+F12 |

## Verify and package

```powershell
.\scripts\verify.ps1
```

`verify` runs core behavior tests, real IntelliJ/Scala integration tests, native
process tests, plugin packaging and project-configuration checks. Reports:

- `core/build/reports/tests/test/index.html`
- `workbench/build/reports/tests/test/index.html`
- Installable product plugin: `workbench/build/distributions/workbench-0.1.0.zip`

The plugin ZIP is not an IDE installer. It can be installed into the declared
IntelliJ build range with the required Scala and terminal plugins.

The build currently emits one intentional compatibility recommendation: remove
`until-build`. We retain `262.*` until the next platform line has been tested.
This avoids advertising unverified cross-version compatibility.

## Repository layout

| Area | Responsibility |
| --- | --- |
| `core` | Immutable project observations and build command policy; no IDE dependency |
| `workbench` | IntelliJ actions, tool window, local settings, process lifecycle |
| `examples/hello-scala` | Runnable Scala 3 + sbt + MUnit acceptance project |
| `docs/adr` | Architectural choices and constraints |
| `docs/roadmap.md` | Milestones and incomplete product requirements |

Development instance data lives under `.intellijPlatform/sandbox/workbench/`.
Look in `IU-2026.2.1/log_runIde/idea.log` for launch, import and Workbench build logs.
The normal installed IDE's config and plugins are separate. Opening an existing
project can still create its ordinary `.idea`, build and cache files.

## Next product requirements

Standalone source-built distribution, curated modules, isolated worker plugins,
a versioned SDK, plugin catalog, semantic AI gateway and release engineering
remain planned. Native IntelliJ plugins use the real SDK but share a JVM and are
trusted code. This repository does not claim arbitrary plugin compatibility or
process isolation for native plugins. See [the roadmap](docs/roadmap.md).
