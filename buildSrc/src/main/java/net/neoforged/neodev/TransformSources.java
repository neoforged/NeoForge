package net.neoforged.neodev;

import net.neoforged.neodev.utils.FileUtils;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Runs <a href="https://github.com/neoforged/JavaSourceTransformer">JavaSourceTransformer</a> over the Minecraft source code.
 */
abstract class TransformSources extends JavaExec {
    @Optional
    @InputFiles
    public abstract ConfigurableFileCollection getAccessTransformers();

    @Input
    @Optional
    public abstract Property<Boolean> getValidateAccessTransformers();

    @Optional
    @InputFiles
    public abstract ConfigurableFileCollection getInterfaceInjectionData();

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
    public TransformSources() {}

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

        if (!getAccessTransformers().isEmpty()) {
            args.addAll(Arrays.asList(
                    "--enable-accesstransformers",
                    "--access-transformer-validation", getValidateAccessTransformers().get() ? "error" : "log"
            ));
            for (var file : getAccessTransformers().getFiles()) {
                args.addAll(Arrays.asList(
                        "--access-transformer", file.getAbsolutePath()
                ));
            }
        }

        if (!getInterfaceInjectionData().isEmpty()) {
            args.add("--enable-interface-injection");

            for (var file : getInterfaceInjectionData().getFiles()) {
                args.addAll(Arrays.asList(
                        "--interface-injection-data", file.getAbsolutePath()
                ));
            }
        }

        args.addAll(Arrays.asList(
                getInputJar().getAsFile().get().getAbsolutePath(),
                getOutputJar().getAsFile().get().getAbsolutePath()));

        args(args);

        super.exec();
    }
}
