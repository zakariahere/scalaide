# Validation record — 2026-09-17

Environment: Windows, JDK 25.0.2, IntelliJ IDEA 2026.2.1 (262.9437.185), Scala
plugin 2026.2.18, Gradle 9.5.1. The host was read from the installed location;
the development instance used separate Gradle-managed sandbox directories.

## Automated

`scripts/verify.ps1` passes:

- 12 core tests: sbt/BSP coexistence, all supported Mill marker names, Scala CLI
  detection, no recursive build guessing, missing root, empty BSP metadata,
  argument boundaries, explicit Mill selectors, invalid selector rejection,
  Scala CLI commands and explicit unsupported BSP execution.
- 5 real-platform tests: required plugin/action registration, project settings
  and service scope, Scala file type and definition resolution, Scala member
  completion, and parser syntax diagnostics.
- 3 real process tests: native Java execution in a directory containing spaces;
  a Windows batch launcher with spaces in its path/argument and exit code 7;
  stopping an already-started long-running process.
- Plugin compilation and packaging succeed. The Gradle configuration check
  recommends removing `until-build`; retaining the verified 262.* upper bound
  is intentional and documented.

The sample `examples/hello-scala` also passes sbt `compile test`: Scala 3.3.6,
sbt 1.12.11, MUnit 1.0.4, two tests, zero failures.

## Desktop checks

Observed in the actual development window:

- Launch opens the included existing sbt project. Native import creates its
  module and Scala SDK model; indexing completes.
- Scala Workbench is registered with its icon and real plugin version, detects
  sbt/BSP metadata, and shows imported IDE-module count without claiming that
  markers alone prove readiness.
- Clicking **Run project tests** creates a native Run tab, executes sbt, displays
  both passing MUnit tests and exit code 0, and posts a success notification.
- Ctrl+Shift+N finds `Main.scala`; opening it shows native Scala 3 highlighting,
  parameter inlays and run gutter actions.
- Clicking **Open terminal** opens a real PowerShell session at the sample's
  project root. No terminal commands were entered through UI automation.
- A final restart through `scripts/start.ps1` restores the project and editor.
  The fixed Workbench terminal button stays enabled at startup and successfully
  reopens the hidden terminal.

Completion/definition/parser behavior is additionally checked by the platform
fixtures, not inferred merely from a screenshot or an installed plugin.

## Branding adaptation

- Re-ran verification after adding Zakaria Graphite: all 20 tests pass. The
  final plugin ZIP also packages successfully with the ZB plugin icon.
- Applied the theme through the Workbench button. The native editor, tool windows,
  controls and confirmation dialog use the graphite/blue palette.
- Inspected the original mascot, ZB mark and bundled Space Grotesk/Hanken Grotesk
  fonts at full width and in a roughly 345-pixel-wide Workbench panel.
- Restarted the development IDE and observed the same theme and editor scheme,
  mascot header and panel widths. Native settings persist the theme ID and scheme.
- The packaged PNG and original WebP decode to identical RGBA pixels, including
  transparency. Brand resources load locally.

## Scala Learning — 2026-09-18

- Verification passes with 37 tests: the previous 20 plus four prompt-policy,
  eight real loopback HTTP, four IntelliJ learning integration tests and one
  modal settings-save regression test.
- Coverage includes unsaved document snapshots, source-size rejection, non-Scala
  gating, request/schema boundaries, safe provider errors, partial/malformed/large
  responses, timeouts, cancellation and escaped Markdown rendering.
- Cancellation coverage found a race in HttpClient's derived future cancellation.
  The client now owns its public future and explicitly propagates cancellation
  to the transport. The regression test passes.
- Desktop checks confirm the separate Learning panel opens, follows the active
  filename, disables summarization for `build.sbt`, and reports a missing key.
- A real DeepSeek explanation of the user's `Policy.scala` exercise was observed
  in the running panel. It described the class and identified its unfinished
  companion method; this confirms the live request/display path, not universal
  correctness of generated explanations.
- The settings regression reproduces a save completion deferred by IntelliJ's
  modal event queue. Capturing the dialog's modality before background work lets
  the completion close the dialog while it is still modal. The test failed before
  this change and passes after it, without accessing stored credentials.
- Compilation, plugin packaging and the existing configuration checks pass.

## Limits and observations

- No standalone source-built distribution, signed installer or SDK isolation
  claim is made. The running window is a development IntelliJ host.
- Native compiler diagnostics beyond parser errors, rename, find usages,
  debugger stepping, structured test tree, Git workflows, recovery and keyboard
  remapping have not yet received full end-to-end acceptance here.
- Mill and Scala CLI have detection/command policy but still need full project
  import/build/test acceptance. Cross-platform launcher tests remain pending.
- Stop is process-tested and project-close cleanup is implemented; cancellation races and process
  trees need dedicated stress coverage before production release.
- sbt wrapper options-file parity is incomplete; see README.
- Native import created an empty `null/Coursier` directory in the sample during
  this run. It is an upstream/import-environment observation, not product
  content, and is ignored by Git. Investigate before product packaging.
- The unified-host test environment logs upstream component-override warnings.
  There were no failing assertions after correcting the test's assumption that
  a headless fixture registers a desktop terminal-activation action. That action
  is window-lifecycle dependent. Restart validation also exposed a real startup
  ordering issue: eagerly looking up that action could permanently disable the
  terminal button. Workbench now registers its own action and resolves the native
  terminal at invocation; provision is checked in fixtures and activation on desktop.
