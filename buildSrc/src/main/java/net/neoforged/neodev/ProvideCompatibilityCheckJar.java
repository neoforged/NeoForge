package net.neoforged.neodev;

import net.neoforged.neodev.e2e.InstallProductionClient;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Produces a merged production Jar file with Minecraft and NeoForge classes for any given NeoForge version.
 * <p>This jar is used as the baseline for finding breaking changes by JCC.
 */
public abstract class ProvideCompatibilityCheckJar extends InstallProductionClient {
    @Input
    public abstract Property<String> getVersion();

    @OutputFile
    public abstract RegularFileProperty getOutput();

    @Override
    public void exec() {
        var nfVersion = getVersion().get();

        getLogger().lifecycle("Installing previous NeoForge version " + nfVersion);

        super.exec();

        getLogger().lifecycle("Merging Minecraft and NeoForge jars");

        var installationDir = getInstallationDir().getAsFile().get();

        var patchedMinecraftPath = new File(installationDir, "libraries/net/neoforged/neoforge/" + nfVersion + "/neoforge-" + nfVersion + "-client.jar");
        var neoforgePath = new File(installationDir, "libraries/net/neoforged/neoforge/" + nfVersion + "/neoforge-" + nfVersion + "-universal.jar");

        if (!patchedMinecraftPath.isFile()) {
            throw new GradleException("Expected patched Minecraft client at " + patchedMinecraftPath + " after installation, but it's missing.");
        }
        if (!neoforgePath.isFile()) {
            throw new GradleException("Expected NeoForge jar at " + neoforgePath + " after installation, but it's missing.");
        }

        // Merge the patched Minecraft classes with the Neoforge classes
        File outputFile = getOutput().getAsFile().get();
        try (var mcIn = new ZipFile(patchedMinecraftPath);
             var nfIn = new ZipFile(neoforgePath);
             var zout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            copyEntries(mcIn, zout);
            copyEntries(nfIn, zout);
        } catch (IOException e) {
            throw new GradleException("Failed to merge the patched MC and NF jars.", e);
        }
    }

    private static void copyEntries(ZipFile zf, ZipOutputStream out) throws IOException {
        var it = zf.entries();
        while (it.hasMoreElements()) {
            var entry = it.nextElement();
            try (var in = zf.getInputStream(entry)) {
                out.putNextEntry(entry);
                in.transferTo(out);
                out.closeEntry();
            }
        }
    }
}
