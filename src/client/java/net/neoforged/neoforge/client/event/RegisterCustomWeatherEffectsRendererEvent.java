/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.CustomWeatherEffectRenderer;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom {@link CustomWeatherEffectRenderer}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterCustomWeatherEffectsRendererEvent extends Event implements IModBusEvent {
    private final Map<Identifier, CustomWeatherEffectRenderer> renderers;

    @ApiStatus.Internal
    public RegisterCustomWeatherEffectsRendererEvent(Map<Identifier, CustomWeatherEffectRenderer> renderers) {
        this.renderers = renderers;
    }

    /**
     * Registers the renderer for a given custom weather effects type.
     *
     * @see NeoForgeEnvironmentAttributes#CUSTOM_WEATHER_EFFECTS
     */
    public void register(Identifier id, CustomWeatherEffectRenderer effects) {
        if (NeoForgeEnvironmentAttributes.NO_CUSTOM_WEATHER_EFFECTS.equals(id)) {
            throw new IllegalArgumentException("You cannot register a renderer for the default weather effects");
        }
        this.renderers.put(id, effects);
    }
}
