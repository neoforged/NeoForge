/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ItemContextResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Predicate;

public class ItemContextItemHandler extends ItemContextResourceHandler<ItemResource> {
    public ItemContextItemHandler(IItemContext context, DataComponentType<ResourceStack<ItemResource>> componentType, int singleItemLimit, Predicate<ItemResource> validator) {
        super(context, componentType, ItemResource.EMPTY, singleItemLimit, validator);
    }

    public ItemContextItemHandler(IItemContext context, DataComponentType<ResourceStack<ItemResource>> componentType, int singleItemLimit) {
        super(context, componentType, ItemResource.EMPTY, singleItemLimit);
    }

    public static class Consumable extends ItemContextItemHandler {
        public Consumable(IItemContext context, DataComponentType<ResourceStack<ItemResource>> componentType, int singleItemLimit, Predicate<ItemResource> validator) {
            super(context, componentType, singleItemLimit, validator);
        }

        public Consumable(IItemContext context, DataComponentType<ResourceStack<ItemResource>> componentType, int singleItemLimit) {
            super(context, componentType, singleItemLimit);
        }

        @Override
        protected int empty(int count, TransactionContext context) {
            return itemContext.extract(itemContext.getResource(), count, context);
        }
    }

    public static class SwapEmpty extends ItemContextItemHandler {
        protected final ItemResource emptyContainer;

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<ItemResource>> componentType, int singleItemLimit, Predicate<ItemResource> validator, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit, validator);
            this.emptyContainer = emptyContainer;
        }

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<ItemResource>> componentType, int singleItemLimit, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected int empty(int count, TransactionContext context) {
            return itemContext.exchange(emptyContainer, count, context);
        }
    }
}
