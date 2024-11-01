/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class RenderStateExtensions {
    private RenderStateExtensions() {}

    private static final Map<Class<?>, RenderStateExtender<?, ?>> EXTENSIONS = new Object2ObjectOpenHashMap<>();

    @Nullable
    static <E extends Entity, S extends EntityRenderState> RenderStateExtender<E, S> getMergedExtension(Class<E> entityClass, Class<S> renderState) {
        return (RenderStateExtender<E, S>) EXTENSIONS.get(renderState);
    }

    @ApiStatus.Internal
    public static <E extends Entity, S extends EntityRenderState> void registerExtender(Class<E> entityClass, Class<S> renderState, RenderStateExtender<E, S> extender) {
        EXTENSIONS.merge(renderState, extender, (renderStateExtender, renderStateExtender2) -> {
            var extender1 = (RenderStateExtender<E, S>) renderStateExtender;
            var extender2 = (RenderStateExtender<E, S>) renderStateExtender2;
            return (RenderStateExtender<?, ?>) extender1.andThen(extender2);
        });
    }

    public interface RenderStateExtender<E extends Entity, S extends EntityRenderState> extends BiConsumer<E, S> {}
}
