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
import java.util.List;

abstract class DownloadAssetsTask extends NeoFormRuntimeTask {
    @Inject
    public DownloadAssetsTask() {
    }

    @Input
    abstract Property<String> getNeoFormArtifact();

    @OutputFile
    abstract RegularFileProperty getAssetPropertiesFile();

    @TaskAction
    public void createArtifacts() {
        var artifactId = getNeoFormArtifact().get();

        run(List.of(
                "download-assets",
                "--neoform", artifactId,
                "--write-properties", getAssetPropertiesFile().get().getAsFile().getAbsolutePath()
        ));
    }
}
