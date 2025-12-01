/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.autoinstall;

import static net.neoforged.fml.loading.game.GameDiscovery.LIBRARIES_DIRECTORY_PROPERTY;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.MavenCoordinate;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.fml.util.ClasspathResourceUtils;
import net.neoforged.installertools.ProcessMinecraftJar;
import net.neoforged.neoforgespi.installation.GameDiscoveryOrInstallationService;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record ClientAutoInstaller() implements GameDiscoveryOrInstallationService {
    private static final Logger LOG = LoggerFactory.getLogger(ClientAutoInstaller.class);

    @Override
    public String name() {
        return "default";
    }

    @Override
    public @Nullable Result discoverOrInstall(final Dist dist) throws Exception {
        //We only support clients!
        if (!dist.isClient()) {
            return null;
        }

        var version = getNeoForgeVersion();
        if (version == null) {
            return null;
        }

        var minecraftVersion = getMinecraftVersion();
        if (minecraftVersion == null) {
            return null;
        }

        var progress = StartupNotificationManager.addProgressBar("Installation", 3);
        progress.label("Installation - Extracting resources...");

        var tempDir = Files.createTempDirectory("nf-auto-installer");
        var minecraftClientJar = getRawMinecraftClient();
        var clientMappings = getClientMappings(minecraftVersion);
        var binaryPatches = getBinaryPatches(tempDir);
        var neoFormMappings = getNeoFormMappings(tempDir);
        var output = tempDir.resolve("client.jar");

        progress.increment();
        progress.label("Installation - Installing NeoForge...");

        new ProcessMinecraftJar().process(
                new String[] {
                        "--input", minecraftClientJar.toAbsolutePath().toString(),
                        "--output", output.toAbsolutePath().toString(),
                        "--input-mappings", clientMappings.toAbsolutePath().toString(),
                        "--neoform-data", neoFormMappings.toAbsolutePath().toString(),
                        "--apply-patches", binaryPatches.toAbsolutePath().toString()
                });

        progress.increment();
        progress.label("Installation - Finalizing changes...");

        var patchedMinecraftPath = copyToLibraries(version, dist, output);

        progress.increment();
        progress.complete();

        return new Result(patchedMinecraftPath);
    }

    private static Path copyToLibraries(final String neoForgeVersion, final Dist dist, final Path output) throws IOException {
        var librariesDirectory = System.getProperty(LIBRARIES_DIRECTORY_PROPERTY);
        var patchedMinecraftPath = Path.of(librariesDirectory).resolve((switch (dist) {
            case CLIENT -> new MavenCoordinate("net.neoforged", "minecraft-client-patched", "", "", neoForgeVersion);
            case DEDICATED_SERVER -> new MavenCoordinate("net.neoforged", "minecraft-server-patched", "", "", neoForgeVersion);
        }).toRelativeRepositoryPath());

        Files.createDirectories(patchedMinecraftPath.getParent());
        Files.copy(output, patchedMinecraftPath);
        return patchedMinecraftPath;
    }

    @SuppressWarnings("UnstableApiUsage")
    private static Path getRawMinecraftClient() throws IOException {
        var jarsWithEntrypoint = new HashSet<Path>();

        var ourCl = Thread.currentThread().getContextClassLoader();
        var resources = ourCl.getResources("net/minecraft/client/main/Main.class");
        while (resources.hasMoreElements()) {
            jarsWithEntrypoint.add(ClasspathResourceUtils.findJarPathFor("net/minecraft/client/main/Main.class", "minecraft jar", resources.nextElement()));
        }

        // This class would only be present in deobfuscated jars
        resources = ourCl.getResources("net/minecraft/client/Minecraft.class");
        while (resources.hasMoreElements()) {
            jarsWithEntrypoint.remove(ClasspathResourceUtils.findJarPathFor("net/minecraft/client/Minecraft.class", "minecraft jar", resources.nextElement()));
        }

        if (jarsWithEntrypoint.size() != 1) {
            throw new IllegalStateException("Failed to find the raw minecraft client from the classpath");
        }

        //Get the minecraft jar (currently obfuscated)
        return jarsWithEntrypoint.iterator().next();
    }

    @Nullable
    private static String getNeoForgeVersion() throws IOException {
        return getManifestAttribute("version");
    }

    @Nullable
    private static String getMinecraftVersion() throws IOException {
        return getManifestAttribute("minecraft");
    }

    private static @Nullable String getManifestAttribute(final String version) throws IOException {
        String className = ClientAutoInstaller.class.getSimpleName() + ".class";
        String classPath = Objects.requireNonNull(ClientAutoInstaller.class.getResource(className)).toString();
        if (!classPath.startsWith("jar")) {
            return null;
        }
        String jarPath = classPath.substring("jar:file:".length(), classPath.indexOf("!"));
        try (JarFile jarFile = new JarFile(Paths.get(jarPath).toFile())) {
            Manifest manifest = jarFile.getManifest();
            Attributes attrs = manifest.getMainAttributes();
            return attrs.getValue(version);
        }
    }

    private static Path getClientMappings(final String minecraftVersion) {
        var fileName = "mappings-%s-client.jar".formatted(minecraftVersion);

        String classpath = System.getProperty("java.class.path");
        String[] entries = classpath.split(File.pathSeparator);
        for (String entry : entries) {
            File file = new File(entry);
            if (file.isFile() && file.getName().equals(fileName)) {
                return Path.of(file.getAbsolutePath());
            }

        }

        throw new IllegalStateException("Failed to find the client mappings on the classpath");
    }

    private static Path getBinaryPatches(Path tempDir) throws IOException {
        return extractFromAutoInstallerJar(tempDir, "patches.lzma", "/patches.lzma");
    }

    private static Path getNeoFormMappings(Path tempDir) throws IOException {
        return extractFromAutoInstallerJar(tempDir, "neoform.tsrg.lzma", "/neoform.tsrg.lzma");
    }

    private static Path extractFromAutoInstallerJar(final Path tempDir, final String targetName, final String packagedName) throws IOException {
        var targetFile = tempDir.resolve(targetName);
        var patchResource = ClientAutoInstaller.class.getResource(packagedName);
        if (patchResource == null) {
            throw new IllegalStateException("Could not find patches in the auto installer.");
        }

        try (BufferedInputStream in = new BufferedInputStream(patchResource.openStream());
                FileOutputStream fileOutputStream = new FileOutputStream(targetFile.toFile())) {
            byte[] dataBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
        }

        return targetFile;
    }
}
