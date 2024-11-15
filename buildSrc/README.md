# NeoForge Development Gradle Plugin

## NeoForge Project Structure

Before understanding the `buildSrc` plugin, one should understand the structure of the NeoForge Gradle project it is
applied to.

The project consists of a project tree with the following structure:

| Folder Path                                                            | Gradle Project Path  | Applied Plugins                                             | Description                                                                                                                                                                   |
|------------------------------------------------------------------------|----------------------|:------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [`/build.gradle`](../build.gradle)                                     | `:`                  | &mdash;                                                     | The root project. Since this project is reused for Kits, the root project name is based on the checkout folder, which actually can lead to issues if it is called `NeoForge`. |
| [`/projects/neoforge/build.gradle`](../projects/neoforge/build.gradle) | `:neoforge`          | [NeoDevPlugin](#neodevplugin)                               | The core NeoForge project, which produces the artifacts that will be published.                                                                                               |
| [`/projects/base/build.gradle`](../projects/base/build.gradle)         | `:base`              | [NeoDevBasePlugin](#neodevbaseplugin)                       | A utility project that contains the Minecraft sources without any NeoForge additions. Can be used to quickly compare what NeoForge has changed.                               |
| [`/tests/build.gradle`](../tests/build.gradle)                         | `:tests`             | [NeoDevExtraPlugin](#neodevextraplugin)                     | Contains the game and unit tests for NeoForge.                                                                                                                                |
| [`/testframework/build.gradle`](../testframework/build.gradle)         | `:testframework`     | [MinecraftDependenciesPlugin](#minecraftdependenciesplugin) | A library providing support classes around writing game tests.                                                                                                                |
| [`/coremods/build.gradle`](../coremods/build.gradle)                   | `:neoforge-coremods` | &mdash;                                                     | Java Bytecode transformers that are embedded into NeoForge as a nested Jar file.                                                                                              |
|

## Plugins

### NeoDevBasePlugin

Sources: [NeoDevBasePlugin.java](src/main/java/net/neoforged/neodev/NeoDevBasePlugin.java)

Implicitly applies: [MinecraftDependenciesPlugin](#minecraftdependenciesplugin).

Sets up a `setup` task that reuses code from [NeoDevPlugin](#neodevplugin) to decompile Minecraft and place the
decompiled sources in `projects/base/src/main/java`.

### NeoDevPlugin

Sources: [NeoDevPlugin.java](src/main/java/net/neoforged/neodev/NeoDevPlugin.java)

Implicitly applies: [MinecraftDependenciesPlugin](#minecraftdependenciesplugin).

The primary plugin of this repository sets up the `neoforge` project.

#### Setup

It creates a `setup` task that performs the following actions via various subtasks:

- Decompile Minecraft using the [NeoForm Runtime](https://github.com/neoforged/neoformruntime) and Minecraft version specific [NeoForm data](https://github.com/neoforged/NeoForm).
- Applies [Access Transformers](../src/main/resources/META-INF/accesstransformer.cfg) to Minecraft sources.
- Applies [NeoForge patches](../patches) to Minecraft sources. Any rejects are saved to the `/rejects` folder in the repository for manual inspection. During updates to new versions, the task can be run with `-Pupdating=true` to apply patches more leniently.
- Finally it unpacks the patched sources to `projects/neoforge/src/main/java`.

### NeoDevExtraPlugin

Sources: [NeoDevExtraPlugin.java](src/main/java/net/neoforged/neodev/NeoDevExtraPlugin.java)

This plugin can be applied to obtain a dependency on the `neoforge` project to depend on NeoForge including Minecraft
itself. Besides wiring up the dependency, it also creates run configurations based on the run-types defined in the
`neoforge` project.

### MinecraftDependenciesPlugin

This plugin is reused from [ModDevGradle](https://github.com/neoforged/ModDevGradle/).

It sets up repositories and attributes such that
the [libraries that Minecraft itself depends upon](https://github.com/neoforged/GradleMinecraftDependencies) can be
used. 
