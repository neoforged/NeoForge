/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.gui.map;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class MapDecorationRendererManager {
    private static final Map<MapDecorationType, IMapDecorationRenderer> RENDERERS = new IdentityHashMap<>();

    private MapDecorationRendererManager() {}

    public static boolean render(
            MapRenderState.MapDecorationRenderState decorationRenderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            MapRenderState mapRenderState,
            TextureAtlas decorationSprites,
            boolean inItemFrame,
            int packedLight,
            int index) {
        IMapDecorationRenderer decorationRenderer = RENDERERS.get(decorationRenderState.type.value());
        if (decorationRenderer != null) {
            return decorationRenderer.render(decorationRenderState, poseStack, submitNodeCollector, mapRenderState, decorationSprites, inItemFrame, packedLight, index);
        }
        return false;
    }

    public static void updateMapping(Registry<IMapDecorationRenderer> registry) {
        RENDERERS.clear();
        registry.listElements().forEach(holder -> {
            MapDecorationType decorationType = BuiltInRegistries.MAP_DECORATION_TYPE.getValue(holder.key().identifier());
            if (decorationType != null) {
                RENDERERS.put(decorationType, holder.value());
            }
        });
    }
}
