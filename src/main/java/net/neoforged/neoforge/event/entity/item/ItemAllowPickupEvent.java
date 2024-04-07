/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.item;

import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event which is fired to determine whether item pickup should be allowed.
 * <p>
 * This event is {@linkplain ICancellableEvent cancellable}.
 * When cancelled item pickup is disallowed.
 * <p>
 * This event does not have a {@linkplain Result result}.
 * <p>
 * This event is fired on {@linkplain NeoForge#EVENT_BUS}
 */
public class ItemAllowPickupEvent extends ItemEvent implements ICancellableEvent {
    @ApiStatus.Internal
    public ItemAllowPickupEvent(ItemEntity itemEntity) {
        super(itemEntity);
    }
}
