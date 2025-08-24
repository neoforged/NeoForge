/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.templates.resources.StackListHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;

// TODO: class javadoc needs a solid pass to reference all the common methods that should be overridden
public class ItemStackListHandler extends StackListHandler<ItemStack, ItemResource> {
    public ItemStackListHandler(int size) {
        super(size, ItemStack.EMPTY, ItemStack.OPTIONAL_CODEC);
    }

    public ItemStackListHandler(NonNullList<ItemStack> stacks) {
        super(stacks, ItemStack.EMPTY, ItemStack.OPTIONAL_CODEC);
    }

    @Override
    public ItemResource getResourceFrom(ItemStack stack) {
        return ItemResource.of(stack);
    }

    @Override
    public int getAmountFrom(ItemStack stack) {
        return stack.getCount();
    }

    @Override
    protected ItemStack getStackFrom(ItemResource resource, int amount) {
        return resource.toStack(amount);
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return Math.min(resource.getMaxStackSize(), 99);
    }

    @Override
    protected ItemStack copyOf(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public boolean matches(ItemStack stack, ItemResource resource) {
        return resource.matches(stack);
    }
}
