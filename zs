[35m.github/ISSUE_TEMPLATE/feature_request.md[m[36m:[m<!-- If you'd like to see something in [1;31mNeoForge[m, you should be ready to implement it as a PR. -->
[35m.github/ISSUE_TEMPLATE/issue_report.md[m[36m:[m<!-- For support with crashes and issues, please make a post in #user-support channel in [1;31mNeoForge[m Discord instead: -->
[35m.github/ISSUE_TEMPLATE/issue_report.md[m[36m:[m**[1;31mNeoForge[m Version:** {[1;31mNeoForge[m version. *Version number, not latest/rb*}
[35m.github/renovate.json[m[36m:[m    "github>[1;31mneoforge[md/actions//renovate/[1;31mneoforge[m"
[35m.github/workflows/build-prs.yml[m[36m:[m# The template can be found at https://github.com/[1;31mneoforge[md/GradleUtils/blob/44d9e09cfa2c6032b84ac40495ea5ab7d64fe521/src/actionsTemplate/resources/.github/workflows/build-prs.yml
[35m.github/workflows/build-prs.yml[m[36m:[m        uses: [1;31mneoforge[md/actions/checkout@main
[35m.github/workflows/build-prs.yml[m[36m:[m        uses: [1;31mneoforge[md/actions/setup-java@main
[35m.github/workflows/build-prs.yml[m[36m:[m        run: ./gradlew :[1;31mneoforge[m:setup -PvalidateAccessTransformers
[35m.github/workflows/build-prs.yml[m[36m:[m        uses: [1;31mneoforge[md/action-jar-compatibility/upload@v1
[35m.github/workflows/build-prs.yml[m[36m:[m        uses: [1;31mneoforge[md/action-pr-publishing/upload@v1
[35m.github/workflows/check-local-changes.yml[m[36m:[m        uses: [1;31mneoforge[md/actions/checkout@main
[35m.github/workflows/check-local-changes.yml[m[36m:[m        uses: [1;31mneoforge[md/actions/setup-java@main
[35m.github/workflows/check-local-changes.yml[m[36m:[m        run: ./gradlew :[1;31mneoforge[m:setup
[35m.github/workflows/check-local-changes.yml[m[36m:[m        run: ./gradlew :[1;31mneoforge[m:genPatches
[35m.github/workflows/check-local-changes.yml[m[36m:[m        run: ./gradlew :[1;31mneoforge[m:runData :tests:runData
[35m.github/workflows/publish-jcc.yml[m[36m:[m# The template can be found at https://github.com/[1;31mneoforge[md/GradleUtils/blob/a65628b0c89dec60b357ce3f8f6bfa62934b8357/src/actionsTemplate/resources/.github/workflows/publish-jcc.yml
[35m.github/workflows/publish-jcc.yml[m[36m:[m    uses: [1;31mneoforge[md/actions/.github/workflows/publish-jcc.yml@main
[35m.github/workflows/publish-prs.yml[m[36m:[m# The template can be found at https://github.com/[1;31mneoforge[md/GradleUtils/blob/44d9e09cfa2c6032b84ac40495ea5ab7d64fe521/src/actionsTemplate/resources/.github/workflows/publish-prs.yml
[35m.github/workflows/publish-prs.yml[m[36m:[m    uses: [1;31mneoforge[md/actions/.github/workflows/publish-prs.yml@main
[35m.github/workflows/publish-prs.yml[m[36m:[m      artifact_base_path: net/[1;31mneoforge[md/[1;31mneoforge[m/|net/[1;31mneoforge[md/testframework/
[35m.github/workflows/release.yml[m[36m:[m# The template can be found at https://github.com/[1;31mneoforge[md/GradleUtils/blob/44d9e09cfa2c6032b84ac40495ea5ab7d64fe521/src/actionsTemplate/resources/.github/workflows/release.yml
[35m.github/workflows/release.yml[m[36m:[m    uses: [1;31mneoforge[md/actions/.github/workflows/gradle-publish.yml@main
[35m.github/workflows/release.yml[m[36m:[m    if: ${{ github.repository == '[1;31mneoforge[md/[1;31mNeoForge[m' }}
[35m.github/workflows/test-prs.yml[m[36m:[m        uses: [1;31mneoforge[md/actions/checkout@main
[35m.github/workflows/test-prs.yml[m[36m:[m        uses: [1;31mneoforge[md/actions/setup-java@main
[35m.github/workflows/test-prs.yml[m[36m:[m        run: xvfb-run ./gradlew :[1;31mneoforge[m:testProductionClient
[35m.github/workflows/test-prs.yml[m[36m:[m        run: ./gradlew :[1;31mneoforge[m:testProductionServer
[35mREADME-LICENSE.md[m[36m:[mUnless noted below, [1;31mNeoForge[m and all its parts here in this repository are
[35mREADME-LICENSE.md[m[36m:[mThe homepage for [1;31mNeoForge[m is at https://[1;31mneoforge[md.net, with the Git 
[35mREADME-LICENSE.md[m[36m:[mrepository located at https://github.com/[1;31mneoforge[md/[1;31mNeoForge[m.
[35mREADME-LICENSE.md[m[36m:[mAs [1;31mNeoForge[m is a software modification made for Minecraft: Java Edition, it and
[35mREADME-LICENSE.md[m[36m:[mall other projects made using [1;31mNeoForge[m (primarily, Minecraft mods) are bound by
[35mREADME-LICENSE.md[m[36m:[m[1;31mNeoForge[m's features and APIs without being bound to be themselves licensed
[35mREADME-LICENSE.md[m[36m:[mThus, mods or other projects using [1;31mNeoForge[m's code through the various Java
[35mREADME-LICENSE.md[m[36m:[mAll contributors to [1;31mNeoForge[m are required to agree to a **Contributor License
[35mREADME-LICENSE.md[m[36m:[m> - You grant [1;31mNeoForge[md a license to use your code contributed to the primary 
[35mREADME-LICENSE.md[m[36m:[m> codebase (everything **not** under patches) in [1;31mNeoForge[m, under the LGPLv2.1 
[35mREADME-LICENSE.md[m[36m:[m> codebase (everything under patches) to [1;31mNeoForge[md, where it will be licensed
[35mREADME-LICENSE.md[m[36m:[m> [1;31mNeoForge[md project for contributions under the patches codebase. However you
[35mREADME-LICENSE.md[m[36m:[mownership of the [1;31mNeoForge[md Team. 
[35mbuild.gradle[m[36m:[m    id 'net.[1;31mneoforge[md.gradleutils' version '4.0.1'
[35mbuild.gradle[m[36m:[m    id 'net.[1;31mneoforge[md.licenser' version '0.7.5'
[35mbuild.gradle[m[36m:[m    id '[1;31mneoforge[m.formatting-conventions'
[35mbuild.gradle[m[36m:[m    id '[1;31mneoforge[m.versioning'
[35mbuild.gradle[m[36m:[m    project.version = "${project.[1;31mneoforge[m_snapshot_next_stable}.0-alpha.${project.minecraft_version}.${(new Date()).format('yyyyMMdd.HHmmss', TimeZone.getTimeZone('UTC'))}"
[35mbuild.gradle[m[36m:[mSystem.out.println("[1;31mNeoForge[m version ${project.version}")
[35mbuild.gradle[m[36m:[m    group = 'net.[1;31mneoforge[md'
[35mbuild.gradle[m[36m:[m// Remove src/ sources from the root project. They are used in the [1;31mneoforge[m subproject.
[35mbuild.gradle[m[36m:[m        [1;31mneoforge[m {
[35mbuild.gradle[m[36m:[m            // Add all [1;31mNeoForge[m sources
[35mbuildSrc/README.md[m[36m:[m# [1;31mNeoForge[m Development Gradle Plugin
[35mbuildSrc/README.md[m[36m:[m## [1;31mNeoForge[m Project Structure
[35mbuildSrc/README.md[m[36m:[mBefore understanding the `buildSrc` plugin, one should understand the structure of the [1;31mNeoForge[m Gradle project it is
[35mbuildSrc/README.md[m[36m:[m| [`/build.gradle`](../build.gradle)                                     | `:`                  | &mdash;                                                     | The root project. Since this project is reused for Kits, the root project name is based on the checkout folder, which actually can lead to issues if it is called `[1;31mNeoForge[m`. |
[35mbuildSrc/README.md[m[36m:[m| [`/projects/[1;31mneoforge[m/build.gradle`](../projects/[1;31mneoforge[m/build.gradle) | `:[1;31mneoforge[m`          | [NeoDevPlugin](#neodevplugin)                               | The core [1;31mNeoForge[m project, which produces the artifacts that will be published.                                                                                               |
[35mbuildSrc/README.md[m[36m:[m| [`/projects/base/build.gradle`](../projects/base/build.gradle)         | `:base`              | [NeoDevBasePlugin](#neodevbaseplugin)                       | A utility project that contains the Minecraft sources without any [1;31mNeoForge[m additions. Can be used to quickly compare what [1;31mNeoForge[m has changed.                               |
[35mbuildSrc/README.md[m[36m:[m| [`/tests/build.gradle`](../tests/build.gradle)                         | `:tests`             | [NeoDevExtraPlugin](#neodevextraplugin)                     | Contains the game and unit tests for [1;31mNeoForge[m.                                                                                                                                |
[35mbuildSrc/README.md[m[36m:[m| [`/coremods/build.gradle`](../coremods/build.gradle)                   | `:[1;31mneoforge[m-coremods` | &mdash;                                                     | Java Bytecode transformers that are embedded into [1;31mNeoForge[m as a nested Jar file.                                                                                              |
[35mbuildSrc/README.md[m[36m:[mSources: [NeoDevBasePlugin.java](src/main/java/net/[1;31mneoforge[md/neodev/NeoDevBasePlugin.java)
[35mbuildSrc/README.md[m[36m:[mSources: [NeoDevPlugin.java](src/main/java/net/[1;31mneoforge[md/neodev/NeoDevPlugin.java)
[35mbuildSrc/README.md[m[36m:[mThis is the primary of this repository and is used to configure the `[1;31mneoforge[m` subproject.
[35mbuildSrc/README.md[m[36m:[m- Decompile Minecraft using the [NeoForm Runtime](https://github.com/[1;31mneoforge[md/neoformruntime) and Minecraft version specific [NeoForm data](https://github.com/[1;31mneoforge[md/NeoForm).
[35mbuildSrc/README.md[m[36m:[m- Applies [[1;31mNeoForge[m patches](../patches) to Minecraft sources. Any rejects are saved to the `/rejects` folder in the repository for manual inspection. During updates to new versions, the task can be run with `-Pupdating=true` to apply patches more leniently.
[35mbuildSrc/README.md[m[36m:[m- Unpacks the patched sources to `projects/[1;31mneoforge[m/src/main/java`.
[35mbuildSrc/README.md[m[36m:[mmods with this version of [1;31mNeoForge[m ([CreateUserDevConfig](src/main/java/net/[1;31mneoforge[md/neodev/CreateUserDevConfig.java)), or install it ([CreateInstallerProfile](src/main/java/net/[1;31mneoforge[md/neodev/installer/CreateInstallerProfile.java) and [CreateLauncherProfile](src/main/java/net/[1;31mneoforge[md/neodev/installer/CreateLauncherProfile.java)).
[35mbuildSrc/README.md[m[36m:[m[1;31mNeoForge[m injects its hooks into Minecraft by patching the decompiled source code.
[35mbuildSrc/README.md[m[36m:[m([GenerateSourcePatches](src/main/java/net/[1;31mneoforge[md/neodev/GenerateSourcePatches.java)).
[35mbuildSrc/README.md[m[36m:[mThe source patches are only used during development of [1;31mNeoForge[m itself and development of mods that use Gradle plugins implementing the decompile/patch/recompile pipeline. 
[35mbuildSrc/README.md[m[36m:[mFor use by the installer intended for players as well as Gradle plugins wanting to replicate the production artifacts more closely, binary patches are generated using the ([GenerateBinaryPatches](src/main/java/net/[1;31mneoforge[md/neodev/GenerateBinaryPatches.java)) task.
[35mbuildSrc/README.md[m[36m:[mSources: [NeoDevExtraPlugin.java](src/main/java/net/[1;31mneoforge[md/neodev/NeoDevExtraPlugin.java)
[35mbuildSrc/README.md[m[36m:[mThis plugin can be applied to obtain a dependency on the `[1;31mneoforge[m` project to depend on [1;31mNeoForge[m including Minecraft
[35mbuildSrc/README.md[m[36m:[m`[1;31mneoforge[m` project.
[35mbuildSrc/README.md[m[36m:[mThis plugin is reused from [ModDevGradle](https://github.com/[1;31mneoforge[md/ModDevGradle/).
[35mbuildSrc/README.md[m[36m:[mthe [libraries that Minecraft itself depends upon](https://github.com/[1;31mneoforge[md/GradleMinecraftDependencies) can be
[35mbuildSrc/build.gradle[m[36m:[m        name = "[1;31mNeoForge[md"
[35mbuildSrc/build.gradle[m[36m:[m        url = "https://maven.[1;31mneoforge[md.net/releases"
[35mbuildSrc/build.gradle[m[36m:[m            includeGroup "net.[1;31mneoforge[md"
[35mbuildSrc/build.gradle[m[36m:[m        url = "https://maven.[1;31mneoforge[md.net/mojang-meta"
[35mbuildSrc/build.gradle[m[36m:[m            includeModule("net.[1;31mneoforge[md", "minecraft-dependencies")
[35mbuildSrc/build.gradle[m[36m:[m    implementation "net.[1;31mneoforge[md:moddev-gradle:${gradle.parent.ext.moddevgradle_plugin_version}"
[35mbuildSrc/build.gradle[m[36m:[m    implementation(platform("net.[1;31mneoforge[md:minecraft-dependencies:${gradle.parent.ext.minecraft_version}") {
[35mbuildSrc/src/main/groovy/neoforge.formatting-conventions.gradle[m[36m:[m                pkgName = pkgName.substring(pkgName.indexOf('net/[1;31mneoforge[md/'), pkgName.lastIndexOf('/'))
[35mbuildSrc/src/main/groovy/neoforge.versioning.gradle[m[36m:[mproject.plugins.apply('net.[1;31mneoforge[md.gradleutils')
[35mbuildSrc/src/main/java/net/neoforged/neodev/ApplyPatches.java[m[36m:[mpackage net.[1;31mneoforge[md.neodev;
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateCleanArtifacts.java[m[36m:[mpackage net.[1;31mneoforge[md.neodev;
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateCleanArtifacts.java[m[36m:[mimport net.[1;31mneoforge[md.nfrtgradle.CreateMinecraftArtifacts;
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[mpackage net.[1;31mneoforge[md.neodev;
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[mimport net.[1;31mneoforge[md.neodev.utils.FileUtils;
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[m * mods for [1;31mNeoForge[m, such as <a href="https://github.com/architectury/architectury-loom">Architectury Loom</a>,
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[m * <a href="https://github.com/[1;31mneoforge[md/ModDevGradle/">ModDevGradle
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[m * or <a href="https://github.com/[1;31mneoforge[md/NeoGradle">NeoGradle</a>.
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[m    abstract Property<String> get[1;31mNeoForge[mVersion();
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[m                "net.[1;31mneoforge[md:neoform:%s-%s@zip".formatted(getMinecraftVersion().get(), getRawNeoFormVersion().get()),
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[m                "net.[1;31mneoforge[md:[1;31mneoforge[m:%s:sources".formatted(get[1;31mNeoForge[mVersion().get()),
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[m                "net.[1;31mneoforge[md:[1;31mneoforge[m:%s:universal".formatted(get[1;31mNeoForge[mVersion().get()),
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[m                        "--version", get[1;31mNeoForge[mVersion().get());
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[m                    "--fml.[1;31mneoForge[mVersion", get[1;31mNeoForge[mVersion().get(),
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[m                systemProperties.put("[1;31mneoforge[m.enableGameTest", "true");
[35mbuildSrc/src/main/java/net/neoforged/neodev/CreateUserDevConfig.java[m[36m:[m                    systemProperties.put("[1;31mneoforge[m.gameTestServer", "true");
[35mbuildSrc/src/main/java/net/neoforged/neodev/GenerateBinaryPatches.java[m[36m:[mpackage net.[1;31mneoforge[md.neodev;
[35mbuildSrc/src/main/java/net/neoforged/neodev/GenerateSourcePatches.java[m[36m:[mpackage net.[1;31mneoforge[md.neodev;
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevBasePlugin.java[m[36m:[mpackage net.[1;31mneoforge[md.neodev;
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevBasePlugin.java[m[36m:[mimport net.[1;31mneoforge[md.minecraftdependencies.MinecraftDependenciesPlugin;
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevBasePlugin.java[m[36m:[mimport net.[1;31mneoforge[md.moddevgradle.internal.NeoDevFacade;
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevBasePlugin.java[m[36m:[mimport net.[1;31mneoforge[md.nfrtgradle.CreateMinecraftArtifacts;
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevBasePlugin.java[m[36m:[mimport net.[1;31mneoforge[md.nfrtgradle.DownloadAssets;
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevConfigurations.java[m[36m:[mpackage net.[1;31mneoforge[md.neodev;
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevConfigurations.java[m[36m:[m * Helper class to keep track of the many {@link Configuration}s used for the {@code [1;31mneoforge[m} project.
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevConfigurations.java[m[36m:[m     * Libraries used by [1;31mNeoForge[m at compilation and runtime.
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevConfigurations.java[m[36m:[m     * Libraries used by [1;31mNeoForge[m at compilation and runtime that need to be placed on the jvm's module path to end up in the boot layer.
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevConfigurations.java[m[36m:[m     * Currently, this is only used for MixinExtras, which is already available at runtime via JiJ in the [1;31mNeoForge[m universal jar.
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevConfigurations.java[m[36m:[m     * This should contain all of [1;31mNeoForge[m's additional dependencies for userdev,
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevConfigurations.java[m[36m:[m     * Libraries that need to be added to the classpath when launching [1;31mNeoForge[m through the launcher.
[35mbuildSrc/src/main/java/net/neoforged/neodev/NeoDevConfigurations.java[m[36m:[m     * This contains all dependencies added by [1;31mNeoForge[m, but does not include all of Minecraft's libraries.
