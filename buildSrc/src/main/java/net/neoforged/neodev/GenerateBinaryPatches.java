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
     * This directory of patch files for the Java sources is used as a hint to only diff class files that
     * supposedly have changed. If it is not set, the tool will diff every .class file instead.
     */
    @InputDirectory
    @Optional
    abstract DirectoryProperty getSourcePatchesFolder();

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
        if (getSourcePatchesFolder().isPresent()) {
            args("--patches", getSourcePatchesFolder().get().getAsFile().getAbsolutePath());
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
