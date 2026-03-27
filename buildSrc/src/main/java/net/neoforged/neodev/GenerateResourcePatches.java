package net.neoforged.neodev;

import io.codechicken.diffpatch.cli.CliOperation;
import io.codechicken.diffpatch.cli.DiffOperation;
import io.codechicken.diffpatch.util.Input.MultiInput;
import io.codechicken.diffpatch.util.Output.MultiOutput;
import java.io.File;
import java.io.IOException;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

/**
 * Generates patch files for all resources found in a given folder to facilitate injecting them into
 * the moddev environment.
 */
abstract class GenerateResourcePatches extends DefaultTask {
    @InputDirectory
    public abstract DirectoryProperty getAdditionalResourcesDir();

    @Optional
    @OutputFile
    public abstract RegularFileProperty getPatchesJar();

    @Optional
    @OutputDirectory
    public abstract DirectoryProperty getPatchesFolder();

    @Inject
    public GenerateResourcePatches() {}

    @TaskAction
    public void generateSourcePatches() throws IOException {
        var emptyFolder = new File(getTemporaryDir(), "empty");
        emptyFolder.mkdir();

        var builder = DiffOperation.builder()
                .logTo(getLogger()::lifecycle)
                .baseInput(MultiInput.folder(emptyFolder.toPath()))
                .changedInput(MultiInput.folder(getAdditionalResourcesDir().get().getAsFile().toPath()))
                .patchesOutput(getPatchesJar().isPresent() ? MultiOutput.detectedArchive(getPatchesJar().get().getAsFile().toPath()) : MultiOutput.folder(getPatchesFolder().getAsFile().get().toPath()))
                .autoHeader(true)
                .level(io.codechicken.diffpatch.util.LogLevel.WARN)
                .summary(false)
                .aPrefix("a/")
                .bPrefix("b/")
                .lineEnding("\n");

        CliOperation.Result<DiffOperation.DiffSummary> result = builder.build().operate();

        int exit = result.exit;
        if (exit != 0 && exit != 1) {
            throw new RuntimeException("DiffPatch failed with exit code: " + exit);
        }
    }
}
