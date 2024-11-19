/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.client.event.RegisterRenderStateModifiersEvent;
import org.jetbrains.annotations.ApiStatus;

public final class RenderStateExtensions {
    private RenderStateExtensions() {}

    private static final Map<Class<? extends EntityRenderer<?, ?>>, Collection<BiConsumer<?, ?>>> ENTITY = new Reference2ObjectArrayMap<>();
    private static final Map<Class<? extends EntityRenderer<?, ?>>, Collection<BiConsumer<?, ?>>> ENTITY_CACHE = new Reference2ObjectOpenHashMap<>();

    private static final List<BiConsumer<MapItemSavedData, MapRenderState>> MAP = new ObjectArrayList<>();

    private static final Map<ResourceKey<MapDecorationType>, Collection<RegisterRenderStateModifiersEvent.MapDecorationRenderStateModifier>> MAP_DECORATION = new Reference2ObjectArrayMap<>();

    @SuppressWarnings("unchecked")
    static <E extends Entity, S extends EntityRenderState> Collection<BiConsumer<E, S>> getCachedEntityModifiers(EntityRenderer<E, S> renderer) {
        var modifiers = (Collection<BiConsumer<E, S>>) (Object) ENTITY_CACHE.computeIfAbsent((Class<? extends EntityRenderer<E, S>>) renderer.getClass(), aClass -> {
            var list = new ObjectArrayList<BiConsumer<?, ?>>();
            for (var entry : ENTITY.entrySet()) {
                if (aClass.isAssignableFrom(entry.getKey())) {
                    list.addAll(entry.getValue());
                }
            }
            if (list.isEmpty()) {
                return List.of();
            }
            return list;
        });

        return modifiers;
    }

    static Collection<BiConsumer<MapItemSavedData, MapRenderState>> getMapModifiers() {
        return MAP;
    }

    static Collection<RegisterRenderStateModifiersEvent.MapDecorationRenderStateModifier> getMapDecorationModifiers(ResourceKey<MapDecorationType> mapDecorationTypeKey) {
        return MAP_DECORATION.getOrDefault(mapDecorationTypeKey, List.of());
    }

    @ApiStatus.Internal
    public static <E extends Entity, S extends EntityRenderState> void registerEntity(Class<? extends EntityRenderer<E, S>> baseRenderer, BiConsumer<E, S> modifier) {
        ENTITY.computeIfAbsent(baseRenderer, aClass -> new ObjectArrayList<>()).add(modifier);
    }

    @ApiStatus.Internal
    public static void registerMap(BiConsumer<MapItemSavedData, MapRenderState> modifier) {
        MAP.add(modifier);
    }

    @ApiStatus.Internal
    public static void registerMapDecoration(ResourceKey<MapDecorationType> mapDecorationTypeKey, RegisterRenderStateModifiersEvent.MapDecorationRenderStateModifier modifier) {
        MAP_DECORATION.computeIfAbsent(mapDecorationTypeKey, aClass -> new ObjectArrayList<>()).add(modifier);
    }
}
