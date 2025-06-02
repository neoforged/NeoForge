/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ItemContextResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Predicate;

public class ItemContextFluidHandler extends ItemContextResourceHandler<FluidResource> {
    public ItemContextFluidHandler(IItemContext itemContext, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator) {
        super(itemContext, componentType, FluidResource.EMPTY, singleItemLimit, validator);
    }

    public ItemContextFluidHandler(IItemContext itemContext, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
        super(itemContext, componentType, FluidResource.EMPTY, singleItemLimit);
    }

    public static class Consumable extends ItemContextFluidHandler {
        public Consumable(IItemContext itemContext, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator) {
            super(itemContext, componentType, singleItemLimit, validator);
        }

        public Consumable(IItemContext itemContext, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
            super(itemContext, componentType, singleItemLimit);
        }

        @Override
        protected int empty(int count, TransactionContext context) {
            return itemContext.extract(itemContext.getResource(), count, context);
        }
    }

    public static class SwapEmpty extends ItemContextFluidHandler {
        protected final ItemResource emptyContainer;

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit, validator);
            this.emptyContainer = emptyContainer;
        }

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected int empty(int count, TransactionContext context) {
            return itemContext.exchange(emptyContainer, count, context);
        }
    }
}
