/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.adapters;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.IResourceContainer;

/**
 * An adapter to convert an {@link IResourceContainer} to a vanilla Minecraft {@link Container}.
 */
public record ItemContainerToVanillaAdapter(IResourceContainer<ItemResource> itemContainer)
        implements Container {
    @Override
    public int getContainerSize() {
        return itemContainer.size();
    }

    @Override
    public boolean isEmpty() {
        return itemContainer.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        var resourceStack = itemContainer.get(slot);
        return resourceStack.resource().toStack(resourceStack.amount());
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        var stack = getItem(slot);
        ItemStack split = stack.split(amount);
        itemContainer.set(slot, MutableResourceStack.of(ItemResource.of(stack), stack.getCount()));
        return split;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = getItem(slot);
        itemContainer.set(slot, MutableResourceStack.of(ItemResource.EMPTY, 0));
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        itemContainer.set(slot, MutableResourceStack.of(ItemResource.of(stack), stack.getCount()));
    }

    @Override
    public void setChanged() {}

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        itemContainer.clearContent();
    }
}
