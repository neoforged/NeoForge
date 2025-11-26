package net.neoforged.neodev;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.inject.Inject;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
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
    abstract RegularFileProperty getBaseClientJar();

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
     * Ant-Style path patterns for paths to include in diffing.
     */
    @Input
    abstract ListProperty<String> getInclude();

    /**
     * Ant-Style path patterns for paths to exclude from diffing.
     */
    @Input
    abstract ListProperty<String> getExclude();

    /**
     * Where the created patch bundle should be written to.
     */
    @OutputFile
    abstract RegularFileProperty getOutputFile();

    @Override
    public void exec() {
        args("--diff");
        args("--base-client", getBaseClientJar().get().getAsFile().getAbsolutePath());
        args("--base-server", getBaseServerJar().get().getAsFile().getAbsolutePath());
        args("--base-joined", getBaseJoinedJar().get().getAsFile().getAbsolutePath());
        args("--modified-client", getModifiedClientJar().get().getAsFile().getAbsolutePath());
        args("--modified-server", getModifiedServerJar().get().getAsFile().getAbsolutePath());
        args("--modified-joined", getModifiedJoinedJar().get().getAsFile().getAbsolutePath());
        for (String pattern : getInclude().get()) {
            args("--include", pattern);
        }
        for (String pattern : getExclude().get()) {
            args("--exclude", pattern);
        }
        args("--optimize-constantpool");
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
