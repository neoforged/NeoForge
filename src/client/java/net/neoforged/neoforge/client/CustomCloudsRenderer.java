/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.renderer.CloudRenderer;
import net.minecraft.client.renderer.oit.OitRenderPassProvider;
import net.minecraft.client.renderer.oit.OitStage;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterCustomEnvironmentEffectRendererEvent;
import org.joml.Matrix4fc;

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
    /// Prepare the geometry and other buffers to be rendered in [#renderClouds(LevelRenderState, CloudStatus, Matrix4fc, RenderPass)] or
    /// [#renderCloudsOit(LevelRenderState, CloudStatus, Matrix4fc, OitStage, GpuTextureView, OitRenderPassProvider.Parameters)].
    ///
    /// @see CloudRenderer#prepare(int, CloudStatus, float, int, Vec3, long, float)
    default void prepare(LevelRenderState levelRenderState, Vec3 camPos, CloudStatus cloudStatus, int cloudColor, float cloudHeight, int cloudRange, Matrix4fc modelViewMatrix) {
    }

    /// Renders the clouds of this dimension.
    ///
    /// @return true to prevent vanilla cloud rendering
    ///
    /// @see CloudRenderer#render(CloudStatus, RenderPass)
    default boolean renderClouds(LevelRenderState levelRenderState, CloudStatus cloudStatus, Matrix4fc modelViewMatrix, RenderPass renderPass) {
        return false;
    }

    /// Renders the clouds of this dimension with order-independent translucency.
    ///
    /// @return true to prevent vanilla cloud rendering
    ///
    /// @see CloudRenderer#renderOit(CloudStatus, OitStage, GpuTextureView, OitRenderPassProvider.Parameters)
    default boolean renderCloudsOit(LevelRenderState levelRenderState, CloudStatus cloudStatus, Matrix4fc modelViewMatrix, OitStage stage, GpuTextureView mainDepthTextureView, OitRenderPassProvider.Parameters params) {
        return false;
    }
}
