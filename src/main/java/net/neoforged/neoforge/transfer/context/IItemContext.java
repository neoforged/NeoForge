/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.context;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.items.ItemResource;

public interface IItemContext {
    default <T> T getCapability(ItemCapability<T, IItemContext> capability) {
        return capability.getCapability(getResource().toStack(), this);
    }

    ItemResource getResource();

    int getAmount();

    // ResourceStack<ItemResource> getMainStack() instead of the 2 methods?

    int insert(ItemResource resource, int amount, TransferAction action);

    int extract(ItemResource resource, int amount, TransferAction action);

    int exchange(ItemResource resource, int amount, TransferAction action);

    default ItemStack toStack() {
        return getResource().toStack(getAmount());
    }
}
