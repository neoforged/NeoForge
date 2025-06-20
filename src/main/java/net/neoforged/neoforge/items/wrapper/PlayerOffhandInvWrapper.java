/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.ResourceHandlerDeprecationHandling;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.PlayerInventoryWrapper;

/**
 * @deprecated Not a 1:1 but {@link PlayerInventoryWrapper}
 */
@Deprecated(since = ResourceHandlerDeprecationHandling.MC_1_21_6, forRemoval = true)
public class PlayerOffhandInvWrapper extends RangedWrapper {
    public PlayerOffhandInvWrapper(Inventory inv) {
        super(new InvWrapper(inv), Inventory.SLOT_OFFHAND, Inventory.SLOT_OFFHAND + 1);
    }
}
