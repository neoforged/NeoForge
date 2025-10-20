/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items.wrapper;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.PlayerInventoryWrapper;

/**
 * @deprecated Use {@link PlayerInventoryWrapper} instead, in particular {@link PlayerInventoryWrapper#getArmorSlots()} for the armor slots only.
 */
@Deprecated(since = "1.21.9", forRemoval = true)
public class PlayerArmorInvWrapper extends RangedWrapper {
    private final Inventory inventoryPlayer;

    public PlayerArmorInvWrapper(Inventory inv) {
        super(new InvWrapper(inv), Inventory.INVENTORY_SIZE, Inventory.INVENTORY_SIZE + 4);
        inventoryPlayer = inv;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        EquipmentSlot equ = null;
        for (EquipmentSlot s : EquipmentSlot.values()) {
            if (s.getType() == EquipmentSlot.Type.HUMANOID_ARMOR && s.getIndex() == slot) {
                equ = s;
                break;
            }
        }
        // check if it's valid for the armor slot
        if (equ != null && slot < 4 && !stack.isEmpty() && stack.canEquip(equ, getInventoryPlayer().player)) {
            return super.insertItem(slot, stack, simulate);
        }
        return stack;
    }

    public Inventory getInventoryPlayer() {
        return inventoryPlayer;
    }
}
