/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.player;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.extensions.IBlockStateExtension;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * This event is fired when the game checks if a sleeping entity may continue sleeping.
 * <p>
 * It can be used to overwrite the vanilla check, forcing the entity to continue or stop sleeping.
 * <p>
 * This event is only fired on the logical server.
 */
public class SleepingLocationCheckEvent extends LivingEvent {
    protected final boolean hasBed;
    protected boolean mayContinueSleeping;

    /**
     * Fire via {@link EventHooks#canEntityContinueSleeping}
     */
    public SleepingLocationCheckEvent(LivingEntity sleeper, boolean hasBed) {
        super(sleeper);
        this.hasBed = hasBed;
        this.mayContinueSleeping = hasBed;
    }

    /**
     * {@return the sleeping entity}
     */
    @Override
    public LivingEntity getEntity() {
        return super.getEntity();
    }

    /**
     * Returns if the sleeping entity has a bed. The entity is considered to have a bed if:
     * 
     * <ol>
     * <li>The entity has a valid {@link LivingEntity#getSleepingPos() sleeping position}.</li>
     * <li>The block at the sleeping position {@link IBlockStateExtension#isBed is a bed}.</li>
     * </ol>
     * 
     * If the entity has a bed, the default state of {@link #mayContinueSleeping()} is true.
     */
    public boolean hasBed() {
        return this.hasBed;
    }

    /**
     * {@return if the sleeping entity may continue sleeping}
     */
    public boolean mayContinueSleeping() {
        return this.mayContinueSleeping;
    }

    /**
     * Sets if the sleeping entity may continue sleeping
     */
    public void setContinueSleeping(boolean sleeping) {
        this.mayContinueSleeping = sleeping;
    }
}
