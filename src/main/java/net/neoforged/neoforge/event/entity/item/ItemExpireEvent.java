/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.item.ItemEntity;

/**
 * Event that is fired when an EntityItem's age has reached its maximum
 * lifespan. Adding extra life time will prevent the EntityItem from being
 * flagged as dead, thus staying it's removal from the world.
 */
public class ItemExpireEvent extends ItemEvent {
    private int extraLife = 0;

    /**
     * Creates a new event for an expiring EntityItem.
     * 
     * @param entityItem The EntityItem being deleted.
     */
    public ItemExpireEvent(ItemEntity entityItem) {
        super(entityItem);
    }

    public int getExtraLife() {
        return extraLife;
    }

    /**
     * Sets the amount of extra life time (in ticks) to give this EntityItem.
     * 
     * @param extraLife The amount of time to be added to this entities lifespan.
     */
    public void setExtraLife(int extraLife) {
        this.extraLife = extraLife;
    }
}
