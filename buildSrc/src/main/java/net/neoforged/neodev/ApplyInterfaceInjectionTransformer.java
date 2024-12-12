package net.neoforged.neodev;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Runs <a href="https://github.com/neoforged/JavaSourceTransformer">JavaSourceTransformer</a> to apply
 * interface injections to the Minecraft source code.
 */
abstract class ApplyInterfaceInjectionTransformer extends ApplyJSTTransformer {
    @InputFiles
    public abstract ConfigurableFileCollection getInterfaceInjectionData();

    @OutputFile
    public abstract RegularFileProperty getStubsJar();

    @Inject
    public ApplyInterfaceInjectionTransformer() {}

    @Override
    void addArgs(List<String> arguments) {
        arguments.add("--enable-interface-injection");

        for (var file : getInterfaceInjectionData().getFiles()) {
            arguments.addAll(Arrays.asList(
                    "--interface-injection-data", file.getAbsolutePath()
            ));
        }

        arguments.addAll(Arrays.asList(
                "--interface-injection-stubs", getStubsJar().get().getAsFile().getAbsolutePath()
        ));
    }
}
