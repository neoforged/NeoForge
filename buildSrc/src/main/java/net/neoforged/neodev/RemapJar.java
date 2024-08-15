package net.neoforged.neodev;

import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;

import javax.inject.Inject;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

        var logFile = new File(getTemporaryDir(), "console.log");
        try (var out = new BufferedOutputStream(new FileOutputStream(logFile))) {
            getLogger().info("Logging ART console output to {}", logFile.getAbsolutePath());
            setStandardOutput(out);
            super.exec();
        } catch (IOException e) {
            throw new GradleException("Failed to create binary patches.", e);
        }
    }
}
