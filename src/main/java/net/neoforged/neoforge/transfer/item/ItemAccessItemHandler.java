/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.access.ItemAccess;

/**
 * A {@link ComponentItemAccessItemHandler} for {@link ItemContainerContents}.
 */
public class ItemAccessItemHandler extends ComponentItemAccessItemHandler<ItemContainerContents> {
    public ItemAccessItemHandler(ItemAccess itemAccess, DataComponentType<ItemContainerContents> component, int size) {
        super(itemAccess, component, size, () -> ItemContainerContents.EMPTY);
        Preconditions.checkArgument(size <= ItemContainerContents.MAX_SIZE,
                "The max size of ItemContainerContents is 256 slots.");
    }

    @Override
    protected ItemStack getStackFromContents(ItemContainerContents contents, int slot) {
        return slot < contents.getSlots() ? contents.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    protected ItemContainerContents updateContents(ItemContainerContents contents, int index, ItemStack stack) {
        // Ensure we don't truncate any data by taking the max of the number of slots we need to fit, and our desired size
        NonNullList<ItemStack> list = NonNullList.withSize(Math.max(contents.getSlots(), size), ItemStack.EMPTY);
        contents.copyInto(list);
        list.set(index, stack);
        return ItemContainerContents.fromItems(list);
    }
}
