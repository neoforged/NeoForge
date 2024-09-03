package net.neoforged.neodev;

import codechicken.diffpatch.cli.CliOperation;
import codechicken.diffpatch.cli.DiffOperation;
import codechicken.diffpatch.util.LoggingOutputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;

abstract class GenerateSourcePatches extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getOriginalJar();

    @InputDirectory
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract DirectoryProperty getModifiedSources();

    @OutputFile
    public abstract RegularFileProperty getPatchesJar();

    @Inject
    public GenerateSourcePatches() {}

    @TaskAction
    public void generateSourcePatches() throws IOException {
        var builder = DiffOperation.builder()
                .logTo(new LoggingOutputStream(getLogger(), LogLevel.LIFECYCLE))
                .aPath(getOriginalJar().get().getAsFile().toPath())
                .bPath(getModifiedSources().get().getAsFile().toPath())
                .outputPath(getPatchesJar().get().getAsFile().toPath()) // TODO: do we need ArchiveFormat?
                .autoHeader(true)
                .level(codechicken.diffpatch.util.LogLevel.WARN)
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
