package net.neoforged.neodev.utils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class FileUtils {
    private FileUtils() {
    }

    public static void writeStringSafe(Path destination, String content, Charset charset) throws IOException {
        if (!charset.newEncoder().canEncode(content)) {
            throw new IllegalArgumentException("The given character set " + charset
                    + " cannot represent this string: " + content);
        }

        try (var out = newSafeFileOutputStream(destination)) {
            var encodedContent = content.getBytes(charset);
            out.write(encodedContent);
        }
    }

    public static void writeLinesSafe(Path destination, List<String> lines, Charset charset) throws IOException {
        writeStringSafe(destination, String.join("\n", lines), charset);
    }

    public static OutputStream newSafeFileOutputStream(Path destination) throws IOException {
        var uniqueId = ProcessHandle.current().pid() + "." + Thread.currentThread().getId();

        var tempFile = destination.resolveSibling(destination.getFileName().toString() + "." + uniqueId + ".tmp");
        var closed = new boolean[1];
        return new FilterOutputStream(Files.newOutputStream(tempFile)) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                    if (!closed[0]) {
                        atomicMoveIfPossible(tempFile, destination);
                    }
                } finally {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (IOException ignored) {
                    }
                    closed[0] = true;
                }
            }
        };
    }

    /**
     * Atomically moves the given source file to the given destination file.
     * If the atomic move is not supported, the file will be moved normally.
     *
     * @param source      The source file
     * @param destination The destination file
     * @throws IOException If an I/O error occurs
     */
    private static void atomicMoveIfPossible(final Path source, final Path destination) throws IOException {
        try {
            Files.move(source, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException ex) {
            Files.move(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
