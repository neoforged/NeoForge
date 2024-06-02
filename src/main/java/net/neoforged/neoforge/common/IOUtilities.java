/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.jetbrains.annotations.Nullable;

/**
 * Declares a class containing helpers for performing file I/O in a resillient
 * manner.
 */
public final class IOUtilities {
    private static final String TEMP_FILE_SUFFIX = ".neoforge-tmp";
    private static final OpenOption[] OPEN_OPTIONS = {
            StandardOpenOption.DSYNC, // TODO: DSYNC or SYNC here?
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
    };

    private IOUtilities() {}

    /**
     * Cleans up any temporary files which may have been created due to previous
     * writes to {@link #atomicWrite(Path, WriteCallback)}.
     *
     * @param targetPath The target path to clean up temporary files in.
     * @param prefix     The prefix of temporary files to clean up, or null if all
     *                   temporary files should be removed.
     *
     * @throws IOException if an I/O error occurs during deletion.
     */
    public static void cleanupTempFiles(Path targetPath, @Nullable String prefix) throws IOException {
        try (var filesToDelete = Files.find(targetPath, 1, createPredicate(prefix))) {
            for (var file : filesToDelete.toList()) {
                Files.deleteIfExists(file);
            }
        }
    }

    private static BiPredicate<Path, BasicFileAttributes> createPredicate(@Nullable String prefix) {
        return (file, attributes) -> {
            final var fileName = file.getFileName().toString();
            return fileName.endsWith(TEMP_FILE_SUFFIX) && (prefix == null || fileName.startsWith(prefix));
        };
    }

    /**
     * Behaves much the same as {@link NbtIo#writeCompressed(CompoundTag, Path)},
     * but uses {@link #atomicWrite(Path, WriteCallback)} behind the scenes to
     * ensure the data is stored resilliently.
     *
     * @param tag  The tag to write.
     * @param path The path to write the NBT to.
     *
     * @throws IOException if an I/O error occurs during writing.
     */
    public static void writeNbtCompressed(CompoundTag tag, Path path) throws IOException {
        atomicWrite(path, stream -> {
            try (var bufferedStream = new BufferedOutputStream(stream)) {
                NbtIo.writeCompressed(tag, bufferedStream);
            }
        });
    }

    /**
     * Behaves much the same as {@link NbtIo#write(CompoundTag, Path)},
     * but uses {@link #atomicWrite(Path, WriteCallback)} behind the scenes to
     * ensure the data is stored resilliently.
     *
     * @param tag  The tag to write.
     * @param path The path to write the NBT to.
     *
     * @throws IOException if an I/O error occurs during writing.
     */
    public static void writeNbt(CompoundTag tag, Path path) throws IOException {
        atomicWrite(path, stream -> {
            try (var bufferedStream = new BufferedOutputStream(stream);
                    var dataStream = new DataOutputStream(bufferedStream)) {
                NbtIo.write(tag, dataStream);
            }
        });
    }

    /**
     * Writes data to the given path "atomically", such that a crash will not
     * leave the the file containing corrupted or otherwise half-written data.
     * <p>
     * This method operates by creating a temporary file, writing to that file,
     * and then moving the temporary file to the correct location after flushing.
     * If a crash occurs during this process, the temporary file will be
     * abandoned.
     * <p>
     * Furthermore, the stream passed to {@code writeCallback} is not buffered,
     * and it is the handler's responsibility to implement any buffering on top
     * of this method.
     *
     * @param targetPath    The desired path to write to.
     * @param writeCallback A callback which receives the opened stream to write
     *                      data to.
     *
     * @throws IOException if an I/O error occurs during writing.
     */
    public static void atomicWrite(Path targetPath, WriteCallback writeCallback) throws IOException {
        final var tempPath = Files.createTempFile(
                targetPath.getParent(),
                targetPath.getFileName().toString(),
                TEMP_FILE_SUFFIX);

        try {
            writeImpl(tempPath, targetPath, writeCallback);
        } catch (Exception first) {
            // If an exception occurs, we want to try and clean up if we can before rethrowing.
            try {
                Files.deleteIfExists(tempPath);
            } catch (Exception second) {
                // But if we can't, we suppress the exception.
                first.addSuppressed(second);
            }

            throw first;
        }
    }

    private static void writeImpl(Path tempFile, Path destination, WriteCallback callback) throws IOException {
        try (OutputStream writeStream = Files.newOutputStream(tempFile, OPEN_OPTIONS)) {
            callback.write(writeStream);
        }

        // Now we try and move the file to the correct location, atomically if possible.
        try {
            Files.move(tempFile, destination, StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(tempFile, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    /***
     * Declares an interface which is functionally equivalent to {@link Consumer},
     * except supports the ability to throw IOExceptions that may occur.
     */
    public interface WriteCallback {
        void write(OutputStream stream) throws IOException;
    }
}
