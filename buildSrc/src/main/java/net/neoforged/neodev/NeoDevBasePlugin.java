package net.neoforged.neodev;

import net.neoforged.minecraftdependencies.MinecraftDependenciesPlugin;
import net.neoforged.moddevgradle.internal.NeoDevFacade;
import net.neoforged.nfrtgradle.CreateMinecraftArtifacts;
import net.neoforged.nfrtgradle.DownloadAssets;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.Sync;

public class NeoDevBasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        // These plugins allow us to declare dependencies on Minecraft libraries needed to compile the official sources
        project.getPlugins().apply(MinecraftDependenciesPlugin.class);

        var dependencyFactory = project.getDependencyFactory();
        var tasks = project.getTasks();
        var neoDevBuildDir = project.getLayout().getBuildDirectory().dir("neodev");

        var rawNeoFormVersion = project.getProviders().gradleProperty("neoform_version");
        var minecraftVersion = project.getProviders().gradleProperty("minecraft_version");
        var mcAndNeoFormVersion = minecraftVersion.zip(rawNeoFormVersion, (mc, nf) -> mc + "-" + nf);

        var extension = project.getExtensions().create(NeoDevExtension.NAME, NeoDevExtension.class);

        var createSources = NeoDevPlugin.configureMinecraftDecompilation(project);
        // Task must run on sync to have MC resources available for IDEA nondelegated builds.
        NeoDevFacade.runTaskOnProjectSync(project, createSources);

        tasks.register("setup", Sync.class, task -> {
            task.setGroup(NeoDevPlugin.GROUP);
            task.setDescription("Replaces the contents of the base project sources with the unpatched, decompiled Minecraft source code.");
            task.from(project.zipTree(createSources.flatMap(CreateMinecraftArtifacts::getSourcesArtifact)));
            task.into(project.file("src/main/java/"));
        });

        var downloadAssets = tasks.register("downloadAssets", DownloadAssets.class, task -> {
            task.setGroup(NeoDevPlugin.INTERNAL_GROUP);
            task.getNeoFormArtifact().set(createSources.flatMap(CreateMinecraftArtifacts::getNeoFormArtifact));
            task.getAssetPropertiesFile().set(neoDevBuildDir.map(dir -> dir.file("minecraft_assets.properties")));
        });

        // MC looks for its resources on the classpath.
        var runtimeClasspath = project.getConfigurations().getByName(JavaPlugin.RUNTIME_ONLY_CONFIGURATION_NAME);
        runtimeClasspath.getDependencies().add(
                dependencyFactory.create(
                        project.files(createSources.flatMap(CreateMinecraftArtifacts::getResourcesArtifact))
                )
        );
        NeoDevFacade.setupRuns(
                project,
                neoDevBuildDir,
                extension.getRuns(),
                // Pass an empty file collection for the userdev config.
                // This will cause MDG to generate a dummy config suitable for vanilla.
                project.files(),
                modulePath -> {},
                legacyClasspath -> {},
                downloadAssets.flatMap(DownloadAssets::getAssetPropertiesFile),
                mcAndNeoFormVersion
        );
    }
}
