/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import net.neoforged.neoforge.client.model.quad.MutableQuad;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Extension interface for {@link VertexConsumer}.
 */
public interface IVertexConsumerExtension {
    private VertexConsumer self() {
        return (VertexConsumer) this;
    }

    /**
     * Consumes an unknown {@link VertexFormatElement} as a raw int data array.
     * <p>
     * If the consumer needs to store the data for later use, it must copy it. There are no guarantees on immutability.
     */
    default VertexConsumer misc(VertexFormatElement element, int... rawData) {
        return self();
    }

    /**
     * Same as {@link VertexConsumer#putBakedQuad(PoseStack.Pose, BakedQuad, QuadInstance)}, but sources the data from a {@link MutableQuad}.
     */
    default void putMutableQuad(PoseStack.Pose pose, MutableQuad quad, QuadInstance instance) {
        Vector3fc normalVec = quad.direction().getUnitVec3f();
        Matrix4f matrix = pose.pose();
        Vector3f faceNormal = pose.transformNormal(normalVec, new Vector3f());
        int lightEmission = quad.lightEmission();

        for (int vertex = 0; vertex < 4; vertex++) {
            long packedUv = quad.packedUv(vertex);
            int vertexColor = ARGB.multiply(instance.getColor(vertex), quad.color(vertex));
            int light = instance.getLightCoordsWithEmission(vertex, lightEmission);
            Vector3f pos = matrix.transformPosition(quad.x(vertex), quad.y(vertex), quad.z(vertex), new Vector3f());
            float u = UVPair.unpackU(packedUv);
            float v = UVPair.unpackV(packedUv);
            int packedNormal = quad.packedNormal(vertex);
            Vector3fc normal;
            if (!BakedNormals.isUnspecified(packedNormal)) {
                normal = BakedNormals.unpack(packedNormal, new Vector3f()).mul(pose.normal());
            } else {
                normal = faceNormal;
            }
            self().addVertex(pos.x(), pos.y(), pos.z(), vertexColor, u, v, instance.overlayCoords(), light, normal.x(), normal.y(), normal.z());
        }
    }

    default void applyBakedNormals(Vector3f generated, BakedNormals data, int vertex, Matrix3f normalTransform) {
        int packed = data.normal(vertex);
        if (!BakedNormals.isUnspecified(packed)) {
            BakedNormals.unpack(packed, generated);
            generated.mul(normalTransform);
        }
    }
}
