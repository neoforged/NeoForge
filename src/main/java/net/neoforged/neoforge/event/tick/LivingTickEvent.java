/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.tick;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Base class of the two player tick events.
 * 
 * @see Pre
 * @see Post
 */
public abstract class LivingTickEvent extends LivingEvent {
    @ApiStatus.Internal
    public LivingTickEvent(LivingEntity entity) {
        super(entity);
    }

    /**
     * {@link LivingTickEvent.Pre} is fired once per game tick, per entity, before the entity performs work for the current tick.
     * <p>
     * This event fires on both the logical server and logical client.
     */
    public static class Pre extends LivingTickEvent implements ICancellableEvent {
        public Pre(LivingEntity entity) {
            super(entity);
        }

        /**
         * Cancels this event, preventing the current tick from being executed for the entity.
         * <p>
         * Additionally, if this event is canceled, then {@link LivingTickEvent.Post} will not be fired for the current tick.
         */
        @Override
        public void setCanceled(boolean canceled) {
            ICancellableEvent.super.setCanceled(canceled);
        }
    }

    /**
     * {@link LivingTickEvent.Post} is fired once per game tick, per entity, after the entity performs work for the current tick.
     * <p>
     * If {@link LivingTickEvent.Pre} was canceled for the current tick, this event will not fire.
     * <p>
     * This event fires on both the logical server and logical client.
     */
    public static class Post extends LivingTickEvent {
        public Post(LivingEntity entity) {
            super(entity);
        }
    }
}
