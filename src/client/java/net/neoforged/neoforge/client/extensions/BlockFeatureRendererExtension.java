/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import java.util.List;
import java.util.function.Predicate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public interface BlockFeatureRendererExtension {
    default void renderExtendedBlockModelSubmits(
            List<SubmitNodeStorage.ExtendedBlockModelSubmit> submits,
            MultiBufferSource.BufferSource bufferSource,
            OutlineBufferSource outlineBufferSource,
            boolean translucent) {
        if (submits.isEmpty()) return;

        Predicate<ChunkSectionLayer> chunkLayerFilter = translucent ? ChunkSectionLayer::sortOnUpload : layer -> !layer.sortOnUpload();
        for (SubmitNodeStorage.ExtendedBlockModelSubmit submit : submits) {
            ModelBlockRenderer.renderModel(
                    submit.pose(),
                    bufferSource,
                    submit.model(),
                    submit.r(),
                    submit.g(),
                    submit.b(),
                    submit.lightCoords(),
                    submit.overlayCoords(),
                    net.minecraft.world.level.EmptyBlockAndTintGetter.INSTANCE,
                    net.minecraft.core.BlockPos.ZERO,
                    submit.state(),
                    chunkLayerFilter);
            if (submit.outlineColor() != 0) {
                outlineBufferSource.setColor(submit.outlineColor());
                ModelBlockRenderer.renderModel(
                        submit.pose(),
                        outlineBufferSource,
                        submit.model(),
                        submit.r(),
                        submit.g(),
                        submit.b(),
                        submit.lightCoords(),
                        submit.overlayCoords(),
                        net.minecraft.world.level.EmptyBlockAndTintGetter.INSTANCE,
                        net.minecraft.core.BlockPos.ZERO,
                        submit.state(),
                        chunkLayerFilter);
            }
        }
    }
}
