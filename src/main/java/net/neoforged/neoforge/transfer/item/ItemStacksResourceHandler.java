/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.StacksResourceHandler;

/**
 * Base implementation of a {@code ResourceHandler<ItemResource>} backed by a list of {@link ItemStack}s.
 *
 * <p>The following methods will typically be overridden:
 * <ul>
 * <li>(optional) {@link #isValid} to limit which resources are allowed in this handler; by default any resource is allowed.</li>
 * <li>(optional) {@link #getCapacity} to specify the capacity of this handler; by default the maximum stack size is used.</li>
 * <li>(recommended) {@link #onContentsChanged} to react to changes in this handler, for example to trigger {@code setChanged()}.</li>
 * </ul>
 *
 * @see StacksResourceHandler
 */
public class ItemStacksResourceHandler extends StacksResourceHandler<ItemStack, ItemResource> {
    public ItemStacksResourceHandler(int size) {
        super(size, ItemStack.EMPTY, ItemStack.OPTIONAL_CODEC);
    }

    public ItemStacksResourceHandler(NonNullList<ItemStack> stacks) {
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
        return resource.isEmpty() ? Item.ABSOLUTE_MAX_STACK_SIZE : Math.min(resource.getMaxStackSize(), Item.ABSOLUTE_MAX_STACK_SIZE);
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
