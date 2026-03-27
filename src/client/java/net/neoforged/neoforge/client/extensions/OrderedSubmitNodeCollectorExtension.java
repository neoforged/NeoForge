/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;

public interface OrderedSubmitNodeCollectorExtension {
    /// Submit the provided [BlockStateModelPart]s with full support for per-quad render types.
    ///
    /// Primarily intended to be used via [BlockModelRenderState#submitMultiLayer(PoseStack, SubmitNodeCollector, int, int, int)].
    ///
    /// @param poseStack           The transformations to apply to the parts
    /// @param modelParts          The model parts to submit
    /// @param translucent         Whether the parts contain any translucent quads
    /// @param lightCoords         The packed light coordinates to render the parts with
    /// @param overlayCoords       The overlay texture coordinates to render the parts with
    /// @param outlineColor        The outline color to render the parts with, or `0` to render no outline
    default void submitMultiLayerBlockModel(
            PoseStack poseStack,
            List<BlockStateModelPart> modelParts,
            boolean translucent,
            int[] tintLayers,
            int lightCoords,
            int overlayCoords,
            int outlineColor) {}
}
