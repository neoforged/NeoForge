/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.quad;

import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

public final class QuadTransforms {
    private QuadTransforms() {}

    /**
     * Returns a baked quad with the passed transformation applied.
     */
    public static BakedQuad applyTransformation(BakedQuad quad, Transformation transformation) {
        if (transformation.isIdentity()) {
            return quad;
        }
        BakedNormals updatedNormals;
        if (quad.bakedNormals() == BakedNormals.UNSPECIFIED) {
            updatedNormals = BakedNormals.UNSPECIFIED;
        } else {
            var normalTemp = new Vector3f();
            updatedNormals = switch (quad.bakedNormals()) {
                case BakedNormals.PerQuad(int n) -> BakedNormals.of(
                        transformNormal(normalTemp, n, transformation));
                case BakedNormals.PerVertex(int n0, int n1, int n2, int n3) -> BakedNormals.of(
                        transformNormal(normalTemp, n0, transformation),
                        transformNormal(normalTemp, n1, transformation),
                        transformNormal(normalTemp, n2, transformation),
                        transformNormal(normalTemp, n3, transformation));
            };
        }
        var posTemp = new Vector4f();
        return new BakedQuad(
                transformPosition(posTemp, quad.position0(), transformation),
                transformPosition(posTemp, quad.position1(), transformation),
                transformPosition(posTemp, quad.position2(), transformation),
                transformPosition(posTemp, quad.position3(), transformation),
                quad.packedUV0(),
                quad.packedUV1(),
                quad.packedUV2(),
                quad.packedUV3(),
                quad.tintIndex(),
                // TODO: the direction is currently not being transformed, but probably should be
                quad.direction(),
                quad.sprite(),
                quad.shade(),
                quad.lightEmission(),
                updatedNormals,
                quad.bakedColors(),
                quad.hasAmbientOcclusion());
    }

    private static Vector3fc transformPosition(Vector4f temp, Vector3fc pos, Transformation transformation) {
        temp.set(pos.x(), pos.y(), pos.z(), 1);
        transformation.transformPosition(temp);
        temp.div(temp.w);
        return new Vector3f(temp.x(), temp.y(), temp.z());
    }

    private static int transformNormal(Vector3f temp, int packedNormal, Transformation transformation) {
        BakedNormals.unpack(packedNormal, temp);
        transformation.transformNormal(temp);
        return BakedNormals.pack(temp);
    }
}
