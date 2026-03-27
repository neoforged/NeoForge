package net.neoforged.neodev;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.inject.Inject;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

/**
 * Splits a merged Minecraft source jar into client and common jars based on source-file content analysis.
 * Files containing "@OnlyIn(Dist.CLIENT)" annotations are placed in the client jar,
 * while all other files go into the common jar.
 */
abstract class SplitMergedSources extends DefaultTask {
    @Inject
    public SplitMergedSources() {}

    @InputFile
    abstract RegularFileProperty getOriginalResourcesJar();

    @InputFile
    abstract RegularFileProperty getMergedJar();

    @OutputFile
    abstract RegularFileProperty getCommonJar();

    @OutputFile
    abstract RegularFileProperty getClientJar();

    @TaskAction
    public void splitMergedJar() throws IOException {
        try (
                var originalResources = new JarFile(getOriginalResourcesJar().get().getAsFile());
                var merged = new ZipInputStream(new BufferedInputStream(Files.newInputStream(getMergedJar().get().getAsFile().toPath())));
                var common = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(getCommonJar().get().getAsFile().toPath())));
                var client = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(getClientJar().get().getAsFile().toPath())))) {

            var manifest = originalResources.getManifest();
            var sourceDistName = new Attributes.Name("Minecraft-Dist");

            for (var entry = merged.getNextEntry(); entry != null; entry = merged.getNextEntry()) {
                if (entry.isDirectory()) {
                    continue;
                }

                var fileEntry = manifest.getEntries().get(entry.getName().replace(".java", ".class"));
                String sourceDist = null;
                if (fileEntry != null) {
                    sourceDist = fileEntry.getValue(sourceDistName);
                }

                if ("client".equals(sourceDist)) {
                    client.putNextEntry(entry);
                    merged.transferTo(client);
                    client.closeEntry();
                } else {
                    common.putNextEntry(entry);
                    merged.transferTo(common);
                    common.closeEntry();
                }
            }
        }
    }
}
