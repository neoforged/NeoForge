/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Matrix3f;
import org.joml.Vector3f;

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

    default void applyBakedNormals(Vector3f generated, BakedNormals data, int vertex, Matrix3f normalTransform) {
        int packed = data.normals(vertex);
        if ((packed & 0x00FFFFFF) != 0) {
            byte nx = (byte) (packed & 0xFF);
            byte ny = (byte) (packed >> 8 & 0xFF);
            byte nz = (byte) (packed >> 16 & 0xFF);
            generated.set(nx / 127f, ny / 127f, nz / 127f);
            generated.mul(normalTransform);
        }
    }
}
