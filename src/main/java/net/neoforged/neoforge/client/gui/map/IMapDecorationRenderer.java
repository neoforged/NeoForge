/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.map;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.world.level.saveddata.maps.MapDecoration;

/**
 * Interface for custom {@link MapDecoration} renderers
 */
public interface IMapDecorationRenderer {
    /**
     * Render the given {@link MapDecoration} on the map. If this method returns true, the vanilla rendering will be
     * canceled. Otherwise, it will render above whatever is rendered in this method, if anything
     *
     * @param decorationRenderState The state decoration to be rendered
     * @param poseStack             The {@link PoseStack} to render the decoration with
     * @param bufferSource          The {@link MultiBufferSource} to render the decoration with
     * @param mapRenderState        The state of the map being rendered
     * @param decorationTextures    The manager holding map decoration sprites
     * @param inItemFrame           Whether the map is being rendered in an item frame
     * @param packedLight           The packed light value
     * @param index                 The z index of the decoration being rendered
     * @return true to cancel vanilla rendering
     */
    boolean render(
            MapRenderState.MapDecorationRenderState decorationRenderState,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            MapRenderState mapRenderState,
            MapDecorationTextureManager decorationTextures,
            boolean inItemFrame,
            int packedLight,
            int index);
}
