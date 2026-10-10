/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.resources.model.sprite.SpriteGetter;

public interface CustomBlockScreenEffectRenderer {
    /// No-op renderer which renders nothing and prevents vanilla rendering
    CustomBlockScreenEffectRenderer NO_OP = (_, _, _, _, _, _, _) -> true;

    /// Render the screen effect overlay of a block.
    ///
    /// @param playerRenderState   The render state of the player which the overlay applies to
    /// @param cameraRenderState   The camera render state of the player which the overlay applies to
    /// @param submitNodeCollector The collector to submit the overlay quads to
    /// @param poseStack           The pose stack to use for transformations of the overlay
    /// @param sprites             Access to sprites to use for rendering
    /// @param partialTicks        The "world" partial ticks
    /// @param hideGui             Whether the HUD is hidden
    ///
    /// @return true to prevent vanilla rendering
    boolean submit(
            PlayerRenderState playerRenderState,
            CameraRenderState cameraRenderState,
            SubmitNodeCollector submitNodeCollector,
            PoseStack poseStack,
            SpriteGetter sprites,
            float partialTicks,
            boolean hideGui);
}
