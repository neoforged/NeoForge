/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.initem;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ContainerStorage;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Implementation of {@link InItemStorageContext} that will mutate a stack directly,
 * possibly changing the components and the count, but never the underlying Item as it's final.
 *
 * <p>This can be used when it is known that the storage will not change the underlying Item.
 */
public final class StackStorageContext implements InItemStorageContext {
    // We essentially reuse the ability of the Container wrappers to mutate the original stack.
    private final Item item;
    private final Storage<ItemVariant> wrapper;

    public StackStorageContext(ItemStack stack) {
        var container = new SimpleContainer(stack) {
            // Override to avoid clamping oversized stacks to their max stack size, just in case.
            @Override
            public void setItem(int slot, ItemStack stack, boolean performSideEffects) {
                getItems().set(slot, stack);
            }
        };
        this.item = stack.getItem();
        this.wrapper = ContainerStorage.of(container);
    }

    @Override
    public ItemVariant getCurrent() {
        return wrapper.getResource(0);
    }

    @Override
    public long getCurrentAmount() {
        return wrapper.getAmount(0);
    }

    @Override
    public long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        if (!itemVariant.is(this.item)) {
            // Make sure that we do not change the underlying stack, even if it becomes temporarily empty.
            return 0;
        }
        return wrapper.insert(itemVariant, maxAmount, transaction);
    }

    @Override
    public long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
        return wrapper.extract(itemVariant, maxAmount, transaction);
    }
}
