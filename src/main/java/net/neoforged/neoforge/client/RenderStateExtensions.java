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
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;

public final class RenderStateExtensions {
    private RenderStateExtensions() {}

    private static final Map<Class<? extends EntityRenderer<?, ?>>, Collection<BiConsumer<?, ?>>> ENTITY = new Reference2ObjectArrayMap<>();
    private static final Map<Class<? extends EntityRenderer<?, ?>>, Collection<BiConsumer<?, ?>>> ENTITY_CACHE = new Reference2ObjectOpenHashMap<>();

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
    }

    @ApiStatus.Internal
    public static <E extends Entity, S extends EntityRenderState> void registerEntity(Class<? extends EntityRenderer<E, S>> baseRenderer, EntityRenderStateModifier<E, S> modifier) {
        ENTITY_EXTENSIONS.computeIfAbsent(baseRenderer, aClass -> new ObjectArrayList<>()).add(modifier);
    }
}
