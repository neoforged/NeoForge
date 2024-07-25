/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluids.templates;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.ResourceStack;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.fluids.FluidResource;
import net.neoforged.neoforge.transfer.handlers.ISingleResourceHandler;
import net.neoforged.neoforge.transfer.handlers.templates.SteppedSingleResourceStorageItem;
import net.neoforged.neoforge.transfer.items.ItemResource;

import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * A simple fluid storage handler that uses a single item context to store a fluid resource.
 * An item with this handler can only ever be full or empty, and will not allow for partial fills or extractions.
 * This handler is designed to support stacked items
 */
public class SteppedFluidStorageItem extends SteppedSingleResourceStorageItem<FluidResource> {
    public SteppedFluidStorageItem(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
        super(context, componentType, FluidResource.NONE, singleItemLimit);
    }

    public static class Consumable extends SteppedFluidStorageItem {
        public Consumable(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, int singleItemLimit) {
            super(context, componentType, singleItemLimit);
        }

        @Override
        protected int empty(int count, TransferAction action) {
            return context.extract(context.getResource(), count, action);
        }
    }

    public static class SwapEmpty extends SteppedFluidStorageItem {
        protected final ItemResource emptyContainer;

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<FluidResource>> componentType, ItemResource emptyContainer, int singleItemLimit) {
            super(context, componentType, singleItemLimit);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected int empty(int count, TransferAction action) {
            return context.exchange(emptyContainer, count, action);
        }
    }
}