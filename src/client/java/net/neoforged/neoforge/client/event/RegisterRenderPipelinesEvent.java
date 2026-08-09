/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.oit.OitPipelineSet;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired to allow mods to register custom {@linkplain RenderPipeline pipelines}.
 * This event is fired after the default Minecraft pipelines have been registered.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.</p>
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterRenderPipelinesEvent extends Event implements IModBusEvent {
    private final Registrar registrar;

    @ApiStatus.Internal
    public RegisterRenderPipelinesEvent(Registrar registrar) {
        this.registrar = registrar;
    }

    /// Registers a required [RenderPipeline].
    ///
    /// @param pipeline a render pipeline
    public void registerPipeline(RenderPipeline pipeline) {
        registrar.register(pipeline, false);
    }

    /// Registers an optional [RenderPipeline].
    ///
    /// @param pipeline a render pipeline
    public void registerOptionalPipeline(RenderPipeline pipeline) {
        registrar.register(pipeline, false);
    }

    /// Registers an OIT [RenderPipeline] set.
    ///
    /// @param pipelineSet an OIT pipeline set
    public void registerOitPipelineSet(OitPipelineSet pipelineSet) {
        registrar.register(pipelineSet.depthBoundsPipeline(), false);
        registrar.register(pipelineSet.transmittancePipeline(), false);
        registrar.register(pipelineSet.accumulatePipeline(), false);
    }

    @ApiStatus.Internal
    @FunctionalInterface
    public interface Registrar {
        void register(RenderPipeline pipeline, boolean optional);
    }
}
