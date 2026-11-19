/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.pipeline;

import com.mojang.blaze3d.pipeline.PipelineCache;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPassLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.renderer.RenderPassLayouts;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.pipeline.variant.RenderPipelineVariant;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.Internal
public final class RenderPipelineManager {
    private static final Set<PipelineCache.Key> STATIC_PIPELINES = new HashSet<>();
    private static final List<RegisterRenderPipelinesEvent.DynamicPipelineFactory> DYNAMIC_PIPELINES = new ArrayList<>();
    private static final OitStage[] OIT_STAGES = OitStage.values();

    public static Stream<PipelineCache.Key> getStaticPipelines() {
        return STATIC_PIPELINES.stream();
    }

    public static void appendDynamicPipelines(List<PipelineCache.Key> dynamicPipelines, ShaderManager.PipelineCompilationContext context) {
        RegisterRenderPipelinesEvent.DynamicPipelineRegistrar registrar = new DynamicPipelineRegistrarImpl(dynamicPipelines, context);
        for (RegisterRenderPipelinesEvent.DynamicPipelineFactory pipeline : DYNAMIC_PIPELINES) {
            pipeline.createPipelines(registrar, context);
        }
    }

    public static void init() {
        ModLoader.postEvent(new RegisterRenderPipelinesEvent(new RegisterRenderPipelinesEvent.Registrar() {
            @Override
            public void registerStatic(RenderPipeline pipeline, List<RenderPassLayout.Subpass> subpasses) {
                for (RenderPassLayout.Subpass subpass : subpasses) {
                    STATIC_PIPELINES.add(new PipelineCache.Key(pipeline, subpass));
                }
            }

            @Override
            public void registerDynamic(RegisterRenderPipelinesEvent.DynamicPipelineFactory factory) {
                DYNAMIC_PIPELINES.add(factory);
            }
        }));
    }

    private record DynamicPipelineRegistrarImpl(List<PipelineCache.Key> dynamicPipelines, ShaderManager.PipelineCompilationContext context)
            implements RegisterRenderPipelinesEvent.DynamicPipelineRegistrar {
        @Override
        public void register(RenderPipeline pipeline, RenderPassLayout... renderPassLayouts) {
            if (renderPassLayouts.length < 1) {
                throw new IllegalArgumentException("Pipelines require at least one RenderPassLayout");
            }
            for (RenderPassLayout renderPassLayout : renderPassLayouts) {
                register(pipeline, renderPassLayout.subpass(0));
            }
        }

        @Override
        public void register(RenderPipeline pipeline, RenderPassLayout.Subpass... subpasses) {
            if (subpasses.length < 1) {
                throw new IllegalArgumentException("Pipelines require at least one RenderPassLayout.Subpass");
            }
            for (RenderPassLayout.Subpass subpass : subpasses) {
                dynamicPipelines.add(new PipelineCache.Key(pipeline, subpass));
            }
        }

        @Override
        public void registerVariant(RenderPipelineVariant variant, Function<@Nullable OitStage, RenderPipeline> pipelineSupplier) {
            if (variant.supportsOit()) {
                registerOit(pipelineSupplier);
            } else {
                register(pipelineSupplier.apply(null), RenderPassLayouts.COLOR_DEPTH);
            }
        }

        @Override
        public void registerOit(Function<@Nullable OitStage, RenderPipeline> pipelineSupplier) {
            if (context.options().improvedTransparency) {
                for (OitStage oitStage : OIT_STAGES) {
                    register(pipelineSupplier.apply(oitStage), oitStage.subpass());
                }
            }
        }
    }

    private RenderPipelineManager() {}
}
