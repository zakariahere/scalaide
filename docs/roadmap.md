# Product roadmap

Every milestone must launch, have relevant tests, and distinguish upstream
capabilities from our own integration and validation. The complete brief remains
the north star; this list is not a reduction of its requirements.

## M1 — Usable Scala development slice (current)

- Pin and launch the real IntelliJ host with Scala provisioned.
- Open/import sbt, browse/edit Scala, syntax highlighting, diagnostics,
  completion and definition navigation through the real Scala engine.
- Workbench project view, build choice, compile/test process, cancellation and
  output; inherit native terminal, keymap, search, Git and run/debug controls.
- Test core logic and real platform registration/navigation; document actual
  desktop verification.
- Remaining M1 breadth: full Mill/Scala CLI acceptance projects, sbt options-file
  parity, cancellation/failure/project-close stress tests and cross-platform QA.

## M2 — Product distribution and Scala workflow polish

- Pin `intellij-community` source and Scala-plugin source/artifacts; build a
  custom product using upstream product-properties/build infrastructure.
- Curate open-source platform modules (Java, Scala, terminal, Git, test runner,
  debugger, project/search/settings); remove unrelated ecosystem UI.
- Independent branding and source artifact/license inventory. No copied
  proprietary IDEA binaries and no implied JetBrains endorsement.
- Produce reproducible Windows package, then signed installers and update
  metadata; repeat M1 tests against that exact runtime.
- Welcome screen optimized for Scala, build-import progress/recovery, SDK
  discovery, project diagnostics, persisted native run configurations and
  structured test navigation. Benchmark cold/warm open and typing latency.
- Validate intentions, import optimization, rename preview, mixed Java/Scala
  references, compiler highlighting, formatter and debugger against Scala 2/3.

## M3 — Extension SDK and fault isolation

- Native extensions use genuine IntelliJ APIs. Pin supported SDK/build ranges;
  verify each curated plugin's dependencies and APIs. Trusted native plugins
  remain in-process and can crash or stall the IDE.
- Make a separate-process worker SDK the default for computational extensions:
  version negotiation, request IDs, advertised capabilities, cancellation,
  bounded frames, timeouts, output/log limits, teardown on project close,
  disable-on-fault and deliberate restart. Test hanging/crashing workers.
- Start with a working inspection provider as the consumer. Expand to build
  systems, test runners, protocol adapters and AI tools as concrete needs arise.
- Route commands, settings, themes, tool windows and editor contributions
  through a versioned declarative manifest and reviewed host adapters. Native UI
  extensions remain explicitly trusted. Publish compatibility tests and examples.
- Add plugin installation, enable/disable/update/rollback with verified metadata
  and a catalog. Marketplace distribution and repository compatibility require
  their own terms/verification; a catalog URL alone is not a marketplace.

## M4 — Controlled semantic access for AI

- A read-only project semantic facade over authoritative PSI/compiler/build
  services: symbols, types, references, diagnostics, build graph, tests and Git
  diff. Mark provenance, document version and index readiness.
- SemanticDB and Metals adapters only with explicit capability/ownership rules.
  Never conflate source parsing with compiler-proven types or fabricate results.
- Mutations go through previewable workspace edits and native undo/refactoring
  transactions. Run/build/terminal access requires an explicit capability and
  user-authorized operation. No implicit upload of project source or credentials.
- Validate the service-refactor workflow from the brief, including public API
  preservation, affected modules, selected tests and final diff.

## Production release gates

Signed/reproducible installers and updates; third-party notices; compatible
plugin SDK; documented trust/isolation; automated multi-platform Scala 2/3
acceptance; performance budgets; crash/hang recovery and local-history restore;
accessible UI/keymap QA; dependency update policy; privacy-preserving diagnostics;
support/release procedure. No production-ready claim before these gates pass.
