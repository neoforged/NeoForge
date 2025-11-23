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
import net.neoforged.neoforge.client.CustomCloudsRenderer;
import net.neoforged.neoforge.client.CustomSkyboxRenderer;
import net.neoforged.neoforge.client.CustomWeatherEffectRenderer;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom environmental effect renderers, such as {@link CustomCloudsRenderer},
 * {@link CustomSkyboxRenderer} or {@link CustomWeatherEffectRenderer}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterCustomEnvironmentEffectRendererEvent extends Event implements IModBusEvent {
    private final Map<Identifier, CustomCloudsRenderer> cloudRenderers;
    private final Map<Identifier, CustomSkyboxRenderer> skyboxRenderers;
    private final Map<Identifier, CustomWeatherEffectRenderer> weatherEffectsRenderers;

    @ApiStatus.Internal
    public RegisterCustomEnvironmentEffectRendererEvent(Map<Identifier, CustomCloudsRenderer> cloudRenderers,
            Map<Identifier, CustomSkyboxRenderer> skyboxRenderers,
            Map<Identifier, CustomWeatherEffectRenderer> weatherEffectsRenderers) {
        this.cloudRenderers = cloudRenderers;
        this.skyboxRenderers = skyboxRenderers;
        this.weatherEffectsRenderers = weatherEffectsRenderers;
    }

    /**
     * Registers the renderer for a given custom clouds type.
     *
     * @see net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes#CUSTOM_CLOUDS
     */
    public void registerCloudRenderer(Identifier id, CustomCloudsRenderer effects) {
        if (NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_CLOUDS.equals(id)) {
            throw new IllegalArgumentException("You cannot register a renderer for the default clouds");
        }
        this.cloudRenderers.put(id, effects);
    }

    /**
     * Registers the renderer for a given custom skybox type.
     *
     * @see NeoForgeEnvironmentAttributes#CUSTOM_SKYBOX
     */
    public void registerSkyboxRenderer(Identifier id, CustomSkyboxRenderer effects) {
        if (NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_SKYBOX.equals(id)) {
            throw new IllegalArgumentException("You cannot register a renderer for the default skybox");
        }
        this.skyboxRenderers.put(id, effects);
    }

    /**
     * Registers the renderer for a given custom weather effects type.
     *
     * @see NeoForgeEnvironmentAttributes#CUSTOM_WEATHER_EFFECTS
     */
    public void registerWeatherEffectRenderer(Identifier id, CustomWeatherEffectRenderer effects) {
        if (NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_WEATHER_EFFECTS.equals(id)) {
            throw new IllegalArgumentException("You cannot register a renderer for the default weather effects");
        }
        this.weatherEffectsRenderers.put(id, effects);
    }
}
