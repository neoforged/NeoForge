package net.neoforged.neodev;

import net.neoforged.nfrtgradle.CreateMinecraftArtifacts;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;

import javax.inject.Inject;

abstract class CreateCleanArtifacts extends CreateMinecraftArtifacts {
    /**
     * The unmodified downloaded client jar.
     */
    @OutputFile
    abstract RegularFileProperty getRawClientJar();

    @OutputFile
    abstract RegularFileProperty getCleanClientJar();

    /**
     * The unmodified downloaded server jar.
     */
    @OutputFile
    abstract RegularFileProperty getRawServerJar();

    @OutputFile
    abstract RegularFileProperty getCleanServerJar();

    @OutputFile
    abstract RegularFileProperty getCleanJoinedJar();

    @OutputFile
    abstract RegularFileProperty getMergedMappings();

    @Inject
    public CreateCleanArtifacts() {
        getAdditionalResults().put("node.downloadClient.output.output", getRawClientJar().getAsFile());
        getAdditionalResults().put("node.stripClient.output.output", getCleanClientJar().getAsFile());
        getAdditionalResults().put("node.downloadServer.output.output", getRawServerJar().getAsFile());
        getAdditionalResults().put("node.stripServer.output.output", getCleanServerJar().getAsFile());
        getAdditionalResults().put("node.rename.output.output", getCleanJoinedJar().getAsFile());
        getAdditionalResults().put("node.mergeMappings.output.output", getMergedMappings().getAsFile());

        // TODO: does anyone care about this? they should be contained in the client mappings
        //"--write-result", "node.downloadServerMappings.output.output:" + getServerMappings().get().getAsFile().getAbsolutePath()
    }
}
