package net.neoforged.neodev;

import javax.inject.Inject;
import net.neoforged.nfrtgradle.CreateMinecraftArtifacts;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.OutputFile;

abstract class CreateCleanArtifacts extends CreateMinecraftArtifacts {
    /**
     * The unmodified downloaded client jar.
     */
    @OutputFile
    abstract RegularFileProperty getRawClientJar();

    /**
     * The unmodified downloaded server jar.
     */
    @OutputFile
    abstract RegularFileProperty getRawServerJar();

    @OutputFile
    abstract RegularFileProperty getCleanJoinedJar();

    @Inject
    public CreateCleanArtifacts() {
        getAdditionalResults().put("node.downloadClient.output.output", getRawClientJar().getAsFile());
        getAdditionalResults().put("node.downloadServer.output.output", getRawServerJar().getAsFile());
        getAdditionalResults().put("vanillaDeobfuscated", getCleanJoinedJar().getAsFile());
    }
}
