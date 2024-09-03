package net.neoforged.neodev;

import net.neoforged.moddevgradle.dsl.NeoForgeExtension;
import net.neoforged.moddevgradle.dsl.RunModel;
import net.neoforged.moddevgradle.internal.ArtifactManifestEntry;
import net.neoforged.moddevgradle.internal.CreateArtifactManifestTask;
import net.neoforged.moddevgradle.internal.DistributionDisambiguation;
import net.neoforged.moddevgradle.internal.ModDevPlugin;
import net.neoforged.moddevgradle.internal.OperatingSystemDisambiguation;
import net.neoforged.moddevgradle.internal.PrepareRun;
import net.neoforged.moddevgradle.internal.RunUtils;
import net.neoforged.moddevgradle.internal.utils.DependencyUtils;
import net.neoforged.moddevgradle.internal.utils.ExtensionUtils;
import net.neoforged.neodev.installer.CreateArgsFile;
import net.neoforged.neodev.installer.CreateInstallerProfile;
import net.neoforged.neodev.installer.CreateLauncherProfile;
import net.neoforged.neodev.installer.InstallerProcessor;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.artifacts.dsl.DependencyFactory;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.plugins.BasePluginExtension;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.api.tasks.testing.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NeoDevPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getRepositories().maven(repo -> {
            repo.setName("NeoForged maven");
            repo.setUrl("https://maven.neoforged.net/releases/");
        });
        // TODO: why addLast?
        project.getRepositories().addLast(project.getRepositories().maven(repo -> {
            repo.setUrl(URI.create("https://libraries.minecraft.net/"));
            repo.metadataSources(sources -> sources.artifact());
            // TODO: Filter known groups that they ship and dont just run everything against it
        }));
        project.getRepositories().maven(repo -> {
            repo.setName("Mojang Meta");
            repo.setUrl("https://maven.neoforged.net/mojang-meta/");
            repo.metadataSources(sources -> sources.gradleMetadata());
            repo.content(content -> {
                content.includeModule("net.neoforged", "minecraft-dependencies");
            });
        });

        project.getDependencies().attributesSchema(attributesSchema -> {
            attributesSchema.attribute(ModDevPlugin.ATTRIBUTE_DISTRIBUTION).getDisambiguationRules().add(DistributionDisambiguation.class);
            attributesSchema.attribute(ModDevPlugin.ATTRIBUTE_OPERATING_SYSTEM).getDisambiguationRules().add(OperatingSystemDisambiguation.class);
        });
    }

    public void configureBase(Project project) {
        var configurations = project.getConfigurations();
        var dependencyFactory = project.getDependencyFactory();
        var tasks = project.getTasks();
        var neoDevBuildDir = project.getLayout().getBuildDirectory().dir("neodev");

        var rawNeoFormVersion = project.getProviders().gradleProperty("neoform_version");
        var minecraftVersion = project.getProviders().gradleProperty("minecraft_version");
        var mcAndNeoFormVersion = minecraftVersion.zip(rawNeoFormVersion, (mc, nf) -> mc + "-" + nf);
        // TODO: pull from MDG
        var neoFormRuntimeVersion = project.getProviders().provider(() -> "1.0.5");

        var neoFormRuntimeConfig = configurations.create("neoFormRuntime", files -> {
            files.setCanBeConsumed(false);
            files.setCanBeResolved(true);
            files.defaultDependencies(spec -> {
                spec.addLater(neoFormRuntimeVersion.map(version -> dependencyFactory.create("net.neoforged:neoform-runtime:" + version).attributes(attributes -> {
                    attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, Bundling.SHADOWED));
                })));
            });
        });

        // Configuration for all artifact that should be passed to NFRT for preventing repeated downloads
        var neoFormRuntimeArtifactManifestNeoForm = configurations.create("neoFormRuntimeArtifactManifestNeoForm", spec -> {
            spec.setCanBeConsumed(false);
            spec.setCanBeResolved(true);
            spec.withDependencies(dependencies -> {
                dependencies.addLater(mcAndNeoFormVersion.map(version -> {
                    return dependencyFactory.create("net.neoforged:neoform:" + version);
                }));
            });
        });

        var createManifest = tasks.register("createArtifactManifest", CreateArtifactManifestTask.class, task -> {
            task.getNeoForgeModDevArtifacts().addAll(neoFormRuntimeArtifactManifestNeoForm.getIncoming().getArtifacts().getResolvedArtifacts().map(results -> {
                return results.stream().map(ArtifactManifestEntry::new).collect(Collectors.toSet());
            }));
            task.getManifestFile().set(neoDevBuildDir.map(dir -> dir.file("neoform_artifact_manifest.properties")));
        });

        var createSources = tasks.register("createSourcesArtifact", CreateMinecraftArtifactsTask.class, task -> {
            task.getNeoFormRuntime().from(neoFormRuntimeConfig);
            task.getArtifactManifestFile().set(createManifest.get().getManifestFile());
            task.getNeoFormArtifact().set(mcAndNeoFormVersion);

            var minecraftArtifactsDir = neoDevBuildDir.map(dir -> dir.dir("artifacts"));
            task.getSourcesArtifact().set(minecraftArtifactsDir.map(dir -> dir.file("base-sources.jar")));
            task.getResourcesArtifact().set(minecraftArtifactsDir.map(dir -> dir.file("minecraft-local-resources-aka-client-extra.jar")));
        });

        tasks.register("setup", Sync.class, task -> {
            task.from(project.zipTree(createSources.flatMap(CreateMinecraftArtifactsTask::getSourcesArtifact)));
            task.into(project.file("src/main/java/"));
        });
    }

    public void configureNeoForge(Project project) {
        var configurations = project.getConfigurations();
        var runtimeClasspath = configurations.getByName(JavaPlugin.RUNTIME_CLASSPATH_CONFIGURATION_NAME);
        var dependencyFactory = project.getDependencyFactory();
        var tasks = project.getTasks();
        var neoDevBuildDir = project.getLayout().getBuildDirectory().dir("neodev");

        var rawNeoFormVersion = project.getProviders().gradleProperty("neoform_version");
        var fmlVersion = project.getProviders().gradleProperty("fancy_mod_loader_version");
        var minecraftVersion = project.getProviders().gradleProperty("minecraft_version");
        var neoForgeVersion = project.provider(() -> (String) project.getVersion()); // TODO: is this correct?
        var mcAndNeoFormVersion = minecraftVersion.zip(rawNeoFormVersion, (mc, nf) -> mc + "-" + nf);
        // TODO: pull from MDG
        var neoFormRuntimeVersion = project.getProviders().provider(() -> "1.0.5");

        var extension = project.getExtensions().create(NeoForgeExtension.NAME, NeoForgeExtension.class);

        var neoFormRuntimeConfig = configurations.create("neoFormRuntime", files -> {
            files.setCanBeConsumed(false);
            files.setCanBeResolved(true);
            files.defaultDependencies(spec -> {
                spec.addLater(neoFormRuntimeVersion.map(version -> dependencyFactory.create("net.neoforged:neoform-runtime:" + version).attributes(attributes -> {
                    attributes.attribute(Bundling.BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, Bundling.SHADOWED));
                })));
            });
        });

        // Configuration for all artifact that should be passed to NFRT for preventing repeated downloads
        var neoFormRuntimeArtifactManifestNeoForm = configurations.create("neoFormRuntimeArtifactManifestNeoForm", spec -> {
            spec.setCanBeConsumed(false);
            spec.setCanBeResolved(true);
            spec.withDependencies(dependencies -> {
                dependencies.addLater(mcAndNeoFormVersion.map(version -> {
                    return dependencyFactory.create("net.neoforged:neoform:" + version);
                }));
            });
        });
        var neoFormDependencies = configurations.create("neoFormDependencies", spec -> {
            spec.setCanBeConsumed(false);
            spec.setCanBeResolved(true);
            spec.withDependencies(dependencies -> {
                dependencies.addLater(mcAndNeoFormVersion.map(version -> {
                    var dep = dependencyFactory.create("net.neoforged:neoform:" + version).capabilities(caps -> {
                        caps.requireCapability("net.neoforged:neoform-dependencies");
                    });
                    dep.endorseStrictVersions();
                    return dep;
                }));
            });
        });

        var jstConfiguration = configurations.create("javaSourceTransformer", files -> {
            files.setCanBeConsumed(false);
            files.setCanBeResolved(true);
            files.defaultDependencies(spec -> {
                spec.add(dependencyFactory.create("net.neoforged.jst:jst-cli-bundle:1.0.38")); // TODO: don't hardcode here
            });
        });

        var createManifest = tasks.register("createArtifactManifest", CreateArtifactManifestTask.class, task -> {
            task.getNeoForgeModDevArtifacts().addAll(neoFormRuntimeArtifactManifestNeoForm.getIncoming().getArtifacts().getResolvedArtifacts().map(results -> {
                return results.stream().map(ArtifactManifestEntry::new).collect(Collectors.toSet());
            }));
            task.getManifestFile().set(neoDevBuildDir.map(dir -> dir.file("neoform_artifact_manifest.properties")));
        });

        var createArtifacts = tasks.register("createMinecraftArtifacts", CreateMinecraftArtifactsTask.class, task -> {
            task.getNeoFormRuntime().from(neoFormRuntimeConfig);
            task.getArtifactManifestFile().set(createManifest.get().getManifestFile());
            task.getNeoFormArtifact().set(mcAndNeoFormVersion);

            var minecraftArtifactsDir = neoDevBuildDir.map(dir -> dir.dir("artifacts"));
            task.getSourcesArtifact().set(minecraftArtifactsDir.map(dir -> dir.file("base-sources.jar")));
            task.getResourcesArtifact().set(minecraftArtifactsDir.map(dir -> dir.file("minecraft-local-resources-aka-client-extra.jar")));
        });

        var atFile = project.getRootProject().file("src/main/resources/META-INF/accesstransformer.cfg");
        var applyAt = tasks.register("applyAccessTransformer", ApplyAccessTransformer.class, task -> {
            task.classpath(jstConfiguration);
            task.getInputJar().set(createArtifacts.flatMap(CreateMinecraftArtifactsTask::getSourcesArtifact));
            task.getAccessTransformer().set(atFile);
            task.getOutputJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/access-transformed-sources.jar")));
            task.getLibraries().from(neoFormDependencies);
            task.getLibrariesFile().set(neoDevBuildDir.map(dir -> dir.file("minecraft-libraries-for-jst.txt")));
        });

        var patchesFolder = project.getRootProject().file("patches");
        var applyPatches = tasks.register("applyPatches", ApplyPatches.class, task -> {
            task.getOriginalJar().set(applyAt.flatMap(ApplyAccessTransformer::getOutputJar));
            task.getPatchesFolder().set(patchesFolder);
            task.getPatchedJar().set(neoDevBuildDir.map(dir -> dir.file("artifacts/patched-sources.jar")));
            task.getRejectsFolder().set(project.getRootProject().file("rejects"));
        });

        var mcSourcesPath = project.file("src/main/java");
        tasks.register("setup", Sync.class, task -> {
            task.from(project.zipTree(applyPatches.flatMap(ApplyPatches::getPatchedJar)));
            task.into(mcSourcesPath);
        });

        var downloadAssets = tasks.register("downloadAssets", DownloadAssetsTask.class, task -> {
            task.getNeoFormArtifact().set(mcAndNeoFormVersion.map(v -> "net.neoforged:neoform:" + v + "@zip"));
            task.getNeoFormRuntime().from(neoFormRuntimeConfig);
            task.getArtifactManifestFile().set(createManifest.get().getManifestFile());
            task.getAssetPropertiesFile().set(neoDevBuildDir.map(dir -> dir.file("minecraft_assets.properties")));
        });

        var localRuntime = configurations.create("localRuntime", config -> {
            config.withDependencies(dependencies -> {
                dependencies.add(dependencyFactory.create(RunUtils.DEV_LAUNCH_GAV));
            });
        });

        var installerConfiguration = project.getConfigurations().create("installer");
        var installerLibrariesConfiguration = configurations.create("installerLibraries");
        var modulesConfiguration = configurations.create("moduleOnly");
        var userDevCompileOnlyConfiguration = configurations.create("userdevCompileOnly");
        var userDevTestImplementationConfiguration = configurations.create("userdevTestImplementation");
        userDevTestImplementationConfiguration.shouldResolveConsistentlyWith(runtimeClasspath);

        var devLibrariesConfiguration = configurations.create("devLibraries", files -> {
            files.setCanBeConsumed(false);
            files.setCanBeResolved(true);
        });
        devLibrariesConfiguration.shouldResolveConsistentlyWith(runtimeClasspath);
        devLibrariesConfiguration.extendsFrom(installerLibrariesConfiguration, modulesConfiguration, userDevCompileOnlyConfiguration);

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
                task.getFmlVersion().set(fmlVersion);
                task.getMinecraftVersion().set(minecraftVersion);
                task.getNeoForgeVersion().set(neoForgeVersion);
                task.getRawNeoFormVersion().set(rawNeoFormVersion);
                task.getLibraries().addAll(configurationToGavList(devLibrariesConfiguration));
                task.getModules().addAll(configurationToGavList(modulesConfiguration));
                task.getTestLibraries().addAll(configurationToGavList(userDevTestImplementationConfiguration));
                task.getTestLibraries().add(neoForgeVersion.map(v -> "net.neoforged:testframework:" + v));
                task.getIgnoreList().addAll(modulesConfiguration.getIncoming().getArtifacts().getResolvedArtifacts().map(results -> {
                    return results.stream().map(r -> r.getFile().getName()).toList();
                }));
                task.getIgnoreList().addAll(userDevCompileOnlyConfiguration.getIncoming().getArtifacts().getResolvedArtifacts().map(results -> {
                    return results.stream().map(r -> r.getFile().getName()).toList();
                }));
                task.getIgnoreList().addAll("client-extra", "neoforge-");
            });
        }

        var ideSyncTask = tasks.register("neoForgeIdeSync");

        // TODO: Do we even want that?
        var additionalClasspath = configurations.create("additionalRuntimeClasspath", spec -> {
            spec.setDescription("Contains dependencies of every run, that should not be considered boot classpath modules.");
            spec.setCanBeResolved(true);
            spec.setCanBeConsumed(false);
        });

        Map<RunModel, TaskProvider<PrepareRun>> prepareRunTasks = new IdentityHashMap<>();
        extension.getRuns().all(run -> {
            var prepareRunTask = ModDevPlugin.setupRunInGradle(
                    project,
                    neoDevBuildDir,
                    ideSyncTask,
                    run,
                    modulesConfiguration,
                    writeNeoDevConfig,
                    spec -> {
                        spec.withDependencies(set -> {
                            set.addLater(mcAndNeoFormVersion.map(v -> dependencyFactory.create("net.neoforged:neoform:" + v).capabilities(caps -> {
                                caps.requireCapability("net.neoforged:neoform-dependencies");
                            })));
                        });
                        spec.extendsFrom(installerLibrariesConfiguration, modulesConfiguration, userDevCompileOnlyConfiguration);
                    },
                    additionalClasspath,
                    createArtifacts.get().getResourcesArtifact(),
                    downloadAssets.flatMap(DownloadAssetsTask::getAssetPropertiesFile));
            prepareRunTasks.put(run, prepareRunTask);
            ideSyncTask.configure(task -> task.dependsOn(prepareRunTask));
        });

        ModDevPlugin.configureIntelliJModel(project, ideSyncTask, extension, prepareRunTasks);

        // TODO: configure eclipse

        var genSourcePatches = tasks.register("generateSourcePatches", GenerateSourcePatches.class, task -> {
            task.getOriginalJar().set(applyAt.flatMap(ApplyAccessTransformer::getOutputJar));
            task.getModifiedSources().set(project.file("src/main/java"));
            task.getPatchesJar().set(neoDevBuildDir.map(dir -> dir.file("source-patches.zip")));
        });

        var genPatches = tasks.register("genPatches", Sync.class, task -> {
            task.from(project.zipTree(genSourcePatches.flatMap(GenerateSourcePatches::getPatchesJar)));
            task.into(project.getRootProject().file("patches"));
        });

        // TODO: signing?
        var universalJar = tasks.register("universalJar", Jar.class, task -> {
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
        ModDevPlugin.setupJarJar(project, ExtensionUtils.getSourceSets(project).getByName("main"), "universalJar");

        var createCleanArtifacts = tasks.register("createCleanArtifacts", CreateCleanArtifacts.class, task -> {
            task.getNeoFormRuntime().from(neoFormRuntimeConfig);
            task.getNeoFormArtifact().set(mcAndNeoFormVersion);

            var cleanArtifactsDir = neoDevBuildDir.map(dir -> dir.dir("artifacts/clean"));
            task.getCleanClientJar().set(cleanArtifactsDir.map(dir -> dir.file("client.jar")));
            task.getRawServerJar().set(cleanArtifactsDir.map(dir -> dir.file("raw-server.jar")));
            task.getCleanServerJar().set(cleanArtifactsDir.map(dir -> dir.file("server.jar")));
            task.getCleanJoinedJar().set(cleanArtifactsDir.map(dir -> dir.file("joined.jar")));
            task.getMergedMappings().set(cleanArtifactsDir.map(dir -> dir.file("merged-mappings.txt")));
        });

        var fartConfig = configurations.create("fart", files -> {
            files.setCanBeConsumed(false);
            files.setCanBeResolved(true);
            files.getDependencies().add(dependencyFactory.create(InstallerProcessor.FART.gav));
        });
        var remapClientJar = tasks.register("remapClientJar", RemapJar.class, task -> {
            task.classpath(fartConfig);
            task.getMainClass().set("net.neoforged.art.Main");
            task.getObfSlimJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanClientJar));
            task.getMergedMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            task.getMojmapJar().set(neoDevBuildDir.map(dir -> dir.file("remapped-client.jar")));
        });
        var remapServerJar = tasks.register("remapServerJar", RemapJar.class, task -> {
            task.classpath(fartConfig);
            task.getMainClass().set("net.neoforged.art.Main");
            task.getObfSlimJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanServerJar));
            task.getMergedMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            task.getMojmapJar().set(neoDevBuildDir.map(dir -> dir.file("remapped-server.jar")));
        });

        var binpatcherConfig = configurations.create("binpatcher", files -> {
            files.setCanBeConsumed(false);
            files.setCanBeResolved(true);
            files.setTransitive(false);
            // TODO: don't hardcode version like that
            files.getDependencies().add(dependencyFactory.create("net.minecraftforge:binarypatcher:%s:fatjar".formatted("1.1.1")));
        });
        var generateMergedBinPatches = tasks.register("generateMergedBinPatches", GenerateBinaryPatches.class, task -> {
            task.classpath(binpatcherConfig);
            task.getCleanJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getCleanJoinedJar));
            task.getPatchedJar().set(tasks.named("jar", Jar.class).flatMap(Jar::getArchiveFile));
            task.getPatches().set(patchesFolder);
            task.getMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            task.getOutputFile().set(neoDevBuildDir.map(dir -> dir.file("merged-binpatches.lzma")));
        });
        var generateClientBinPatches = tasks.register("generateClientBinPatches", GenerateBinaryPatches.class, task -> {
            task.classpath(binpatcherConfig);
            task.getCleanJar().set(remapClientJar.flatMap(RemapJar::getMojmapJar));
            task.getPatchedJar().set(tasks.named("jar", Jar.class).flatMap(Jar::getArchiveFile));
            task.getPatches().set(patchesFolder);
            task.getMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            task.getOutputFile().set(neoDevBuildDir.map(dir -> dir.file("client-binpatches.lzma")));
        });
        var generateServerBinPatches = tasks.register("generateServerBinPatches", GenerateBinaryPatches.class, task -> {
            task.classpath(binpatcherConfig);
            task.getCleanJar().set(remapServerJar.flatMap(RemapJar::getMojmapJar));
            task.getPatchedJar().set(tasks.named("jar", Jar.class).flatMap(Jar::getArchiveFile));
            task.getPatches().set(patchesFolder);
            task.getMappings().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getMergedMappings));
            task.getOutputFile().set(neoDevBuildDir.map(dir -> dir.file("server-binpatches.lzma")));
        });

        var createLauncherProfile = tasks.register("createLauncherProfile", CreateLauncherProfile.class, task -> {
            task.getFmlVersion().set(fmlVersion);
            task.getMinecraftVersion().set(minecraftVersion);
            task.getNeoForgeVersion().set(neoForgeVersion);
            task.getRawNeoFormVersion().set(rawNeoFormVersion);
            task.getLibraries().from(installerConfiguration, modulesConfiguration);
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
            task.getIgnoreList().addAll(modulesConfiguration.getIncoming().getArtifacts().getResolvedArtifacts().map(results -> {
                return results.stream().map(r -> r.getFile().getName()).toList();
            }));
            task.getIgnoreList().addAll("client-extra", "neoforge-");
            task.getModulePath().from(modulesConfiguration);
            task.getLauncherProfile().set(neoDevBuildDir.map(dir -> dir.file("launcher-profile.json")));
        });

        var installerProfileLibraries = configurations.create("installerProfileLibraries", spec -> {
            spec.setCanBeResolved(true);
            spec.setCanBeConsumed(false);
        });
        installerProfileLibraries.extendsFrom(installerLibrariesConfiguration);
        installerProfileLibraries.shouldResolveConsistentlyWith(runtimeClasspath);
        var createInstallerProfile = tasks.register("createInstallerProfile", CreateInstallerProfile.class, task -> {
            task.getMinecraftVersion().set(minecraftVersion);
            task.getNeoForgeVersion().set(neoForgeVersion);
            task.getMcAndNeoFormVersion().set(mcAndNeoFormVersion);
            task.getIcon().set(project.getRootProject().file("docs/assets/neoforged.ico"));
            task.getLibraries().from(installerProfileLibraries);
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
            var configuration = configurations.create("installerProcessor" + installerProcessor.toString(), files -> {
                files.setCanBeConsumed(false);
                files.setCanBeResolved(true);
                files.getDependencies().add(dependencyFactory.create(installerProcessor.gav));
            });
            installerProfileLibraries.extendsFrom(configuration);
            // Each tool should resolve consistently with the full set of installed libraries.
            configuration.shouldResolveConsistentlyWith(installerProfileLibraries);
            createInstallerProfile.configure(task -> {
                task.getProcessorClasspaths().put(installerProcessor, configuration.getIncoming().getArtifacts().getResolvedArtifacts().map(results -> {
                    // Using .toList() fails with the configuration cache - looks like Gradle can't deserialize the resulting list?
                    return results.stream().map(DependencyUtils::guessMavenGav).collect(Collectors.toCollection(ArrayList::new));
                }));
            });
        }

        var createWindowsServerArgsFile = tasks.register("createWindowsServerArgsFile", CreateArgsFile.class, task -> {
            task.getPathSeparator().set(";");
            task.getArgsFile().set(neoDevBuildDir.map(dir -> dir.file("windows-server-args.txt")));
        });
        var createUnixServerArgsFile = tasks.register("createUnixServerArgsFile", CreateArgsFile.class, task -> {
            task.getPathSeparator().set(":");
            task.getArgsFile().set(neoDevBuildDir.map(dir -> dir.file("unix-server-args.txt")));
        });

        for (var taskProvider : List.of(createWindowsServerArgsFile, createUnixServerArgsFile)) {
            taskProvider.configure(task -> {
                task.getTemplate().set(project.getRootProject().file("server_files/args.txt"));
                task.getFmlVersion().set(fmlVersion);
                task.getMinecraftVersion().set(minecraftVersion);
                task.getNeoForgeVersion().set(neoForgeVersion);
                task.getRawNeoFormVersion().set(rawNeoFormVersion);
                task.getModules().from(modulesConfiguration);
                task.getIgnoreList().addAll(modulesConfiguration.getIncoming().getArtifacts().getResolvedArtifacts().map(results -> {
                    return results.stream().map(r -> r.getFile().getName()).toList();
                }));
                task.getClasspath().from(installerConfiguration);
                task.getRawServerJar().set(createCleanArtifacts.flatMap(CreateCleanArtifacts::getRawServerJar));
            });
        }

        var installerConfig = configurations.create("legacyInstaller", files -> {
            files.setCanBeConsumed(false);
            files.setCanBeResolved(true);
            files.setTransitive(false);
            // TODO: don't hardcode version like that
            files.getDependencies().add(dependencyFactory.create("net.neoforged:legacyinstaller:%s:shrunk".formatted("3.0.27")));
        });
        // TODO: signing?
        // We want to use the manifest from LegacyInstaller.
        // - Jar tasks have special manifest handling, so use Zip.
        // - The manifest must be the first entry in the jar so LegacyInstaller has to be the first input.
        var installerJar = tasks.register("installerJar", Zip.class, task -> {
            task.getArchiveClassifier().set("installer");
            task.getArchiveExtension().set("jar");
            task.setMetadataCharset("UTF-8");
            task.getDestinationDirectory().convention(project.getExtensions().getByType(BasePluginExtension.class).getLibsDirectory());

            // TODO: is this correct?
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
            task.from(generateClientBinPatches.flatMap(GenerateBinaryPatches::getOutputFile), spec -> {
                spec.into("data");
                spec.rename(s -> "client.lzma");
            });
            task.from(generateServerBinPatches.flatMap(GenerateBinaryPatches::getOutputFile), spec -> {
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
            task.getArchiveClassifier().set("userdev");

            task.from(writeUserDevConfig.flatMap(WriteUserDevConfig::getUserDevConfig), spec -> {
                spec.rename(s -> "config.json");
            });
            task.from(atFile, spec -> {
                spec.into("ats/");
            });
            task.from(generateMergedBinPatches.flatMap(GenerateBinaryPatches::getOutputFile), spec -> {
                spec.rename(s -> "joined.lzma");
            });
            task.from(project.zipTree(genSourcePatches.flatMap(GenerateSourcePatches::getPatchesJar)), spec -> {
                spec.into("patches/");
            });
        });

        project.getExtensions().getByType(JavaPluginExtension.class).withSourcesJar();
        final TaskProvider<? extends Jar> sourcesJarProvider = project.getTasks().named("sourcesJar", Jar.class);
        sourcesJarProvider.configure(task ->  {
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

    private Provider<List<String>> configurationToGavList(Configuration configuration) {
        return configuration.getIncoming().getArtifacts().getResolvedArtifacts().map(results -> {
            return results.stream().map(DependencyUtils::guessMavenGav).toList();
        });
    }

    // TODO: the only point of this is to configure runs that depend on neoforge. Maybe this could be done with less code duplication...
    // TODO: Gradle says "thou shalt not referenceth otherth projects" yet here we are
    // TODO: depend on neoforge configurations that the moddev plugin also uses
    public void configureExtra(Project project, boolean junit) {
        var neoForgeProject = project.getRootProject().getChildProjects().get("neoforge");

        var dependencyFactory = project.getDependencyFactory();
        var configurations = project.getConfigurations();
        var tasks = project.getTasks();
        var neoDevBuildDir = project.getLayout().getBuildDirectory().dir("neodev");

        var extension = project.getExtensions().create(NeoForgeExtension.NAME, NeoForgeExtension.class);

        var rawNeoFormVersion = project.getProviders().gradleProperty("neoform_version");
        var minecraftVersion = project.getProviders().gradleProperty("minecraft_version");
        var mcAndNeoFormVersion = minecraftVersion.zip(rawNeoFormVersion, (mc, nf) -> mc + "-" + nf);

        // TODO: this is temporary
        var modulesConfiguration = project.getConfigurations().create("moduleOnly", spec -> {
            spec.withDependencies(set -> {
                set.add(projectDep(dependencyFactory, neoForgeProject, "moduleOnly"));
            });
        });

        var downloadAssets = neoForgeProject.getTasks().named("downloadAssets", DownloadAssetsTask.class);
        var createArtifacts = neoForgeProject.getTasks().named("createMinecraftArtifacts", CreateMinecraftArtifactsTask.class);
        var writeNeoDevConfig = neoForgeProject.getTasks().named("writeNeoDevConfig", WriteUserDevConfig.class);

        var localRuntime = project.getConfigurations().create("localRuntime", config -> {
            config.withDependencies(dependencies -> {
                dependencies.add(dependencyFactory.create(RunUtils.DEV_LAUNCH_GAV));
            });
        });

        var ideSyncTask = tasks.register("neoForgeIdeSync");

        // TODO: Do we even want that?
        var additionalClasspath = configurations.create("additionalRuntimeClasspath", spec -> {
            spec.setDescription("Contains dependencies of every run, that should not be considered boot classpath modules.");
            spec.setCanBeResolved(true);
            spec.setCanBeConsumed(false);
        });

        Consumer<Configuration> configureLegacyClasspath = spec -> {
            spec.withDependencies(set -> {
                set.addLater(mcAndNeoFormVersion.map(v -> dependencyFactory.create("net.neoforged:neoform:" + v).capabilities(caps -> {
                    caps.requireCapability("net.neoforged:neoform-dependencies");
                })));
            });
            spec.withDependencies(set -> {
                set.add(projectDep(dependencyFactory, neoForgeProject, "installer"));
                set.add(projectDep(dependencyFactory, neoForgeProject, "moduleOnly"));
                set.add(projectDep(dependencyFactory, neoForgeProject, "userdevCompileOnly"));
            });
        };

        Map<RunModel, TaskProvider<PrepareRun>> prepareRunTasks = new IdentityHashMap<>();
        extension.getRuns().all(run -> {
            var prepareRunTask = ModDevPlugin.setupRunInGradle(
                    project,
                    neoDevBuildDir,
                    ideSyncTask,
                    run,
                    modulesConfiguration,
                    writeNeoDevConfig,
                    configureLegacyClasspath,
                    additionalClasspath,
                    createArtifacts.get().getResourcesArtifact(),
                    downloadAssets.flatMap(DownloadAssetsTask::getAssetPropertiesFile));
            prepareRunTasks.put(run, prepareRunTask);
            ideSyncTask.configure(task -> task.dependsOn(prepareRunTask));
        });

        ModDevPlugin.configureIntelliJModel(project, ideSyncTask, extension, prepareRunTasks);

        // TODO: configure eclipse

        if (junit) {
            var testTask = tasks.register("junitTest", Test.class);

            ModDevPlugin.setupTesting(
                    project,
                    neoDevBuildDir,
                    ideSyncTask,
                    modulesConfiguration,
                    writeNeoDevConfig,
                    configureLegacyClasspath,
                    createArtifacts.get().getResourcesArtifact(),
                    downloadAssets.flatMap(DownloadAssetsTask::getAssetPropertiesFile),
                    testTask
            );
        }
    }

    private static ProjectDependency projectDep(DependencyFactory dependencyFactory, Project project, String configurationName) {
        var dep = dependencyFactory.create(project);
        dep.setTargetConfiguration(configurationName);
        return dep;
    }
}
