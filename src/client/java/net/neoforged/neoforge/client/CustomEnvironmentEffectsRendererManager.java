/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.registries.NeoForgeClientRegistries;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;
import org.jspecify.annotations.Nullable;

/**
 * Manager for custom renderers referred to by {@link net.minecraft.world.attribute.EnvironmentAttribute}.
 */
public final class CustomEnvironmentEffectsRendererManager {
    /**
     * Finds the {@link CustomCloudsRenderer} for a given identifier, or null if none is registered.
     */
    public static @Nullable CustomCloudsRenderer getCustomCloudsRenderer(Identifier id) {
        if (NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_CLOUDS.equals(id)) {
            return null;
        }
        return NeoForgeClientRegistries.CUSTOM_CLOUDS_RENDERER.getValue(id);
    }

    /**
     * Finds the {@link CustomCloudsRenderer} to use for the given position in the given level.
     */
    public static @Nullable CustomCloudsRenderer getCustomCloudsRenderer(Level level, Vec3 position) {
        var id = level.environmentAttributes().getValue(NeoForgeEnvironmentAttributes.CUSTOM_CLOUDS, position);
        return getCustomCloudsRenderer(id);
    }

    /**
     * Finds the {@link CustomSkyboxRenderer} for a given identifier, or null if none is registered.
     */
    public static @Nullable CustomSkyboxRenderer getCustomSkyboxRenderer(Identifier id) {
        if (NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_SKYBOX.equals(id)) {
            return null;
        }
        return NeoForgeClientRegistries.CUSTOM_SKYBOX_RENDERER.getValue(id);
    }

    /**
     * Finds the {@link CustomSkyboxRenderer} to use for the given position in the given level.
     */
    public static @Nullable CustomSkyboxRenderer getCustomSkyboxRenderer(Level level, Vec3 position) {
        var id = level.environmentAttributes().getValue(NeoForgeEnvironmentAttributes.CUSTOM_SKYBOX, position);
        return getCustomSkyboxRenderer(id);
    }

    /**
     * Finds the {@link CustomWeatherEffectRenderer} for a given identifier, or null if none is registered.
     */
    public static @Nullable CustomWeatherEffectRenderer getCustomWeatherEffectRenderer(Identifier id) {
        if (NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_WEATHER_EFFECTS.equals(id)) {
            return null;
        }
        return NeoForgeClientRegistries.CUSTOM_WEATHER_EFFECT_RENDERER.getValue(id);
    }

    /**
     * Finds the {@link CustomWeatherEffectRenderer} to use for the given position in the given level.
     */
    public static @Nullable CustomWeatherEffectRenderer getCustomWeatherEffectRenderer(Level level, Vec3 position) {
        var id = level.environmentAttributes().getValue(NeoForgeEnvironmentAttributes.CUSTOM_WEATHER_EFFECTS, position);
        return getCustomWeatherEffectRenderer(id);
    }

    private CustomEnvironmentEffectsRendererManager() {}
}
