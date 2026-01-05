/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event allows mods to register custom {@link DebugRenderer.SimpleDebugRenderer debug renderers}.
 * <p>
 * This event is fired during {@linkplain DebugRenderer#refreshRendererList() debug renderer refreshes}.
 * <p>
 * This event is fired for the mod-specific event bus and on the {@linkplain LogicalSide#CLIENT logical client}
 */
public final class RegisterDebugRenderersEvent extends Event implements IModBusEvent {
    private final Consumer<Function<Minecraft, DebugRenderer.SimpleDebugRenderer>> registrar;

    @ApiStatus.Internal
    public RegisterDebugRenderersEvent(Consumer<Function<Minecraft, DebugRenderer.SimpleDebugRenderer>> registrar) {
        this.registrar = registrar;
    }

    /**
     * Registers the given debug renderer
     *
     * @param factory Factory used to construct the debug renderer
     */
    public void register(Function<Minecraft, DebugRenderer.SimpleDebugRenderer> factory) {
        registrar.accept(factory);
    }

    /**
     * Registers the given debug renderer
     *
     * @param renderer Debug renderer to be registered
     */
    public void register(DebugRenderer.SimpleDebugRenderer renderer) {
        register(client -> renderer);
    }
}
