/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.client.registries.NeoForgeClientRegistries;

/**
 * Functional interface for modifying or replacing {@link RenderPipeline}s in arbitrary render paths on the fly.
 * <p>
 * All implementations of this interface must be idempotent and have to return the exact same result for the given
 * input. The result of invoking {@link #apply(RenderPipeline, Identifier)} will be cached.
 *
 * @see NeoForgeClientRegistries#PIPELINE_MODIFIER
 * @see NeoForgeClientRegistries.Keys#PIPELINE_MODIFIER
 * @see RenderSystem#pushPipelineModifier(ResourceKey)
 * @see RenderSystem#popPipelineModifier()
 * @see RenderSystem#renderWithPipelineModifier(ResourceKey, Runnable)
 */
@FunctionalInterface
public interface PipelineModifier {
    /**
     * Apply modifications to the provided {@link RenderPipeline} (see {@link RenderPipeline#toBuilder()}), return
     * an existing {@link RenderPipeline} to replace the provided one or return the provided one to pass.
     * <p>
     * If the provided pipeline is modified, then the returned pipeline must use the provided {@link Identifier}
     * as the pipeline's location in order to ensure traceability of pipeline modifications.
     *
     * @param pipeline The {@link RenderPipeline} to be modified
     * @param name     The name to use for the modified pipeline
     */
    RenderPipeline apply(RenderPipeline pipeline, Identifier name);
}
