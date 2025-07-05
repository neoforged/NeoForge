/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.templates.resources.ItemContextResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A template that stores a single {@link ItemResource} in the form of a {@link ResourceStack} on a component.
 */
public class ItemContextItemHandler extends ItemContextResourceHandler<ItemResource> {
    public ItemContextItemHandler(IItemContext itemContext, DataComponentType<ResourceStack<ItemResource>> componentType, int capacityOfOneItem) {
        super(itemContext, componentType, ItemResource.EMPTY_STACK, capacityOfOneItem);
    }

    public ItemContextItemHandler(IItemContext itemContext, DataComponentType<ResourceStack<ItemResource>> componentType, int capacityOfOneItem, Predicate<ItemResource> validator) {
        super(itemContext, componentType, ItemResource.EMPTY_STACK, capacityOfOneItem, validator);
    }

    public static class Consumable extends ItemContextItemHandler {
        public Consumable(IItemContext context, DataComponentType<ResourceStack<ItemResource>> componentType, int capacityOfOneItem) {
            super(context, componentType, capacityOfOneItem);
        }

        public Consumable(IItemContext context, DataComponentType<ResourceStack<ItemResource>> componentType, int capacityOfOneItem, Predicate<ItemResource> validator) {
            super(context, componentType, capacityOfOneItem, validator);
        }

        protected int setEmpty(int count, TransactionContext context) {
            return itemContext.extract(itemContext.getResource(), count, context);
        }
    }

    public static class SwapEmpty extends ItemContextItemHandler {
        protected final ItemResource emptyContainer;

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<ItemResource>> componentType, int capacityOfOneItem, ItemResource emptyContainer) {
            super(context, componentType, capacityOfOneItem);
            this.emptyContainer = emptyContainer;
        }

        public SwapEmpty(IItemContext context, DataComponentType<ResourceStack<ItemResource>> componentType, int capacityOfOneItem, Predicate<ItemResource> validator, ItemResource emptyContainer) {
            super(context, componentType, capacityOfOneItem, validator);
            this.emptyContainer = emptyContainer;
        }

        protected int setEmpty(int count, TransactionContext context) {
            return itemContext.exchange(emptyContainer, count, context);
        }
    }
}
