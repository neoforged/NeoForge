/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import org.joml.Matrix4f;

/**
 * A custom cloud renderer that can be registered using {@link RegisterCustomEnvironmentEffectRendererEvent#registerCloudRenderer)}
 * and used with {@link net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes#CUSTOM_CLOUDS}.
 * <p>
 * Custom render state needed for the various render methods must be extracted via {@link ExtractLevelRenderStateEvent}
 * and stored in the provided {@link LevelRenderState}.
 *
 * @see RegisterCustomEnvironmentEffectRendererEvent#registerCloudRenderer
 * @see net.neoforged.neoforge.common.world.NeoForgeEnvironmentAttributes#CUSTOM_CLOUDS
 */
public interface CustomCloudsRenderer {
    /**
     * Renders the clouds of this dimension.
     *
     * @return true to prevent vanilla cloud rendering
     */
    default boolean renderClouds(LevelRenderState levelRenderState, Vec3 camPos, CloudStatus cloudStatus, int cloudColor, float cloudHeight, Matrix4f modelViewMatrix) {
        return false;
    }
}
