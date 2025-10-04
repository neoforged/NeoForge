/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluid;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Base implementation of a simple fluid {@link ResourceHandler} backed by an {@link ItemAccess}.
 * Data is stored in a {@link SimpleFluidContent} component.
 *
 * <p>This class allows the backing items to contain any partial level of fluid up to its capacity.
 */
public class ItemAccessFluidHandler extends ItemAccessResourceHandler<FluidResource> {
    protected final Item validItem;
    protected final DataComponentType<SimpleFluidContent> component;
    protected int capacity;

    public ItemAccessFluidHandler(ItemAccess itemAccess, DataComponentType<SimpleFluidContent> component, int capacity) {
        super(itemAccess, 1);
        // Store the current item, such that if the item changes later we don't return any stored content from it.
        this.validItem = itemAccess.getResource().getItem();
        this.component = component;
        this.capacity = capacity;
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return FluidResource.of(accessResource.getOrDefault(component, SimpleFluidContent.EMPTY).copy());
        } else {
            return FluidResource.EMPTY;
        }
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return accessResource.getOrDefault(component, SimpleFluidContent.EMPTY).getAmount();
        } else {
            return 0;
        }
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
        return accessResource.with(component, SimpleFluidContent.copyOf(newResource.toStack(newAmount)));
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        // Any resource is valid, but we have to check that the item of the item access has not changed.
        return itemAccess.getResource().is(validItem);
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return capacity;
    }
}
