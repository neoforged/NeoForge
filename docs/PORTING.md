Porting NeoForge to New Minecraft Versions
==========================================

## NeoForm

[NeoForm](https://github.com/neoforged/NeoForm) must be updated before anything can be done in NeoForge itself.

## NeoForge

To update to a new Minecraft version, perform these actions to update the source patches:

1. Update `minecraft_version` and `neoform_version` in the `gradle.properties` file.
2. Run `gradlew setup -Pupdating=true` to patch the Minecraft sources in a lenient/"fuzzy" way.
3. Patches that could not be fuzzily applied will be put in the `rejects/` folder.
   The folder must be renamed such that running `gradlew setup` again will not wipe it.
   Rename it to `rejects-<minecraft version>/`. For example `rejects-26.1-snapshot-3/`.
4. Run `gradlew genPatches` to generated updated patches, such that a plain `gradlew setup` will work.
5. Commit the updated files and push them to the repo.
   If this is the first snapshot for a new upcoming version, push it to a `port/<upcoming mc>` branch.
   For example `port/26.1` for the snapshots leading up to the 26.1 release.

At this stage, NeoForge will likely not compile or run yet.
The following steps are usually taken:
- Manually reapplying the patches from a `rejects-*/` folder, or deleting them if the patch is not applicable anymore. 
  Once a patch is applied, delete the corresponding reject.
  Once you are finished, remember to run `gradlew genPatches` to update the patches.
- Fixing compilation errors.
- Making sure the game runs.
- Running and fixing tests.
- Fixing the formatting (`gradlew applyAllFormatting`).

To make a release, first figure out the version. Follow the following format:
```
<upcoming mc>.0-alpha.<N>+<snapshot>
where <upcoming mc> = upcoming minecraft release, padded to 3 components
      <snapshot> = current snapshot, for example snapshot-6
      <N> = alpha number, generally increment for every alpha release leading to a given stable mc version
```
For example, in the 26.1 cycle we published the following alphas:
```
(...)
26.1.0.0-alpha.4+snapshot-1
26.1.0.0-alpha.5+snapshot-2
26.1.0.0-alpha.6+snapshot-2
26.1.0.0-alpha.7+snapshot-3
(...)
```
Once you have the version figured out:
1. Create the corresponding (lightweight) tag: `git tag release/<version>`.
2. Push it: `git push origin release/<version>`.
   This will automatically trigger a release of the snapshot build to our Maven repository.

Contributor License Agreement
=============================

- You grant NeoForged a license to use your code contributed to the primary codebase (everything **not** under patches)
  in NeoForge, under the LGPLv2.1 license.
- You assign copyright ownership of your contributions to the patches codebase (everything under patches) to NeoForged,
  where it will be licensed under the LGPLv2.1 license.

This is intended as a **legally binding copyright assignment** to the NeoForged project for contributions under the
patches codebase. However you retain your copyright for all other contributions.
