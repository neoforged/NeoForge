/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.joml.Matrix4f;

/**
 * Extension interface for {@link DimensionSpecialEffects}.
 */
public interface IDimensionSpecialEffectsExtension {
    private DimensionSpecialEffects self() {
        return (DimensionSpecialEffects) this;
    }

    /**
     * Renders the clouds of this dimension.
     *
     * @return true to prevent vanilla cloud rendering
     */
    default boolean renderClouds(ClientLevel level, int ticks, float partialTick, double camX, double camY, double camZ, Matrix4f modelViewMatrix, Matrix4f projectionMatrix) {
        return false;
    }

    /**
     * Renders the sky of this dimension.
     *
     * @return true to prevent vanilla sky rendering
     */
    default boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, Runnable setupFog) {
        return false;
    }

    /**
     * Renders the snow and rain effects of this dimension.
     *
     * @return true to prevent vanilla snow and rain rendering
     */
    default boolean renderSnowAndRain(ClientLevel level, int ticks, float partialTick, double camX, double camY, double camZ) {
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
