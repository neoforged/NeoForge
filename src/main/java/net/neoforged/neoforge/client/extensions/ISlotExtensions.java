/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public interface ISlotExtensions {
    private Slot self() {
        return (Slot) this;
    }

    /**
     * Controls whether a slot should render its containing item stack.
     * 
     * @param stack The stack in the current slot.
     * @return True to render the item stack, false otherwise
     */
    default boolean shouldRenderItem(ItemStack stack) {
        return true;
    }

    /**
     * Checks if the other slot is in the same inventory, by comparing the inventory reference.
     * 
     * @param other
     * @return true if the other slot is in the same inventory
     */
    default boolean isSameInventory(Slot other) {
        return self().container == other.container;
    }
}
