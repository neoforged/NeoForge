/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.access;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Implementation of {@link ItemAccess} that will mutate a stack directly,
 * possibly changing the components and the count, but never the underlying Item as it's final.
 *
 * <p>This can be used when it is known that the resource handler will not change the underlying Item.
 */
class StackItemAccess implements ItemAccess {
    private final Item item;
    // We essentially reuse the ability of the Container wrappers to mutate the original stack.
    private final ResourceHandler<ItemResource> wrapper;

    public StackItemAccess(ItemStack stack) {
        item = stack.getItem();
        wrapper = VanillaContainerWrapper.of(new SimpleContainer(stack) {
            // Override to avoid clamping oversized stacks to their max stack size, just in case.
            @Override
            public void setItem(int slot, ItemStack stack, boolean performSideEffects) {
                getItems().set(slot, stack);
            }
        });
    }

    @Override
    public ItemResource getResource() {
        return wrapper.getResource(0);
    }

    @Override
    public int getAmount() {
        return wrapper.getAmountAsInt(0);
    }

    @Override
    public int insert(ItemResource resource, int amount, TransactionContext transaction) {
        if (!resource.is(this.item)) {
            // Make sure that we do not change the underlying stack, even if it becomes temporarily empty.
            return 0;
        }
        return wrapper.insert(resource, amount, transaction);
    }

    @Override
    public int extract(ItemResource resource, int amount, TransactionContext transaction) {
        return wrapper.extract(resource, amount, transaction);
    }
}
