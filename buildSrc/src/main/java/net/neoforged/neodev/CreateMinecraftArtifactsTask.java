package net.neoforged.neodev;

import net.neoforged.moddevgradle.internal.NeoFormRuntimeTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;

abstract class CreateMinecraftArtifactsTask extends NeoFormRuntimeTask {
    @Inject
    public CreateMinecraftArtifactsTask() {}

    @Input
    abstract Property<String> getNeoFormArtifact();

    @OutputFile
    abstract RegularFileProperty getSourcesArtifact();

    /**
     * Also known as "client-extra". Contains the non-class files from the original Minecraft jar (excluding META-INF)
     */
    @OutputFile
    abstract RegularFileProperty getResourcesArtifact();

    @TaskAction
    public void setupBaseSources() {
        var artifactId = getNeoFormArtifact().get();

        var args = new ArrayList<String>();
        Collections.addAll(
                args,
                "run",
                "--neoform", "net.neoforged:neoform:%s@zip".formatted(artifactId),
                "--dist", "joined",
                "--write-result", "sources:" + getSourcesArtifact().get().getAsFile().getAbsolutePath(),
                "--write-result", "clientResources:" + getResourcesArtifact().get().getAsFile().getAbsolutePath()
        );

        run(args);
    }
}
