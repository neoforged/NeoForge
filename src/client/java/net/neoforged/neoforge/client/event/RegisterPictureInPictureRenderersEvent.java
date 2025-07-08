/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.List;
import java.util.function.Function;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.gui.PictureInPictureRendererRegistration;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event to register custom {@link PictureInPictureRenderer}s for specialized rendering in UIs.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public final class RegisterPictureInPictureRenderersEvent extends Event implements IModBusEvent {
    private final List<PictureInPictureRendererRegistration<?>> renderers;

    @ApiStatus.Internal
    public RegisterPictureInPictureRenderersEvent(List<PictureInPictureRendererRegistration<?>> renderers) {
        this.renderers = renderers;
    }

    /**
     * Register a custom {@link PictureInPictureRenderer} factory.
     *
     * @param stateClass The type of state that the renderers constructed by the given factory can handle.
     * @param factory    A function to construct a PiP renderer
     */
    public <T extends PictureInPictureRenderState> void register(Class<T> stateClass,
            Function<MultiBufferSource.BufferSource, PictureInPictureRenderer<T>> factory) {
        this.renderers.add(new PictureInPictureRendererRegistration<>(stateClass, factory));
    }
}
