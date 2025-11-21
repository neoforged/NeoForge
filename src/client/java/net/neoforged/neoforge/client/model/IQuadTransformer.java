/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;

/**
 * Transformer for {@link BakedQuad baked quads}.
 *
 * @see QuadTransformers
 */
public interface IQuadTransformer {
    int STRIDE = DefaultVertexFormat.BLOCK.getVertexSize() / 4;
    int POSITION = findOffset(VertexFormatElement.POSITION);
    int COLOR = findOffset(VertexFormatElement.COLOR);
    int UV0 = findOffset(VertexFormatElement.UV0);
    int UV1 = findOffset(VertexFormatElement.UV1);
    int UV2 = findOffset(VertexFormatElement.UV2);
    int NORMAL = findOffset(VertexFormatElement.NORMAL);

    void processInPlace(BakedQuad quad);

    default void processInPlace(List<BakedQuad> quads) {
        for (BakedQuad quad : quads)
            processInPlace(quad);
    }

    default BakedQuad process(BakedQuad quad) {
        var copy = copy(quad);
        processInPlace(copy);
        return copy;
    }

    default List<BakedQuad> process(List<BakedQuad> inputs) {
        return inputs.stream().map(IQuadTransformer::copy).peek(this::processInPlace).toList();
    }

    default IQuadTransformer andThen(IQuadTransformer other) {
        return quad -> {
            processInPlace(quad);
            other.processInPlace(quad);
        };
    }

    private static BakedQuad copy(BakedQuad quad) {
        // TODO 1.21.11: this is pretty much useless now
        return new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(), quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), quad.tintIndex(), quad.direction(), quad.sprite(), quad.shade(), quad.lightEmission(), quad.bakedNormals(), quad.bakedColors(), quad.hasAmbientOcclusion());
    }

    private static int findOffset(VertexFormatElement element) {
        if (DefaultVertexFormat.BLOCK.contains(element)) {
            // Divide by 4 because we want the int offset
            return DefaultVertexFormat.BLOCK.getOffset(element) / 4;
        }
        return -1;
    }
}
