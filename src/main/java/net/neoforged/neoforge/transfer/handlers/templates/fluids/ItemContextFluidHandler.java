/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.fluids;

import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ItemContextResourceHandler;
import net.neoforged.neoforge.transfer.resources.FluidResource;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class ItemContextFluidHandler extends ItemContextResourceHandler<FluidResource> {
    public ItemContextFluidHandler(IItemContext itemContext, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
        super(itemContext, componentType, FluidResource.EMPTY_STACK, singleItemLimit);
    }

    public ItemContextFluidHandler(IItemContext itemContext, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator) {
        super(itemContext, componentType, FluidResource.EMPTY_STACK, singleItemLimit, validator);
    }

    /**
     * A consumable fluid container that, when emptied, will be destroyed rather than just have the fluid removed.
     */
    public static class Consumable extends ItemContextFluidHandler {
        public Consumable(IItemContext itemContext, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
            super(itemContext, componentType, singleItemLimit);
        }

        public Consumable(IItemContext itemContext, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator) {
            super(itemContext, componentType, singleItemLimit, validator);
        }

        @Override
        protected int set(int count, ResourceStack<FluidResource> resultStack, TransactionContext transaction) {
            //if the fluid is empty, we then want to "consume" the container
            if (resultStack.isEmpty())
                return itemContext.extract(itemContext.getResource(), count, transaction);
            return super.set(count, resultStack, transaction);
        }
    }

    /**
     * A swappable fluid container that, when emptied, will swap from the fluid storing to a different Item Resource.
     * Conceptually, it similar to a bucket of fluid -> an empty bucket
     * (despite them having their own custom handler)
     */
    public static class SwapEmpty extends ItemContextFluidHandler {
        protected final ItemResource emptyContainer;

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit);
            this.emptyContainer = emptyContainer;
        }

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit, Predicate<FluidResource> validator, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit, validator);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected int set(int count, ResourceStack<FluidResource> resultStack, TransactionContext transaction) {
            //if the fluid is empty, we want to set it to a different resource. Like a water bucket -> an empty bucket.
            if (resultStack.isEmpty())
                return itemContext.exchange(emptyContainer, count, transaction);
            return super.set(count, resultStack, transaction);
        }
    }
}
