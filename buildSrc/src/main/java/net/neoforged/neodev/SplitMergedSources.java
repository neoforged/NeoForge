package net.neoforged.neodev;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Splits a merged Minecraft source jar into client and common jars based on source-file content analysis.
 * Files containing "@OnlyIn(Dist.CLIENT)" annotations are placed in the client jar,
 * while all other files go into the common jar.
 */
abstract class SplitMergedSources extends DefaultTask {
    @Inject
    public SplitMergedSources() {}

    @InputFile
    abstract RegularFileProperty getMergedJar();

    @OutputFile
    abstract RegularFileProperty getCommonJar();

    @OutputFile
    abstract RegularFileProperty getClientJar();

    @TaskAction
    public void splitMergedJar() throws IOException {
        try (
                var merged = new ZipInputStream(new BufferedInputStream(Files.newInputStream(getMergedJar().get().getAsFile().toPath())));
                var common = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(getCommonJar().get().getAsFile().toPath())));
                var client = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(getClientJar().get().getAsFile().toPath())))
        ) {
            for (var entry = merged.getNextEntry(); entry != null; entry = merged.getNextEntry()) {
                if (entry.isDirectory()) {
                    continue;
                }
                var bytes = merged.readAllBytes();
                if (new String(bytes).contains("\n@OnlyIn(Dist.CLIENT)")) {
                    client.putNextEntry(entry);
                    client.write(bytes);
                    client.closeEntry();
                } else {
                    common.putNextEntry(entry);
                    common.write(bytes);
                    common.closeEntry();
                }
            }
        }
    }
}
