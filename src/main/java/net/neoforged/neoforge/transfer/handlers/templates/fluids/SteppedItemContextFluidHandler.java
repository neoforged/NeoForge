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
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A simple fluid storage handler that uses a single item context to store a fluid resource.
 * An item with this handler can only ever be full or empty, and will not allow for partial fills or extractions.
 * This handler is designed to support stacked items.
 * <p>
 * This is similar to the {@link net.neoforged.neoforge.transfer.handlers.wrappers.fluids.BucketResourceHandler}, but is controlled by the DataComponentType
 */
public abstract class SteppedItemContextFluidHandler extends SteppedItemContextResourceHandler<FluidResource> {
    public SteppedItemContextFluidHandler(IItemContext context, DataComponentType<Component<FluidResource>> componentType, int singleItemLimit) {
        super(context, componentType, new Component<>(FluidResource.EMPTY_STACK, singleItemLimit));
    }

    public SteppedItemContextFluidHandler(IItemContext context, DataComponentType<Component<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator) {
        super(context, componentType, new Component<>(FluidResource.EMPTY_STACK, singleItemLimit), validator);
    }

    public static class Consumable extends SteppedItemContextFluidHandler {
        public Consumable(IItemContext context, DataComponentType<Component<FluidResource>> componentType, int singleItemLimit) {
            super(context, componentType, singleItemLimit);
        }

        public Consumable(IItemContext context, DataComponentType<Component<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator) {
            super(context, componentType, singleItemLimit, validator);
        }

        @Override
        protected int empty(int count, TransactionContext context) {
            return itemContext.extract(itemContext.getResource(), count, context);
        }
    }

    public static class SwapEmpty extends SteppedItemContextFluidHandler {
        protected final ItemResource emptyContainer;

        public SwapEmpty(IItemContext context, DataComponentType<Component<FluidResource>> componentType, int singleItemLimit, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit);
            this.emptyContainer = emptyContainer;
        }

        public SwapEmpty(IItemContext context, DataComponentType<Component<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit, validator);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected int empty(int count, TransactionContext context) {
            return itemContext.exchange(emptyContainer, count, context);
        }
    }
}
