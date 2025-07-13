/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;

import java.util.function.Function;

/**
 * This class represents a "vanilla" {@code T} wrapped as a {@link Outgredient}. Common uses are {@code ResultWrapper<ItemStack>} and {@code ResultWrapper<FluidStack>}.
 * As it represents "vanilla" values, it will receive special treatment during network serialization,
 * see {@link OutgredientCodecs#makeStreamCodec(StreamCodec, StreamCodec, Function)} for how that is implemented.
 *
 * <p>Note: For construction, prefer using {@link OutgredientWrapper#item(ItemStack)} and {@link OutgredientWrapper#fluid(FluidStack)} where possible.</p>
 *
 * @param outgredient The {@code T} to use.
 * @param display     The {@link SlotDisplay} to use.
 * @param <T>         The wrapped type.
 */
public record OutgredientWrapper<T>(T outgredient, SlotDisplay display) implements Outgredient<T> {
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

    @SuppressWarnings("DataFlowIssue") // we return null in a non-null method
    @Override
    public OutgredientType<? extends Outgredient<T>> type() {
        return null;
    }
}
