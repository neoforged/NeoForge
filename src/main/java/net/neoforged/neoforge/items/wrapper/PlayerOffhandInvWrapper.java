/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.InventoryStorage;

/**
 * @deprecated in favor of {@link InventoryStorage#getHandSlot}
 */
@Deprecated(forRemoval = true)
public class PlayerOffhandInvWrapper extends RangedWrapper {
    public PlayerOffhandInvWrapper(Inventory inv) {
        super(new InvWrapper(inv), inv.items.size() + inv.armor.size(),
                inv.items.size() + inv.armor.size() + inv.offhand.size());
    }
}
