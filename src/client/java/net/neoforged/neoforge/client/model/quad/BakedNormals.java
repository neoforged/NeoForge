/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.quad;

/**
 * Holds pre-computed normals, either for the entire quad or for each vertex.
 * <p>
 * The normal values are quantized to an integer between -127 and 127, truncated to a byte
 * and packed into an int, leaving the MSB unused.
 */
public sealed interface BakedNormals {
    BakedNormals UNSPECIFIED = new PerQuad(0);

    int normals(int vertex);

    record PerQuad(int normals) implements BakedNormals {
        @Override
        public int normals(int vertex) {
            return normals;
        }
    }

    record PerVertex(int normals0, int normals1, int normals2, int normals3) implements BakedNormals {
        @Override
        public int normals(int vertex) {
            return switch (vertex) {
                case 0 -> normals0;
                case 1 -> normals1;
                case 2 -> normals2;
                case 3 -> normals3;
                default -> throw new IndexOutOfBoundsException(vertex);
            };
        }
    }

    static BakedNormals of(int normals0, int normals1, int normals2, int normals3) {
        if (normals0 == normals1 && normals0 == normals2 && normals0 == normals3) {
            return of(normals0);
        }
        return new PerVertex(normals0, normals1, normals2, normals3);
    }

    static BakedNormals of(int normals) {
        return normals == 0 ? UNSPECIFIED : new PerQuad(normals);
    }
}
