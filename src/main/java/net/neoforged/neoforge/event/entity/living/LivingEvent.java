/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import org.jspecify.annotations.Nullable;

/**
 * Base class of the events specific to LivingEntities.
 * 
 * @see LivingEvent.LivingJumpEvent
 * @see LivingEvent.LivingVisibilityEvent
 **/
public abstract class LivingEvent extends EntityEvent {
    private final LivingEntity livingEntity;

    public LivingEvent(LivingEntity entity) {
        super(entity);
        livingEntity = entity;
    }

    @Override
    public LivingEntity getEntity() {
        return livingEntity;
    }

    /**
     * LivingJumpEvent is fired when a LivingEntity jumps.
     * <p>
     * This event is fired whenever a LivingEntity jumps in
     * {@code LivingEntity#jumpFromGround()}, {@code MagmaCube#jumpFromGround()},
     * {@code Slime#jumpFromGround()}, {@code Camel#executeRidersJump()},
     * and {@code AbstractHorse#executeRidersJump()}.
     * <p>
     * This event is fired via the {@link CommonHooks#onLivingJump(LivingEntity)}.
     * <p>
     * <ul><li>{@link #getEntity} contains the entity that caused this event to occur.</li></ul>
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
     **/
    public static class LivingJumpEvent extends LivingEvent {
        public LivingJumpEvent(LivingEntity e) {
            super(e);
        }
    }

    /**
     * LivingVisibilityEvent is fired when an LivingEntity's visibility is queried.
     * <p>
     * This event is fired whenever {@code LivingEntity#getVisibilityPercent()} is called.
     * <p>
     * This event is fired via the {@link CommonHooks#getEntityVisibilityMultiplier(LivingEntity, Entity, double)}.
     * <p>
     * <ul><li>{@link #getEntity} contains the entity that caused this event to occur.</li></ul>
     * <p>
     * This event is not {@linkplain net.neoforged.bus.api.ICancellableEvent cancellable}, and is fired on the
     * {@linkplain NeoForge#EVENT_BUS game event bus}.
     **/
    public static class LivingVisibilityEvent extends LivingEvent {
        private double visibilityModifier;
        @Nullable
        private final Entity lookingEntity;

        public LivingVisibilityEvent(LivingEntity livingEntity, @Nullable Entity lookingEntity, double originalMultiplier) {
            super(livingEntity);
            this.visibilityModifier = originalMultiplier;
            this.lookingEntity = lookingEntity;
        }

        /**
         * @param mod Is multiplied with the current modifier
         */
        public void modifyVisibility(double mod) {
            visibilityModifier *= mod;
        }

        /**
         * @return The current modifier
         */
        public double getVisibilityModifier() {
            return visibilityModifier;
        }

        /**
         * @return The entity trying to see this LivingEntity, if available
         */
        @Nullable
        public Entity getLookingEntity() {
            return lookingEntity;
        }
    }
}
