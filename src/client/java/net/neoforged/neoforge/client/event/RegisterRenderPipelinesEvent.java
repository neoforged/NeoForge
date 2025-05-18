/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.function.Consumer;
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
    private final Consumer<RenderPipeline> registrar;

    @ApiStatus.Internal
    public RegisterRenderPipelinesEvent(Consumer<RenderPipeline> registrar) {
        this.registrar = registrar;
    }

    /**
     * Registers a {@link RenderPipeline}
     *
     * @param pipeline a pipeline
     */
    public void registerPipeline(RenderPipeline pipeline) {
        registrar.accept(pipeline);
    }
}
