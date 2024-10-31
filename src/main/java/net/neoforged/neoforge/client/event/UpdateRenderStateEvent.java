/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

public class UpdateRenderStateEvent<E extends Entity, S extends EntityRenderState> extends Event {
    private final E entity;
    private final S renderState;

    @ApiStatus.Internal
    public UpdateRenderStateEvent(E entity, S renderState) {
        this.entity = entity;
        this.renderState = renderState;
    }

    public E getEntity() {
        return entity;
    }

    public S getCurrentState() {
        return renderState;
    }
}
