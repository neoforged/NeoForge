/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

@EventBusSubscriber(value = Dist.CLIENT, modid = NeoForgeVersion.MOD_ID)
public final class NeoForgeRenderPipelines {
    // Duplicate of RenderPipelines.ENTITY_TRANSLUCENT with directional shading and lighting disabled
    public static final RenderPipeline ENTITY_UNLIT_TRANSLUCENT = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(ResourceLocation.parse("neoforge:pipeline/entity_unlit_translucent"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withSampler("Sampler1")
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .build();
    // Duplicate of RenderPipelines.ENTITY_TRANSLUCENT with backface culling enabled
    public static final RenderPipeline ENTITY_TRANSLUCENT_CULL = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(ResourceLocation.parse("neoforge:pipeline/entity_translucent_cull"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withSampler("Sampler1")
            .withBlend(BlendFunction.TRANSLUCENT)
            .build();
    // Duplicate of RenderPipelines.ENTITY_SMOOTH_CUTOUT with backface culling enabled
    public static final RenderPipeline ENTITY_SMOOTH_CUTOUT_CULL = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(ResourceLocation.parse("neoforge:pipeline/entity_smooth_cutout_cull"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withSampler("Sampler1")
            .build();

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(ENTITY_UNLIT_TRANSLUCENT);
        event.registerPipeline(ENTITY_TRANSLUCENT_CULL);
        event.registerPipeline(ENTITY_SMOOTH_CUTOUT_CULL);
    }

    private NeoForgeRenderPipelines() {}
}
