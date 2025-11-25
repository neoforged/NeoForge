package net.neoforged.neodev;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;

@CacheableTask
public abstract class GenerateBaseJar extends JavaExec {
    @InputFiles
    @PathSensitive(PathSensitivity.NONE)
    public abstract ConfigurableFileCollection getMinecraft();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getMappings();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getNeoFormMappings();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    @Override
    public void exec() {
        args("--task", "PROCESS_MINECRAFT_JAR");
        for (var file : getMinecraft().getFiles()) {
            args("--input", file.getAbsolutePath());
        }
        args("--input-mappings", getMappings().get().getAsFile().getAbsolutePath());
        args("--output", getOutput().get().getAsFile().getAbsolutePath());
        args("--neoform-data", getNeoFormMappings().get().getAsFile().getAbsolutePath());

        getLogger().error("Arguments: {}", String.join(" ", getArgs()));

        super.exec();
    }
}
