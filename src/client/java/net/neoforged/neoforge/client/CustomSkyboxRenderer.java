/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.state.SkyRenderState;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import org.joml.Matrix4f;

/**
 * A custom skybox renderer that can be registered using {@link RegisterCustomEnvironmentEffectRendererEvent#registerSkyboxRenderer}
 * and used with {@link net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes#CUSTOM_SKYBOX}.
 * <p>
 * Custom render state needed for the various render methods must be extracted via {@link ExtractLevelRenderStateEvent}
 * and stored in the provided {@link LevelRenderState}.
 *
 * @see RegisterCustomEnvironmentEffectRendererEvent#registerSkyboxRenderer
 * @see net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes#CUSTOM_SKYBOX
 */
public interface CustomSkyboxRenderer {
    /**
     * Renders the sky of this dimension.
     *
     * @return true to prevent vanilla sky rendering
     */
    default boolean renderSky(LevelRenderState levelRenderState, SkyRenderState skyRenderState, Matrix4f modelViewMatrix, Runnable setupFog) {
        return false;
    }
}
