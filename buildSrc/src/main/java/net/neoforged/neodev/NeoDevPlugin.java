package net.neoforged.neodev;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.neoforged.minecraftdependencies.MinecraftDependenciesPlugin;
import net.neoforged.moddevgradle.internal.NeoDevFacade;
import net.neoforged.moddevgradle.tasks.JarJar;
import net.neoforged.neodev.e2e.InstallProductionClient;
import net.neoforged.neodev.e2e.InstallProductionServer;
import net.neoforged.neodev.e2e.RunProductionClient;
import net.neoforged.neodev.e2e.RunProductionServer;
import net.neoforged.neodev.e2e.TestProductionClient;
import net.neoforged.neodev.e2e.TestProductionServer;
import net.neoforged.neodev.installer.CreateArgsFile;
import net.neoforged.neodev.installer.CreateInstallerProfile;
import net.neoforged.neodev.installer.CreateLauncherProfile;
import net.neoforged.neodev.installer.IdentifiedFile;
import net.neoforged.neodev.installer.InstallerProcessor;
import net.neoforged.neodev.utils.DependencyUtils;
import net.neoforged.nfrtgradle.CreateMinecraftArtifacts;
import net.neoforged.nfrtgradle.DownloadAssets;
import net.neoforged.nfrtgradle.NeoFormRuntimePlugin;
import net.neoforged.nfrtgradle.NeoFormRuntimeTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.language.jvm.tasks.ProcessResources;

public class NeoDevPlugin implements Plugin<Project> {
    static final String GROUP = "neoforge development";
    static final String INTERNAL_GROUP = "neoforge development/internal";

    @Override
    public void apply(Project project) {
        project.getPlugins().apply(MinecraftDependenciesPlugin.class);

        var dependencyFactory = project.getDependencyFactory();
        var tasks = project.getTasks();
        var neoDevBuildDir = project.getLayout().getBuildDirectory().dir("neodev");

        var rawNeoFormVersion = project.getProviders().gradleProperty("neoform_version");
        var minecraftVersion = project.getProviders().gradleProperty("minecraft_version");
        var neoForgeVersion = project.provider(() -> project.getVersion().toString());
        var mcAndNeoFormVersion = minecraftVersion.zip(rawNeoFormVersion, (mc, nf) -> mc + "-" + nf);

        var extension = project.getExtensions().create(NeoDevExtension.NAME, NeoDevExtension.class);
        var configurations = NeoDevConfigurations.createAndSetup(project);

        // Pre-create the "client" source set
        project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets().create("client");

        /*
         * MINECRAFT SOURCES SETUP
         */
        // 1. Obtain decompiled Minecraft sources jar using NeoForm.
        var decompilationSetup = configureMinecraftDecompilation(project);
        // Task must run on sync to have MC resources available for IDEA nondelegated builds.
        NeoDevFacade.runTaskOnProjectSync(project, decompilationSetup.vanillaResources());

        // Remove resources which the "sources" result actually contains
        var vanillaSources = tasks.register("vanillaSources", Zip.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getDestinationDirectory().set(neoDevBuildDir.map(d -> d.dir("artifacts")));
            task.getArchiveFileName().set("base-sources-only.jar");
            task.from(project.zipTree(decompilationSetup.createArtifacts().flatMap(CreateMinecraftArtifacts::getGameSourcesArtifact)));
            task.include("**/*.java");
        });

        // Obtain clean binary artifacts, needed to be able to generate ATs and binary patches
        var createCleanArtifacts = tasks.register("createCleanArtifacts", CreateCleanArtifacts.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.setDescription("This task retrieves various files for the Minecraft version without applying NeoForge patches to them");
            var cleanArtifactsDir = neoDevBuildDir.map(dir -> dir.dir("artifacts/clean"));
            task.getRawClientJar().set(cleanArtifactsDir.map(dir -> dir.file("raw-client.jar")));
            task.getRawServerJar().set(cleanArtifactsDir.map(dir -> dir.file("raw-server.jar")));
            task.getCleanJoinedJar().set(cleanArtifactsDir.map(dir -> dir.file("joined.jar")));
            task.getNeoFormArtifact().set(mcAndNeoFormVersion.map(version -> "net.neoforged:neoform:" + version + "@zip"));
        });

        var genAts = project.getRootProject().file("src/main/resources/META-INF/accesstransformergenerated.cfg");

        var genAtsTask = tasks.register("generateAccessTransformers", GenerateAccessTransformers.class, task -> {
            task.setGroup(GROUP);
            task.setDescription("Generate access transformers based on a set of rules defined in the buildscript");
            task.getInput().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanJoinedJar));
            task.getAccessTransformer().set(genAts);
        });

        // 2. Apply AT to the source jar from 1.
        var atFiles = List.of(
                project.getRootProject().file("src/main/resources/META-INF/accesstransformer.cfg"),
                genAts);
        var applyAt = configureAccessTransformer(
                project,
                vanillaSources.flatMap(Zip::getArchiveFile),
                neoDevBuildDir,
                atFiles);

        applyAt.configure(task -> task.mustRunAfter(genAtsTask));

        // 3. Apply interface injections after the ATs
        // this jar is only used for the patches in the repo
        var applyInterfaceInjection = project.getTasks().register("applyInterfaceInjection", TransformSources.class, task -> {
            task.getInputJar().set(applyAt.flatMap(TransformSources::getOutputJar));
            task.getInterfaceInjectionData().from(project.getRootProject().file("src/main/resources/META-INF/injected-interfaces.json"));
            task.getOutputJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/interface-injected-sources.jar")));
        });

        tasks.withType(TransformSources.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.classpath(configurations.getExecutableTool(Tools.JST));

            task.getLibraries().from(configurations.neoFormClasspath);
            task.getLibrariesFile().set(neoDevBuildDir.map(dir -> dir.file("minecraft-libraries-for-" + task.getName() + ".txt")));
        });

        // 4. Apply patches to the source jar from 3.
        var patchesFolder = project.getRootProject().file("patches");
        var applyPatches = tasks.register("applyPatches", ApplyPatches.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getOriginalJar().set(applyInterfaceInjection.flatMap(TransformSources::getOutputJar));
            task.getPatchesFolder().set(patchesFolder);
            task.getPatchedJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/patched-sources.jar")));
            task.getRejectsFolder().set(project.getRootProject().file("rejects"));
        });

        // 5. Split source jar from 4. into client and server.
        var splitPatchedSources = tasks.register("splitPatchedSources", SplitMergedSources.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getOriginalResourcesJar().set(decompilationSetup.vanillaResources.flatMap(Zip::getArchiveFile));
            task.getMergedJar().set(applyPatches.flatMap(ApplyPatches::getPatchedJar));
            task.getCommonJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/common-patched-sources.jar")));
            task.getClientJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/client-patched-sources.jar")));
        });

        // 6. Unpack jars from 5.
        var setupCommon = tasks.register("setupCommon", Sync.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.from(project.zipTree(splitPatchedSources.flatMap(SplitMergedSources::getCommonJar)));
            task.into(project.file("src/main/java"));
        });
        var setupClient = tasks.register("setupClient", Sync.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.from(project.zipTree(splitPatchedSources.flatMap(SplitMergedSources::getClientJar)));
            task.into(project.file("src/client/java"));
        });
        tasks.register("setup", task -> {
            task.dependsOn(setupCommon, setupClient);
        });

        /*
         * RUNS SETUP
         */

        // 1. Write configs that contain the runs in a format understood by MDG/NG/etc. Currently one for neodev and one for userdev.
        var writeUserDevConfig = tasks.register("writeUserDevConfig", CreateUserDevConfig.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getUserDevConfig().set(neoDevBuildDir.map(dir -> dir.file("userdev-config.json")));
            task.getMinecraftVersion().set(minecraftVersion);
            task.getNeoForgeVersion().set(neoForgeVersion);
            task.getRawNeoFormVersion().set(rawNeoFormVersion);
            task.getLibraries().addAll(DependencyUtils.configurationToGavList(configurations.userdevClasspath));
            task.getTestLibraries().addAll(DependencyUtils.configurationToGavList(configurations.userdevTestClasspath));
            task.getTestLibraries().add(neoForgeVersion.map(v -> "net.neoforged:testframework:" + v));
            task.getBinpatcherGav().set(Tools.BINPATCHER.asGav(project));
        });

        // 2. Task to download assets.
        var downloadAssets = tasks.register("downloadAssets", DownloadAssets.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getNeoFormArtifact().set(mcAndNeoFormVersion.map(v -> "net.neoforged:neoform:" + v + "@zip"));
            task.getAssetPropertiesFile().set(neoDevBuildDir.map(dir -> dir.file("minecraft_assets.properties")));
        });

        // FML needs Minecraft resources on the classpath to find it. Add to runtimeOnly so subprojects also get it at runtime.
        var runtimeClasspath = project.getConfigurations().getByName(JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME);
        runtimeClasspath.getDependencies().add(
                dependencyFactory.create(
                        project.files(decompilationSetup.vanillaResources())));
        // 3. Let MDG do the rest of the setup. :)
        NeoDevFacade.setupRuns(
                project,
                neoDevBuildDir,
                extension.getRuns(),
                writeUserDevConfig,
                modulePath -> {},
                legacyClasspath -> {},
                downloadAssets.flatMap(DownloadAssets::getAssetPropertiesFile),
                mcAndNeoFormVersion);
        // TODO: Gradle run tasks should be moved to gradle group GROUP

        /*
         * OTHER TASKS
         */

        var generateAdditionalMinecraftJarResources = tasks.register("generateMinecraftModsToml", ProcessResources.class, task -> {
            task.getInputs().property("minecraft_version", minecraftVersion.get());
            task.setGroup(INTERNAL_GROUP);
            task.from(new File(project.getRootDir(), "src/main/templates/minecraft.neoforge.mods.toml"), spec -> {
                spec.rename("(.*)", "META-INF/neoforge.mods.toml");
                spec.expand(Map.of("minecraft_version", minecraftVersion.get()));
            });
            task.into(neoDevBuildDir.map(d -> d.dir("additional-minecraft-resources")).get());
        });
        var additionalMinecraftResourcesDir = project.getLayout().dir(generateAdditionalMinecraftJarResources.map(Copy::getDestinationDir));

        // Task to create a jar with both common and client classes.
        // We cannot add the client classes to the default `jar` task because it might be used
        // as a dependency for the compilation of the client classes, leading to a circular dependency.
        var joinedJar = tasks.register("joinedJar", Jar.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getArchiveClassifier().set("joined");
            task.from(project.zipTree(tasks.named("jar", Jar.class).flatMap(AbstractArchiveTask::getArchiveFile)));
            task.from(project.zipTree(tasks.named("clientJar", Jar.class).flatMap(AbstractArchiveTask::getArchiveFile)));
        });

        var mergeSources = tasks.register("mergePatchedSources", Zip.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.from(project.files("src/main/java", "src/client/java"));
            task.getDestinationDirectory().set(neoDevBuildDir.map(dir -> dir.dir("artifacts/merged-sources")));
            task.getArchiveFileName().set("merged-patched-sources.jar");
        });

        var commonSources = tasks.register("commonPatchedSources", Zip.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.from(project.files("src/main/java"));
            task.getDestinationDirectory().set(neoDevBuildDir.map(dir -> dir.dir("artifacts/common-sources")));
            task.getArchiveFileName().set("common-patched-sources.jar");
        });

        // Generate source patches into a patch archive, based on the jar with injected interfaces.
        var genSourcePatches = tasks.register("generateSourcePatches", GenerateSourcePatches.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getOriginalJar().set(applyInterfaceInjection.flatMap(TransformSources::getOutputJar));
            task.getModifiedSources().set(mergeSources.flatMap(AbstractArchiveTask::getArchiveFile));
            task.getPatchesJar().set(neoDevBuildDir.map(dir -> dir.file("source-patches.zip")));
        });

        // Generate source patches that are based on the production environment (without separate interface injection)
        var genProductionSourcePatches = tasks.register("generateProductionSourcePatches", GenerateSourcePatches.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getOriginalJar().set(applyAt.flatMap(TransformSources::getOutputJar));
            task.getModifiedSources().set(mergeSources.flatMap(AbstractArchiveTask::getArchiveFile));
            task.getPatchesFolder().set(neoDevBuildDir.map(dir -> dir.dir("production-source-patches")));
        });
        var genProductionResourcePatches = tasks.register("generateProductionResourcePatches", GenerateResourcePatches.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getAdditionalResourcesDir().set(additionalMinecraftResourcesDir);
            task.getPatchesFolder().set(neoDevBuildDir.map(dir -> dir.dir("production-resource-patches")));
        });

        // Update the patch/ folder with the current patches.
        tasks.register("genPatches", Sync.class, task -> {
            task.setGroup(GROUP);
            task.from(project.zipTree(genSourcePatches.flatMap(GenerateSourcePatches::getPatchesJar)));
            task.into(project.getRootProject().file("patches"));
        });

        // Even the jar built only for local usage in other tasks needs the MANIFEST.MF used to tell FML it's the
        // NeoForge resource jar.
        tasks.named("jar", Jar.class).configure(task -> {
            task.getManifest().attributes(Map.of("FML-System-Mods", "neoforge"));
        });

        var binaryPatchOutputs = configureBinaryPatchCreation(
                project,
                configurations,
                createCleanArtifacts,
                neoDevBuildDir,
                additionalMinecraftResourcesDir);

        // Universal jar = the jar that contains NeoForge classes
        // TODO: signing?
        var universalJar = tasks.register("universalJar", Jar.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getArchiveClassifier().set("universal");
            task.manifest(manifest -> {
                manifest.attributes(Map.of("FML-System-Mods", "neoforge"));
            });

            task.from(project.zipTree(joinedJar.flatMap(AbstractArchiveTask::getArchiveFile)));
            task.exclude("net/minecraft/**");
            task.exclude("com/**");
            task.exclude("mcp/**");
            task.from(binaryPatchOutputs, spec -> {
                spec.into("net/neoforged/neoforge/common/");
                spec.rename(s -> "patches.lzma");
            });
        });

        var jarJarTask = JarJar.registerWithConfiguration(project, "jarJar");
        jarJarTask.configure(task -> task.setGroup(INTERNAL_GROUP));
        universalJar.configure(task -> task.from(jarJarTask));

        var installerRepositoryUrls = getInstallerRepositoryUrls(project);
        // Launcher profile = the version.json file used by the Minecraft launcher.
        var createLauncherProfile = tasks.register("createLauncherProfile", CreateLauncherProfile.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getMinecraftVersion().set(minecraftVersion);
            task.getNeoForgeVersion().set(neoForgeVersion);
            task.getRawNeoFormVersion().set(rawNeoFormVersion);
            task.setLibraries(configurations.launcherProfileClasspath);
            task.setMinecraftLibraries(configurations.minecraftClientClasspath);
            task.getRepositoryURLs().set(installerRepositoryUrls);
            task.getLauncherProfile().set(neoDevBuildDir.map(dir -> dir.file("launcher-profile.json")));
        });

        // Installer profile = the .json file used by the NeoForge installer.
        var createInstallerProfile = tasks.register("createInstallerProfile", CreateInstallerProfile.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getMinecraftVersion().set(minecraftVersion);
            task.getNeoForgeVersion().set(neoForgeVersion);
            task.getMcAndNeoFormVersion().set(mcAndNeoFormVersion);
            task.getIcon().set(project.getRootProject().file("docs/assets/installer_profile_icon.png"));
            // Anything that is on the launcher classpath should be downloaded by the installer.
            // (At least on the server side).
            task.addLibraries(configurations.launcherProfileClasspath);
            // Note: to properly support that, we need to know which libraries are part of the Vanilla server jar
            // to *not* download them before it is unpacked.
            task.addMinecraftServerLibraries(configurations.minecraftServerClasspath);
            task.addMinecraftClientLibraries(configurations.minecraftClientClasspath);
            task.getRepositoryURLs().set(installerRepositoryUrls);
            task.getUniversalJar().set(universalJar.flatMap(AbstractArchiveTask::getArchiveFile));
            task.getInstallerProfile().set(neoDevBuildDir.map(dir -> dir.file("installer-profile.json")));

            // Make all installer processor tools available to the profile
            for (var installerProcessor : InstallerProcessor.values()) {
                var configuration = configurations.getExecutableTool(installerProcessor.tool);
                // Different processors might use different versions of the same library,
                // but that is fine because each processor gets its own classpath.
                task.addLibraries(configuration);
                task.getProcessorClasspaths().put(installerProcessor, DependencyUtils.configurationToGavList(configuration));
                task.getProcessorGavs().put(installerProcessor, installerProcessor.tool.asGav(project));
            }
        });

        var createWindowsServerArgsFile = tasks.register("createWindowsServerArgsFile", CreateArgsFile.class, task -> {
            task.setLibraries(";", configurations.launcherProfileClasspath);
            task.getArgsFile().set(neoDevBuildDir.map(dir -> dir.file("windows-server-args.txt")));
        });
        var createUnixServerArgsFile = tasks.register("createUnixServerArgsFile", CreateArgsFile.class, task -> {
            task.setLibraries(":", configurations.launcherProfileClasspath);
            task.getArgsFile().set(neoDevBuildDir.map(dir -> dir.file("unix-server-args.txt")));
        });

        for (var taskProvider : List.of(createWindowsServerArgsFile, createUnixServerArgsFile)) {
            taskProvider.configure(task -> {
                task.setGroup(INTERNAL_GROUP);
                task.getTemplate().set(project.getRootProject().file("server_files/args.txt"));
                task.getMinecraftVersion().set(minecraftVersion);
                task.getNeoForgeVersion().set(neoForgeVersion);
                task.getRawNeoFormVersion().set(rawNeoFormVersion);
                task.getRawServerJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getRawServerJar));
            });
        }

        var installerConfig = configurations.getExecutableTool(Tools.LEGACYINSTALLER);
        // TODO: signing?
        // We want to inherit the executable JAR manifest from LegacyInstaller.
        // - Jar tasks have special manifest handling, so use Zip.
        // - The manifest must be the first entry in the jar so LegacyInstaller has to be the first input.
        var installerJar = tasks.register("installerJar", Zip.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getArchiveClassifier().set("installer");
            task.getArchiveExtension().set("jar");
            task.setMetadataCharset("UTF-8");
            task.getDestinationDirectory().convention(project.getExtensions().getByType(BasePluginExtension.class).getLibsDirectory());

            task.from(project.zipTree(project.provider(installerConfig::getSingleFile)), spec -> {
                spec.exclude("big_logo.png");
            });
            task.from(createLauncherProfile.flatMap(CreateLauncherProfile::getLauncherProfile), spec -> {
                spec.rename(s -> "version.json");
            });
            task.from(createInstallerProfile.flatMap(CreateInstallerProfile::getInstallerProfile), spec -> {
                spec.rename(s -> "install_profile.json");
            });
            task.from(project.getRootProject().file("src/main/resources/url.png"));
            task.from(project.getRootProject().file("src/main/resources/neoforged_logo.png"), spec -> {
                spec.rename(s -> "big_logo.png");
            });
            task.from(createUnixServerArgsFile.flatMap(CreateArgsFile::getArgsFile), spec -> {
                spec.into("data");
                spec.rename(s -> "unix_args.txt");
            });
            task.from(createWindowsServerArgsFile.flatMap(CreateArgsFile::getArgsFile), spec -> {
                spec.into("data");
                spec.rename(s -> "win_args.txt");
            });
            task.from(binaryPatchOutputs, spec -> {
                spec.into("data");
                spec.rename(s -> "client.lzma");
            });
            var mavenPath = neoForgeVersion.map(v -> "net/neoforged/neoforge/" + v);
            task.getInputs().property("mavenPath", mavenPath);
            task.from(project.getRootProject().files("server_files"), spec -> {
                spec.into("data");
                spec.exclude("args.txt");
                spec.filter(s -> {
                    return s.replaceAll("@MAVEN_PATH@", mavenPath.get());
                });
            });

            // This is true by default (see gradle.properties), and needs to be disabled explicitly when building (see release.yml).
            String installerDebugProperty = "neogradle.runtime.platform.installer.debug";
            if (project.getProperties().containsKey(installerDebugProperty) && Boolean.parseBoolean(project.getProperties().get(installerDebugProperty).toString())) {
                task.from(universalJar.flatMap(AbstractArchiveTask::getArchiveFile), spec -> {
                    spec.into(String.format("/maven/net/neoforged/neoforge/%s/", neoForgeVersion.get()));
                    spec.rename(name -> String.format("neoforge-%s-universal.jar", neoForgeVersion.get()));
                });
            }
        });

        var userdevJar = tasks.register("userdevJar", Jar.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getArchiveClassifier().set("userdev");

            task.from(writeUserDevConfig.flatMap(CreateUserDevConfig::getUserDevConfig), spec -> {
                spec.rename(s -> "config.json");
            });
            task.from(atFiles, spec -> {
                spec.into("ats/");
            });
            task.from(binaryPatchOutputs, spec -> {
                spec.rename(s -> "patches.lzma");
            });
            task.from(genProductionSourcePatches.flatMap(GenerateSourcePatches::getPatchesFolder), spec -> {
                spec.into("patches/");
            });
            task.from(genProductionResourcePatches.flatMap(GenerateResourcePatches::getPatchesFolder), spec -> {
                spec.into("patches/");
            });
        });

        project.getExtensions().getByType(JavaPluginExtension.class).withSourcesJar();
        var sourcesJarProvider = project.getTasks().named("sourcesJar", Jar.class);
        sourcesJarProvider.configure(task -> {
            task.exclude("net/minecraft/**");
            task.exclude("com/**");
            task.exclude("mcp/**");
        });

        tasks.named("assemble", task -> {
            task.dependsOn(installerJar);
            task.dependsOn(universalJar);
            task.dependsOn(userdevJar);
            task.dependsOn(sourcesJarProvider);
        });

        // Set up E2E testing of the produced installer
        setupProductionClientTest(
                project,
                configurations,
                downloadAssets,
                installerJar,
                minecraftVersion,
                neoForgeVersion,
                createCleanArtifacts.flatMap(CreateCleanArtifacts::getRawClientJar));
        setupProductionServerTest(project, installerJar);
    }

    /**
     * Get the list of Maven repositories that may contain artifacts for the installer.
     */
    private static Provider<List<URI>> getInstallerRepositoryUrls(Project project) {
        return project.provider(() -> {
            List<URI> repos = new ArrayList<>();
            var projectRepos = project.getRepositories();
            if (!projectRepos.isEmpty()) {
                for (var repo : projectRepos.withType(MavenArtifactRepository.class)) {
                    repos.add(repo.getUrl());
                }
            } else {
                // If no project repos are defined, use the repository list we exposed in settings.gradle via an extension
                // See the end of settings.gradle for details
                Collections.addAll(repos, (URI[]) project.getGradle().getExtensions().getByName("repositoryBaseUrls"));
            }

            // Ensure all base urls end with a slash
            repos.replaceAll(uri -> uri.toString().endsWith("/") ? uri : URI.create(uri + "/"));

            return repos;
        });
    }

    private static TaskProvider<TransformSources> configureAccessTransformer(
            Project project,
            Provider<RegularFile> sourceArtifact,
            Provider<Directory> neoDevBuildDir,
            List<File> atFiles) {
        // Pass -PvalidateAccessTransformers to validate ATs.
        var validateAts = project.getProviders().gradleProperty("validateAccessTransformers").map(p -> true).orElse(false);
        return project.getTasks().register("applyAccessTransformer", TransformSources.class, task -> {
            task.getInputJar().set(sourceArtifact);
            task.getAccessTransformers().from(atFiles);
            task.getValidateAccessTransformers().set(validateAts);
            task.getOutputJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/access-transformed-sources.jar")));
        });
    }

    private static Provider<RegularFile> configureBinaryPatchCreation(
            Project project,
            NeoDevConfigurations neoDevConfigurations,
            TaskProvider<CreateCleanArtifacts> createCleanArtifacts,
            Provider<Directory> neoDevBuildDir,
            Provider<Directory> additionalMinecraftResources) {
        var tasks = project.getTasks();

        var clientBaseJar = setupBinaryPatchBaseJar(project, neoDevBuildDir, BinaryPatchBaseType.CLIENT, neoDevConfigurations, createCleanArtifacts);
        var serverBaseJar = setupBinaryPatchBaseJar(project, neoDevBuildDir, BinaryPatchBaseType.SERVER, neoDevConfigurations, createCleanArtifacts);
        var joinedBaseJar = setupBinaryPatchBaseJar(project, neoDevBuildDir, BinaryPatchBaseType.JOINED, neoDevConfigurations, createCleanArtifacts);
        var clientModifiedJar = setupBinaryPatchModifiedJar(project, neoDevBuildDir, BinaryPatchBaseType.CLIENT, additionalMinecraftResources);
        var serverModifiedJar = setupBinaryPatchModifiedJar(project, neoDevBuildDir, BinaryPatchBaseType.SERVER, additionalMinecraftResources);

        var binpatcherConfig = neoDevConfigurations.getExecutableTool(Tools.BINPATCHER);
        var generatePatchBundles = tasks.register("generatePatchBundle", GenerateBinaryPatches.class, task -> {
            task.setDescription("Generates the binary patches.");
            task.setGroup(INTERNAL_GROUP);
            task.classpath(binpatcherConfig);

            task.getBaseClientJar().set(clientBaseJar);
            task.getModifiedClientJar().set(clientModifiedJar);
            task.getBaseServerJar().set(serverBaseJar);
            task.getModifiedServerJar().set(serverModifiedJar);
            task.getBaseJoinedJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanJoinedJar));
            // Since we're filtering by *.class, the modified jar for client and joined is identical. They differ in manifest only.
            task.getModifiedJoinedJar().set(clientModifiedJar);
            task.getInclude().add("**/*.class");
            task.getInclude().add("META-INF/neoforge.mods.toml");

            task.getOutputFile().set(neoDevBuildDir.map(dir -> dir.file("patches.lzma")));
        });

        var patchBundle = generatePatchBundles.flatMap(GenerateBinaryPatches::getOutputFile);

        tasks.register("listPatchBundleContent", ListBinaryPatches.class, task -> {
            task.setDescription("Lists the content of the created binary patch bundle.");
            task.setGroup(INTERNAL_GROUP);
            task.classpath(binpatcherConfig);
            task.getPatchBundle().set(patchBundle);
            task.getOutputFile().set(neoDevBuildDir.map(dir -> dir.file("patches-content.txt")));
        });

        return patchBundle;
    }

    /**
     * Sets up NFRT, and creates the sources and resources artifacts.
     */
    static DecompilationSetup configureMinecraftDecompilation(Project project) {
        project.getPlugins().apply(NeoFormRuntimePlugin.class);

        var configurations = project.getConfigurations();
        var dependencyFactory = project.getDependencyFactory();
        var tasks = project.getTasks();
        var neoDevBuildDir = project.getLayout().getBuildDirectory().dir("neodev");

        var rawNeoFormVersion = project.getProviders().gradleProperty("neoform_version");
        var minecraftVersion = project.getProviders().gradleProperty("minecraft_version");
        var mcAndNeoFormVersion = minecraftVersion.zip(rawNeoFormVersion, (mc, nf) -> mc + "-" + nf);

        // NeoForm data + tools to run it
        var neoFormRuntimeDataOnly = configurations.create("neoFormRuntimeDataOnly", spec -> {
            spec.setCanBeConsumed(false);
            spec.setCanBeResolved(true);
            spec.getDependencies().addLater(mcAndNeoFormVersion.map(version -> {
                return dependencyFactory.create("net.neoforged:neoform:" + version);
            }));
        });
        // Minecraft's dependencies
        var neoFormRuntimeMinecraftDependencies = configurations.create("neoFormRuntimeMinecraftDependencies", spec -> {
            spec.setCanBeConsumed(false);
            spec.setCanBeResolved(true);
            spec.getDependencies().addLater(mcAndNeoFormVersion.map(version -> {
                return dependencyFactory.create("net.neoforged:neoform:" + version).capabilities(caps -> {
                    caps.requireCapability("net.neoforged:neoform-dependencies");
                });
            }));
            spec.attributes(attrs -> {
                attrs.attribute(Usage.USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_API));
            });
        });

        tasks.withType(NeoFormRuntimeTask.class, task -> {
            task.addArtifactsToManifest(neoFormRuntimeDataOnly);
            task.addArtifactsToManifest(neoFormRuntimeMinecraftDependencies);
        });

        var minecraftArtifactsDir = neoDevBuildDir.map(dir -> dir.dir("artifacts"));
        var createSources = tasks.register("createSourceArtifacts", CreateMinecraftArtifacts.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getGameSourcesArtifact().set(minecraftArtifactsDir.map(dir -> dir.file("base-sources.jar")));
            task.getNeoFormArtifact().set(mcAndNeoFormVersion.map(version -> "net.neoforged:neoform:" + version + "@zip"));
        });

        var vanillaResources = tasks.register("vanillaResources", Zip.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getDestinationDirectory().set(minecraftArtifactsDir);
            task.getArchiveFileName().set("minecraft-resources.jar");
            task.from(project.zipTree(createSources.flatMap(CreateMinecraftArtifacts::getGameSourcesArtifact)));
            task.exclude("**/*.java");
        });

        return new DecompilationSetup(createSources, vanillaResources);
    }

    record DecompilationSetup(TaskProvider<CreateMinecraftArtifacts> createArtifacts, TaskProvider<Zip> vanillaResources) {}

    enum BinaryPatchBaseType {
        CLIENT,
        SERVER,
        JOINED;

        public String taskName(String prefix) {
            return prefix + Character.toUpperCase(toString().charAt(0)) + toString().substring(1);
        }

        @Override
        public String toString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private static Provider<RegularFile> setupBinaryPatchBaseJar(
            Project project,
            Provider<Directory> neoDevBuildDir,
            BinaryPatchBaseType type,
            NeoDevConfigurations neoDevConfigurations,
            TaskProvider<CreateCleanArtifacts> createCleanArtifacts) {
        var tasks = project.getTasks();

        var binpatchesDir = neoDevBuildDir.map(dir -> dir.dir("artifacts/binpatches"));

        var installerToolsConfig = neoDevConfigurations.getExecutableTool(Tools.INSTALLERTOOLS);
        var baseJar = tasks.register(type.taskName("createBaseJar"), GenerateBaseJar.class, task -> {
            task.setDescription("Generates the base jar for creating binary patches of the " + type + " distribution");
            task.setGroup(INTERNAL_GROUP);
            if (type == BinaryPatchBaseType.CLIENT || type == BinaryPatchBaseType.JOINED) {
                task.getMinecraft().from(createCleanArtifacts.flatMap(CreateCleanArtifacts::getRawClientJar));
            }
            if (type == BinaryPatchBaseType.SERVER || type == BinaryPatchBaseType.JOINED) {
                task.getMinecraft().from(createCleanArtifacts.flatMap(CreateCleanArtifacts::getRawServerJar));
            }
            task.getOutput().set(binpatchesDir.map(dir -> dir.file(type + "-base.jar")));
            task.classpath(installerToolsConfig);
        });

        return baseJar.flatMap(GenerateBaseJar::getOutput);
    }

    private static Provider<RegularFile> setupBinaryPatchModifiedJar(
            Project project,
            Provider<Directory> neoDevBuildDir,
            BinaryPatchBaseType type,
            Provider<Directory> extraResourcesDir) {
        var tasks = project.getTasks();

        var binpatchesDir = neoDevBuildDir.map(dir -> dir.dir("artifacts/binpatches"));

        // Create the jar file in its target state. We will create binary patches to convert the base-jar to this jar.
        var sourceSets = project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
        var modifiedJar = tasks.register(type.taskName("createModifiedJar"), Jar.class, task -> {
            task.setDescription("Create the jar file for " + type + " in the state that we want to create binpatches from. This jar only contains classes since we don't modify original resources at the moment.");
            task.setGroup(INTERNAL_GROUP);
            task.getDestinationDirectory().set(binpatchesDir);
            task.getArchiveFileName().set(type + "-modified-classes.jar");

            // Copy only the unmodified+modified Minecraft classes, excluding NeoForges own classes
            var mainSourceSet = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            task.from(mainSourceSet.getJava().getClassesDirectory(), spec -> {
                spec.exclude("net/neoforged/**");
            });
            if (type == BinaryPatchBaseType.CLIENT || type == BinaryPatchBaseType.JOINED) {
                var clientSourceSet = sourceSets.getByName("client");
                task.from(clientSourceSet.getJava().getClassesDirectory(), spec -> {
                    spec.exclude("net/neoforged/**");
                });
            }
            task.from(extraResourcesDir);
        });

        return modifiedJar.flatMap(AbstractArchiveTask::getArchiveFile);
    }

    private void setupProductionClientTest(
            Project project,
            NeoDevConfigurations configurations,
            TaskProvider<? extends DownloadAssets> downloadAssets,
            TaskProvider<? extends AbstractArchiveTask> installer,
            Provider<String> minecraftVersion,
            Provider<String> neoForgeVersion,
            Provider<RegularFile> originalClientJar) {
        var installClient = project.getTasks().register("installProductionClient", InstallProductionClient.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.setDescription("Runs the installer produced by this build and installs a production client.");
            task.getInstaller().from(installer.flatMap(AbstractArchiveTask::getArchiveFile));

            var destinationDir = project.getLayout().getBuildDirectory().dir("production-client");
            task.getInstallationDir().set(destinationDir);
        });

        Consumer<RunProductionClient> configureRunProductionClient = task -> {
            task.getLibraryFiles().addAll(IdentifiedFile.listFromConfiguration(project, configurations.neoFormClasspath));
            task.getLibraryFiles().addAll(IdentifiedFile.listFromConfiguration(project, configurations.launcherProfileClasspath));
            task.getAssetPropertiesFile().set(downloadAssets.flatMap(DownloadAssets::getAssetPropertiesFile));
            task.getMinecraftVersion().set(minecraftVersion);
            task.getNeoForgeVersion().set(neoForgeVersion);
            task.getInstallationDir().set(installClient.flatMap(InstallProductionClient::getInstallationDir));
            task.getOriginalClientJar().set(originalClientJar);
            task.getJavaRuntimeVersion().set(project.getProviders().gradleProperty("java_version").map(Integer::parseInt));
        };
        project.getTasks().register("runProductionClient", RunProductionClient.class, task -> {
            task.setGroup(GROUP);
            task.setDescription("Runs the production client installed by installProductionClient.");
            configureRunProductionClient.accept(task);
        });
        project.getTasks().register("testProductionClient", TestProductionClient.class, task -> {
            task.setGroup(GROUP);
            task.setDescription("Tests the production client installed by installProductionClient.");
            configureRunProductionClient.accept(task);
        });
    }

    private void setupProductionServerTest(Project project, TaskProvider<? extends AbstractArchiveTask> installer) {
        var installServer = project.getTasks().register("installProductionServer", InstallProductionServer.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.setDescription("Runs the installer produced by this build and installs a production server.");
            task.getInstaller().from(installer.flatMap(AbstractArchiveTask::getArchiveFile));

            var destinationDir = project.getLayout().getBuildDirectory().dir("production-server");
            task.getInstallationDir().set(destinationDir);
        });

        project.getTasks().register("runProductionServer", RunProductionServer.class, task -> {
            task.setGroup(GROUP);
            task.setDescription("Runs the production server installed by installProductionServer.");
            task.getInstallationDir().set(installServer.flatMap(InstallProductionServer::getInstallationDir));
            task.getJavaRuntimeVersion().set(project.getProviders().gradleProperty("java_version").map(Integer::parseInt));
        });

        project.getTasks().register("testProductionServer", TestProductionServer.class, task -> {
            task.setGroup(GROUP);
            task.setDescription("Tests the production server installed by installProductionServer.");
            task.getInstallationDir().set(installServer.flatMap(InstallProductionServer::getInstallationDir));
            task.getJavaRuntimeVersion().set(project.getProviders().gradleProperty("java_version").map(Integer::parseInt));
        });
    }
}
