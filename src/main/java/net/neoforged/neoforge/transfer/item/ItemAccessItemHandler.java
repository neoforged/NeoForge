/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import com.google.common.base.Preconditions;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;

/**
 * Base implementation of an item {@link ResourceHandler} backed by an {@link ItemAccess}.
 * The stacks are stored in a {@link ItemContainerContents} data component.
 * <p>
 * To use this class, register a new {@link DataComponentType} which holds an {@link ItemContainerContents} for your item.
 * Then reference that component from your {@link ICapabilityProvider} passed to {@link RegisterCapabilitiesEvent#registerItem} to create an instance of this class.
 */
public class ItemAccessItemHandler extends ItemAccessResourceHandler<ItemResource> {
    protected final Item validItem;
    protected final DataComponentType<ItemContainerContents> component;

    public ItemAccessItemHandler(ItemAccess itemAccess, DataComponentType<ItemContainerContents> component, int size) {
        super(itemAccess, size);
        // Store the current item, such that if the item changes later we don't return any stored content from it.
        this.validItem = itemAccess.getResource().getItem();
        this.component = component;
        Preconditions.checkArgument(size <= /* ItemContainerContents.MAX_SIZE */ 256,
                "The max size of ItemContainerContents is 256 slots.");
    }

    /**
     * Retrieves the {@link ItemContainerContents} from the current resource of the item access.
     */
    protected ItemContainerContents getContents(ItemResource accessResource) {
        return accessResource.getOrDefault(component, ItemContainerContents.EMPTY);
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
        return slot < contents.getSlots() ? contents.getStackInSlot(slot) : ItemStack.EMPTY;
    }

    @Override
    protected ItemResource getResourceFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return ItemResource.of(getStackFromContents(getContents(accessResource), index));
        } else {
            return ItemResource.EMPTY;
        }
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return getStackFromContents(getContents(accessResource), index).getCount();
        } else {
            return 0;
        }
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, ItemResource newResource, int newAmount) {
        var contents = getContents(accessResource);
        // Ensure we don't truncate any data by taking the max of the number of slots we need to fit, and our desired size
        NonNullList<ItemStack> list = NonNullList.withSize(Math.max(contents.getSlots(), size), ItemStack.EMPTY);
        contents.copyInto(list);
        list.set(index, newResource.toStack(newAmount));
        return accessResource.with(this.component, ItemContainerContents.fromItems(list));
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        // Any resource is valid, but we have to check that the item of the item access has not changed.
        return itemAccess.getResource().is(validItem);
    }

    @Override
    protected int getCapacity(int index, ItemResource resource) {
        return resource.isEmpty() ? Item.ABSOLUTE_MAX_STACK_SIZE : Math.min(resource.getMaxStackSize(), Item.ABSOLUTE_MAX_STACK_SIZE);
    }
}
