package net.neoforged.neodev;

import javax.inject.Inject;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

/**
 * Create the base jar file that will be diffed against the modified jar to create binary patch files.
 */
abstract class GenerateBaseJar extends JavaExec {
    @Inject
    public GenerateBaseJar() {}

    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    abstract ConfigurableFileCollection getMinecraft();

    /**
     * The official Mojang mappings file.
     */
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    @Optional
    abstract RegularFileProperty getMappings();

    /**
     * The NeoForm mappings (either LZMA or ZIP data file), this can be empty to not apply NeoForm mappings.
     */
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    abstract ConfigurableFileCollection getNeoFormMappings();

    @OutputFile
    abstract RegularFileProperty getOutput();

    @Override
    public void exec() {
        args("--task", "PROCESS_MINECRAFT_JAR");
        for (var file : getMinecraft().getFiles()) {
            args("--input", file.getAbsolutePath());
        }
        if (getMappings().isPresent()) {
            args("--input-mappings", getMappings().get().getAsFile().getAbsolutePath());
        }
        args("--output", getOutput().get().getAsFile().getAbsolutePath());
        if (!getNeoFormMappings().isEmpty()) {
            args("--neoform-data", getNeoFormMappings().getSingleFile().getAbsolutePath());
        }
        super.exec();
    }
}
