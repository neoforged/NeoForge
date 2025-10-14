/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;

/**
 * @deprecated Use {@link PlayerInventoryWrapper} instead, in particular {@link PlayerInventoryWrapper#getHandSlot} for the offhand only.
 */
@Deprecated(since = "1.21.9", forRemoval = true)
public class PlayerOffhandInvWrapper extends RangedWrapper {
    public PlayerOffhandInvWrapper(Inventory inv) {
        super(new InvWrapper(inv), Inventory.SLOT_OFFHAND, Inventory.SLOT_OFFHAND + 1);
    }
}
