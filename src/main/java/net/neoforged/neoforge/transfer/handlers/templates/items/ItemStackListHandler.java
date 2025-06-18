/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.templates.resource.StackListHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import org.jetbrains.annotations.Nullable;

public class ItemStackListHandler extends StackListHandler<ItemStack, ItemResource> {
    public ItemStackListHandler(int size, int capacity, @Nullable Runnable onChangedCallback) {
        super(size, ItemStack.EMPTY, capacity, onChangedCallback);
    }

    public ItemStackListHandler(NonNullList<ItemStack> stacks, int capacity, @Nullable Runnable onChangedCallback) {
        super(stacks, ItemStack.EMPTY, capacity, onChangedCallback);
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
    public int getCapacityFrom(ItemResource stack) {
        return stack.getMaxStackSize();
    }

    @Override
    public boolean isStackEmpty(ItemStack stack) {
        return stack.isEmpty();
    }

    @Override
    public boolean matches(ItemResource resource, ItemStack stack) {
        return resource.is(stack);
    }

    @Override
    public ItemStack toStack(ItemResource resource, int amount) {
        return resource.toStack(amount);
    }

    @Override
    public ItemStack copyOf(ItemStack stack) {
        return stack.copy();
    }
}
