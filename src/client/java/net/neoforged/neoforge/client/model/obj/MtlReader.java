/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.obj;

import java.io.BufferedReader;
import java.io.IOException;
import net.minecraft.server.packs.resources.Resource;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class MtlReader implements AutoCloseable {
    // No MTL command is longer than 32 bytes, so this is a reasonable limit.
    private static final int MTL_COMMAND_READ_AHEAD_LIMIT = 32;
    // An integer is at most 11 characters (10 digits and the minus sign)
    private static final int INTEGER_READ_AHEAD_LIMIT = 11;
    // A float is at most 112 characters (112 digits and the minus sign)
    private static final int FLOAT_READ_AHEAD_LIMIT = 113;
    // Comments are arbitrarily sized, but let's assume most are short for now.
    private static final int COMMENT_READ_AHEAD_LIMIT = 64;

    private final Resource _resource;
    private final BufferedReader _reader;

    public MtlReader(Resource resource) throws IOException {
        _resource = resource;
        _reader = resource.openAsReader();
    }

    @Override
    public void close() throws IOException {
        _reader.close();
    }

    public boolean hasMoreData() throws IOException {
        _reader.mark(1);
        var read = _reader.read();
        _reader.reset();

        return read != -1;
    }

    @Nullable
    public MtlCommand readCommand() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();
        // Return early if we've reached the end of the file.
        if (!hasMoreData()) return null;

        var buffer = ArrayPool.SHARED.rentArray(MTL_COMMAND_READ_AHEAD_LIMIT);
        try {
            _reader.mark(MTL_COMMAND_READ_AHEAD_LIMIT);
            var read = _reader.read(buffer, 0, MTL_COMMAND_READ_AHEAD_LIMIT);
            _reader.reset();

            if (read < 0) throw new IOException("Couldn't read command");
            read = CharArrayUtilities.findFirstWhitespace(buffer, 0, read);
            if (read < 0) throw new IOException("Unable to tokenize command");

            // The read command is now between [0, read)
            var cmd = MtlCommand.parse(buffer, 0, read);
            var _ = _reader.skip(read);
            return cmd;
        } finally {
            ArrayPool.SHARED.returnArray(buffer);
        }
    }

    public Vector3fc readColor() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();

        var r = readFloat();
        skipWhitespace();
        var g = readFloat();
        skipWhitespace();
        var b = readFloat();
        skipWhitespace();

        return new Vector3f(r, g, b);
    }

    /// Reads a string in the stream.
    public String readString() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();

        return _reader.readLine();
    }

    /// Reads a required float in the stream.
    ///
    /// Throws `NumberFormatException` if there was no valid number.
    public float readFloat() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();

        var buffer = ArrayPool.SHARED.rentArray(FLOAT_READ_AHEAD_LIMIT);
        try {
            _reader.mark(FLOAT_READ_AHEAD_LIMIT);
            var read = _reader.read(buffer, 0, FLOAT_READ_AHEAD_LIMIT);
            _reader.reset();

            if (read < 0) {
                throw new IOException("Couldn't read a number");
            }
            var firstNonDigit = CharArrayUtilities.findFirstNonFloat(buffer, 0, read);
            if (firstNonDigit >= 0) {
                read = firstNonDigit;
            }

            // Unfortunately because Java is stupid we have to allocate a string here.
            var result = Float.parseFloat(String.valueOf(buffer, 0, read));
            var _ = _reader.skip(read);
            return result;
        } finally {
            ArrayPool.SHARED.returnArray(buffer);
        }
    }

    /// Reads a required integer in the stream.
    ///
    /// Throws `NumberFormatException` if there was no valid integer.
    public int readInteger() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();

        var buffer = ArrayPool.SHARED.rentArray(INTEGER_READ_AHEAD_LIMIT);
        try {
            _reader.mark(INTEGER_READ_AHEAD_LIMIT);
            var read = _reader.read(buffer, 0, INTEGER_READ_AHEAD_LIMIT);
            _reader.reset();

            if (read < 0) {
                throw new IOException("Couldn't read a number");
            }
            var firstNonDigit = CharArrayUtilities.findFirstNonDigit(buffer, 0, read);
            if (firstNonDigit >= 0) {
                read = firstNonDigit;
            }

            // Unfortunately because Java is stupid we have to allocate a string here.
            var result = Integer.parseInt(String.valueOf(buffer, 0, read));
            var _ = _reader.skip(read);
            return result;
        } finally {
            ArrayPool.SHARED.returnArray(buffer);
        }
    }

    /// Skips whitespace-like content in the stream, including comments.
    private void skipWhitespace() throws IOException {
        while (true) {
            _reader.mark(2);
            var c = _reader.read();

            // Skip any regular whitespace
            if (c == '\n' || c == ' ') {
                continue;
            }

            // CRLF are only valid if they're a complete sequence
            else if (c == '\r') {
                var c2 = _reader.read();
                if (c2 != '\n') {
                    // If it's not a '\n', we know it's invalid syntax
                    throw new IOException("Invalid line terminator");
                }

                continue;
            }

            // Skip any valid comments
            else if (c == '#') {
                var c2 = _reader.read();
                if (c2 == ' ') {
                    skipComment();
                    continue;
                }
            }

            // If it's not any other valid whitespace, put it back and return.
            _reader.reset();
            return;
        }
    }

    /// Skips a comment in the stream.
    private void skipComment() throws IOException {
        var buffer = ArrayPool.SHARED.rentArray(COMMENT_READ_AHEAD_LIMIT);
        try {
            while (true) {
                _reader.mark(COMMENT_READ_AHEAD_LIMIT);
                var read = _reader.read(buffer, 0, COMMENT_READ_AHEAD_LIMIT);
                _reader.reset();

                if (read < 0) {
                    throw new IOException("Couldn't read a comment");
                }

                // Skip the first character
                for (int i = 0; i < read; i++) {
                    if ((i < read - 1) && (buffer[i] == '\r') && (buffer[i + 1] == '\n')) {
                        var _ = _reader.skip(i + 2);
                        return;
                    } else if (buffer[i] == '\n') {
                        var _ = _reader.skip(i + 1);
                        return;
                    }
                }

                // If there was no newline, skip forward to read the next block.
                var _ = _reader.skip(read);
            }
        } finally {
            ArrayPool.SHARED.returnArray(buffer);
        }
    }
}
