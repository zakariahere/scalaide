# Goal: Build a Scala-First IntelliJ-Class IDE

Build a production-quality desktop IDE dedicated primarily to **Scala development**.

The goal is not to create another lightweight text editor with Scala support.

The goal is to reproduce the **developer experience, productivity, discoverability, keyboard-driven workflow, extensibility, and depth expected from IntelliJ IDEA**, while designing the product specifically around Scala.

## Core Product Principle

A professional IntelliJ Scala developer should be able to switch to this IDE and feel immediately at home.

Preserve IntelliJ-style muscle memory wherever practical:

- keyboard shortcuts
- Search Everywhere
- Find Action
- project navigation
- symbol/class/file navigation
- go to definition
- find usages
- rename/refactoring workflows
- intentions and quick fixes
- code completion
- parameter information
- inline diagnostics
- inspections
- formatting
- code generation
- run/debug configurations
- test execution
- gutter actions
- project/tool windows
- terminal
- Git workflows
- settings and keymaps

The default keymap should closely reproduce IntelliJ IDEA behavior, while remaining fully configurable.

## Scala Is the First-Class Language

Do not attempt to support every ecosystem IntelliJ supports.

Optimize aggressively for:

- Scala 3
- Scala 2 where practical
- JVM
- Java interoperability
- sbt
- Mill
- Scala CLI
- testing frameworks
- debugging
- SemanticDB
- Metals
- LSP
- BSP

Use existing Scala ecosystem protocols and tooling whenever they provide reliable functionality rather than unnecessarily rebuilding compiler intelligence.

Opening an existing Scala project should require as little configuration as possible.

## IntelliJ-Class UX

The IDE should feel like a cohesive IDE rather than a collection of LSP features.

For example, when a developer writes:

```scala
val user = User("Zak", 30)

user.
```

completion should appear naturally.

The developer should then be able to:

- navigate to `User`
- inspect its type
- find usages
- rename it safely
- invoke intentions
- automatically manage imports
- format code
- inspect compiler errors
- run related tests
- set breakpoints
- debug execution
- inspect JVM values

without leaving the IDE.

## Extensibility Is a Fundamental Requirement

The architecture MUST contain a serious plugin system.

Plugins must be capable of extending areas such as:

- commands/actions
- keyboard shortcuts
- editor behavior
- language support
- inspections
- intentions
- refactorings
- tool windows
- project views
- build systems
- test runners
- debuggers
- themes
- settings
- version-control integrations
- AI functionality

Design stable extension points and a versioned Plugin SDK.

Plugins should be isolated sufficiently that a badly behaving plugin cannot trivially destabilize the entire IDE.

Eventually provide plugin discovery, installation, updating, disabling and a marketplace/catalog mechanism.

## IntelliJ Compatibility Investigation

Before committing to the platform architecture, investigate whether building upon the open-source IntelliJ Platform is technically and legally appropriate for this project.

Compare at minimum:

1. Building on the IntelliJ Platform.
2. Building an independent IDE platform around technologies such as Metals, LSP, BSP, DAP and SemanticDB.
3. A hybrid architecture.

Existing IntelliJ plugin compatibility would be extremely valuable.

However, DO NOT claim compatibility with IntelliJ plugins unless the required IntelliJ Platform APIs and runtime behavior are actually supported.

Prefer real compatibility over superficial API emulation.

Document the architectural decision and its tradeoffs before deeply coupling the implementation to one approach.

## AI-Native Architecture

Design the architecture so that AI capabilities can eventually operate on semantic IDE information rather than merely reading files.

An AI agent should eventually be able to access controlled abstractions for:

- project structure
- symbols
- types
- references
- SemanticDB information
- diagnostics
- build graph
- compiler results
- tests
- Git diff
- terminal commands

This should eventually enable workflows such as:

> Refactor this service without changing its public API, compile the affected modules, run the relevant tests and show me the resulting diff.

AI is NOT required to replace deterministic IDE functionality.

Compiler, language-server and semantic information should remain authoritative where appropriate.

## Architecture Standards

Treat this as a real software product.

Prioritize:

- modular architecture
- strong boundaries
- testability
- observability
- performance
- responsiveness
- crash recovery
- plugin isolation
- backwards-compatible extension APIs
- maintainability

Keep UI, IDE platform services, Scala intelligence, build integration, debugging, plugin infrastructure and AI capabilities appropriately separated.

Avoid premature abstractions, but do not create architectural shortcuts that prevent the IDE from eventually becoming a serious extensible platform.

## Development Strategy

Build vertically.

Do not spend months implementing infrastructure without producing a usable IDE.

The first meaningful milestone should allow me to:

1. Launch the desktop application.
2. Open an existing Scala project.
3. Browse its files.
4. Edit Scala source code.
5. Receive syntax highlighting.
6. Receive diagnostics.
7. Use completion.
8. Navigate to definitions.
9. Build the project.
10. Run tests.
11. Use an integrated terminal.

Then progressively add:

- IntelliJ-compatible keymap behavior
- Search Everywhere
- actions
- refactoring
- intentions
- debugger
- Git
- run configurations
- richer project model
- plugin SDK
- plugin manager
- AI capabilities

Every milestone should leave the repository in a runnable state.

## Engineering Behavior

You are not building a demo.

Whenever implementing functionality:

- inspect the existing architecture first
- preserve established patterns unless there is a strong reason to change them
- prefer established protocols over proprietary reinvention
- write tests for important behavior
- avoid fake implementations that masquerade as completed functionality
- clearly mark temporary implementations
- document significant architectural decisions
- keep changes reasonably scoped
- run relevant tests/builds after modifications
- investigate failures rather than bypassing them

When faced with a major architectural decision, reason from the long-term product goal rather than optimizing only for the immediate task.

## North Star

The product should eventually reach the point where a Scala developer can say:

> "This gives me the productivity and muscle memory I had in IntelliJ, but the entire IDE feels designed around Scala."

That is the standard.

Do not optimize for feature count.

Optimize for **Scala developer experience, IDE depth, speed, extensibility and architectural quality**.