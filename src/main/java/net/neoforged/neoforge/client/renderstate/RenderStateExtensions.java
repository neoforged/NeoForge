/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.renderstate;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.ApiStatus;

public final class RenderStateExtensions {
    private RenderStateExtensions() {}

    private static final Map<Class<?>, Collection<BiConsumer<?, ?>>> ENTITY = new Reference2ObjectArrayMap<>();
    private static final Map<Class<?>, Collection<BiConsumer<?, ?>>> ENTITY_CACHE = new Reference2ObjectOpenHashMap<>();

    private static final List<BiConsumer<MapItemSavedData, MapRenderState>> MAP = new ObjectArrayList<>();

    private static final Map<ResourceKey<MapDecorationType>, Collection<MapDecorationRenderStateModifier>> MAP_DECORATION = new Reference2ObjectArrayMap<>();

    @SuppressWarnings("unchecked")
    @ApiStatus.Internal
    public static <E extends Entity, S extends EntityRenderState> void onUpdateEntityRenderState(EntityRenderer<E, S> renderer, E entity, S renderState) {
        renderState.resetRenderData();
        var modifiers = (Collection<BiConsumer<E, S>>) (Object) ENTITY_CACHE.computeIfAbsent(renderer.getClass(), aClass -> {
            var builder = ImmutableList.<BiConsumer<?, ?>>builder();
            for (var entry : ENTITY.entrySet()) {
                if (entry.getKey().isAssignableFrom(aClass)) {
                    builder.addAll(entry.getValue());
                }
            }
            return builder.build();
        });
        for (BiConsumer<E, S> modifier : modifiers) {
            modifier.accept(entity, renderState);
        }
    }

    @ApiStatus.Internal
    public static void onUpdateMapRenderState(MapItemSavedData mapItemSavedData, MapRenderState renderState) {
        renderState.resetRenderData();
        for (BiConsumer<MapItemSavedData, MapRenderState> modifier : MAP) {
            modifier.accept(mapItemSavedData, renderState);
        }
    }

    @ApiStatus.Internal
    public static MapRenderState.MapDecorationRenderState onUpdateMapDecorationRenderState(Holder<MapDecorationType> mapDecorationTypeHolder, MapItemSavedData mapItemSavedData, MapRenderState mapRenderState, MapRenderState.MapDecorationRenderState mapDecorationRenderState) {
        mapDecorationRenderState.resetRenderData();
        var modifiers = MAP_DECORATION.getOrDefault(mapDecorationTypeHolder.getKey(), List.of());
        for (var modifier : modifiers) {
            modifier.accept(mapItemSavedData, mapRenderState, mapDecorationRenderState);
        }
        return mapDecorationRenderState;
    }

    static void registerEntity(Class<?> baseRenderer, BiConsumer<?, ?> modifier) {
        ENTITY.computeIfAbsent(baseRenderer, aClass -> new ObjectArrayList<>()).add(modifier);
    }

    static void registerMap(BiConsumer<MapItemSavedData, MapRenderState> modifier) {
        MAP.add(modifier);
    }

    static void registerMapDecoration(ResourceKey<MapDecorationType> mapDecorationTypeKey, MapDecorationRenderStateModifier modifier) {
        MAP_DECORATION.computeIfAbsent(mapDecorationTypeKey, aClass -> new ObjectArrayList<>()).add(modifier);
    }
}
