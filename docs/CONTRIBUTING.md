Contributing to NeoForge
=====================

1) Keep patches to Minecraft classes together. If you need a lot of things done, you may either add to relevant forge classes or make a new class. Try not to spread out your patches across multiple disjoint lines, as this makes maintenance of your patches difficult.

2) TODO: Test Mods

3) Follow the code style of the class you're working in (braces on newlines & spaces instead of tabs in Forge classes, inline brackets in patches, etc).

## Workflow

1. Have preliminary discussions on Discord (`#neoforge-github`)
2. Fork the repository
3. Check out your fork
4. Make a branch
5. Run `gradlew setup` from the project root to decompile sources and apply current patches
6. Import project into your IDE (IntelliJ/Eclipse) or Reload Gradle Project
7. Modify the patched Minecraft sources in `projects/neoforge/src/main/java` as needed. The unmodified sources are available in `projects/base/src/main/java` for your reference. Do not modify these.
8. Test your changes
   - Run the game (Runs are available in the IDE)
   - Run `gradlew :tests:runGameTestServer` or `Tests: GameTestServer` from IDE
   - Run `gradlew :tests:runGameTestClient` or `Tests: GameTestClient` from IDE
9. Run `gradlew unpackSourcePatches` to generate patch-files from the patched sources
10. Run `gradlew applyAllFormatting` to automatically format sources
11. Check correct formatting with `gradlew spotlessCheck`
12. Commit & Push
13. Make PR

Contributor License Agreement
=============================
- You grant NeoForged a license to use your code contributed to the primary codebase (everything **not** under patches) in NeoForge, under the LGPLv2.1 license.
- You assign copyright ownership of your contributions to the patches codebase (everything under patches) to NeoForged, where it will be licensed under the LGPLv2.1 license.

This is intended as a **legally binding copyright assignment** to the NeoForged project for contributions under the patches codebase. However you retain your copyright for all other contributions.
