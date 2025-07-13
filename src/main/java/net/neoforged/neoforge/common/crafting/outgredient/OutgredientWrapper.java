/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;
import org.jetbrains.annotations.Nullable;

/**
 * This class represents a {@code T} wrapped as a {@link Outgredient}. Common uses are {@code ResultWrapper<ItemStack>} and {@code ResultWrapper<FluidStack>}.
 *
 * @param <T> The wrapped type.
 */
public class OutgredientWrapper<T> implements Outgredient<T> {
    private final T outgredient;
    private final SlotDisplay display;

    /**
     * Creates a new {@link OutgredientWrapper} using the provided {@code T} and {@link SlotDisplay}.
     *
     * <p>Note: Prefer using {@link OutgredientWrapper#item(ItemStack)} and {@link OutgredientWrapper#fluid(FluidStack)} where possible.
     * This method is public for when you have another kind of outgredient, and your mod should ideally provide a helper similar to the aforementioned two methods.</p>
     *
     * @param outgredient The {@code T} to use.
     * @param display     The {@link SlotDisplay} to use.
     */
    public OutgredientWrapper(T outgredient, SlotDisplay display) {
        this.outgredient = outgredient;
        this.display = display;
    }

    /**
     * Creates a new {@link ItemStack}-backed {@link OutgredientWrapper}.
     *
     * @param stack The {@link ItemStack} backing the {@link OutgredientWrapper}.
     * @return A new {@link OutgredientWrapper}.
     */
    public static OutgredientWrapper<ItemStack> item(ItemStack stack) {
        return new OutgredientWrapper<>(stack, new SlotDisplay.ItemStackSlotDisplay(stack));
    }

    /**
     * Creates a new {@link FluidStack}-backed {@link OutgredientWrapper}.
     *
     * @param stack The {@link FluidStack} backing the {@link OutgredientWrapper}.
     * @return A new {@link OutgredientWrapper}.
     */
    public static OutgredientWrapper<FluidStack> fluid(FluidStack stack) {
        return new OutgredientWrapper<>(stack, new FluidStackSlotDisplay(stack));
    }

    @Override
    public T resolve() {
        return outgredient;
    }

    @Override
    public boolean isVanilla() {
        return true;
    }

    @Override
    @Nullable
    public OutgredientType<? extends Outgredient<T>> type() {
        return null;
    }

    @Override
    public SlotDisplay display() {
        return display;
    }
}
