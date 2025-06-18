package net.neoforged.neodev;

import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

abstract class GenerateBinaryPatches extends JavaExec {
    @Inject
    public GenerateBinaryPatches() {}

    /**
     * The jar file containing classes in the base state.
     */
    @InputFile
    abstract RegularFileProperty getCleanJar();

    /**
     * The jar file containing classes in the desired target state.
     */
    @InputFile
    abstract RegularFileProperty getPatchedJar();

    @InputFile
    abstract RegularFileProperty getMappings();

    /**
     * The files in this optional directory are used to filter which binary patches should be created.
     * <p>A binary patch is only created for a file from {@link #getPatchedJar()}, if a source patch (A corresponding file
     * with {@code .java.patch} extension) is present in this directory, or if a class with the same path is present in
     * {@link #getIncludeClassesJar()} (if set).
     * <p>For inner classes, only the outermost class is checked against the filters.
     * <p>If neither this nor {@link #getIncludeClassesJar()} are set, no filtering is applied.
     */
    @InputDirectory
    @Optional
    abstract DirectoryProperty getSourcePatchesFolder();

    /**
     * The list of files included in this optional Jar file is used to filter for which files binary patches should be created.
     * <p>A binary patch is only created for a file from {@link #getPatchedJar()}, if a file with the same path is
     * either present in this jar, or if a corresponding source patch is present in {@link #getSourcePatchesFolder()} (if set).
     * <p>For inner classes, only the outermost class is checked against the filters.
     * <p>If neither this nor {@link #getSourcePatchesFolder()} are set, no filtering is applied.
     */
    @InputFile
    @Optional
    abstract RegularFileProperty getIncludeClassesJar();

    /**
     * The location where the LZMA compressed binary patches are written to.
     */
    @OutputFile
    abstract RegularFileProperty getOutputFile();

    @Override
    public void exec() {
        args("--clean", getCleanJar().get().getAsFile().getAbsolutePath());
        args("--dirty", getPatchedJar().get().getAsFile().getAbsolutePath());
        args("--srg", getMappings().get().getAsFile().getAbsolutePath());
        args("--minimize");
        if (getSourcePatchesFolder().isPresent()) {
            args("--patches", getSourcePatchesFolder().get().getAsFile().getAbsolutePath());
        }
        if (getIncludeClassesJar().isPresent()) {
            args("--include-classes", getIncludeClassesJar().get().getAsFile().getAbsolutePath());
        }
        args("--output", getOutputFile().get().getAsFile().getAbsolutePath());

        var logFile = new File(getTemporaryDir(), "console.log");
        try (var out = new BufferedOutputStream(new FileOutputStream(logFile))) {
            getLogger().info("Logging binpatcher console output to {}", logFile.getAbsolutePath());
            setStandardOutput(out);
            super.exec();
        } catch (IOException e) {
            throw new GradleException("Failed to create binary patches.", e);
        }
    }
}
