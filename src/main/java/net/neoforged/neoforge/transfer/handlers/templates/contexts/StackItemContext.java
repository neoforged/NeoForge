/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.contexts;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.handlers.IItemContext;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class StackItemContext implements IItemContext {
    private final VanillaContainerWrapper container;

    public StackItemContext(ItemStack stack) {
        container = VanillaContainerWrapper.of(new StackContainer(stack));
    }

    @Override
    public ItemResource getResource() {
        return container.getResource(0);
    }

    @Override
    public int getAmount() {
        return container.getAmount(0);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        return container.insert(resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        if (ResourceHandlerUtil.isEmpty(resource, amount)) return 0;
        return container.extract(resource, amount, transaction);
    }

    private static class StackContainer extends SimpleContainer {
        public StackContainer(ItemStack stack) {
            super(stack);
        }

        @Override
        public void setItem(int index, ItemStack stack, boolean insideTransaction) {
            getItems().set(index, stack);
        }
    }
}
