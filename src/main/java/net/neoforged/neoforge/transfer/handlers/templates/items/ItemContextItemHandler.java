/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.items;

import java.util.function.Predicate;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.transfer.handlers.IItemCapabilityContext;
import net.neoforged.neoforge.transfer.handlers.templates.resource.ItemContextResourceHandler;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.resources.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * A template that stores a single {@link ItemResource} in the form of a {@link ResourceStack} on a component.
 */
public class ItemContextItemHandler extends ItemContextResourceHandler<ItemResource> {
    public ItemContextItemHandler(IItemCapabilityContext itemContext, DataComponentType<Component<ItemResource>> componentType, int singleItemLimit) {
        super(itemContext, componentType, new Component<>(ItemResource.EMPTY_STACK, singleItemLimit));
    }

    public ItemContextItemHandler(IItemCapabilityContext itemContext, DataComponentType<Component<ItemResource>> componentType, int singleItemLimit, Predicate<ItemResource> validator) {
        super(itemContext, componentType, new Component<>(ItemResource.EMPTY_STACK, singleItemLimit), validator);
    }

    public static class Consumable extends ItemContextItemHandler {
        public Consumable(IItemCapabilityContext context, DataComponentType<Component<ItemResource>> componentType, int singleItemLimit) {
            super(context, componentType, singleItemLimit);
        }

        public Consumable(IItemCapabilityContext context, DataComponentType<Component<ItemResource>> componentType, int singleItemLimit, Predicate<ItemResource> validator) {
            super(context, componentType, singleItemLimit, validator);
        }

        @Override
        protected int empty(int count, TransactionContext context) {
            return itemContext.extract(itemContext.getResource(), count, context);
        }
    }

    public static class SwapEmpty extends ItemContextItemHandler {
        protected final ItemResource emptyContainer;

        public SwapEmpty(IItemCapabilityContext context, DataComponentType<Component<ItemResource>> componentType, int singleItemLimit, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit);
            this.emptyContainer = emptyContainer;
        }

        public SwapEmpty(IItemCapabilityContext context, DataComponentType<Component<ItemResource>> componentType, int singleItemLimit, Predicate<ItemResource> validator, ItemResource emptyContainer) {
            super(context, componentType, singleItemLimit, validator);
            this.emptyContainer = emptyContainer;
        }

        @Override
        protected int empty(int count, TransactionContext context) {
            return itemContext.exchange(emptyContainer, count, context);
        }
    }
}
