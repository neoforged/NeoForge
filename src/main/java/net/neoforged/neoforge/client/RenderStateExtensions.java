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
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.entity.state.EntityRenderStateModifier;
import org.jetbrains.annotations.ApiStatus;

public final class RenderStateExtensions {
    private RenderStateExtensions() {}

    private static final Map<Class<? extends EntityRenderer<?, ?>>, Collection<EntityRenderStateModifier<?, ?>>> ENTITY_EXTENSIONS = new Reference2ObjectArrayMap<>();

    private static final Map<Class<? extends EntityRenderer<?, ?>>, Collection<EntityRenderStateModifier<?, ?>>> ENTITY_CACHE = Util.make(new Reference2ObjectOpenHashMap<>(), map -> map.defaultReturnValue(List.of()));

    @SuppressWarnings("unchecked")
    static <E extends Entity, S extends EntityRenderState> Collection<EntityRenderStateModifier<E, S>> getCachedEntityModifiers(EntityRenderer<E, S> renderer) {
        return (Collection<EntityRenderStateModifier<E, S>>) (Object) ENTITY_CACHE.computeIfAbsent((Class<? extends EntityRenderer<E, S>>) renderer.getClass(), aClass -> {
            var list = new ObjectArrayList<EntityRenderStateModifier<?, ?>>();
            for (var entry : ENTITY_EXTENSIONS.entrySet()) {
                if (aClass.isInstance(entry.getKey())) {
                    list.addAll(entry.getValue());
                }
            }
            if (list.isEmpty()) {
                return null;
            }
            return list;
        });
    }

    @ApiStatus.Internal
    public static <E extends Entity, S extends EntityRenderState> void registerExtender(Class<? extends EntityRenderer<E, S>> baseRenderer, EntityRenderStateModifier<E, S> modifier) {
        ENTITY_EXTENSIONS.computeIfAbsent(baseRenderer, aClass -> new ObjectArrayList<>()).add(modifier);
    }
}
