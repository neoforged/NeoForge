/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.handlers.templates.resources.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;

public class ItemAccessItemHandler extends ItemAccessResourceHandler<ItemResource> {
    protected final Item validItem;
    protected final DataComponentType<ItemContainerContents> component;

    protected ItemAccessItemHandler(ItemAccess itemAccess, DataComponentType<ItemContainerContents> component, int size) {
        super(itemAccess, size);
        // Store the current item, such that if the item changes later we don't return any stored content from it.
        this.validItem = itemAccess.getResource().getItem();
        this.component = component;
        Preconditions.checkArgument(size <= 256, "The max size of ItemContainerContents is 256 slots.");
    }

    /**
     * Retrieves a copy of a single stack from the underlying data component,
     * returning {@link ItemStack#EMPTY} if the component does not have a slot present.
     *
     * @param contents the existing contents
     * @param slot     the target slot
     * @return a copy of the stack in the target slot
     */
    protected ItemStack getStackFromContents(ItemContainerContents contents, int slot) {
        return contents.getSlots() <= slot ? ItemStack.EMPTY : contents.getStackInSlot(slot);
    }

    @Override
    protected ItemResource getResourceFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return ItemResource.of(getStackFromContents(accessResource.getOrDefault(component, ItemContainerContents.EMPTY), index));
        } else {
            return ItemResource.EMPTY;
        }
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return getStackFromContents(accessResource.getOrDefault(component, ItemContainerContents.EMPTY), index).getCount();
        } else {
            return 0;
        }
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, ItemResource newResource, int newAmount) {
        var contents = accessResource.getOrDefault(component, ItemContainerContents.EMPTY);
        // Use the max of the contents slots and the capability slots to avoid truncating
        NonNullList<ItemStack> list = NonNullList.withSize(Math.max(contents.getSlots(), size), ItemStack.EMPTY);
        contents.copyInto(list);
        list.set(index, newResource.toStack(newAmount));
        return accessResource.with(this.component, ItemContainerContents.fromItems(list));
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return itemAccess.getResource().is(validItem);
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return resource.isEmpty() ? Item.ABSOLUTE_MAX_STACK_SIZE : Math.min(resource.getMaxStackSize(), Item.ABSOLUTE_MAX_STACK_SIZE);
    }
}
