/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.oit.OitStage;
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
    /// Prepare the geometry and other buffers to be rendered in [#renderSnowAndRain(LevelRenderState, WeatherRenderState, Vec3, RenderPass)]
    /// or [#renderSnowAndRainOit(LevelRenderState, WeatherRenderState, Vec3, OitStage, RenderPass)].
    ///
    /// @see WeatherEffectRenderer#prepare(Vec3, WeatherRenderState)
    default void prepare(LevelRenderState levelRenderState, WeatherRenderState weatherRenderState, Vec3 camPos) {
    }

    /// Renders the snow and rain effects of this dimension.
    ///
    /// @return true to prevent vanilla snow and rain rendering
    ///
    /// @see WeatherEffectRenderer#render(WeatherRenderState, RenderPass)
    default boolean renderSnowAndRain(LevelRenderState levelRenderState, WeatherRenderState weatherRenderState, Vec3 camPos, RenderPass renderPass) {
        return false;
    }

    /// Renders the snow and rain effects of this dimension with order-independent translucency.
    ///
    /// @return true to prevent vanilla snow and rain rendering
    ///
    /// @see WeatherEffectRenderer#renderOit(OitStage, WeatherRenderState, RenderPass)
    default boolean renderSnowAndRainOit(LevelRenderState levelRenderState, WeatherRenderState weatherRenderState, Vec3 camPos, OitStage stage, RenderPass renderPass) {
        return false;
    }

    /// Ticks the rain of this dimension.
    ///
    /// @return true to prevent vanilla rain ticking
    default boolean tickRain(ClientLevel level, long ticks, Camera camera) {
        return false;
    }
}
