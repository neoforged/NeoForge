Porting NeoForge to New Minecraft Versions
==========================================

## Neoform

[NeoForm](https://github.com/neoforged/NeoForm) must be updated before anything can be done in NeoForge itself.

## Neoforge

1. Start Kits branch with [action](https://github.com/neoforged/actions/actions/workflows/start-kits.yml)
    1. Press "Run workflow"
    2. Fill out the fields
        1. The workflow checks out the specified branch from this repository
        2. It updates `minecraft_version` in gradle.properties with the given Minecraft version
        3. It updates `neoform_version` in gradle.properties with the given NeoForm version. Grab it from
           its [project page](https://projects.neoforged.net/neoforged/neoform). The version does not include the
           Minecraft version (i.e. `20240415.193619`)
    3. Wait
        1. If it fails (i.e. with `Failed to download game artifact EXECUTABLE for CLIENT`), retry the job
2. Clone the Kits repository and check out the branch corresponding to the Minecraft version
3. Setup has already been run and `projects/neoforge` will contain the patched sources
4. Fix rejected patch hunks found in the `rejects/` folder by re-applying the broken hunk and deleting the reject file
5. Fix other compile errors
6. Run tests (see above)
7. Fix any problems found by tests
8. Generate patches (`unpackSourcePatches`)
9. Apply formatting (`applyAllFormatting`)
10. Push this state to Kits
11. Create a squashed branch (i.e. `<mc_version>-squashed`)
12. Remove MC sources and commit
13. Squash all commits from "Initial base" created by the action, ensure the resulting commit contains no bot
    authorship.
14. Push the squashed branch to Kits
15. **Make absolutly sure no Minecraft sources are still present in the history of the branch**
16. If this is the first snapshot for a new version, publish this Kits branch to a `port/<mc_version>` branch on the
    main repository.

Contributor License Agreement
=============================

- You grant NeoForged a license to use your code contributed to the primary codebase (everything **not** under patches)
  in NeoForge, under the LGPLv2.1 license.
- You assign copyright ownership of your contributions to the patches codebase (everything under patches) to NeoForged,
  where it will be licensed under the LGPLv2.1 license.

This is intended as a **legally binding copyright assignment** to the NeoForged project for contributions under the
patches codebase. However you retain your copyright for all other contributions.
