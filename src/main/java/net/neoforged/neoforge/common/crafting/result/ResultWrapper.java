/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.result;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents a {@code T} wrapped as a {@link Result}. Common uses are {@code ResultWrapper<ItemStack>} and {@code ResultWrapper<FluidStack>}.
 *
 * @param <T> The wrapped type.
 */
public class ResultWrapper<T> implements Result<T> {
    private final T result;
    private final SlotDisplay display;

    /**
     * Creates a new {@link ResultWrapper} using the provided {@code T} and {@link SlotDisplay}.
     *
     * <p>Note: Prefer using {@link ResultWrapper#item(ItemStack)} and {@link ResultWrapper#fluid(FluidStack)} where possible.
     * This method is public for when you have another kind of result, and your mod should ideally provide a helper similar to the aforementioned two methods.</p>
     *
     * @param result  The {@code T} to use.
     * @param display The {@link SlotDisplay} to use.
     */
    public ResultWrapper(T result, SlotDisplay display) {
        this.result = result;
        this.display = display;
    }

    /**
     * Creates a new {@link ItemStack}-backed {@link ResultWrapper}.
     *
     * @param stack The {@link ItemStack} backing the {@link ResultWrapper}.
     * @return A new {@link ResultWrapper}.
     */
    public static ResultWrapper<ItemStack> item(ItemStack stack) {
        return new ResultWrapper<>(stack, new SlotDisplay.ItemStackSlotDisplay(stack));
    }

    /**
     * Creates a new {@link FluidStack}-backed {@link ResultWrapper}.
     *
     * @param stack The {@link FluidStack} backing the {@link ResultWrapper}.
     * @return A new {@link ResultWrapper}.
     */
    public static ResultWrapper<FluidStack> fluid(FluidStack stack) {
        return new ResultWrapper<>(stack, new FluidStackSlotDisplay(stack));
    }

    @Override
    public T resolve() {
        return result;
    }

    @Override
    public boolean isVanilla() {
        return true;
    }

    @Override
    @Nullable
    public ResultType<? extends Result<T>> type() {
        return null;
    }

    @Override
    public SlotDisplay display() {
        return display;
    }
}
