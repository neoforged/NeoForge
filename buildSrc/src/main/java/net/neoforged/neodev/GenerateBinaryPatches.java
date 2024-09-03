package net.neoforged.neodev;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

abstract class GenerateBinaryPatches extends JavaExec {
    @Inject
    public GenerateBinaryPatches() {}

    @InputFile
    abstract RegularFileProperty getCleanJar();

    @InputFile
    abstract RegularFileProperty getPatchedJar();

    @InputFile
    abstract RegularFileProperty getMappings();

    @InputDirectory
    abstract DirectoryProperty getPatches();

    @OutputFile
    abstract RegularFileProperty getOutputFile();

    @Override
    public void exec() {
        args("--clean", getCleanJar().get().getAsFile().getAbsolutePath());
        args("--dirty", getPatchedJar().get().getAsFile().getAbsolutePath());
        args("--srg", getMappings().get().getAsFile().getAbsolutePath());
        args("--patches", getPatches().get().getAsFile().getAbsolutePath());
        args("--output", getOutputFile().get().getAsFile().getAbsolutePath());

        super.exec();
    }
}
