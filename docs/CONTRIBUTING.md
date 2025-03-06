Contributing to NeoForge
=====================

1) Keep patches to Minecraft classes together. If you need a lot of things done, you may either add to relevant forge classes or make a new class. Try not to spread out your patches across multiple disjoint lines, as this makes maintenance of your patches difficult.

2) TODO: Test Mods

3) Follow the code style of the class you're working in (braces on newlines & spaces instead of tabs in Forge classes, inline brackets in patches, etc).

## Workflow

1. Have preliminary discussions on Discord (`#neoforge-github`)
2. Fork the repository
3. Check out your fork
4. Fetch tags from the upstream repository by running `git remote add upstream https://github.com/neoforged/NeoForge.git` followed by `git fetch upstream --tags`
5. Make a branch
6. Run `gradlew setup` from the project root to decompile sources and apply current patches
7. Import project into your IDE (IntelliJ/Eclipse) or Reload Gradle Project
8. Modify the patched Minecraft sources in `projects/neoforge/src/main/java` as needed. The unmodified sources are available in `projects/base/src/main/java` for your reference. Do not modify these.
9. Test your changes
   - Run the game (Runs are available in the IDE)
     - Runs starting with `base -` run Vanilla without NeoForge or its patches.
     - Runs starting with `neoforge -` run NeoForge.
     - Runs starting with `tests -` run NeoForge along with the test mods in the `tests` project.
   - Run `gradlew :tests:runGameTestServer` or `tests - GameTestServer` from IDE
   - Run `gradlew :tests:runClient` or `tests - Client` from IDE
   - If possible, write an automated test under the tests project. See [NEOGAMETESTS.md](NEOGAMETESTS.md) for more info.
10. Run `gradlew genPatches` to generate patch-files from the patched sources
11. Run `gradlew applyAllFormatting` to automatically format sources
12. Check correct formatting with `gradlew checkFormatting`
13. Commit & Push
14. Make PR

## Porting

If you are interested in how NeoForge is ported to new Minecraft versions, see [the porting workflow][Porting].
Please note that currently  only maintainers can use all the needed tools.
Please do not open a porting PR without prior coordination.

Contributor License Agreement
=============================
- You grant NeoForged a license to use your code contributed to the primary codebase (everything **not** under patches) in NeoForge, under the LGPLv2.1 license.
- You assign copyright ownership of your contributions to the patches codebase (everything under patches) to NeoForged, where it will be licensed under the LGPLv2.1 license.

This is intended as a **legally binding copyright assignment** to the NeoForged project for contributions under the patches codebase. However you retain your copyright for all other contributions.

[Porting]: ../docs/PORTING.md
