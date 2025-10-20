package net.neoforged.neodev;

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
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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

        /*
         * MINECRAFT SOURCES SETUP
         */
        // 1. Obtain decompiled Minecraft sources jar using NeoForm.
        var createSourceArtifacts = configureMinecraftDecompilation(project);
        // Task must run on sync to have MC resources available for IDEA nondelegated builds.
        NeoDevFacade.runTaskOnProjectSync(project, createSourceArtifacts);

        // Obtain clean binary artifacts, needed to be able to generate ATs
        var createCleanArtifacts = tasks.register("createCleanArtifacts", CreateCleanArtifacts.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.setDescription("This task retrieves various files for the Minecraft version without applying NeoForge patches to them");
            var cleanArtifactsDir = neoDevBuildDir.map(dir -> dir.dir("artifacts/clean"));
            task.getRawClientJar().set(cleanArtifactsDir.map(dir -> dir.file("raw-client.jar")));
            task.getCleanClientJar().set(cleanArtifactsDir.map(dir -> dir.file("client.jar")));
            task.getRawServerJar().set(cleanArtifactsDir.map(dir -> dir.file("raw-server.jar")));
            task.getCleanServerJar().set(cleanArtifactsDir.map(dir -> dir.file("server.jar")));
            task.getCleanJoinedJar().set(cleanArtifactsDir.map(dir -> dir.file("joined.jar")));
            task.getMergedMappings().set(cleanArtifactsDir.map(dir -> dir.file("merged-mappings.txt")));
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
                genAts
        );
        var applyAt = configureAccessTransformer(
                project,
                createSourceArtifacts,
                neoDevBuildDir,
                atFiles);

        applyAt.configure(task -> task.mustRunAfter(genAtsTask));
        
        var splitUnpatchedSources = tasks.register("splitUnpatchedSources", SplitMergedSources.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getMergedJar().set(applyAt.flatMap(TransformSources::getOutputJar));
            task.getCommonJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/common-unpatched-sources.jar")));
            task.getClientJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/client-unpatched-sources.jar")));
        });

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
                        project.files(createSourceArtifacts.flatMap(CreateMinecraftArtifacts::getResourcesArtifact))
                )
        );
        // 3. Let MDG do the rest of the setup. :)
        NeoDevFacade.setupRuns(
                project,
                neoDevBuildDir,
                extension.getRuns(),
                writeUserDevConfig,
                modulePath -> {},
                legacyClasspath -> {},
                downloadAssets.flatMap(DownloadAssets::getAssetPropertiesFile),
                mcAndNeoFormVersion
        );
        // TODO: Gradle run tasks should be moved to gradle group GROUP

        /*
         * OTHER TASKS
         */

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
        var genProductionPatches = tasks.register("generateProductionSourcePatches", GenerateSourcePatches.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getOriginalJar().set(applyAt.flatMap(TransformSources::getOutputJar));
            task.getModifiedSources().set(mergeSources.flatMap(AbstractArchiveTask::getArchiveFile));
            task.getPatchesFolder().set(neoDevBuildDir.map(dir -> dir.dir("production-source-patches")));
        });

        var genCommonProductionPatches = tasks.register("generateCommonProductionSourcePatches", GenerateSourcePatches.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getOriginalJar().set(splitUnpatchedSources.flatMap(SplitMergedSources::getCommonJar));
            task.getModifiedSources().set(commonSources.flatMap(AbstractArchiveTask::getArchiveFile));
            task.getPatchesFolder().set(neoDevBuildDir.map(dir -> dir.dir("production-source-patches-common")));
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

        // Universal jar = the jar that contains NeoForge classes
        // TODO: signing?
        var universalJar = tasks.register("universalJar", Jar.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getArchiveClassifier().set("universal");

            task.from(project.zipTree(
                    joinedJar.flatMap(AbstractArchiveTask::getArchiveFile)));
            task.exclude("net/minecraft/**");
            task.exclude("com/**");
            task.exclude("mcp/**");

            task.manifest(manifest -> {
                manifest.attributes(Map.of("FML-System-Mods", "neoforge"));
                // These attributes are used from NeoForgeVersion.java to find the NF version without command line arguments.
                manifest.attributes(
                        Map.of(
                                "Specification-Title", "NeoForge",
                                "Specification-Vendor", "NeoForge",
                                "Specification-Version", project.getVersion().toString().substring(0, project.getVersion().toString().lastIndexOf(".")),
                                "Implementation-Title", project.getGroup(),
                                "Implementation-Version", project.getVersion(),
                                "Implementation-Vendor", "NeoForged"),
                        "net/neoforged/neoforge/internal/versions/neoforge/");
                manifest.attributes(
                        Map.of(
                                "Specification-Title", "Minecraft",
                                "Specification-Vendor", "Mojang",
                                "Specification-Version", minecraftVersion,
                                "Implementation-Title", "MCP",
                                "Implementation-Version", mcAndNeoFormVersion,
                                "Implementation-Vendor", "NeoForged"),
                        "net/neoforged/neoforge/versions/neoform/");
            });
        });

        var jarJarTask = JarJar.registerWithConfiguration(project, "jarJar");
        jarJarTask.configure(task -> task.setGroup(INTERNAL_GROUP));
        universalJar.configure(task -> task.from(jarJarTask));

        var binaryPatchOutputs = configureBinaryPatchCreation(
                project,
                configurations,
                createCleanArtifacts,
                joinedJar,
                neoDevBuildDir,
                genProductionPatches.flatMap(GenerateSourcePatches::getPatchesFolder),
                genCommonProductionPatches.flatMap(GenerateSourcePatches::getPatchesFolder)
        );

        var installerRepositoryUrls = getInstallerRepositoryUrls(project);
        // Launcher profile = the version.json file used by the Minecraft launcher.
        var createLauncherProfile = tasks.register("createLauncherProfile", CreateLauncherProfile.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getMinecraftVersion().set(minecraftVersion);
            task.getNeoForgeVersion().set(neoForgeVersion);
            task.getRawNeoFormVersion().set(rawNeoFormVersion);
            task.setLibraries(configurations.launcherProfileClasspath);
            task.setMinecraftLibraries(configurations.neoFormClasspath);
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
            // We need the NeoForm zip for the SRG mappings.
            task.addLibraries(configurations.neoFormDataOnly);
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
            task.from(binaryPatchOutputs.binaryPatchesForClient(), spec -> {
                spec.into("data");
                spec.rename(s -> "client.lzma");
            });
            task.from(binaryPatchOutputs.binaryPatchesForServer(), spec -> {
                spec.into("data");
                spec.rename(s -> "server.lzma");
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
            task.from(binaryPatchOutputs.binaryPatchesForMerged(), spec -> {
                spec.rename(s -> "joined.lzma");
            });
            task.from(project.fileTree(genProductionPatches.flatMap(GenerateSourcePatches::getPatchesFolder)), spec -> {
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
                createCleanArtifacts.flatMap(CreateCleanArtifacts::getRawClientJar)
        );
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
            TaskProvider<CreateMinecraftArtifacts> createSourceArtifacts,
            Provider<Directory> neoDevBuildDir,
            List<File> atFiles) {

        // Pass -PvalidateAccessTransformers to validate ATs.
        var validateAts = project.getProviders().gradleProperty("validateAccessTransformers").map(p -> true).orElse(false);
        return project.getTasks().register("applyAccessTransformer", TransformSources.class, task -> {
            task.getInputJar().set(createSourceArtifacts.flatMap(CreateMinecraftArtifacts::getSourcesArtifact));
            task.getAccessTransformers().from(atFiles);
            task.getValidateAccessTransformers().set(validateAts);
            task.getOutputJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/access-transformed-sources.jar")));
        });
    }

    private static BinaryPatchOutputs configureBinaryPatchCreation(Project project,
                                                                   NeoDevConfigurations configurations,
                                                                   TaskProvider<CreateCleanArtifacts> createCleanArtifacts,
                                                                   TaskProvider<Jar> joinedJar,
                                                                   Provider<Directory> neoDevBuildDir,
                                                                   Provider<Directory> sourcesPatchesFolder,
                                                                   Provider<Directory> sourcesServerPatchesFolder) {
        var tasks = project.getTasks();

        var artConfig = configurations.getExecutableTool(Tools.AUTO_RENAMING_TOOL);
        var remapClientJar = tasks.register("remapClientJar", RemapJar.class, task -> {
            task.setDescription("Creates a Minecraft client jar with the official mappings applied. Used as the base for generating binary patches for the client.");
            task.getInputJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanClientJar));
            task.getOutputJar().set(neoDevBuildDir.map(dir -> dir.file("remapped-client.jar")));
        });
        var remapServerJar = tasks.register("remapServerJar", RemapJar.class, task -> {
            task.setDescription("Creates a Minecraft dedicated server jar with the official mappings applied. Used as the base for generating binary patches for the client.");
            task.getInputJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanServerJar));
            task.getOutputJar().set(neoDevBuildDir.map(dir -> dir.file("remapped-server.jar")));
        });
        for (var remapTask : List.of(remapClientJar, remapServerJar)) {
            remapTask.configure(task -> {
                task.setGroup(INTERNAL_GROUP);
                task.classpath(artConfig);
                task.getMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            });
        }

        var binpatcherConfig = configurations.getExecutableTool(Tools.BINPATCHER);
        var generateMergedBinPatches = tasks.register("generateMergedBinPatches", GenerateBinaryPatches.class, task -> {
            task.setDescription("Creates binary patch files by diffing a merged client/server jar-file and the compiled Minecraft classes in this project.");
            task.getCleanJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanJoinedJar));
            // Included so that lambda names are correct in production
            task.getIncludeClassesJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanJoinedJar));
            task.getSourcePatchesFolder().set(sourcesPatchesFolder);
            task.getOutputFile().set(neoDevBuildDir.map(dir -> dir.file("merged-binpatches.lzma")));
        });
        var generateClientBinPatches = tasks.register("generateClientBinPatches", GenerateBinaryPatches.class, task -> {
            task.setDescription("Creates binary patch files by diffing a merged client jar-file and the compiled Minecraft classes in this project.");
            task.getCleanJar().set(remapClientJar.flatMap(RemapJar::getOutputJar));
            // Included so that lambda names are correct in production
            task.getIncludeClassesJar().set(remapClientJar.flatMap(RemapJar::getOutputJar));
            task.getSourcePatchesFolder().set(sourcesPatchesFolder);
            task.getOutputFile().set(neoDevBuildDir.map(dir -> dir.file("client-binpatches.lzma")));
        });
        var generateServerBinPatches = tasks.register("generateServerBinPatches", GenerateBinaryPatches.class, task -> {
            task.setDescription("Creates binary patch files by diffing a merged server jar-file and the compiled Minecraft classes in this project.");
            task.getCleanJar().set(remapServerJar.flatMap(RemapJar::getOutputJar));
            // Included so that lambda names are correct in production
            task.getIncludeClassesJar().set(remapServerJar.flatMap(RemapJar::getOutputJar));
            task.getSourcePatchesFolder().set(sourcesServerPatchesFolder);
            task.getOutputFile().set(neoDevBuildDir.map(dir -> dir.file("server-binpatches.lzma")));
        });
        for (var generateBinPatchesTask : List.of(generateMergedBinPatches, generateClientBinPatches, generateServerBinPatches)) {
            generateBinPatchesTask.configure(task -> {
                task.setGroup(INTERNAL_GROUP);
                task.classpath(binpatcherConfig);
                task.getPatchedJar().set(joinedJar.flatMap(Jar::getArchiveFile));
                task.getMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            });
        }

        return new BinaryPatchOutputs(
                generateMergedBinPatches.flatMap(GenerateBinaryPatches::getOutputFile),
                generateClientBinPatches.flatMap(GenerateBinaryPatches::getOutputFile),
                generateServerBinPatches.flatMap(GenerateBinaryPatches::getOutputFile)
        );
    }

    private record BinaryPatchOutputs(
            Provider<RegularFile> binaryPatchesForMerged,
            Provider<RegularFile> binaryPatchesForClient,
            Provider<RegularFile> binaryPatchesForServer
    ) {
    }

    /**
     * Sets up NFRT, and creates the sources and resources artifacts.
     */
    static TaskProvider<CreateMinecraftArtifacts> configureMinecraftDecompilation(Project project) {
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

        return tasks.register("createSourceArtifacts", CreateMinecraftArtifacts.class, task -> {
            var minecraftArtifactsDir = neoDevBuildDir.map(dir -> dir.dir("artifacts"));
            task.getSourcesArtifact().set(minecraftArtifactsDir.map(dir -> dir.file("base-sources.jar")));
            task.getResourcesArtifact().set(minecraftArtifactsDir.map(dir -> dir.file("minecraft-resources.jar")));
            task.getNeoFormArtifact().set(mcAndNeoFormVersion.map(version -> "net.neoforged:neoform:" + version + "@zip"));
        });
    }

    private void setupProductionClientTest(Project project,
                                      NeoDevConfigurations configurations,
                                      TaskProvider<? extends DownloadAssets> downloadAssets,
                                      TaskProvider<? extends AbstractArchiveTask> installer,
                                      Provider<String> minecraftVersion,
                                           Provider<String> neoForgeVersion,
                                           Provider<RegularFile> originalClientJar
    ) {

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
        });

        project.getTasks().register("testProductionServer", TestProductionServer.class, task -> {
            task.setGroup(GROUP);
            task.setDescription("Tests the production server installed by installProductionServer.");
            task.getInstallationDir().set(installServer.flatMap(InstallProductionServer::getInstallationDir));
        });
    }
}
