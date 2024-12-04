/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.map;

import java.util.Locale;
import java.util.Map;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom {@linkplain IMapDecorationRenderer decoration renderers} for {@link MapDecoration}s
 * which require more dynamic rendering than a single texture on the map decoration atlas allows.
 *
 * <p>
 * This event is not {@linkplain ICancellableEvent cancellable}
 *
 * <p>
 * This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public final class RegisterMapDecorationRenderersEvent extends Event implements IModBusEvent {
    private final Map<MapDecorationType, IMapDecorationRenderer> renderers;

    @ApiStatus.Internal
    public RegisterMapDecorationRenderersEvent(Map<MapDecorationType, IMapDecorationRenderer> renderers) {
        this.renderers = renderers;
    }

    /**
     * Registers a decoration renderer for the given decoration type
     *
     * @param type     The {@link MapDecorationType} the renderer is used for
     * @param renderer The {@link IMapDecorationRenderer} to render the decoration type with
     */
    public void register(MapDecorationType type, IMapDecorationRenderer renderer) {
        IMapDecorationRenderer oldRenderer = renderers.put(type, renderer);
        if (oldRenderer != null) {
            throw new IllegalStateException(String.format(
                    Locale.ROOT,
                    "Duplicate renderer registration for %s (old: %s, new: %s)",
                    type,
                    oldRenderer,
                    renderer));
        }
    }
}
