/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;

/**
 * A rendering callback invoked when a block outline is about to be rendered.
 * <p>
 * Implementations of this class must not capture the level provided in {@link ExtractBlockOutlineRenderStateEvent},
 * instead the necessary data must be extracted in the event handler and only the extracted data may be captured by
 * the custom renderer implementation.
 */
public interface CustomBlockOutlineRenderer {
    /**
     * Called when the block outline described by the provided {@link BlockOutlineRenderState} is about to be rendered.
     * 
     * @return {@code true} to suppress vanilla outline rendering
     */
    boolean render(BlockOutlineRenderState renderState, SubmitNodeCollector submitNodeCollector, PoseStack poseStack, LevelRenderState levelRenderState);
}
