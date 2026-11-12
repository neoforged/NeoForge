/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.obj;

import org.jspecify.annotations.Nullable;

/// A command within an MTL file. This is designed for compatibility with MTL
/// files as exported by Blender. Other programs may use features which are
/// unsupported.
public enum MtlCommand {
    /// A new material command (newmtl)
    NewMaterial,

    /// A specularity exponent command (Ns)
    SpecularExponent,
    /// An optical density command (Ni)
    OpticalDensity,

    /// An ambient reflectivity command (Ka)
    AmbientReflectivity,
    /// A diffuse reflectivity command (Kd)
    DiffuseReflectivity,
    /// A specular reflectivity command (Ks)
    SpecularReflectivity,
    /// A specular emission command (Ke)
    /// This is not part of the MTL/OBJ specification, but is supported as an
    /// extension by many importers/exporters, so we support it here.
    SpecularEmission,

    /// A dissolve command (d)
    Dissolve,

    /// An illumination model command (illum)
    IlluminationModel,

    /// A texture-mapped specularity exponent command (map_Ns)
    MappedSpecularityExponent,

    /// A texture-mapped ambient reflectivity command (map_Ka)
    MappedAmbientReflectivity,
    /// A texture-mapped diffuse reflectivity command (map_Kd)
    MappedDiffuseReflectivity,
    /// A texture-mapped specular reflectivity command (map_Ks)
    MappedSpecularReflectivity,

    /// A texture-mapped specular emission (map_Ke)
    /// This is not part of the MTL/OBJ specification, but is supported as an
    /// extension by Blender, so we support it here.
    MappedSpecularEmission,

    /// A displacement map command (disp)
    DisplacementMap,
    /// A bump map command (bump)
    /// Note that Blender incorrectly generates 'map_Bump' for this value, so
    /// in our command parser we make these equivalent.
    BumpMap,
    /// A reflection map command (refl)
    ReflectionMap,

    // NEOFORGE EXTENSIONS:

    /// A tint index command (neoforge_TintIndex)
    /// For backwards-compatibility, 'forge_TintIndex' is also supported.
    NeoForgeTintIndex;

    public static MtlCommand parse(char[] command, int start, int length) {
        var parsed = tryParse(command, start, length);
        if (parsed == null)
            throw new IllegalArgumentException("Unable to parse command '" + String.valueOf(command, start, length) + "'");

        return parsed;
    }

    public static @Nullable MtlCommand tryParse(char[] command, int start, int length) {
        var maxIndex = start + length;
        if (maxIndex >= command.length)
            throw new IllegalArgumentException(
                    "The maximum possible index (" + maxIndex +
                            ") is larger than the length of the array (" +
                            command.length + ")");

        return switch (length) {
            case 1 -> parseSingle(command[start]);
            case 2 -> parseDouble(command[start], command[start + 1]);
            case 4 -> parseQuad(command[start], command[start + 1],
                    command[start + 2], command[start + 3]);
            case 5 -> parseQuint(command[start], command[start + 1],
                    command[start + 2], command[start + 3],
                    command[start + 4]);
            case 6 -> parseHex(command[start], command[start + 1],
                    command[start + 2], command[start + 3],
                    command[start + 4], command[start + 5]);

            case 15, 18 -> parseTintIndex(command, start);
            default -> null;
        };
    }

    private static @Nullable MtlCommand parseSingle(char value) {
        return switch (value) {
            case 'd' -> Dissolve;
            default -> null;
        };
    }

    private static @Nullable MtlCommand parseDouble(char first, char second) {
        return switch (first) {
            case 'N' -> switch (second) {
                case 's' -> SpecularExponent;
                case 'i' -> OpticalDensity;
                default -> null;
            };
            case 'K' -> switch (second) {
                case 'a' -> AmbientReflectivity;
                case 'd' -> DiffuseReflectivity;
                case 's' -> SpecularReflectivity;
                case 'e' -> SpecularEmission;
                default -> null;
            };
            default -> null;
        };
    }

    private static @Nullable MtlCommand parseQuad(char first, char second,
            char third, char fourth) {
        return switch (first) {
            case 'd' -> (second == 'i' && third == 's' && fourth == 'p') ? DisplacementMap : null;
            case 'b' -> (second == 'u' && third == 'm' && fourth == 'p') ? BumpMap : null;
            case 'r' -> (second == 'e' && third == 'f' && fourth == 'l') ? ReflectionMap : null;
            default -> null;
        };
    }

    private static @Nullable MtlCommand parseQuint(char first, char second,
            char third, char fourth,
            char fifth) {
        if (first == 'i' && second == 'l' && third == 'l' && fourth == 'u' && fifth == 'm') {
            return IlluminationModel;
        }

        return null;
    }

    private static @Nullable MtlCommand parseHex(char first, char second,
            char third, char fourth,
            char fifth, char sixth) {
        // newmtl
        if (first == 'n' && second == 'e' && third == 'w' && fourth == 'm' && fifth == 't' && sixth == 'l') {
            return NewMaterial;
        }

        // all further commands are of the form map_*
        if (first != 'm' || second != 'a' || third != 'p' || fourth != '_')
            return null;

        return switch (fifth) {
            case 'N' -> (sixth == 's') ? MappedSpecularityExponent : null;
            case 'K' -> switch (sixth) {
                case 'a' -> MappedAmbientReflectivity;
                case 'd' -> MappedDiffuseReflectivity;
                case 's' -> MappedSpecularReflectivity;
                case 'e' -> MappedSpecularEmission;
                default -> null;
            };
            default -> null;
        };
    }

    private static @Nullable MtlCommand parseTintIndex(char[] buffer, int start) {
        switch (buffer[start]) {
            case 'f': // 'forge_TintIndex'
                if (buffer[start + 1] == 'o' &&
                        buffer[start + 2] == 'r' &&
                        buffer[start + 3] == 'g' &&
                        buffer[start + 4] == 'e' &&
                        buffer[start + 5] == '_' &&
                        buffer[start + 6] == 'T' &&
                        buffer[start + 7] == 'i' &&
                        buffer[start + 8] == 'n' &&
                        buffer[start + 9] == 't' &&
                        buffer[start + 10] == 'I' &&
                        buffer[start + 11] == 'n' &&
                        buffer[start + 12] == 'd' &&
                        buffer[start + 13] == 'e' &&
                        buffer[start + 14] == 'x')
                    return NeoForgeTintIndex;
                break;
            case 'n': // 'neoforge_TintIndex'
                if (buffer[start + 1] == 'e' &&
                        buffer[start + 2] == 'o' &&
                        buffer[start + 3] == 'f' &&
                        buffer[start + 4] == 'o' &&
                        buffer[start + 5] == 'r' &&
                        buffer[start + 6] == 'g' &&
                        buffer[start + 7] == 'e' &&
                        buffer[start + 8] == '_' &&
                        buffer[start + 9] == 'T' &&
                        buffer[start + 10] == 'i' &&
                        buffer[start + 11] == 'n' &&
                        buffer[start + 12] == 't' &&
                        buffer[start + 13] == 'I' &&
                        buffer[start + 14] == 'n' &&
                        buffer[start + 15] == 'd' &&
                        buffer[start + 16] == 'e' &&
                        buffer[start + 17] == 'x')
                    return NeoForgeTintIndex;
                break;
        }

        return null;
    }
}
