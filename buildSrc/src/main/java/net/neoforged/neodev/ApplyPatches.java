package net.neoforged.neodev;

import codechicken.diffpatch.cli.DiffOperation;
import codechicken.diffpatch.cli.PatchOperation;
import codechicken.diffpatch.util.LoggingOutputStream;
import codechicken.diffpatch.util.PatchMode;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.io.IOException;

@DisableCachingByDefault(because = "Not worth caching")
abstract class ApplyPatches extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getOriginalJar();

    @InputDirectory
    @PathSensitive(PathSensitivity.NONE)
    public abstract DirectoryProperty getPatchesFolder();

    @OutputFile
    public abstract RegularFileProperty getPatchedJar();

    @OutputDirectory
    public abstract DirectoryProperty getRejectsFolder();

    @Input
    protected abstract Property<Boolean> getIsUpdating();

    @Inject
    public ApplyPatches(Project project) {
        getIsUpdating().set(project.getProviders().gradleProperty("updating").map(Boolean::parseBoolean).orElse(false));
    }

    @TaskAction
    public void applyPatches() throws IOException {
        var isUpdating = getIsUpdating().get();

        var builder = PatchOperation.builder()
                .logTo(new LoggingOutputStream(getLogger(), LogLevel.LIFECYCLE))
                .basePath(getOriginalJar().get().getAsFile().toPath())
                .patchesPath(getPatchesFolder().get().getAsFile().toPath())
                .outputPath(getPatchedJar().get().getAsFile().toPath())
                .rejectsPath(getRejectsFolder().get().getAsFile().toPath())
                .mode(isUpdating ? PatchMode.FUZZY : PatchMode.ACCESS)
                .aPrefix("a/")
                .bPrefix("b/")
                .level(isUpdating ? codechicken.diffpatch.util.LogLevel.ALL : codechicken.diffpatch.util.LogLevel.WARN)
                .minFuzz(0.9f); // The 0.5 default in DiffPatch is too low.

        var result = builder.build().operate();

        int exit = result.exit;
        if (exit != 0 && exit != 1) {
            throw new RuntimeException("DiffPatch failed with exit code: " + exit);
        }
        if (exit != 0 && !isUpdating) {
            throw new RuntimeException("Patches failed to apply.");
        }
    }
}
