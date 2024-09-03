package net.neoforged.neodev;

import net.neoforged.moddevgradle.internal.NeoFormRuntimeTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;

abstract class CreateCleanArtifacts extends NeoFormRuntimeTask {
    @Inject
    public CreateCleanArtifacts() {}

    @Input
    abstract Property<String> getNeoFormArtifact();

    @OutputFile
    abstract RegularFileProperty getCleanClientJar();

    @OutputFile
    abstract RegularFileProperty getRawServerJar();

    @OutputFile
    abstract RegularFileProperty getCleanServerJar();

    @OutputFile
    abstract RegularFileProperty getCleanJoinedJar();

    @OutputFile
    abstract RegularFileProperty getMergedMappings();

    @TaskAction
    public void createCleanArtifacts() {
        var artifactId = getNeoFormArtifact().get();

        var args = new ArrayList<String>();
        Collections.addAll(
                args,
                "run",
                "--neoform", "net.neoforged:neoform:%s@zip".formatted(artifactId),
                "--dist", "joined",
                "--write-result", "node.stripClient.output.output:" + getCleanClientJar().get().getAsFile().getAbsolutePath(),
                "--write-result", "node.downloadServer.output.output:" + getRawServerJar().get().getAsFile().getAbsolutePath(),
                "--write-result", "node.stripServer.output.output:" + getCleanServerJar().get().getAsFile().getAbsolutePath(),
                "--write-result", "node.rename.output.output:" + getCleanJoinedJar().get().getAsFile().getAbsolutePath(),
                "--write-result", "node.mergeMappings.output.output:" + getMergedMappings().get().getAsFile().getAbsolutePath()
                // TODO: does anyone care about this? they should be contained in the client mappings
                //"--write-result", "node.downloadServerMappings.output.output:" + getServerMappings().get().getAsFile().getAbsolutePath()
        );

        run(args);
    }
}
