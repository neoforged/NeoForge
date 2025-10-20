/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items.wrapper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;

/**
 * Exposes the player inventory WITHOUT the armor inventory as IItemHandler.
 * Also takes core of inserting/extracting having the same logic as picking up items.
 *
 * @deprecated Use {@link PlayerInventoryWrapper} instead, in particular {@link PlayerInventoryWrapper#getMainSlots()} for the main slots only.
 */
@Deprecated(since = "1.21.9", forRemoval = true)
public class PlayerMainInvWrapper extends RangedWrapper {
    private final Inventory inventoryPlayer;

    public PlayerMainInvWrapper(Inventory inv) {
        super(new InvWrapper(inv), 0, Inventory.INVENTORY_SIZE);
        inventoryPlayer = inv;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        ItemStack rest = super.insertItem(slot, stack, simulate);
        if (rest.getCount() != stack.getCount()) {
            // the stack in the slot changed, animate it
            ItemStack inSlot = getStackInSlot(slot);
            if (!inSlot.isEmpty()) {
                if (getInventoryPlayer().player.level().isClientSide()) {
                    inSlot.setPopTime(5);
                } else if (getInventoryPlayer().player instanceof ServerPlayer) {
                    getInventoryPlayer().player.containerMenu.broadcastChanges();
                }
            }
        }
        return rest;
    }

    public Inventory getInventoryPlayer() {
        return inventoryPlayer;
    }
}
