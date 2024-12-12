package net.neoforged.neodev;

import net.neoforged.neodev.utils.FileUtils;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Runs <a href="https://github.com/neoforged/JavaSourceTransformer">JavaSourceTransformer</a> over the Minecraft source code.
 */
abstract class ApplyJSTTransformer extends JavaExec {
    @InputFile
    public abstract RegularFileProperty getInputJar();

    @OutputFile
    public abstract RegularFileProperty getOutputJar();

    // Used to give JST more information about the classes.
    @Classpath
    public abstract ConfigurableFileCollection getLibraries();

    @Internal
    public abstract RegularFileProperty getLibrariesFile();

    @Inject
    public ApplyJSTTransformer() {}

    abstract void addArgs(List<String> arguments);

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

        var args = new ArrayList<>(Arrays.asList(
                "--libraries-list", getLibrariesFile().getAsFile().get().getAbsolutePath()
        ));

        addArgs(args);

        args.addAll(Arrays.asList(
                getInputJar().getAsFile().get().getAbsolutePath(),
                getOutputJar().getAsFile().get().getAbsolutePath()));

        args(args);

        super.exec();
    }
}
