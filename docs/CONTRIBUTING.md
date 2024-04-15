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

## Porting

### Neoform

[NeoForm](https://github.com/neoforged/NeoForm) must be updated before anything can be done in NeoForge itself.

### Neoforge

1. Start Kits branch with [action](https://github.com/neoforged/actions/actions/workflows/start-kits.yml)
   1. Press "Run workflow"
   2. Fill out the survey
      1. The workflow checks out the specified branch from this repository
      2. It updates `minecraft_version` in gradle.properties with the given Minecraft version
      3. It updates `neoform_version` in gradle.properties with the given NeoForm version. Grab it from its [project page](https://projects.neoforged.net/neoforged/neoform). The version does not include the Minecraft version (i.e. `20240415.193619`)
   3. Wait
      1. If it fails (i.e. with `Failed to download game artifact EXECUTABLE for CLIENT`), retry the job
2. Clone the Kits repository and check out the branch corresponding to the Minecraft version
3. Setup has already been run and `projects/neoforge` will contain the patched sources
4. Fix rejected patch hunks found in the `rejects/` folder by re-applying the broken hunk and deleting the reject file
5. Fix other compile errors
6. Run tests (see above)
7. Fix any problems found by tests
8. Create a squashed branch (i.e. `<mc_version>-squashed`)
9. Generate patches (`unpackSourcePatches`)
10. Apply formatting (`applyAllFormatting`)
11. Remove MC sources and commit
12. Squash all commits from "Initial base" created by the action, ensure the resulting commit contains no bot authorship (due to CLA signing issues later).
13. **Make absolutly sure no Minecraft sources are still present in the history of the branch**
14. If this is the first snapshot for a new version, publish this kits branch to a `port/<mc_version>` branch on the main repository, otherwise push just the new, squashed commit

Contributor License Agreement
=============================
- You grant NeoForged a license to use your code contributed to the primary codebase (everything **not** under patches) in NeoForge, under the LGPLv2.1 license.
- You assign copyright ownership of your contributions to the patches codebase (everything under patches) to NeoForged, where it will be licensed under the LGPLv2.1 license.

This is intended as a **legally binding copyright assignment** to the NeoForged project for contributions under the patches codebase. However you retain your copyright for all other contributions.
