# ADR 0001: Real IntelliJ runtime, Scala product layer, isolated workers

Status: accepted for the first development milestone, 2026-09-17.

## Context and investigation

The product is a Scala-first desktop IDE with IntelliJ muscle memory and depth.
The repository was empty. Choosing a web editor first would make the most
expensive requirements (refactoring, project model, debugging and compatibility)
future rewrites rather than an incremental product.

| Option | Strengths | Costs / limits |
| --- | --- | --- |
| IntelliJ Platform + Scala plugin | Actual actions, keymaps, PSI, inspections, Java interop, sbt/BSP, debugger, terminal, Git, settings and plugin runtime | Large JVM runtime; upstream API churn; native plugins share the IDE process; custom product builds and license inventory required |
| Independent UI + Metals/LSP/BSP/DAP/SemanticDB | Compiler-backed Scala intelligence; process isolation is natural; full control over product and UI | Must build editor integrations, workspace transactions, debugger/test UI, indexing UX, keymaps, accessibility and extension host; no IntelliJ plugin compatibility |
| IntelliJ UI/platform + protocol workers | Preserves mature workflows and native plugins; heavy/optional capabilities can run in supervised processes | Two extension models and lifecycle management; duplicate semantic engines would disagree if both owned a Scala document |

## Decision

Use the IntelliJ Platform and the real Scala plugin as the authoritative Scala
editor engine. Build a small Scala-oriented product layer with independent,
testable project/build policy. Use the platform's existing sbt/BSP import, PSI,
completion, diagnostics, run/debug, terminal, Git and action system. Do not
emulate IntelliJ APIs or run Metals over the same documents in parallel.

The selected direction is the third option, with the IntelliJ path implemented
first. Reserve process workers for optional tools and future AI capabilities;
Metals can later be an explicitly selected provider for project types where it
adds value. A provider must own its documents, diagnostics and edits exclusively.

### First runnable slice and distribution boundary

M1 is an isolated IntelliJ development instance with the Scala plugin and our
Scala Workbench product plugin. It uses a pinned platform version and has its
own config, cache, plugin and log directories. It is **not yet a standalone,
rebranded IDE distribution**. It must not be described as production-ready.
Use a local IDEA install for development or let Gradle resolve the pinned host.
Never modify the user's normal IDE installation or settings.

A later standalone release must build the open-source platform from pinned
source, supply product properties/branding and a curated plugin list, inventory
all bundled licenses, and produce signed installers and update metadata. This
is an explicit release requirement, not accomplished by renaming IDEA binaries.

### Compatibility and isolation

Our plugin runs against real IntelliJ APIs; compatibility is limited to the
declared build range and installed module dependencies. This does not promise
that arbitrary Marketplace plugins will work in a future curated product.
Native plugins have separate class loaders but share one JVM. They are trusted
code, **not crash-isolated or security-sandboxed**. An exception boundary cannot
prevent a native plugin from blocking the UI, exhausting memory or exiting.

The future default SDK for computational extensions uses a separate process,
versioned messages, bounded output, timeouts, cancellation and disable-on-fault.
Only explicitly trusted native extensions may access the Swing/editor runtime.
OS-process isolation protects IDE liveness, not filesystem/network permissions;
an OS sandbox would be a separate requirement. Do not claim workers are a
security boundary without implementing one.

## Legal and source findings

The open-source platform and Scala-plugin source use Apache 2.0. Apache 2.0
allows modification/distribution subject to its conditions, including license
and applicable notices; it does not grant rights to upstream trademarks.
Third-party files and bundled components require their own inventory. Current
unified IDEA downloads contain proprietary components and must not be treated
as wholly Apache-licensed. Using a host locally for development does not give
this project permission to redistribute that host. These findings guide the
engineering choice; release licensing must be checked against exact artifacts.

Sources checked on 2026-09-17:

- [Platform purpose and custom products](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html)
- [Platform license](https://github.com/JetBrains/intellij-community/blob/master/LICENSE.txt)
- [Scala plugin source and license](https://github.com/JetBrains/intellij-scala)
- [Apache 2.0 terms](https://www.apache.org/licenses/LICENSE-2.0)
- [Unified IDEA and open-source builds](https://www.jetbrains.com/help/idea/intellij-idea-single-distribution.html)
- [Native plugin class loaders](https://plugins.jetbrains.com/docs/intellij/plugin-class-loaders.html)
- [Metals editor integration](https://scalameta.org/metals/docs/integrations/new-editor/)
- [Metals build integrations](https://scalameta.org/metals/docs/build-tools/overview/)
- [Gradle platform and local host support](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html)

## Consequences

We inherit a coherent IDE rather than rebuild individual editor gestures. The
product layer must stay small and use public APIs. Core project detection and
command planning remain independent of IntelliJ. Adding a protocol, SDK or
abstraction requires a working consumer and behavioral tests. Platform upgrades
need compilation, descriptor validation, integration tests and a manual Scala
workflow check. The roadmap distinguishes inherited capabilities, implemented
integration and unverified workflows.
