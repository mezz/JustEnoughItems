# Contributing to JEI

Please target the latest active Minecraft branch first. Reviewed changes can then be backported to older supported branches, including 1.12.

## Pull requests

Keep each pull request focused on one fix, feature, or cleanup. Describe the problem, resulting behavior, and validation, and link the relevant issue. Keep unrelated refactors, formatting, and dependency changes separate.

Do not include build outputs, logs, run directories, generated IDE files, or release artifacts.

## Building and testing

This branch targets Minecraft 1.12.2 and Forge, with Java 8 production bytecode. The Gradle daemon uses JDK 25 and the compiler toolchain uses JDK 21. Development clients and servers run on Java 8.

Run the complete verification suite before opening or updating a pull request:

```shell
./gradlew test build --no-daemon
```

This runs unit and integration tests, checks the packaged jar's Java 8 bytecode and JEI interface linkage, and compares the API against the latest published JEI 4 API. The API check can also be run separately:

```shell
./gradlew checkApiCompatibility --no-daemon
```

Include meaningful regression coverage for production changes where practical. Explain when a change does not need an automated test, and record any manual client checks for GUI changes.

Match nearby Java formatting and naming. This branch does not define Spotless tasks.

## API compatibility

Preserve public API types and methods, including deprecated APIs. Add new methods compatibly for existing addon implementations, update the minor specification version for additions, and document the introducing version with `@since`.

Breaking API changes require a major version bump and are reserved for Minecraft-version updates.

## Code organization

This branch is a single-module RetroFuturaGradle project. Public addon contracts belong in `src/api/java`, implementation and resources in `src/main`, and regression tests in `src/test/java`.

When adding a package or source root, include a `package-info.java` with annotations matching nearby packages.

Keep build tooling, Java requirements, publishing, and dependency changes scoped to pull requests that need them. Retain Minecraft 1.12's Forge APIs and runtime requirements when adapting changes from newer branches.
