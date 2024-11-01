/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class UpdateRenderStateEvent<E extends Entity, S extends EntityRenderState> extends Event {
    private final E entity;
    private final S renderState;
    private final ContextMap.Builder extensionBuilder;
    final ContextKeySet.Builder validatorBuilder;

    @ApiStatus.Internal
    public UpdateRenderStateEvent(E entity, S renderState) {
        this.entity = entity;
        this.renderState = renderState;
        this.extensionBuilder = new ContextMap.Builder();
        this.validatorBuilder = new ContextKeySet.Builder();
    }

    public E getEntity() {
        return entity;
    }

    public S getCurrentState() {
        return renderState;
    }

    public <T> void withRequiredState(ContextKey<T> key, T object) {
        validatorBuilder.required(key);
        extensionBuilder.withParameter(key, object);
    }

    public <T> void withOptionalState(ContextKey<T> key, @Nullable T object) {
        validatorBuilder.optional(key);
        extensionBuilder.withOptionalParameter(key, object);
    }

    @ApiStatus.Internal
    public ContextMap buildMap() {
        return extensionBuilder.create(validatorBuilder.build());
    }
}
