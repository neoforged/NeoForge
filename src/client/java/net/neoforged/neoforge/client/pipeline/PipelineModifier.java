/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.pipeline;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

/**
 * Functional interface for modifying or replacing {@link RenderPipeline}s in arbitrary render paths on the fly.
 * <p>
 * All implementations of this interface must be idempotent and have to return the exact same result for the given
 * input. The result of invoking {@link #apply(RenderPipeline, ResourceLocation)} will be cached.
 *
 * @see RegisterPipelineModifiersEvent
 * @see RenderSystem#pushPipelineModifier(ResourceKey)
 * @see RenderSystem#popPipelineModifier()
 * @see RenderSystem#renderWithPipelineModifier(ResourceKey, Runnable)
 */
@FunctionalInterface
public interface PipelineModifier {
    ResourceKey<Registry<PipelineModifier>> MODIFIERS_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "pipeline_modifiers"));

    /**
     * Apply modifications to the provided {@link RenderPipeline} (see {@link RenderPipeline#toBuilder()}), return
     * an existing {@link RenderPipeline} to replace the provided one or return the provided one to pass.
     * <p>
     * If the provided pipeline is modified, then the returned pipeline must use the provided {@link ResourceLocation}
     * as the pipeline's location in order to ensure traceability of pipeline modifications.
     *
     * @param pipeline The {@link RenderPipeline} to be modified
     * @param name     The name to use for the modified pipeline
     */
    RenderPipeline apply(RenderPipeline pipeline, ResourceLocation name);
}
