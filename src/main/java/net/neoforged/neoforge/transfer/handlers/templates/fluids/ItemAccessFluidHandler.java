/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.handlers.resources.ResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.resources.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;

/**
 * Base implementation of a simple fluid {@link ResourceHandler} backed by an {@link ItemAccess}.
 * Data is stored in a {@link SimpleFluidContent} component.
 *
 * <p>This class allows the backing items to contain any partial level of fluid up to its capacity.
 */
public class ItemAccessFluidHandler extends ItemAccessResourceHandler<FluidResource> {
    protected final Item validItem;
    protected final Supplier<DataComponentType<SimpleFluidContent>> componentType;
    protected int capacity;

    protected ItemAccessFluidHandler(ItemAccess itemAccess, Supplier<DataComponentType<SimpleFluidContent>> componentType, int capacity) {
        super(itemAccess, 1);
        // Store the current item, such that if the item changes later we don't return any stored content from it.
        this.validItem = itemAccess.getResource().getItem();
        this.componentType = componentType;
        this.capacity = capacity;
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return FluidResource.of(accessResource.getOrDefault(componentType, SimpleFluidContent.EMPTY).copy());
        } else {
            return FluidResource.EMPTY;
        }
    }

    @Override
    protected int getAmountFrom(ItemResource accessResource, int index) {
        if (accessResource.is(validItem)) {
            return accessResource.getOrDefault(componentType, SimpleFluidContent.EMPTY).getAmount();
        } else {
            return 0;
        }
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
        return accessResource.with(componentType, SimpleFluidContent.copyOf(newResource.toStack(newAmount)));
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return itemAccess.getResource().is(validItem);
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return capacity;
    }
}
