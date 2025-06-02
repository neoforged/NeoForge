/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;

//Not tested, it at least looks to read correctly, but unsure about writing back, as well as not sure the logical use of neo having this.
//This also only handles of a single type, which means recipes looking for a composite of say fluid and item can't use this.
//Leaning towards removal
public class ResourceHandlerRecipeInput implements RecipeInput {
    protected final IResourceHandler<ItemResource> handler;

    public ResourceHandlerRecipeInput(IResourceHandler<ItemResource> handler) {
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
