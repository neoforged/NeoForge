/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.common.NeoForgeMod;

@EventBusSubscriber(value = Dist.CLIENT, modid = NeoForgeMod.MOD_ID)
public final class NeoForgeRenderPipelines {
    // Duplicate of RenderPipelines.ITEM_CUTOUT with directional shading and lighting disabled
    public static final RenderPipeline ITEM_CUTOUT_UNLIT = RenderPipeline.builder(RenderPipelines.ITEM_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/item_cutout_unlit"))
            .withVertexShader(Identifier.parse("neoforge:core/item_unlit"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .build();
    // Duplicate of RenderPipelines.ITEM_TRANSLUCENT with directional shading and lighting disabled
    public static final RenderPipeline ITEM_TRANSLUCENT_UNLIT = RenderPipeline.builder(RenderPipelines.ITEM_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/item_translucent_unlit"))
            .withVertexShader(Identifier.parse("neoforge:core/item_unlit"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();
    // Duplicate of RenderPipelines.ENTITY_TRANSLUCENT with directional shading and lighting disabled
    public static final RenderPipeline ENTITY_UNLIT_TRANSLUCENT = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/entity_unlit_translucent"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withSampler("Sampler1")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build();
    // Duplicate of RenderPipelines.ENTITY_TRANSLUCENT with backface culling enabled
    public static final RenderPipeline ENTITY_TRANSLUCENT_CULL = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/entity_translucent_cull"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withSampler("Sampler1")
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();
    // Duplicate of RenderPipelines.ENTITY_SMOOTH_CUTOUT with backface culling enabled
    public static final RenderPipeline ENTITY_SMOOTH_CUTOUT_CULL = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/entity_smooth_cutout_cull"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withSampler("Sampler1")
            .build();

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(ITEM_CUTOUT_UNLIT);
        event.registerPipeline(ITEM_TRANSLUCENT_UNLIT);
        event.registerPipeline(ENTITY_UNLIT_TRANSLUCENT);
        event.registerPipeline(ENTITY_TRANSLUCENT_CULL);
        event.registerPipeline(ENTITY_SMOOTH_CUTOUT_CULL);
    }

    private NeoForgeRenderPipelines() {}
}
