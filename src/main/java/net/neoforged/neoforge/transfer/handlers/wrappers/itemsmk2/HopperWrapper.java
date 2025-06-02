/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.itemsmk2;

import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class HopperWrapper extends AlternateVanillaContainerResourceWrapper {
    public HopperWrapper(HopperBlockEntity container) {
        super(container);
    }

    public HopperBlockEntity getContainer() {
        return (HopperBlockEntity) super.getContainer();
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext action) {
        boolean wasEmpty = getContainer().isEmpty();
        int inserted = super.insert(index, resource, amount, action);
        if (wasEmpty && inserted > 0) {
            if (!getContainer().isOnCustomCooldown()) {
                // This cooldown is always set to 8 in vanilla with one exception:
                // Hopper -> Hopper transfer sets this cooldown to 7 when this hopper
                // has not been updated as recently as the one pushing items into it.
                // This vanilla behavior is preserved by VanillaInventoryCodeHooks#insertStack,
                // the cooldown is set properly by the hopper that is pushing items into this one.
                getContainer().setCooldown(8);
            }
        }
        return inserted;
    }
}
