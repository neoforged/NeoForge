/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.item.ItemEntity;

/**
 * Event that is fired when an {@link ItemEntity}'s age has reached its maximum
 * lifespan. Adding extra life time will prevent the {@link ItemEntity} from being
 * flagged as dead, thus staying it's removal from the world.
 * <p>
 * This event will only be fired server-side.
 */
public class ItemExpireEvent extends ItemEvent {
    private int extraLife = 0;

    /**
     * Creates a new event for an expiring {@link ItemEntity}.
     * 
     * @param itemEntity The {@link ItemEntity} being deleted.
     */
    public ItemExpireEvent(ItemEntity itemEntity) {
        super(itemEntity);
    }

    /**
     * Query the amount of extra time that will be added.
     * <p>
     * Note that this is the event result. If you need data from the entity, query it directly.
     * {@link ItemEntity#lifespan} is the entities maximum lifespan and also its current age.
     * 
     * @return Extra time to be added in ticks.
     */
    public int getExtraLife() {
        return extraLife;
    }

    /**
     * Sets the amount of extra life time (in ticks) to give this {@link ItemEntity}.
     * <p>
     * <em>Consider using {@link #addExtraLife(int)} in case another mod also adds extra time.</em>
     * 
     * @param extraLife The amount of time to be added to this entities lifespan.
     */
    public void setExtraLife(int extraLife) {
        this.extraLife = extraLife;
    }

    /**
     * Adds to the amount of extra life time (in ticks) to give this {@link ItemEntity}.
     * 
     * @param extraLife The amount of time to be added to this entities lifespan.
     */
    public void addExtraLife(int extraLife) {
        this.extraLife += extraLife;
    }
}
