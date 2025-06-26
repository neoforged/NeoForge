/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import com.mojang.serialization.Codec;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.handlers.templates.resource.StackListHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import org.jetbrains.annotations.Nullable;

public class ItemStackListHandler extends StackListHandler<ItemStack, ItemResource> {
    public ItemStackListHandler(int size, int capacity, @Nullable Runnable onChangedCallback) {
        super(size, ItemStack.EMPTY, capacity, ItemResource::toStack, onChangedCallback);
    }

    public ItemStackListHandler(NonNullList<ItemStack> stacks, int capacity, @Nullable Runnable onChangedCallback) {
        super(stacks, ItemStack.EMPTY, capacity, ItemResource::toStack, onChangedCallback);
    }

    @Override
    public Codec<ItemStack> stackCodec() {
        return ItemStack.OPTIONAL_CODEC;
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
    public boolean matches(ItemStack stack, ItemResource resource) {
        return resource.is(stack);
    }

    @Override
    public ItemStack snapshotOf(ItemStack stack) {
        return stack.copy();
    }
}
