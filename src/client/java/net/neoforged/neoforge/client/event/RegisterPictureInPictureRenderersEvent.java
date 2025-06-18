/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.List;
import java.util.function.Function;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event to register custom {@link PictureInPictureRenderer}s for specialized rendering in UIs.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public final class RegisterPictureInPictureRenderersEvent extends Event implements IModBusEvent {
    private final List<PictureInPictureRenderer<?>> renderers;
    private final MultiBufferSource.BufferSource bufferSource;

    @ApiStatus.Internal
    public RegisterPictureInPictureRenderersEvent(List<PictureInPictureRenderer<?>> renderers, MultiBufferSource.BufferSource bufferSource) {
        this.renderers = renderers;
        this.bufferSource = bufferSource;
    }

    /**
     * Register a custom {@link PictureInPictureRenderer}
     *
     * @param factory A function to construct a PiP renderer
     */
    public void register(Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<?>> factory) {
        this.renderers.add(factory.apply(this.bufferSource));
    }
}
