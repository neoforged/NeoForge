package net.neoforged.neodev;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;

public abstract class GenerateBinaryPatches extends JavaExec {
    @InputFile
    public abstract RegularFileProperty getBaseClientJar();

    @InputFile
    public abstract RegularFileProperty getModifiedClientJar();

    @InputFile
    public abstract RegularFileProperty getBaseServerJar();

    @InputFile
    public abstract RegularFileProperty getModifiedServerJar();

    @InputFile
    public abstract RegularFileProperty getBaseJoinedJar();

    @InputFile
    public abstract RegularFileProperty getModifiedJoinedJar();

    @OutputFile
    public abstract RegularFileProperty getOutputJar();

    @Override
    public void exec() {
        args("--diff");
        args("--base-client", getBaseClientJar().get().getAsFile().getAbsolutePath());
        args("--base-server", getBaseServerJar().get().getAsFile().getAbsolutePath());
        args("--base-joined", getBaseJoinedJar().get().getAsFile().getAbsolutePath());
        args("--modified-client", getModifiedClientJar().get().getAsFile().getAbsolutePath());
        args("--modified-server", getModifiedServerJar().get().getAsFile().getAbsolutePath());
        args("--modified-joined", getModifiedJoinedJar().get().getAsFile().getAbsolutePath());
        args("--optimize-constantpool");
        args("--output", getOutputJar().get().getAsFile().getAbsolutePath());

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
