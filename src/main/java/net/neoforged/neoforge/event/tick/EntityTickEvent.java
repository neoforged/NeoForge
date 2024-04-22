/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.EntityEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class of the two entity tick events.
 * 
 * @see Pre
 * @see Post
 */
public abstract class EntityTickEvent extends EntityEvent {
    @ApiStatus.Internal
    public EntityTickEvent(Entity entity) {
        super(entity);
    }

    /**
     * {@link EntityTickEvent.Pre} is fired once per game tick, per entity, before the entity performs work for the current tick.
     * <p>
     * This event fires on both the logical server and logical client.
     */
    public static class Pre extends EntityTickEvent implements ICancellableEvent {
        public Pre(Entity entity) {
            super(entity);
        }

        /**
         * Cancels this event, preventing the current tick from being executed for the entity.
         * <p>
         * Additionally, if this event is canceled, then {@link EntityTickEvent.Post} will not be fired for the current tick.
         */
        @Override
        public void setCanceled(boolean canceled) {
            ICancellableEvent.super.setCanceled(canceled);
        }
    }

    /**
     * {@link EntityTickEvent.Post} is fired once per game tick, per entity, after the entity performs work for the current tick.
     * <p>
     * If {@link EntityTickEvent.Pre} was canceled for the current tick, this event will not fire.
     * <p>
     * This event fires on both the logical server and logical client.
     */
    public static class Post extends EntityTickEvent {
        public Post(Entity entity) {
            super(entity);
        }
    }
}
