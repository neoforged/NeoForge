/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.apache.commons.io.output.CloseShieldOutputStream;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains helpers for performing file I/O in a resilient manner.
 */
public final class IOUtilities {
    private static final String TEMP_FILE_SUFFIX = ".neoforge-tmp";
    private static final OpenOption[] OPEN_OPTIONS = {
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
    };
    private static final Logger LOGGER = LoggerFactory.getLogger(IOUtilities.class);

    private IOUtilities() {}

    /**
     * Tries to clean up any temporary files that may have been left over from interrupted
     * calls to {@link #atomicWrite(Path, WriteCallback)}.
     * <p>
     * Failures to find or remove the temporary files are logged instead of thrown.
     *
     * @param targetPath The target path to clean up temporary files in.
     * @param prefix     The prefix of temporary files to clean up, or null if all
     *                   temporary files should be removed.
     */
    public static void tryCleanupTempFiles(Path targetPath, @Nullable String prefix) {
        for (var file : tryListTempFiles(targetPath, prefix)) {
            try {
                Files.deleteIfExists(file);
            } catch (IOException e) {
                // Note: The stack trace of an I/O exception thrown by delete is not very useful, hence only logging the toString()
                LOGGER.error("Could not delete temp file {}: {}", file, e.toString());
            }
        }
    }

    private static List<Path> tryListTempFiles(Path targetPath, @Nullable String prefix) {
        try (var stream = Files.find(targetPath, 1, createPredicate(prefix))) {
            return stream.toList();
        } catch (IOException e) {
            LOGGER.error("Failed to list temporary files in {}", targetPath, e);
            return List.of();
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
     * ensure the data is stored resiliently.
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
     * ensure the data is stored resiliently.
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
     * leave the file containing corrupted or otherwise half-written data.
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
            try (var channel = FileChannel.open(tempPath, OPEN_OPTIONS)) {
                // We need to prevent the callback from closing the channel, since that
                // would prevent us from flushing it.
                var stream = CloseShieldOutputStream.wrap(Channels.newOutputStream(channel));
                writeCallback.write(stream);
                // Ensure the file is fully flushed to disk before moving it into place
                channel.force(true);
            }

            // Now we try and move the file to the correct location, atomically if possible.
            try {
                Files.move(tempPath, targetPath, StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
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

    /**
     * Declares an interface which is functionally equivalent to {@link Consumer},
     * except supports the ability to throw IOExceptions that may occur.
     */
    public interface WriteCallback {
        void write(OutputStream stream) throws IOException;
    }
}
