/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.quad;

import com.mojang.math.Quadrant;
import java.util.Objects;
import net.minecraft.client.model.geom.builders.UVPair;

public class UVTransform {
    private static final UVTransform[] TRANSFORMS = createTransforms();
    public static final UVTransform IDENTITY = of(Quadrant.R0, false, false);

    final int rotation;
    final boolean flipU;
    final boolean flipV;
    final Quadrant quadrant;

    private UVTransform(Quadrant rotation, boolean flipU, boolean flipV) {
        this.rotation = rotation.shift;
        this.quadrant = rotation;
        this.flipU = flipU;
        this.flipV = flipV;
    }

    public boolean isIdentity() {
        return this == IDENTITY;
    }

    public static UVTransform of(Quadrant rotation, boolean flipU, boolean flipV) {
        return TRANSFORMS[makeIndex(rotation.shift, flipU, flipV)];
    }

    public long transformPacked(long packedUv) {
        float u = UVPair.unpackU(packedUv);
        float v = UVPair.unpackV(packedUv);

        switch (rotation) {
            case 0 -> {}
            case 1 -> {
                var tmp = u;
                u = v;
                v = 1 - tmp;
            }
            case 2 -> {
                u = 1 - u;
                v = 1 - v;
            }
            case 3 -> {
                var tmp = u;
                u = 1 - v;
                v = tmp;
            }
        }

        if (flipU) {
            u = 1 - u;
        }
        if (flipV) {
            v = 1 - v;
        }

        return UVPair.pack(u, v);
    }

    private static UVTransform[] createTransforms() {
        var result = new UVTransform[16];
        for (var quadrant : Quadrant.values()) {
            for (var flipU = 0; flipU < 2; flipU++) {
                for (var flipV = 0; flipV < 2; flipV++) {
                    var transform = new UVTransform(quadrant, flipU > 0, flipV > 0);
                    result[makeIndex(transform.rotation, flipU > 0, flipV > 0)] = transform;
                }
            }
        }
        // Just to be sure
        for (var uvTransform : result) {
            Objects.requireNonNull(uvTransform);
        }
        return result;
    }

    private static int makeIndex(int rotation, boolean flipU, boolean flipV) {
        var result = rotation & 3;
        if (flipU) {
            result |= 0x4;
        }
        if (flipV) {
            result |= 0x8;
        }
        return result;
    }
}
