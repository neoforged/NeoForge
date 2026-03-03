/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;

/**
 * A custom renderer for snow and rain that can be registered using {@link RegisterCustomEnvironmentEffectRendererEvent#registerWeatherEffectRenderer}
 * and used with {@link net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes#CUSTOM_WEATHER_EFFECTS}.
 * <p>
 * Custom render state needed for the various render methods must be extracted via {@link ExtractLevelRenderStateEvent}
 * and stored in the provided {@link LevelRenderState}.
 *
 * @see RegisterCustomEnvironmentEffectRendererEvent#registerWeatherEffectRenderer
 * @see net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes#CUSTOM_WEATHER_EFFECTS
 */
public interface CustomWeatherEffectRenderer {
    /**
     * Renders the snow and rain effects of this dimension.
     *
     * @return true to prevent vanilla snow and rain rendering
     */
    default boolean renderSnowAndRain(LevelRenderState levelRenderState, WeatherRenderState weatherRenderState, MultiBufferSource bufferSource, Vec3 camPos) {
        return false;
    }

    /**
     * Ticks the rain of this dimension.
     *
     * @return true to prevent vanilla rain ticking
     */
    default boolean tickRain(ClientLevel level, int ticks, Camera camera) {
        return false;
    }
}
