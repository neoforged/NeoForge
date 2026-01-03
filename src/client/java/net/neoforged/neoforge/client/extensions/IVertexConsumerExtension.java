/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
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
     * Same as {@link #putBulkData(PoseStack.Pose, MutableQuad, float[], float, float, float, float, int[], int)},
     * but does not shade the color (assumes brightness = 1), and uses the same {@code lightCoords} for all four vertices.
     */
    default void putBulkData(PoseStack.Pose pose, net.neoforged.neoforge.client.model.quad.MutableQuad quad, float r, float g, float b, float a, int lightCoords, int overlayCoords) {
        this.putBulkData(
                pose, quad, new float[] { 1.0F, 1.0F, 1.0F, 1.0F }, r, g, b, a, new int[] { lightCoords, lightCoords, lightCoords, lightCoords }, overlayCoords);
    }

    /**
     * Same as {@link VertexConsumer#putBulkData(PoseStack.Pose, BakedQuad, float[], float, float, float, float, int[], int)},
     * but sources the data from a {@link MutableQuad}.
     */
    default void putBulkData(
            PoseStack.Pose pose, net.neoforged.neoforge.client.model.quad.MutableQuad quad, float[] brightness, float r, float g, float b, float a, int[] lightmapCoord, int overlayCoords) {
        Matrix4f matrix = pose.pose();
        Vector3f faceNormal = pose.transformNormal(quad.direction().getUnitVec3f(), new Vector3f());
        int lightEmission = quad.lightEmission();

        for (int vertex = 0; vertex < 4; vertex++) {
            long packedUv = quad.packedUv(vertex);
            float brightnessForVertex = brightness[vertex];
            int color = ARGB.colorFromFloat(a, brightnessForVertex * r, brightnessForVertex * g, brightnessForVertex * b);
            color = ARGB.multiply(color, quad.color(vertex)); // Neo: apply baked color from the quad
            int light = LightCoordsUtil.lightCoordsWithEmission(lightmapCoord[vertex], lightEmission);
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
            self().addVertex(pos.x(), pos.y(), pos.z(), color, u, v, overlayCoords, light, normal.x(), normal.y(), normal.z());
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
