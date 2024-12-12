package net.neoforged.neodev;

import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Runs <a href="https://github.com/neoforged/JavaSourceTransformer">JavaSourceTransformer</a> to apply
 * access transformers to the Minecraft source code for extending the access level of existing classes/methods/etc.
 * <p>
 * Note that at runtime, FML also applies access transformers.
 */
abstract class ApplyAccessTransformer extends ApplyJSTTransformer {
    @InputFiles
    public abstract ConfigurableFileCollection getAccessTransformers();

    @Input
    public abstract Property<Boolean> getValidate();

    @Inject
    public ApplyAccessTransformer() {}

    @Override
    void addArgs(List<String> arguments) {
        arguments.add("--enable-accesstransformers");
        arguments.addAll(Arrays.asList(
                "--access-transformer-validation", getValidate().get() ? "error" : "log"
        ));
        for (var file : getAccessTransformers().getFiles()) {
            arguments.addAll(Arrays.asList(
                    "--access-transformer", file.getAbsolutePath()
            ));
        }
    }
}
