/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.toremove_before_pr_merging.handlers.templates.container.resources;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.MutableResourceStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class SimpleItemResourceContainer extends ResourceContainer<ItemResource> {
    /**
     * @param capacity Typically is expected to be {@link Item#ABSOLUTE_MAX_STACK_SIZE} as the baseline capacity allowing for the item to be the limit,
     *                 but you can set your own as high as you like.
     *                 Just note you will need to override the {@link #getCapacity(int, ItemResource)} to allow more control per item
     */
    public SimpleItemResourceContainer(NonNullList<MutableResourceStack<ItemResource>> mutableResourceStacks, int capacity, @Nullable Runnable updateCallback) {
        super(mutableResourceStacks, ItemResource.EMPTY_STACK, capacity, updateCallback);
    }

    /**
     * Copies all the contents of this container to a non-null list of the same size.
     *
     * @return A new non-null list.
     */
    @Contract(pure = true)
    public NonNullList<ItemStack> copyToItemStackList() {
        var size = size();
        var list = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int index = 0; index < size; index++) {
            list.set(index, ItemResource.itemStackOf(get(index)));
        }
        return list;
    }

    //Because Items also have their own stack sizes, there are scenarios for default chest implementations to handle this.
    // Overriding this limit is possible, it just requires a subclass
    @Override
    public int getCapacity(int index, ItemResource resource) {
        return Math.min(resource.getMaxStackSize(), super.getCapacity(index, resource));
    }

    public static SimpleItemResourceContainer.Builder builder(int size) {
        return new Builder().size(size).capacity(Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    public static SimpleItemResourceContainer.Builder from(NonNullList<MutableResourceStack<ItemResource>> stacks) {
        return new Builder().from(stacks).capacity(Item.ABSOLUTE_MAX_STACK_SIZE);
    }

    public static class Builder extends ResourceContainer.Builder<ItemResource, Builder> {
        public Builder() {
            super(ItemResource.EMPTY_STACK);
        }

        @Override
        public SimpleItemResourceContainer build() {
            if (stacks == null)
                throw new IllegalArgumentException("SimpleItemResourceContainer's stacks must not be null");
            return new SimpleItemResourceContainer(stacks, capacity, updateCallback);
        }
    }
}
