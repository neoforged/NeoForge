package net.neoforged.neodev;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarInputStream;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

/**
 * Extracts a Jar manifest to a file.
 */
public abstract class ExtractManifest extends DefaultTask {
    @InputFile
    public abstract RegularFileProperty getJarFile();

    @OutputFile
    public abstract RegularFileProperty getManifest();

    @TaskAction
    public void extract() throws IOException {
        try (var jin = new JarInputStream(new FileInputStream(getJarFile().getAsFile().get()))) {
            if (jin.getManifest() != null) {
                try (var out = new BufferedOutputStream(new FileOutputStream(getManifest().getAsFile().get()))) {
                    jin.getManifest().write(out);
                }
            }
        }
    }
}
