package net.neoforged.neodev;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;

abstract class RemapJar extends JavaExec {
    @Inject
    public RemapJar() {}

    @InputFile
    abstract RegularFileProperty getObfSlimJar();

    @InputFile
    abstract RegularFileProperty getMergedMappings();

    @OutputFile
    abstract RegularFileProperty getMojmapJar();

    @Override
    public void exec() {
        args("--input", getObfSlimJar().get().getAsFile().getAbsolutePath());
        args("--output", getMojmapJar().get().getAsFile().getAbsolutePath());
        args("--names", getMergedMappings().get().getAsFile().getAbsolutePath());
        args("--ann-fix", "--ids-fix", "--src-fix", "--record-fix");

        super.exec();
    }
}
