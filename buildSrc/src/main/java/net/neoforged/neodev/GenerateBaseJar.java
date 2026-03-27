package net.neoforged.neodev;

import javax.inject.Inject;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.JavaExec;
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

    @OutputFile
    abstract RegularFileProperty getOutput();

    @Override
    public void exec() {
        args("--task", "PROCESS_MINECRAFT_JAR");
        for (var file : getMinecraft().getFiles()) {
            args("--input", file.getAbsolutePath());
        }
        args("--output", getOutput().get().getAsFile().getAbsolutePath());
        args("--no-dist-annotation");
        args("--no-mod-manifest");
        super.exec();
    }
}
