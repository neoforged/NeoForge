/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.resource.SteppedItemContextResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A simple fluid storage handler that uses a single item context to store a fluid resource.
 * An item with this handler can only ever be full or empty, and will not allow for partial fills or extractions.
 * This handler is designed to support stacked items.
 * <p>
 * This is similar to the {@link net.neoforged.neoforge.transfer.handlers.wrappers.fluids.BucketResourceHandler}, but is controlled by the DataComponentType
 */
//todo name to not stepped. While it is technically correct, the intent is not incremental anymore, but rather all or nothing.
public abstract class SteppedItemContextFluidHandler extends SteppedItemContextResourceHandler<FluidResource> {
    public SteppedItemContextFluidHandler(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int capacityOfOneItem) {
        super(context, componentType, FluidResource.EMPTY_STACK, capacityOfOneItem);
    }

    public SteppedItemContextFluidHandler(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int capacityOfOneItem, Predicate<FluidResource> validator) {
        super(context, componentType, FluidResource.EMPTY_STACK, capacityOfOneItem, validator);
    }

    public static class Consumable extends SteppedItemContextFluidHandler {
        public Consumable(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int capacityOfOneItem) {
            super(context, componentType, capacityOfOneItem);
        }

        public Consumable(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int capacityOfOneItem, Predicate<FluidResource> validator) {
            super(context, componentType, capacityOfOneItem, validator);
        }

        protected int setEmpty(int count, TransactionContext context) {
            return itemContext.extract(itemContext.getResource(), count, context);
        }
    }

    public static class SwapEmpty extends SteppedItemContextFluidHandler {
        protected final ItemResource emptyContainer;

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int capacityOfOneItem, ItemResource emptyContainer) {
            super(context, componentType, capacityOfOneItem);
            this.emptyContainer = emptyContainer;
        }

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int capacityOfOneItem, Predicate<FluidResource> validator, ItemResource emptyContainer) {
            super(context, componentType, capacityOfOneItem, validator);
            this.emptyContainer = emptyContainer;
        }

        protected int setEmpty(int count, TransactionContext context) {
            return itemContext.exchange(emptyContainer, count, context);
        }
    }
}
