/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPassLayout;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.pipeline.RenderPipelines;
import net.minecraft.client.renderer.pipeline.variant.RenderPipelineVariant;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// Fired to allow mods to register custom [pipelines][RenderPipeline] for precompilation.
/// This event is fired after the static vanilla pipelines have been registered.
///
/// This event is not [cancellable][ICancellableEvent].
///
/// This event is fired on the mod-specific event bus, only on the [logical client][LogicalSide#CLIENT].
public final class RegisterRenderPipelinesEvent extends Event implements IModBusEvent {
    private final Registrar registrar;

    @ApiStatus.Internal
    public RegisterRenderPipelinesEvent(Registrar registrar) {
        this.registrar = registrar;
    }

    /// Registers a static (i.e. independent of game settings) [RenderPipeline] for precompilation.
    ///
    /// @param pipeline          a render pipeline
    /// @param renderPassLayouts the render pass layout(s) to use with the given pipeline
    public void registerStaticPipeline(RenderPipeline pipeline, RenderPassLayout... renderPassLayouts) {
        if (renderPassLayouts.length < 1) {
            throw new IllegalArgumentException("Pipelines require at least one RenderPassLayout");
        }
        registrar.registerStatic(pipeline, Lists.transform(Arrays.asList(renderPassLayouts), layout -> layout.subpass(0)));
    }

    /// Registers a static (i.e. independent of game settings) [RenderPipeline] for precompilation.
    ///
    /// @param pipeline  a render pipeline
    /// @param subpasses the subpass layout(s) to use with the given pipeline
    public void registerStaticPipeline(RenderPipeline pipeline, RenderPassLayout.Subpass... subpasses) {
        if (subpasses.length < 1) {
            throw new IllegalArgumentException("Pipelines require at least one RenderPassLayout.Subpass");
        }
        registrar.registerStatic(pipeline, Arrays.asList(subpasses));
    }

    /// Registers a factory for dynamic [RenderPipeline]s (i.e. pipelines dependent on game settings such as improved transparency).
    ///
    /// @param factory the pipeline factory
    public void registerDynamicPipelines(DynamicPipelineFactory factory) {
        registrar.registerDynamic(factory);
    }

    @FunctionalInterface
    public interface DynamicPipelineFactory {
        void createPipelines(DynamicPipelineRegistrar registrar, ShaderManager.PipelineCompilationContext context);
    }

    @ApiStatus.NonExtendable
    public interface DynamicPipelineRegistrar {
        /// Register the given pipeline with the given render pass layouts.
        ///
        /// @see RenderPipelines#precompiledDynamic(List, RenderPipeline, RenderPassLayout, RenderPassLayout...)
        void register(RenderPipeline pipeline, RenderPassLayout... renderPassLayouts);

        /// Register the given pipeline with the given subpass layouts.
        ///
        /// @see RenderPipelines#precompiledDynamic(List, RenderPipeline, RenderPassLayout, RenderPassLayout...)
        void register(RenderPipeline pipeline, RenderPassLayout.Subpass... subpasses);

        /// Register the given pipeline variant with OIT-aware pipelines created by the given function.
        ///
        /// @see RenderPipelines#precompiledVariant(List, RenderPipelineVariant, Function, boolean)
        void registerVariant(RenderPipelineVariant variant, Function<@Nullable OitStage, RenderPipeline> pipelineSupplier);

        /// Register OIT-aware pipelines created with the given function.
        ///
        /// @see RenderPipelines#precompiledOitPipelines(List, Function, boolean)
        void registerOit(Function<@Nullable OitStage, RenderPipeline> pipelineSupplier);
    }

    @ApiStatus.Internal
    public interface Registrar {
        void registerStatic(RenderPipeline pipeline, List<RenderPassLayout.Subpass> subpasses);

        void registerDynamic(DynamicPipelineFactory factory);
    }
}
