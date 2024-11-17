package net.neoforged.neodev;

import net.neoforged.minecraftdependencies.MinecraftDependenciesPlugin;
import net.neoforged.moddevgradle.internal.NeoDevFacade;
import net.neoforged.moddevgradle.tasks.JarJar;
import net.neoforged.neodev.installer.CreateArgsFile;
import net.neoforged.neodev.installer.CreateInstallerProfile;
import net.neoforged.neodev.installer.CreateLauncherProfile;
import net.neoforged.neodev.installer.InstallerProcessor;
import net.neoforged.neodev.utils.DependencyUtils;
import net.neoforged.nfrtgradle.CreateMinecraftArtifacts;
import net.neoforged.nfrtgradle.DownloadAssets;
import net.neoforged.nfrtgradle.NeoFormRuntimePlugin;
import net.neoforged.nfrtgradle.NeoFormRuntimeTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.file.Directory;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.BasePluginExtension;
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
import java.util.List;
import java.util.Map;

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
        var fmlVersion = project.getProviders().gradleProperty("fancy_mod_loader_version");
        var minecraftVersion = project.getProviders().gradleProperty("minecraft_version");
        var neoForgeVersion = project.provider(() -> (String) project.getVersion()); // TODO: is this correct?
        var mcAndNeoFormVersion = minecraftVersion.zip(rawNeoFormVersion, (mc, nf) -> mc + "-" + nf);

        var extension = project.getExtensions().create(NeoDevExtension.NAME, NeoDevExtension.class);
        var configurations = NeoDevConfigurations.createAndSetup(project);

        /*
         * MINECRAFT SOURCES SETUP
         */
        // 1. Obtain decompiled Minecraft sources jar using NeoForm.
        var createSourceArtifacts = configureMinecraftDecompilation(project);

        // 2. Apply AT to the source jar from 1.
        var atFile = project.getRootProject().file("src/main/resources/META-INF/accesstransformer.cfg");
        var applyAt = configureAccessTransformer(
                project,
                configurations,
                createSourceArtifacts,
                neoDevBuildDir,
                atFile);

        // 3. Apply patches to the source jar from 2.
        var patchesFolder = project.getRootProject().file("patches");
        var applyPatches = tasks.register("applyPatches", ApplyPatches.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getOriginalJar().set(applyAt.flatMap(ApplyAccessTransformer::getOutputJar));
            task.getPatchesFolder().set(patchesFolder);
            task.getPatchedJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/patched-sources.jar")));
            task.getRejectsFolder().set(project.getRootProject().file("rejects"));
        });

        // 4. Unpack jar from 3.
        var mcSourcesPath = project.file("src/main/java");
        tasks.register("setup", Sync.class, task -> {
            task.setGroup(GROUP);
            task.from(project.zipTree(applyPatches.flatMap(ApplyPatches::getPatchedJar)));
            task.into(mcSourcesPath);
        });

        // 1. Write configs that contain the runs in a format understood by MDG/NG/etc. Currently one for neodev and one for userdev.
        var writeNeoDevConfig = tasks.register("writeNeoDevConfig", WriteUserDevConfig.class, task -> {
            task.getForNeoDev().set(true);
            task.getUserDevConfig().set(neoDevBuildDir.map(dir -> dir.file("neodev-config.json")));
        });
        var writeUserDevConfig = tasks.register("writeUserDevConfig", WriteUserDevConfig.class, task -> {
            task.getForNeoDev().set(false);
            task.getUserDevConfig().set(neoDevBuildDir.map(dir -> dir.file("userdev-config.json")));
        });
        for (var taskProvider : List.of(writeNeoDevConfig, writeUserDevConfig)) {
            taskProvider.configure(task -> {
                task.setGroup(INTERNAL_GROUP);
                task.getFmlVersion().set(fmlVersion);
                task.getMinecraftVersion().set(minecraftVersion);
                task.getNeoForgeVersion().set(neoForgeVersion);
                task.getRawNeoFormVersion().set(rawNeoFormVersion);
                task.getLibraries().addAll(DependencyUtils.configurationToGavList(configurations.userdevClasspath));
                task.getModules().addAll(DependencyUtils.configurationToGavList(configurations.modulePath));
                task.getTestLibraries().addAll(DependencyUtils.configurationToGavList(configurations.userdevTestClasspath));
                task.getTestLibraries().add(neoForgeVersion.map(v -> "net.neoforged:testframework:" + v));
                task.getIgnoreList().addAll(configurations.userdevCompileOnlyClasspath.getIncoming().getArtifacts().getResolvedArtifacts().map(results -> {
                    return results.stream().map(r -> r.getFile().getName()).toList();
                }));
                task.getIgnoreList().addAll("client-extra", "neoforge-");
                task.getBinpatcherGav().set(Tools.BINPATCHER.asGav(project));
            });
        }

        // 2. Task to download assets.
        var downloadAssets = tasks.register("downloadAssets", DownloadAssets.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getNeoFormArtifact().set(mcAndNeoFormVersion.map(v -> "net.neoforged:neoform:" + v + "@zip"));
            task.getAssetPropertiesFile().set(neoDevBuildDir.map(dir -> dir.file("minecraft_assets.properties")));
        });

        // 3. Let MDG do the rest of the setup. :)
        NeoDevFacade.setupRuns(
                project,
                neoDevBuildDir,
                extension.getRuns(),
                writeNeoDevConfig,
                modulePath -> {
                    modulePath.extendsFrom(configurations.moduleLibraries);
                },
                legacyClassPath -> {
                    legacyClassPath.getDependencies().addLater(mcAndNeoFormVersion.map(v -> dependencyFactory.create("net.neoforged:neoform:" + v).capabilities(caps -> {
                        caps.requireCapability("net.neoforged:neoform-dependencies");
                    })));
                    legacyClassPath.getDependencies().add(
                            dependencyFactory.create(
                                    project.files(createSourceArtifacts.flatMap(CreateMinecraftArtifacts::getResourcesArtifact))
                            )
                    );
                    legacyClassPath.extendsFrom(configurations.libraries, configurations.moduleLibraries, configurations.userdevCompileOnly);
                },
                downloadAssets.flatMap(DownloadAssets::getAssetPropertiesFile)
        );
        // TODO: Gradle run tasks should be moved to gradle group GROUP

        /*
         * OTHER TASKS
         */

        // Generate source patches into a patch archive.
        var genSourcePatches = tasks.register("generateSourcePatches", GenerateSourcePatches.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getOriginalJar().set(applyAt.flatMap(ApplyAccessTransformer::getOutputJar));
            task.getModifiedSources().set(project.file("src/main/java"));
            task.getPatchesJar().set(neoDevBuildDir.map(dir -> dir.file("source-patches.zip")));
        });

        // Update the patch/ folder with the current patches.
        tasks.register("genPatches", Sync.class, task -> {
            task.setGroup(GROUP);
            task.from(project.zipTree(genSourcePatches.flatMap(GenerateSourcePatches::getPatchesJar)));
            task.into(project.getRootProject().file("patches"));
        });

        // Universal jar = the jar that contains NeoForge classes
        // TODO: signing?
        var universalJar = tasks.register("universalJar", Jar.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getArchiveClassifier().set("universal");

            task.from(project.zipTree(
                    tasks.named("jar", Jar.class).flatMap(AbstractArchiveTask::getArchiveFile)));
            task.exclude("net/minecraft/**");
            task.exclude("com/**");
            task.exclude("mcp/**");

            task.manifest(manifest -> {
                manifest.attributes(Map.of("FML-System-Mods", "neoforge"));
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

        var createCleanArtifacts = tasks.register("createCleanArtifacts", CreateCleanArtifacts.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            var cleanArtifactsDir = neoDevBuildDir.map(dir -> dir.dir("artifacts/clean"));
            task.getCleanClientJar().set(cleanArtifactsDir.map(dir -> dir.file("client.jar")));
            task.getRawServerJar().set(cleanArtifactsDir.map(dir -> dir.file("raw-server.jar")));
            task.getCleanServerJar().set(cleanArtifactsDir.map(dir -> dir.file("server.jar")));
            task.getCleanJoinedJar().set(cleanArtifactsDir.map(dir -> dir.file("joined.jar")));
            task.getMergedMappings().set(cleanArtifactsDir.map(dir -> dir.file("merged-mappings.txt")));
            task.getNeoFormArtifact().set(mcAndNeoFormVersion.map(version -> "net.neoforged:neoform:" + version + "@zip"));
        });

        var binaryPatchOutputs = configureBinaryPatchCreation(
                project,
                configurations,
                createCleanArtifacts,
                neoDevBuildDir,
                patchesFolder
        );

        // Launcher profile = the version.json file used by the Minecraft launcher.
        var createLauncherProfile = tasks.register("createLauncherProfile", CreateLauncherProfile.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getFmlVersion().set(fmlVersion);
            task.getMinecraftVersion().set(minecraftVersion);
            task.getNeoForgeVersion().set(neoForgeVersion);
            task.getRawNeoFormVersion().set(rawNeoFormVersion);
            task.setLibraries(configurations.launcherProfileClasspath);
            task.getRepositoryURLs().set(project.provider(() -> {
                List<URI> repos = new ArrayList<>();
                for (var repo : project.getRepositories().withType(MavenArtifactRepository.class)) {
                    var uri = repo.getUrl();
                    if (!uri.toString().endsWith("/")) {
                        uri = URI.create(uri + "/");
                    }
                    repos.add(uri);
                }
                return repos;
            }));
            task.getIgnoreList().addAll("client-extra", "neoforge-");
            task.setModules(configurations.modulePath);
            task.getLauncherProfile().set(neoDevBuildDir.map(dir -> dir.file("launcher-profile.json")));
        });

        // Installer profile = the .json file used by the NeoForge installer.
        var createInstallerProfile = tasks.register("createInstallerProfile", CreateInstallerProfile.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getMinecraftVersion().set(minecraftVersion);
            task.getNeoForgeVersion().set(neoForgeVersion);
            task.getMcAndNeoFormVersion().set(mcAndNeoFormVersion);
            task.getIcon().set(project.getRootProject().file("docs/assets/neoforged.ico"));
            // Anything that is on the launcher classpath should be downloaded by the installer.
            // (At least on the server side).
            task.addLibraries(configurations.launcherProfileClasspath);
            // We need the NeoForm zip for the SRG mappings.
            task.addLibraries(configurations.neoFormDataOnly);
            task.getRepositoryURLs().set(project.provider(() -> {
                List<URI> repos = new ArrayList<>();
                for (var repo : project.getRepositories().withType(MavenArtifactRepository.class)) {
                    var uri = repo.getUrl();
                    if (!uri.toString().endsWith("/")) {
                        uri = URI.create(uri + "/");
                    }
                    repos.add(uri);
                }
                return repos;
            }));
            task.getUniversalJar().set(universalJar.flatMap(AbstractArchiveTask::getArchiveFile));
            task.getInstallerProfile().set(neoDevBuildDir.map(dir -> dir.file("installer-profile.json")));
        });

        for (var installerProcessor : InstallerProcessor.values()) {
            var configuration = project.getConfigurations().getByName(installerProcessor.tool.getGradleConfigurationName());
            createInstallerProfile.configure(task -> {
                // Add installer processor.
                // Different processors might use different versions of the same library,
                // but that is fine because each processor gets its own classpath.
                task.addLibraries(configuration);
                task.getProcessorClasspaths().put(installerProcessor, DependencyUtils.configurationToGavList(configuration));
                task.getProcessorGavs().put(installerProcessor, installerProcessor.tool.asGav(project));
            });
        }

        var createWindowsServerArgsFile = tasks.register("createWindowsServerArgsFile", CreateArgsFile.class, task -> {
            task.setLibraries(";", configurations.launcherProfileClasspath, configurations.modulePath);
            task.getArgsFile().set(neoDevBuildDir.map(dir -> dir.file("windows-server-args.txt")));
        });
        var createUnixServerArgsFile = tasks.register("createUnixServerArgsFile", CreateArgsFile.class, task -> {
            task.setLibraries(":", configurations.launcherProfileClasspath, configurations.modulePath);
            task.getArgsFile().set(neoDevBuildDir.map(dir -> dir.file("unix-server-args.txt")));
        });

        for (var taskProvider : List.of(createWindowsServerArgsFile, createUnixServerArgsFile)) {
            taskProvider.configure(task -> {
                task.setGroup(INTERNAL_GROUP);
                task.getTemplate().set(project.getRootProject().file("server_files/args.txt"));
                task.getFmlVersion().set(fmlVersion);
                task.getMinecraftVersion().set(minecraftVersion);
                task.getNeoForgeVersion().set(neoForgeVersion);
                task.getRawNeoFormVersion().set(rawNeoFormVersion);
                task.getIgnoreList().set(List.of());
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
            if (project.getProperties().containsKey("neogradle.runtime.platform.installer.debug") && Boolean.parseBoolean(project.getProperties().get("neogradle.runtime.platform.installer.debug").toString())) {
                task.from(universalJar.flatMap(AbstractArchiveTask::getArchiveFile), spec -> {
                    spec.into(String.format("/maven/net/neoforged/neoforge/%s/", neoForgeVersion.get()));
                    spec.rename(name -> String.format("neoforge-%s-universal.jar", neoForgeVersion.get()));
                });
            }
        });

        var userdevJar = tasks.register("userdevJar", Jar.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.getArchiveClassifier().set("userdev");

            task.from(writeUserDevConfig.flatMap(WriteUserDevConfig::getUserDevConfig), spec -> {
                spec.rename(s -> "config.json");
            });
            task.from(atFile, spec -> {
                spec.into("ats/");
            });
            task.from(binaryPatchOutputs.binaryPatchesForMerged(), spec -> {
                spec.rename(s -> "joined.lzma");
            });
            task.from(project.zipTree(genSourcePatches.flatMap(GenerateSourcePatches::getPatchesJar)), spec -> {
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
    }

    private static TaskProvider<ApplyAccessTransformer> configureAccessTransformer(
            Project project,
            NeoDevConfigurations configurations,
            TaskProvider<CreateMinecraftArtifacts> createSourceArtifacts,
            Provider<Directory> neoDevBuildDir,
            File atFile) {

        return project.getTasks().register("applyAccessTransformer", ApplyAccessTransformer.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.classpath(configurations.getExecutableTool(Tools.JST));
            task.getInputJar().set(createSourceArtifacts.flatMap(CreateMinecraftArtifacts::getSourcesArtifact));
            task.getAccessTransformer().set(atFile);
            task.getOutputJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/access-transformed-sources.jar")));
            task.getLibraries().from(configurations.neoFormClasspath);
            task.getLibrariesFile().set(neoDevBuildDir.map(dir -> dir.file("minecraft-libraries-for-jst.txt")));
        });
    }

    private static BinaryPatchOutputs configureBinaryPatchCreation(Project project,
                                                                   NeoDevConfigurations configurations,
                                                                   TaskProvider<CreateCleanArtifacts> createCleanArtifacts,
                                                                   Provider<Directory> neoDevBuildDir,
                                                                   File sourcesPatchesFolder) {
        var tasks = project.getTasks();

        var artConfig = configurations.getExecutableTool(Tools.AUTO_RENAMING_TOOL);
        var remapClientJar = tasks.register("remapClientJar", RemapJar.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.setDescription("Creates a Minecraft client jar with the official mappings applied. Used as the base for generating binary patches for the client.");
            task.classpath(artConfig);
            task.getMainClass().set("net.neoforged.art.Main");
            task.getObfSlimJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanClientJar));
            task.getMergedMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            task.getMojmapJar().set(neoDevBuildDir.map(dir -> dir.file("remapped-client.jar")));
        });
        var remapServerJar = tasks.register("remapServerJar", RemapJar.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.setDescription("Creates a Minecraft dedicated server jar with the official mappings applied. Used as the base for generating binary patches for the client.");
            task.classpath(artConfig);
            task.getMainClass().set("net.neoforged.art.Main");
            task.getObfSlimJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanServerJar));
            task.getMergedMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            task.getMojmapJar().set(neoDevBuildDir.map(dir -> dir.file("remapped-server.jar")));
        });

        var binpatcherConfig = configurations.getExecutableTool(Tools.BINPATCHER);
        var generateMergedBinPatches = tasks.register("generateMergedBinPatches", GenerateBinaryPatches.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.setDescription("Creates binary patch files by diffing a merged client/server jar-file and the compiled Minecraft classes in this project.");
            task.classpath(binpatcherConfig);
            task.getCleanJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanJoinedJar));
            task.getPatchedJar().set(tasks.named("jar", Jar.class).flatMap(Jar::getArchiveFile));
            task.getSourcePatchesFolder().set(sourcesPatchesFolder);
            task.getMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            task.getOutputFile().set(neoDevBuildDir.map(dir -> dir.file("merged-binpatches.lzma")));
        });
        var generateClientBinPatches = tasks.register("generateClientBinPatches", GenerateBinaryPatches.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.setDescription("Creates binary patch files by diffing a merged client jar-file and the compiled Minecraft classes in this project.");
            task.classpath(binpatcherConfig);
            task.getCleanJar().set(remapClientJar.flatMap(RemapJar::getMojmapJar));
            task.getPatchedJar().set(tasks.named("jar", Jar.class).flatMap(Jar::getArchiveFile));
            task.getSourcePatchesFolder().set(sourcesPatchesFolder);
            task.getMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            task.getOutputFile().set(neoDevBuildDir.map(dir -> dir.file("client-binpatches.lzma")));
        });
        var generateServerBinPatches = tasks.register("generateServerBinPatches", GenerateBinaryPatches.class, task -> {
            task.setGroup(INTERNAL_GROUP);
            task.setDescription("Creates binary patch files by diffing a merged server jar-file and the compiled Minecraft classes in this project.");
            task.classpath(binpatcherConfig);
            task.getCleanJar().set(remapServerJar.flatMap(RemapJar::getMojmapJar));
            task.getPatchedJar().set(tasks.named("jar", Jar.class).flatMap(Jar::getArchiveFile));
            task.getSourcePatchesFolder().set(sourcesPatchesFolder);
            task.getMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            task.getOutputFile().set(neoDevBuildDir.map(dir -> dir.file("server-binpatches.lzma")));
        });

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

        // Configuration for all artifacts that should be passed to NFRT to prevent repeated downloads
        var neoFormRuntimeArtifactManifestNeoForm = configurations.create("neoFormRuntimeArtifactManifestNeoForm", spec -> {
            spec.setCanBeConsumed(false);
            spec.setCanBeResolved(true);
            spec.getDependencies().addLater(mcAndNeoFormVersion.map(version -> {
                return dependencyFactory.create("net.neoforged:neoform:" + version);
            }));
        });

        tasks.withType(NeoFormRuntimeTask.class, task -> {
            task.addArtifactsToManifest(neoFormRuntimeArtifactManifestNeoForm);
        });

        return tasks.register("createSourceArtifacts", CreateMinecraftArtifacts.class, task -> {
            var minecraftArtifactsDir = neoDevBuildDir.map(dir -> dir.dir("artifacts"));
            task.getSourcesArtifact().set(minecraftArtifactsDir.map(dir -> dir.file("base-sources.jar")));
            task.getResourcesArtifact().set(minecraftArtifactsDir.map(dir -> dir.file("minecraft-local-resources-aka-client-extra.jar")));
            task.getNeoFormArtifact().set(mcAndNeoFormVersion.map(version -> "net.neoforged:neoform:" + version + "@zip"));
        });
    }
}
