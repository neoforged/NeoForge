/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;

public class ItemAllowPickupEvent extends ItemEvent implements ICancellableEvent {
    @ApiStatus.Internal
    public ItemAllowPickupEvent(ItemEntity itemEntity) {
        super(itemEntity);
    }

    /**
     * Cancelling this event will disallow item from being picked up.
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }
}
