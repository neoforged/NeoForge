/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.map;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.fml.ModLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class MapDecorationRendererManager {
    private static final Map<MapDecorationType, IMapDecorationRenderer> RENDERERS = new IdentityHashMap<>();
    private static boolean initialized = false;

    private MapDecorationRendererManager() {}

    public static boolean render(
            MapDecoration decoration,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            MapItemSavedData mapData,
            MapDecorationTextureManager decorationTextures,
            boolean inItemFrame,
            int packedLight,
            int index) {
        IMapDecorationRenderer decorationRenderer = RENDERERS.get(decoration.type().value());
        if (decorationRenderer != null) {
            return decorationRenderer.render(decoration, poseStack, bufferSource, mapData, decorationTextures, inItemFrame, packedLight, index);
        }
        return false;
    }

    public static void init() {
        if (initialized) {
            throw new IllegalStateException("Duplicate initialization of MapDecorationRendererManager");
        }

        initialized = true;
        ModLoader.postEvent(new RegisterMapDecorationRenderersEvent(RENDERERS));
    }
}
