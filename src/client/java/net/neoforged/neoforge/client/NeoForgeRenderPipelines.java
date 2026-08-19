/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.common.NeoForgeMod;

@EventBusSubscriber(value = Dist.CLIENT, modid = NeoForgeMod.MOD_ID)
public final class NeoForgeRenderPipelines {
    private static final RenderPipeline.Snippet ITEM_UNLIT_SNIPPET = RenderPipeline.builder(RenderPipelines.ITEM_SNIPPET)
            .withVertexShader(Identifier.parse("neoforge:core/item_unlit"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .buildSnippet();
    private static final RenderPipeline.Snippet OIT_ITEM_UNLIT_SNIPPET = RenderPipeline.builder(RenderPipelines.OIT_ITEM_SNIPPET)
            .withVertexShader(Identifier.parse("neoforge:core/item_unlit"))
            .buildSnippet();

    /// Duplicate of [RenderPipelines#ITEM_CUTOUT] with directional shading and lighting disabled
    public static final RenderPipeline ITEM_CUTOUT_UNLIT = RenderPipeline.builder(ITEM_UNLIT_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/item_cutout_unlit"))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .build();
    /// Duplicate of [RenderPipelines#ITEM_CUTOUT_GLINT] with directional shading and lighting disabled
    public static final RenderPipeline ITEM_CUTOUT_UNLIT_GLINT = RenderPipeline.builder(ITEM_UNLIT_SNIPPET, RenderPipelines.GLINT_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/item_cutout_unlit_glint"))
            .withColorTargetState(ColorTargetState.DEFAULT)
            .build();
    /// Duplicate of [RenderPipelines#ITEM_CUTOUT_GLINT_SPECIAL] with directional shading and lighting disabled
    public static final RenderPipeline ITEM_CUTOUT_UNLIT_GLINT_SPECIAL = RenderPipeline.builder(ITEM_UNLIT_SNIPPET, RenderPipelines.GLINT_SPECIAL_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/item_cutout_unlit_glint_special"))
            .withVertexBinding(0, DefaultVertexFormat.ENTITY_GLINT_SPECIAL)
            .withColorTargetState(ColorTargetState.DEFAULT)
            .build();
    /// Duplicate of [RenderPipelines#ITEM_TRANSLUCENT] with directional shading and lighting disabled
    public static final RenderPipeline ITEM_TRANSLUCENT_UNLIT = RenderPipeline.builder(ITEM_UNLIT_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/item_translucent_unlit"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();
    /// Duplicate of [RenderPipelines#OIT_ITEM] with directional shading and lighting disabled
    public static final OitPipelineSet OIT_ITEM_UNLIT = OitPipelineSet.builder(Identifier.parse("neoforge:item_unlit"), RenderPipeline.builder(OIT_ITEM_UNLIT_SNIPPET))
            .withAccumulateModifier(accumulate -> accumulate.withBindGroupLayout(BindGroupLayouts.SAMPLER1).withBindGroupLayout(BindGroupLayouts.SAMPLER2))
            .build();
    /// Duplicate of [RenderPipelines#ITEM_TRANSLUCENT_GLINT] with directional shading and lighting disabled
    public static final RenderPipeline ITEM_TRANSLUCENT_UNLIT_GLINT = RenderPipeline.builder(ITEM_UNLIT_SNIPPET, RenderPipelines.GLINT_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/item_translucent_unlit_glint"))
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();
    /// Duplicate of [RenderPipelines#OIT_ITEM_GLINT] with directional shading and lighting disabled
    public static final OitPipelineSet OIT_ITEM_UNLIT_GLINT = OitPipelineSet.builder(Identifier.parse("neoforge:item_unlit_glint"), RenderPipeline.builder(OIT_ITEM_UNLIT_SNIPPET))
            .withAccumulateModifier(
                    accumulate -> accumulate.withSnippet(RenderPipelines.GLINT_SNIPPET)
                            .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER2)
            )
            .build();
    /// Duplicate of [RenderPipelines#ITEM_TRANSLUCENT_GLINT_SPECIAL] with directional shading and lighting disabled
    public static final RenderPipeline ITEM_TRANSLUCENT_UNLIT_GLINT_SPECIAL = RenderPipeline.builder(ITEM_UNLIT_SNIPPET, RenderPipelines.GLINT_SPECIAL_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/item_translucent_unlit_glint_special"))
            .withVertexBinding(0, DefaultVertexFormat.ENTITY_GLINT_SPECIAL)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .build();
    /// Duplicate of [RenderPipelines#OIT_ITEM_GLINT_SPECIAL] with directional shading and lighting disabled
    public static final OitPipelineSet OIT_ITEM_UNLIT_GLINT_SPECIAL = OitPipelineSet.builder(Identifier.parse("neoforge:item_unlit_glint_special"), RenderPipeline.builder(OIT_ITEM_UNLIT_SNIPPET).withVertexBinding(0, DefaultVertexFormat.ENTITY_GLINT_SPECIAL))
            .withAccumulateModifier(
                    accumulate -> accumulate.withSnippet(RenderPipelines.GLINT_SPECIAL_SNIPPET)
                            .withBindGroupLayout(BindGroupLayouts.GLOBALS)
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER2)
            )
            .build();
    /// Duplicate of [RenderPipelines#ENTITY_TRANSLUCENT] with directional shading and lighting disabled
    public static final RenderPipeline ENTITY_UNLIT_TRANSLUCENT = RenderPipeline.builder(RenderPipelines.ENTITY_SNIPPET)
            .withLocation(Identifier.parse("neoforge:pipeline/entity_unlit_translucent"))
            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
            .withShaderDefine("NO_CARDINAL_LIGHTING")
            .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
            .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
            .withCull(false)
            .build();
    public static final OitPipelineSet OIT_ENTITY_UNLIT = OitPipelineSet.builder(Identifier.parse("neoforge:entity_unlit"), RenderPipeline.builder(RenderPipelines.OIT_ENTITY_SNIPPET).withCull(false))
            .withAccumulateModifier(
                    accumulate -> accumulate.withShaderDefine("NO_CARDINAL_LIGHTING")
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER1)
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER2)
            )
            .build();

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(ITEM_CUTOUT_UNLIT);
        event.registerPipeline(ITEM_CUTOUT_UNLIT_GLINT);
        event.registerPipeline(ITEM_CUTOUT_UNLIT_GLINT_SPECIAL);
        event.registerPipeline(ITEM_TRANSLUCENT_UNLIT);
        event.registerOitPipelineSet(OIT_ITEM_UNLIT);
        event.registerPipeline(ITEM_TRANSLUCENT_UNLIT_GLINT);
        event.registerOitPipelineSet(OIT_ITEM_UNLIT_GLINT);
        event.registerPipeline(ITEM_TRANSLUCENT_UNLIT_GLINT_SPECIAL);
        event.registerOitPipelineSet(OIT_ITEM_UNLIT_GLINT_SPECIAL);
        event.registerPipeline(ENTITY_UNLIT_TRANSLUCENT);
        event.registerOitPipelineSet(OIT_ENTITY_UNLIT);
    }

    private NeoForgeRenderPipelines() {}
}
