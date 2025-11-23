/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * Manager for custom renderers referred to by {@link net.minecraft.world.attribute.EnvironmentAttribute}.
 */
public final class CustomEnvironmentEffectsRendererManager {
    private static @Nullable Map<Identifier, CustomCloudsRenderer> CUSTOM_CLOUD_RENDERERS;
    private static @Nullable Map<Identifier, CustomSkyboxRenderer> CUSTOM_SKYBOX_RENDERERS;
    private static @Nullable Map<Identifier, CustomWeatherEffectRenderer> CUSTOM_WEATHER_EFFECT_RENDERERS;

    /**
     * Finds the {@link CustomCloudsRenderer} for a given identifier, or null if none is registered.
     */
    public static @Nullable CustomCloudsRenderer getCustomCloudsRenderer(Identifier id) {
        if (NeoForgeEnvironmentAttributes.DEFAULT_CUSTOM_CLOUDS.equals(id)) {
            return null;
        }
        return Objects.requireNonNull(CUSTOM_CLOUD_RENDERERS).get(id);
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
        return Objects.requireNonNull(CUSTOM_SKYBOX_RENDERERS).get(id);
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
        return Objects.requireNonNull(CUSTOM_WEATHER_EFFECT_RENDERERS).get(id);
    }

    /**
     * Finds the {@link CustomWeatherEffectRenderer} to use for the given position in the given level.
     */
    public static @Nullable CustomWeatherEffectRenderer getCustomWeatherEffectRenderer(Level level, Vec3 position) {
        var id = level.environmentAttributes().getValue(NeoForgeEnvironmentAttributes.CUSTOM_WEATHER_EFFECTS, position);
        return getCustomWeatherEffectRenderer(id);
    }

    @ApiStatus.Internal
    public static void init() {
        if (CUSTOM_CLOUD_RENDERERS != null) {
            throw new IllegalStateException("Already initialized.");
        }

        var customCloudRenderers = new HashMap<Identifier, CustomCloudsRenderer>();
        var customSkyboxRenderers = new HashMap<Identifier, CustomSkyboxRenderer>();
        var customWeatherEffectRenderers = new HashMap<Identifier, CustomWeatherEffectRenderer>();
        ModLoader.postEventWrapContainerInModOrder(new RegisterCustomEnvironmentEffectRendererEvent(customCloudRenderers, customSkyboxRenderers, customWeatherEffectRenderers));
        CUSTOM_CLOUD_RENDERERS = Map.copyOf(customCloudRenderers);
        CUSTOM_SKYBOX_RENDERERS = Map.copyOf(customSkyboxRenderers);
        CUSTOM_WEATHER_EFFECT_RENDERERS = Map.copyOf(customWeatherEffectRenderers);
    }

    private CustomEnvironmentEffectsRendererManager() {}
}
