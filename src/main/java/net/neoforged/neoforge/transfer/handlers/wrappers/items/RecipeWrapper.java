/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandlerModifiable;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public class RecipeWrapper implements RecipeInput {
    protected final IResourceHandlerModifiable<ItemResource> handler;

    public RecipeWrapper(IResourceHandlerModifiable<ItemResource> handler) {
        this.handler = handler;
    }

    /**
     * Returns the size of this inventory.
     */
    @Override
    public int size() {
        return handler.size();
    }

    /**
     * Returns the stack in this slot. This stack should be a modifiable reference, not a copy of a stack in your inventory.
     */
    @Override
    public ItemStack getItem(int slot) {
        return handler.getResource(slot).toStack(handler.getAmount(slot));
    }
}
