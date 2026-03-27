/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.quad;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * Holds pre-computed normals, either for the entire quad or for each vertex.
 * <p>
 * The normal values are quantized to an integer between -127 and 127, truncated to a byte
 * and packed into an int, leaving the MSB unused.
 * <p>
 * Normals can also be {@linkplain #isUnspecified(int) unspecified}. In that case, rendering will use the normal implied by {@link BakedQuad#direction()},
 * while a face normal will be computed for the purposes of AO calculations.
 */
public sealed interface BakedNormals {
    BakedNormals UNSPECIFIED = new PerQuad(0);

    /**
     * {@return The packed normal of the given vertex}
     * It can also be {@linkplain #isUnspecified(int) unspecified}.
     * 
     * @see #unpack(int, Vector3f)
     */
    int normal(int vertex);

    record PerQuad(int normal) implements BakedNormals {
        @Override
        public int normal(int vertex) {
            return normal;
        }
    }

    record PerVertex(int normal0, int normal1, int normal2, int normal3) implements BakedNormals {
        @Override
        public int normal(int vertex) {
            return switch (vertex) {
                case 0 -> normal0;
                case 1 -> normal1;
                case 2 -> normal2;
                case 3 -> normal3;
                default -> throw new IndexOutOfBoundsException(vertex);
            };
        }
    }

    /**
     * @param normal0 The normal of the first vertex, packed using {@link #pack}.
     * @param normal1 The normal of the second vertex, packed using {@link #pack}.
     * @param normal2 The normal of the third vertex, packed using {@link #pack}.
     * @param normal3 The normal of the fourth vertex, packed using {@link #pack}.
     */
    static BakedNormals of(int normal0, int normal1, int normal2, int normal3) {
        if (normal0 == normal1 && normal0 == normal2 && normal0 == normal3) {
            return of(normal0);
        }
        return new PerVertex(normal0, normal1, normal2, normal3);
    }

    /**
     * @param normal The face normal, packed using {@link #pack}.
     */
    static BakedNormals of(int normal) {
        return isUnspecified(normal) ? UNSPECIFIED : new PerQuad(normal);
    }

    /**
     * {@return a packed representation of the given normal}
     */
    static int pack(float x, float y, float z) {
        int packedx = ((byte) Math.round(x * 127)) & 0xFF;
        int packedy = ((byte) Math.round(y * 127)) & 0xFF;
        int packedz = ((byte) Math.round(z * 127)) & 0xFF;

        return packedx | (packedy << 8) | (packedz << 16);
    }

    /**
     * {@return a packed representation of the given normal}
     */
    static int pack(Vector3fc normal) {
        return pack(normal.x(), normal.y(), normal.z());
    }

    /**
     * {@return the component of the given packed normal}
     *
     * @param axis Pass 0 to extract the x component, 1 for y and 2 for z.
     *
     * @see #unpackX(int)
     * @see #unpackY(int)
     * @see #unpackZ(int)
     */
    static float unpackComponent(int packedNormal, int axis) {
        int encodedNormalComponent = (packedNormal >> (axis * 8)) & 0xFF;
        // Casting to byte will cast to a signed int.
        // This is really important, otherwise negative values will lead to above 1.0 normal components.
        return ((byte) encodedNormalComponent) / 127.0f;
    }

    /**
     * {@return the x component of the given packed normal}
     * 
     * @see #unpackComponent(int, int)
     */
    static float unpackX(int packedNormal) {
        return ((byte) (packedNormal & 0xFF)) / 127.0f;
    }

    /**
     * {@return the y component of the given packed normal}
     * 
     * @see #unpackComponent(int, int)
     */
    static float unpackY(int packedNormal) {
        return ((byte) ((packedNormal >> 8) & 0xFF)) / 127.0f;
    }

    /**
     * {@return the z component of the given packed normal}
     * 
     * @see #unpackComponent(int, int)
     */
    static float unpackZ(int packedNormal) {
        return ((byte) ((packedNormal >> 16) & 0xFF)) / 127.0f;
    }

    /**
     * @param destination The vector to unpack the packed normal into, if {@code null}, a new vector will be allocated.
     * @return The vector that the normal was unpacked into.
     */
    static Vector3f unpack(int packedNormal, @Nullable Vector3f destination) {
        if (destination == null) {
            destination = new Vector3f();
        }
        return destination.set(unpackX(packedNormal), unpackY(packedNormal), unpackZ(packedNormal));
    }

    /**
     * {@return true if the packed normal represents an unspecified normal}
     */
    static boolean isUnspecified(int packedNormal) {
        return (packedNormal & 0x00FFFFFF) == 0;
    }

    /**
     * Computes the packed normal of a quad based on the given vertex positions.
     *
     * @return The packed representation of the computed normal.
     */
    static int computeQuadNormal(Vector3fc position0, Vector3fc position1, Vector3fc position2, Vector3fc position3) {
        float dx0 = position3.x() - position1.x();
        float dy0 = position3.y() - position1.y();
        float dz0 = position3.z() - position1.z();
        float dx1 = position2.x() - position0.x();
        float dy1 = position2.y() - position0.y();
        float dz1 = position2.z() - position0.z();

        float nx = dy1 * dz0 - dz1 * dy0;
        float ny = dz1 * dx0 - dx1 * dz0;
        float nz = dx1 * dy0 - dy1 * dx0;

        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (length > 0) {
            nx /= length;
            ny /= length;
            nz /= length;
        }

        return pack(nx, ny, nz);
    }
}
