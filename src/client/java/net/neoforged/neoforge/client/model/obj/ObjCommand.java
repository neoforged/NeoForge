package net.neoforged.neoforge.client.model.obj;

import org.jspecify.annotations.Nullable;

/// A command within an OBJ file. This is designed for compatibility with OBJ
/// files as exported by Blender. Other programs may use features which are
/// unsupported.
public enum ObjCommand {
    /// A material libary reference (mtllib)
    MaterialLibraryReference,

    /// An object (o)
    Object,

    /// A vertex position (v)
    VertexPosition,
    /// A vertex normal (vn)
    VertexNormal,
    /// A vertex texture coordinate (vt)
    VertexTexture,

    /// A face element (f)
    Face,

    /// A material reference (usemtl)
    MaterialReference,

    /// A smoothing group (s)
    SmoothingGroup;

    public static ObjCommand parse(char[] command, int start, int length) {
        var parsed = tryParse(command, start, length);
        if (parsed == null)
            throw new IllegalArgumentException("Unable to parse command");

        return parsed;
    }

    public static @Nullable ObjCommand tryParse(char[] command, int start, int length) {
        var maxIndex = start + length;
        if (maxIndex >= command.length)
            throw new IllegalArgumentException(
                "The maximum possible index (" + maxIndex +
                    ") is larger than the length of the array (" +
                    command.length + ")");

        return switch (length) {
            case 1 -> parseSingle(command[start]);
            case 2 -> parseDouble(command[start], command[start + 1]);
            case 6 -> parseHex(command[start], command[start + 1],
                command[start + 2], command[start + 3],
                command[start + 4], command[start + 5]);
            default -> null;
        };
    }

    private static @Nullable ObjCommand parseSingle(char value) {
        return switch (value) {
            case 'o' -> Object;
            case 'v' -> VertexPosition;
            case 'f' -> Face;
            case 's' -> SmoothingGroup;
            default -> null;
        };
    }

    private static @Nullable ObjCommand parseDouble(char first, char second) {
        if (first != 'v') return null;

        return switch (second) {
            case 'n' -> VertexNormal;
            case 't' -> VertexTexture;
            default -> null;
        };
    }

    private static @Nullable ObjCommand parseHex(char first, char second,
        char third, char fourth,
        char fifth, char sixth) {

        if (first == 'm' && second == 't' && third == 'l' && fourth == 'l' && fifth == 'i' && sixth == 'b') return MaterialLibraryReference;
        if (first == 'u' && second == 's' && third == 'e' && fourth == 'm' && fifth == 't' && sixth == 'l') return MaterialReference;

        return null;
    }
}
