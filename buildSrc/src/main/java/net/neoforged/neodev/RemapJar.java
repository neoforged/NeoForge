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

/**
 * Produces a remapped jar-file that has almost no other changes applied with the intent of being
 * the base against which we {@link GenerateBinaryPatches generate binary patches}.
 * <p>
 * The installer produces the same Jar file as this task does and then applies the patches against that.
 * <p>
 * Any changes to the options used here have to be reflected in the {@link net.neoforged.neodev.installer.CreateInstallerProfile installer profile}
 * and vice versa, to ensure the patches are generated against the same binary files as they are applied to later.
 */
abstract class RemapJar extends JavaExec {
    @Inject
    public RemapJar() {}

    @InputFile
    abstract RegularFileProperty getInputJar();

    @InputFile
    abstract RegularFileProperty getMappings();

    @OutputFile
    abstract RegularFileProperty getOutputJar();

    @Override
    public void exec() {
        args("--input", getInputJar().get().getAsFile().getAbsolutePath());
        args("--output", getOutputJar().get().getAsFile().getAbsolutePath());
        args("--names", getMappings().get().getAsFile().getAbsolutePath());
        args("--ann-fix", "--ids-fix", "--src-fix", "--record-fix");

        var logFile = new File(getTemporaryDir(), "console.log");
        try (var out = new BufferedOutputStream(new FileOutputStream(logFile))) {
            getLogger().info("Logging ART console output to {}", logFile.getAbsolutePath());
            setStandardOutput(out);
            super.exec();
        } catch (IOException e) {
            throw new GradleException("Failed to remap jar.", e);
        }
    }
}
