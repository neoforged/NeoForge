/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.logging;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.transformers.MixinClassWriter;

public class LogBundle {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final Path LOGS_FOLDER = FMLPaths.GAMEDIR.get().resolve("logs");
    private static final File DEBUG_LOG = LOGS_FOLDER.resolve("debug.log").toFile();
    private static final File LATEST_LOG = LOGS_FOLDER.resolve("latest.log").toFile();
    private static final Path LOG_BUNDLE_FOLDER = LOGS_FOLDER.resolve("log-bundle");

    public static void collectAndSave(@Nullable Throwable crash, @Nullable File crashLog) {
        try {
            FileUtil.createDirectoriesSafe(LOG_BUNDLE_FOLDER);

            Path bundlePath = LOG_BUNDLE_FOLDER.resolve("log-bundle-" + Util.getFilenameFormattedDateTime() + ".zip");

            try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(bundlePath.toFile()))) {
                {
                    writeEntryToZip("README.md", zip);
                    ClassLoader classLoader = LogBundle.class.getClassLoader();
                    try (InputStream inputStream = classLoader.getResourceAsStream("META-INF/log-bundle-base/README.md")) {
                        if (inputStream != null) {
                            byte[] buffer = new byte[8192]; // 8 KB
                            int length;
                            while ((length = inputStream.read(buffer)) != -1) {
                                zip.write(buffer, 0, length);
                            }
                        }
                    }
                }

                {
                    StringBuilder s = new StringBuilder();
                    for (ModInfo modInfo : LoadingModList.get().getMods()) {
                        String modId = modInfo.getModId();
                        String modVersion = modInfo.getVersion().toString();
                        File modFile = modInfo.getOwningFile()
                                .getFile()
                                .getFilePath()
                                .toFile();

                        s.append(modId)
                                .append(" ")
                                .append(modVersion);

                        if (modFile.isFile()) {
                            byte[] buffer = new byte[8192]; // 8 KB
                            int length;
                            MessageDigest digest = MessageDigest.getInstance("SHA-256");
                            try (BufferedInputStream i = new BufferedInputStream(new FileInputStream(modFile))) {
                                while ((length = i.read(buffer)) > 0) {
                                    digest.update(buffer, 0, length);
                                }
                            }

                            s.append(" ")
                                    .append(modFile.getName())
                                    .append(" ")
                                    .append(bytesToHex(digest.digest()));
                        }

                        s.append("\n");
                    }

                    writeStringToZip(s.toString(), "modlist.txt", zip);
                }

                writeFileToZip(crashLog, zip, "crash-reports/");
                writeFileToZip(DEBUG_LOG, zip, "logs/");
                writeFileToZip(LATEST_LOG, zip, "logs/");

                if (crash != null) {
                    StackTraceElement[] stackTrace = crash.getStackTrace();

                    List<String> classNames = Arrays.stream(stackTrace)
                            .map(StackTraceElement::getClassName)
                            .distinct()
                            .toList();

                    for (String className : classNames) {
                        Set<IMixinInfo> mixins = Mixins.getMixinsForClass(className);

                        if (!mixins.isEmpty()) {
                            try {
                                ClassNode node = MixinService.getService().getBytecodeProvider().getClassNode(className);

                                MixinClassWriter cw = new MixinClassWriter(ClassWriter.COMPUTE_FRAMES);
                                node.accept(cw);
                                byte[] bytes = cw.toByteArray();

                                String dumpedClassPath = "mixins/" + className.replace(".", "/") + ".class";
                                writeBytesToZip(bytes, dumpedClassPath, zip);
                            } catch (ClassNotFoundException | IOException e) {
                                LOGGER.warn("Could not dump modified class for {}", className, e);
                            }
                        }
                    }
                }
            }
        } catch (IOException | NoSuchAlgorithmException exception) {
            LOGGER.warn("Failed to collect log bundle", exception);
        }
    }

    private static void writeFileToZip(@Nullable File inputFile, ZipOutputStream zip, String folder) throws IOException {
        if (inputFile == null || !inputFile.exists())
            return;

        ZipEntry crashLogEntry = new ZipEntry(folder + inputFile.getName());
        zip.putNextEntry(crashLogEntry);

        try (FileInputStream inputStream = new FileInputStream(inputFile)) {
            byte[] buffer = new byte[8192]; // 8 KB
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                zip.write(buffer, 0, length);
            }
        }
    }

    private static void writeBytesToZip(byte[] bytes, String fileName, ZipOutputStream zip) throws IOException {
        ZipEntry crashLogEntry = new ZipEntry(fileName);
        zip.putNextEntry(crashLogEntry);
        zip.write(bytes, 0, bytes.length);
    }

    private static void writeStringToZip(String s, String fileName, ZipOutputStream zip) throws IOException {
        writeBytesToZip(s.getBytes(), fileName, zip);
    }

    private static void writeEntryToZip(String fileName, ZipOutputStream zip) throws IOException {
        ZipEntry crashLogEntry = new ZipEntry(fileName);
        zip.putNextEntry(crashLogEntry);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
