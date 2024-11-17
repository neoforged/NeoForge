package net.neoforged.neodev;

import net.neoforged.minecraftdependencies.MinecraftDependenciesPlugin;
import net.neoforged.nfrtgradle.CreateMinecraftArtifacts;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Sync;

public class NeoDevBasePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        // These plugins allow us to declare dependencies on Minecraft libraries needed to compile the official sources
        project.getPlugins().apply(MinecraftDependenciesPlugin.class);

        var createSources = NeoDevPlugin.configureMinecraftDecompilation(project);

        project.getTasks().register("setup", Sync.class, task -> {
            task.setGroup(NeoDevPlugin.GROUP);
            task.setDescription("Replaces the contents of the base project sources with the unpatched, decompiled Minecraft source code.");
            task.from(project.zipTree(createSources.flatMap(CreateMinecraftArtifacts::getSourcesArtifact)));
            task.into(project.file("src/main/java/"));
        });
    }
}
