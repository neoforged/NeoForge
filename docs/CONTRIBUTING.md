Contributing to NeoForge
=====================

1) Keep patches to Minecraft classes together. If you need a lot of things done, you may either add to relevant forge classes or make a new class. Try not to spread out your patches across multiple disjoint lines, as this makes maintenance of your patches difficult.

2) TODO: Test Mods

3) Follow the code style of the class you're working in (braces on newlines & spaces instead of tabs in Forge classes, inline brackets in patches, etc).

## Workflow

0. Have preliminary discussions on Discord (`#neoforge-github`)
1. Fork the repository
2. Check out your fork
3. Make a branch
4. Run `gradlew :neoforge:setup` from the project root
5. Import project into your IDE (IntelliJ/Eclipse) or Reload Gradle Project
6. Make your changes, modify the decompiled Minecraft source-code as needed
7. Test your changes
   - Run the game (Runs are available in the IDE)
   - Run `gradlew :tests:runGameTestServer` or `Tests: GameTestServer` from IDE
   - Run `gradlew :tests:runGameTestClient` or `Tests: GameTestClient` from IDE
   - Check formatting with `gradlew spotlessCheck`
8. Run `gradlew applyAllFormatting`
9. Run `gradlew unpackSourcePatches`
10. Commit & Push
11. Make PR

Contributor License Agreement
=============================
- You grant NeoForged a license to use your code contributed to the primary codebase (everything **not** under patches) in NeoForge, under the LGPLv2.1 license.
- You assign copyright ownership of your contributions to the patches codebase (everything under patches) to NeoForged, where it will be licensed under the LGPLv2.1 license.

This is intended as a **legally binding copyright assignment** to the NeoForged project for contributions under the patches codebase. However you retain your copyright for all other contributions.
