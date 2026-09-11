# Contributing to JEI

Thanks for helping improve JEI. Please keep pull requests focused and target the latest active Minecraft branch. Changes can be backported to older supported branches after they are reviewed and merged.

## Pull requests

Keep each pull request scoped to one fix, feature, or cleanup. Avoid mixing unrelated refactors, formatting-only changes, dependency updates, and behavior changes in the same pull request.

Describe the problem being fixed, the behavior change, and how you tested it. Link the relevant issue when one exists.

Do not include build outputs, logs, run directories, generated IDE files, or release artifacts.

## Formatting

JEI uses Spotless to enforce Java formatting. Before opening or updating a pull request, run:

```shell
./gradlew spotlessApply
```

To verify formatting without changing files, run:

```shell
./gradlew spotlessCheck
```

## Tests

Pull requests that change production Java code should include relevant tests or game tests in the same pull request.

If a production-code change does not need a test, explain why in the pull request description. A maintainer can apply the `no-tests-needed` label to bypass the automated test-change check.

Run the standard verification suite before opening or updating a pull request:

```shell
./gradlew check
```

This checks formatting and API compatibility, runs unit tests and server GameTests, validates the Fabric access widener, and compiles the client GameTest source sets.

Client game tests require a graphical environment. On Linux CI they run through `xvfb-run`.

## API compatibility

Preserve API compatibility by default. Avoid removing, renaming, or changing public API methods and types. Prefer additive APIs and long deprecation windows when migration is needed.

Breaking API changes require a JEI major version bump and should only be considered for Minecraft-version updates.

## Code organization

Keep changes in the module that owns the behavior. Avoid moving code between Common, Gui, Library, Fabric, NeoForge, and their API source sets unless the change specifically requires it.

Subproject relationships are grouped by published code modules and helper modules:

```mermaid
flowchart TB
    subgraph published["Published code modules"]
        direction TB

        Common["Common<br/>shared API + implementation"]

        Gui["Gui<br/>client GUI"]
        Library["Library<br/>runtime implementation"]

        subgraph loaders["Loader packages"]
            direction LR

            Fabric["Fabric<br/>API + packaged mod"]
            NeoForge["NeoForge<br/>API + packaged mod"]
        end
    end

    subgraph helpers["Helper / non-published modules"]
        direction LR
        Debug["Debug<br/>development plugin"]
        Changelog["Changelog<br/>release notes"]
    end

    Common --> Gui
    Common --> Library
    Common --> loaders
    Gui --> loaders
    Library --> loaders

    classDef code fill:#e9fbe8,stroke:#1a7f37,color:#0b1f33
    classDef package fill:#f0e7ff,stroke:#8250df,color:#0b1f33
    classDef helper fill:#fff7d6,stroke:#9a6700,stroke-dasharray: 5 3,color:#0b1f33

    class Common,Gui,Library code
    class Fabric,NeoForge package
    class Debug,Changelog helper
```

When adding Java code in a new package or source root, add the usual `package-info.java` with package annotations matching nearby code.

Avoid build tooling, Java version, dependency, publishing, and version-number changes unless the pull request is specifically about that infrastructure.
