/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.handlers.templates.storage.SteppedItemContextResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;

import java.util.function.Predicate;


/**
 * A simple fluid storage handler that uses a single item context to store a fluid resource.
 * An item with this handler can only ever be full or empty, and will not allow for partial fills or extractions.
 * This handler is designed to support stacked items
 */
public class SteppedItemContextFluidHandler extends SteppedItemContextResourceHandler<FluidResource> {
    public SteppedItemContextFluidHandler(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
        super(context, componentType, FluidResource.NONE, singleItemLimit);
    }

    public SteppedItemContextFluidHandler(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator) {
        super(context, componentType, FluidResource.NONE, singleItemLimit, validator);
    }

    public static class Consumable extends SteppedItemContextFluidHandler {
        public Consumable(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
            super(context, componentType, singleItemLimit);
        }

        public Consumable(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator) {
            super(context, componentType, singleItemLimit, validator);
        }

        @Override
        protected int empty(int count, TransferAction action) {
            return context.extract(context.getResource(), count, action);
        }
    }

    public static class SwapEmpty extends SteppedItemContextFluidHandler {
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
        protected int empty(int count, TransferAction action) {
            return context.exchange(emptyContainer, count, action);
        }
    }
}