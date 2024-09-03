package net.neoforged.neodev;

import net.neoforged.moddevgradle.internal.utils.FileUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

abstract class ApplyAccessTransformer extends JavaExec {
    @InputFile
    public abstract RegularFileProperty getInputJar();

    @InputFile
    public abstract RegularFileProperty getAccessTransformer();

    @OutputFile
    public abstract RegularFileProperty getOutputJar();

    // Used to give JST more information about the classes.
    @Classpath
    public abstract ConfigurableFileCollection getLibraries();

    @Internal
    public abstract RegularFileProperty getLibrariesFile();

    @Inject
    public ApplyAccessTransformer() {}

    @Override
    @TaskAction
    public void exec() {
        try {
            FileUtils.writeLinesSafe(
                    getLibrariesFile().getAsFile().get().toPath(),
                    getLibraries().getFiles().stream().map(File::getAbsolutePath).toList(),
                    StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException("Failed to write libraries for JST.", exception);
        }

        args(
                "--enable-accesstransformers",
                "--access-transformer", getAccessTransformer().getAsFile().get().getAbsolutePath(),
                "--libraries-list", getLibrariesFile().getAsFile().get().getAbsolutePath(),
                getInputJar().getAsFile().get().getAbsolutePath(),
                getOutputJar().getAsFile().get().getAbsolutePath());

        super.exec();
    }
}
