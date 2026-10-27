/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.obj;

import com.google.common.primitives.Ints;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class ObjReader implements AutoCloseable {

    // No OBJ command is longer than 32 bytes, so this is a reasonable limit.
    private static final int OBJ_COMMAND_READ_AHEAD_LIMIT = 32;
    // An integer is at most 11 characters (10 digits and the minus sign)
    private static final int INTEGER_READ_AHEAD_LIMIT = 11;
    // A float is at most 112 characters (112 digits and the minus sign)
    private static final int FLOAT_READ_AHEAD_LIMIT = 113;
    // Comments are arbitrarily sized, but let's assume most are short for now.
    private static final int COMMENT_READ_AHEAD_LIMIT = 64;

    private static final int READ_BUFFER_SIZE = Mth.smallestEncompassingPowerOfTwo(
            Ints.max(
                    OBJ_COMMAND_READ_AHEAD_LIMIT,
                    INTEGER_READ_AHEAD_LIMIT,
                    FLOAT_READ_AHEAD_LIMIT,
                    COMMENT_READ_AHEAD_LIMIT));

    private final Resource _resource;
    private final BufferedReader _reader;

    private final char[] _readBuffer;

    public ObjReader(Resource resource) throws IOException {
        _resource = resource;
        _reader = resource.openAsReader();

        _readBuffer = new char[READ_BUFFER_SIZE];
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
    public ObjCommand readCommand() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();
        // Return early if we've reached the end of the file.
        if (!hasMoreData()) return null;

        _reader.mark(OBJ_COMMAND_READ_AHEAD_LIMIT);
        var read = _reader.read(_readBuffer, 0, OBJ_COMMAND_READ_AHEAD_LIMIT);
        _reader.reset();

        if (read < 0) throw new IOException("Couldn't read command");
        read = CharArrayUtilities.findFirstWhitespace(_readBuffer, 0, read);
        if (read < 0) throw new IOException("Unable to tokenize command");

        // The read command is now between [0, read)
        var cmd = ObjCommand.parse(_readBuffer, 0, read);
        var _ = _reader.skip(read);
        return cmd;
    }

    /// Reads a string in the stream.
    public String readString() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();

        return _reader.readLine();
    }

    /// Reads a vertex position in the stream.
    public Vector3fc readVertexPosition() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();

        var x = readFloat();
        skipWhitespace();
        var y = readFloat();
        skipWhitespace();
        var z = readFloat();
        skipWhitespace();
        // OBJ specifies xyz[w] for the 'v' command, where w defaults to 1.0
        var w = readOptionalFloat(1.0f);
        skipWhitespace();

        if (w != 1.0f) {
            x /= w;
            y /= w;
            z /= w;
        }

        return new Vector3f(x, y, z);
    }

    /// Reads a vertex normal in the stream.
    public Vector3fc readVertexNormal() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();

        var x = readFloat();
        skipWhitespace();
        var y = readFloat();
        skipWhitespace();
        var z = readFloat();
        skipWhitespace();

        return new Vector3f(x, y, z);
    }

    /// Reads a texture coordinate in the stream.
    public Vector3fc readTextureCoordinate() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();

        var u = readFloat();
        skipWhitespace();
        // OBJ specifies u[v[w]] for the 'vt' command, where both default to 0
        var v = readOptionalFloat(0.0f);
        skipWhitespace();
        var w = readOptionalFloat(0.0f);
        skipWhitespace();

        return new Vector3f(u, v, w);
    }

    public record Face(Vertex[] vertices) {
        public record Vertex(int positionIndex,
                @Nullable Integer textureCoordinateIndex,
                @Nullable Integer normalIndex) {}
    }

    /// Reads a face in the stream.
    public Face readFace() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();

        var vertices = new ArrayList<Face.Vertex>();
        while (isInteger()) {
            vertices.add(readVertexReference());
            skipWhitespace();
        }

        return new Face(vertices.toArray(Face.Vertex[]::new));
    }

    /// Reads a vertex reference in the stream.
    private Face.Vertex readVertexReference() throws IOException {
        // The position is always present.
        var position = readInteger();

        // No whitespace is permitted as a separator (including comments).

        // e.g. `f 1 2 3`
        if (!readSlash())
            return new Face.Vertex(position, null, null);

        // e.g. `f 1//2 2//3`
        if (readSlash()) {
            var normal = readInteger();
            return new Face.Vertex(position, null, normal);
        }

        var texCoord = readInteger();

        // e.g. `f 1/2/3 2/3/4'
        if (readSlash()) {
            var normal = readInteger();
            return new Face.Vertex(position, texCoord, normal);
        }

        // e.g. `f 1/2 3/4`
        return new Face.Vertex(position, texCoord, null);
    }

    public int readSmoothingGroup() throws IOException {
        // Before reading anything, skip any whitespace tokens so we're aligned correctly.
        skipWhitespace();

        // If the first character is a digit, then assume it's an integer.
        if (isInteger()) return readInteger();

        // Otherwise, check for 'off'.
        _reader.mark(3);
        var read = _reader.read(_readBuffer, 0, 3);
        _reader.reset();

        if (read < 3) throw new IOException("Couldn't read smoothing group");
        if (_readBuffer[0] != 'o' || _readBuffer[1] != 'f' || _readBuffer[2] != 'f') throw new IOException("Invalid smoothing group specification");

        var _ = _reader.skip(3);
        return 0;
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
        while (true) {
            _reader.mark(COMMENT_READ_AHEAD_LIMIT);
            var read = _reader.read(_readBuffer, 0, COMMENT_READ_AHEAD_LIMIT);
            _reader.reset();

            if (read < 0) {
                throw new IOException("Couldn't read a comment");
            }

            // Skip the first character
            for (int i = 0; i < read; i++) {
                if ((i < read - 1) && (_readBuffer[i] == '\r') && (_readBuffer[i + 1] == '\n')) {
                    var _ = _reader.skip(i + 2);
                    return;
                } else if (_readBuffer[i] == '\n') {
                    var _ = _reader.skip(i + 1);
                    return;
                }
            }

            // If there was no newline, skip forward to read the next block.
            var _ = _reader.skip(read);
        }
    }

    /// Reads a single `/` in the stream.
    ///
    /// Returns `true` if the next value is a `/` character.
    private boolean readSlash() throws IOException {
        _reader.mark(1);
        var read = _reader.read();
        if (read == '/') {
            return true;
        }

        _reader.reset();
        return false;
    }

    /// Reads a required float in the stream.
    ///
    /// Throws `NumberFormatException` if there was no valid number.
    private float readFloat() throws IOException {
        _reader.mark(FLOAT_READ_AHEAD_LIMIT);
        var read = _reader.read(_readBuffer, 0, FLOAT_READ_AHEAD_LIMIT);
        _reader.reset();

        if (read < 0) {
            throw new IOException("Couldn't read a number");
        }
        var firstNonDigit = CharArrayUtilities.findFirstNonFloat(_readBuffer, 0, read);
        if (firstNonDigit >= 0) {
            read = firstNonDigit;
        }

        // Unfortunately because Java is stupid we have to allocate a string here.
        var result = Float.parseFloat(String.valueOf(_readBuffer, 0, read));
        var _ = _reader.skip(read);
        return result;
    }

    /// Reads an optional float in the stream.
    private float readOptionalFloat(float defaultValue) throws IOException {
        try {
            _reader.mark(FLOAT_READ_AHEAD_LIMIT);
            var read = _reader.read(_readBuffer, 0, FLOAT_READ_AHEAD_LIMIT);
            _reader.reset();

            if (read < 0) {
                return defaultValue;
            }
            var firstNonDigit = CharArrayUtilities.findFirstNonFloat(_readBuffer, 0, read);
            if (firstNonDigit >= 0) {
                read = firstNonDigit;
            }

            // Unfortunately because Java is stupid we have to allocate a string here.
            var result = Float.parseFloat(String.valueOf(_readBuffer, 0, read));
            var _ = _reader.skip(read);
            return result;
        } catch (NumberFormatException _) {
            return defaultValue;
        }
    }

    /// Returns true if the next character in the stream is an integer.
    private boolean isInteger() throws IOException {
        _reader.mark(1);
        var read = _reader.read();
        _reader.reset();

        return (read >= '0' && read <= '9');
    }

    /// Reads a required integer in the stream.
    ///
    /// Throws `NumberFormatException` if there was no valid integer.
    private int readInteger() throws IOException {
        _reader.mark(INTEGER_READ_AHEAD_LIMIT);
        var read = _reader.read(_readBuffer, 0, INTEGER_READ_AHEAD_LIMIT);
        _reader.reset();

        if (read < 0) {
            throw new IOException("Couldn't read a number");
        }
        var firstNonDigit = CharArrayUtilities.findFirstNonDigit(_readBuffer, 0, read);
        if (firstNonDigit >= 0) {
            read = firstNonDigit;
        }

        // Unfortunately because Java is stupid we have to allocate a string here.
        var result = Integer.parseInt(String.valueOf(_readBuffer, 0, read));
        var _ = _reader.skip(read);
        return result;
    }
}
