/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.quad;

/**
 * Holds a static color modulator, either for the entire quad or for each vertex, in the ARGB format.
 * <p>
 * The stored color value is converted from ARGB to ABGR during buffering.
 */
public sealed interface BakedColors {
    BakedColors DEFAULT = new PerQuad(0xFFFFFFFF);

    int color(int vertex);

    record PerQuad(int color) implements BakedColors {
        @Override
        public int color(int vertex) {
            return color;
        }
    }

    record PerVertex(int color0, int color1, int color2, int color3) implements BakedColors {
        @Override
        public int color(int vertex) {
            return switch (vertex) {
                case 0 -> color0;
                case 1 -> color1;
                case 2 -> color2;
                case 3 -> color3;
                default -> throw new IndexOutOfBoundsException(vertex);
            };
        }
    }

    static BakedColors of(int color0, int color1, int color2, int color3) {
        if (color0 == color1 && color0 == color2 && color0 == color3) {
            return of(color0);
        }
        return new PerVertex(color0, color1, color2, color3);
    }

    static BakedColors of(int color) {
        return color == 0xFFFFFFFF ? DEFAULT : new PerQuad(color);
    }
}
