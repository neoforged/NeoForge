/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.items.ItemResource;
import net.neoforged.neoforge.transfer.handlers.IResourceHandlerModifiable;

@Deprecated(forRemoval = true, since = "1.21")
public interface IItemHandlerModifiable extends IItemHandler, IResourceHandlerModifiable<ItemResource> {
    /**
     * Overrides the stack in the given slot. This method is used by the
     * standard Forge helper methods and classes. It is not intended for
     * general use by other mods, and the handler may throw an error if it
     * is called unexpectedly.
     *
     * @param slot  Slot to modify
     * @param stack ItemStack to set slot to (may be empty).
     * @throws RuntimeException if the handler is called in a way that the handler
     *                          was not expecting.
     **/
    void setStackInSlot(int slot, ItemStack stack);

    @Override
    default void set(int index, ItemResource resource, int amount) {
        setStackInSlot(index, resource.toStack(amount));
    }
}
