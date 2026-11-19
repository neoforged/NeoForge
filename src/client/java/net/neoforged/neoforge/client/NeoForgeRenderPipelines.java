/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.DepthStencilState;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.pipeline.RenderPipelines;
import net.minecraft.client.renderer.pipeline.variant.EntityRenderPipelineVariant;
import net.minecraft.client.renderer.pipeline.variant.ItemRenderPipelineVariant;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.common.NeoForgeMod;

@EventBusSubscriber(value = Dist.CLIENT, modid = NeoForgeMod.MOD_ID)
public final class NeoForgeRenderPipelines {
    /// Duplicate of [RenderPipelines#item(RenderPipelines.ItemParameters)] with directional shading and lighting disabled
    public static RenderPipeline itemUnlit(RenderPipelines.ItemParameters params) {
        return RenderPipelines.dynamicPipeline(
                params,
                Identifier.parse("neoforge:item_unlit"),
                parameters -> {
                    RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.MATRICES_FOG_SNIPPET)
                            .withVertexShader(Identifier.parse("neoforge:core/item_unlit"))
                            .withFragmentShader("core/item")
                            .withBindGroupLayout(BindGroupLayouts.LIGHTING)
                            .withBindGroupLayout(BindGroupLayouts.SAMPLER0)
                            .withVertexBinding(0, DefaultVertexFormat.ENTITY)
                            .withShaderDefine("ALPHA_CUTOUT", 0.1F)
                            .withPrimitiveTopology(PrimitiveTopology.QUADS);
                    switch (parameters.variant().glintType()) {
                        case NORMAL -> builder.withSnippet(RenderPipelines.GLINT_SNIPPET);
                        case SPECIAL -> builder.withSnippet(RenderPipelines.GLINT_SNIPPET)
                                .withShaderDefine("GLINT_SPECIAL")
                                .withVertexBinding(0, DefaultVertexFormat.ENTITY_GLINT_SPECIAL);
                    }

                    return RenderPipelines.finishDynamicPipeline(
                            builder,
                            Identifier.parse("neoforge:item_unlit_" + parameters.variant().name()),
                            parameters.variant().supportsOit(),
                            parameters.oitStage(),
                            nonOitBuilder -> nonOitBuilder.withColorTargetState(parameters.variant().nonOitColorTargetState()).withDepthStencilState(DepthStencilState.DEFAULT),
                            null,
                            null,
                            colorOutputBuilder -> colorOutputBuilder.withBindGroupLayout(BindGroupLayouts.SAMPLER1).withBindGroupLayout(BindGroupLayouts.SAMPLER2),
                            null);
                });
    }

    /// Duplicate of [EntityRenderPipelineVariant#TRANSLUCENT] with directional shading and lighting disabled
    public static final EntityRenderPipelineVariant ENTITY_UNLIT_VARIANT = new EntityRenderPipelineVariant(
            "neoforge_translucent_unlit",
            new ColorTargetState(BlendFunction.TRANSLUCENT),
            DepthStencilState.DEFAULT,
            EntityRenderPipelineVariant.Lighting.NONE,
            true,
            true,
            true,
            false,
            false,
            false,
            false,
            false,
            false);

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerDynamicPipelines((registrar, _) -> {
            for (ItemRenderPipelineVariant variant : ItemRenderPipelineVariant.values()) {
                registrar.registerVariant(variant, stage -> itemUnlit(new RenderPipelines.ItemParameters(variant, stage)));
            }

            registrar.registerVariant(ENTITY_UNLIT_VARIANT, stage -> RenderPipelines.entity(new RenderPipelines.EntityParameters(ENTITY_UNLIT_VARIANT, stage)));
        });
    }

    private NeoForgeRenderPipelines() {}
}
