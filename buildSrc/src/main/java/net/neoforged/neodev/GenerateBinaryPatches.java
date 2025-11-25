package net.neoforged.neodev;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.inject.Inject;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;

abstract class GenerateBinaryPatches extends JavaExec {
    @Inject
    public GenerateBinaryPatches() {}

    /**
     * The base against which the patches should be created for the client distribution.
     */
    @InputFile
    public abstract RegularFileProperty getBaseClientJar();

    /**
     * The target jar that will be diffed against {@link #getBaseClientJar()} to create the patches for the
     * client distribution.
     */
    @InputFile
    abstract RegularFileProperty getModifiedClientJar();

    /**
     * The base against which the patches should be created for the server distribution.
     */
    @InputFile
    abstract RegularFileProperty getBaseServerJar();

    /**
     * The target jar that will be diffed against {@link #getBaseServerJar()} to create the patches for the
     * server distribution.
     */
    @InputFile
    abstract RegularFileProperty getModifiedServerJar();

    /**
     * The base against which the patches should be created for the combined client+server distribution.
     */
    @InputFile
    abstract RegularFileProperty getBaseJoinedJar();

    /**
     * The target jar that will be diffed against {@link #getBaseServerJar()} to create the patches for the
     * combined client+server distribution.
     */
    @InputFile
    abstract RegularFileProperty getModifiedJoinedJar();

    /**
     * Where the created patch bundle should be written to.
     */
    @OutputFile
    abstract RegularFileProperty getOutputJar();

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
