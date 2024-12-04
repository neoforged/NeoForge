/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.renderstate;

import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.client.gui.map.IMapDecorationRenderer;

/**
 * Function interface for render state modifiers that target MapDecorations. Useful for adding custom data for rendering
 * in {@link IMapDecorationRenderer}s.
 */
@FunctionalInterface
public interface MapDecorationRenderStateModifier {
    /**
     * Called when the registered {@link MapDecorationType} is added to a {@link MapRenderState}.
     *
     * @param mapItemSavedData         The map SavedData.
     * @param mapRenderState           The render state of the map after the texture has been set and custom data is added.
     * @param mapDecorationRenderState The decoration render state after vanilla has set it up.
     */
    void accept(MapItemSavedData mapItemSavedData, MapRenderState mapRenderState, MapRenderState.MapDecorationRenderState mapDecorationRenderState);
}
