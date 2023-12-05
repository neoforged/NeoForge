/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.items.wrapper;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

/**
 * An {@link IItemHandler} that delegates each method to another {@link IItemHandler}.
 * The {@code Supplier} is re-evaluated each time a method is called.
 */
public class ForwardingItemHandler implements IItemHandler {
    protected final Supplier<IItemHandler> delegate;

    public ForwardingItemHandler(IItemHandler delegate) {
        Objects.requireNonNull(delegate);
        this.delegate = () -> delegate;
    }

    public ForwardingItemHandler(Supplier<IItemHandler> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getSlots() {
        return delegate.get().getSlots();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return delegate.get().getStackInSlot(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return delegate.get().insertItem(slot, stack, simulate);
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        return delegate.get().extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.get().getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return delegate.get().isItemValid(slot, stack);
    }
}
