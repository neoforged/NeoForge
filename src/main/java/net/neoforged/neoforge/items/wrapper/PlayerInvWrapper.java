/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;

/**
 * @deprecated Use {@link PlayerInventoryWrapper} instead.
 */
@Deprecated(since = "1.21.9", forRemoval = true)
public class PlayerInvWrapper extends CombinedInvWrapper {
    public PlayerInvWrapper(Inventory inv) {
        super(new PlayerMainInvWrapper(inv), new PlayerArmorInvWrapper(inv), new PlayerOffhandInvWrapper(inv));
    }
}
