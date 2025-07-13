/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import java.util.Optional;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidStackSlotDisplay;

/**
 * This interface represents a generic recipe outgredient. The outgredient can be resolved to a {@code T} when required.
 *
 * @param <T> The type of the recipe outgredient, i.e. what it ultimately resolves to.
 */
public interface Outgredient<T> {
    /**
     * Creates a new {@link ItemStack}-backed {@link Outgredient}.
     *
     * @param stack The {@link ItemStack} backing the {@link Outgredient}.
     * @return A new {@link Outgredient}.
     */
    static Outgredient<ItemStack> ofItem(ItemStack stack) {
        return new OutgredientWrapper<>(stack, new SlotDisplay.ItemStackSlotDisplay(stack));
    }

    /**
     * Creates a new {@link TagKey}-backed {@link Outgredient} with an {@link ItemLike} fallback.
     *
     * @param tagKey   The {@link TagKey} backing the {@link Outgredient}.
     * @param fallback The fallback {@link ItemLike}.
     * @return A new {@link Outgredient}.
     */
    static Outgredient<ItemStack> ofItem(TagKey<Item> tagKey, ItemLike fallback) {
        return new DefaultedItemTagOutgredient(tagKey, fallback.asItem().builtInRegistryHolder());
    }

    /**
     * Creates a new {@link TagKey}-backed {@link Outgredient} without a fallback.
     *
     * @param tagKey The {@link TagKey} backing the {@link Outgredient}.
     * @return A new {@link Outgredient}.
     */
    static Outgredient<ItemStack> ofItem(TagKey<Item> tagKey) {
        return new DefaultedItemTagOutgredient(tagKey, Optional.empty());
    }

    /**
     * Creates a new {@link TagKey}-backed {@link Outgredient} with an {@link ItemLike} fallback.
     *
     * @param tagKey   The {@link TagKey} backing the {@link Outgredient}.
     * @param fallback The fallback {@link ItemLike}.
     * @param count    The output count.
     * @return A new {@link Outgredient}.
     */
    static Outgredient<ItemStack> ofItem(TagKey<Item> tagKey, ItemLike fallback, int count) {
        return new DefaultedItemTagOutgredient(tagKey, fallback.asItem().builtInRegistryHolder());
    }

    /**
     * Creates a new {@link TagKey}-backed {@link Outgredient} without a fallback.
     *
     * @param tagKey The {@link TagKey} backing the {@link Outgredient}.
     * @param count  The output count.
     * @return A new {@link Outgredient}.
     */
    static Outgredient<ItemStack> ofItem(TagKey<Item> tagKey, int count) {
        return new DefaultedItemTagOutgredient(tagKey, Optional.empty());
    }

    /**
     * Creates a new {@link FluidStack}-backed {@link Outgredient}.
     *
     * @param stack The {@link FluidStack} backing the {@link Outgredient}.
     * @return A new {@link Outgredient}.
     */
    static Outgredient<FluidStack> ofFluid(FluidStack stack) {
        return new OutgredientWrapper<>(stack, new FluidStackSlotDisplay(stack));
    }

    /**
     * Creates a new {@link TagKey}-backed {@link Outgredient} with an {@link Fluid} fallback.
     *
     * @param tagKey   The {@link TagKey} backing the {@link Outgredient}.
     * @param fallback The fallback {@link Fluid}.
     * @param amount   The output amount.
     * @return A new {@link Outgredient}.
     */
    static Outgredient<FluidStack> ofFluid(TagKey<Fluid> tagKey, Fluid fallback, int amount) {
        return new DefaultedFluidTagOutgredient(tagKey, fallback.builtInRegistryHolder(), amount);
    }

    /**
     * Creates a new {@link TagKey}-backed {@link Outgredient} without a fallback.
     *
     * @param tagKey The {@link TagKey} backing the {@link Outgredient}.
     * @param amount The output amount.
     * @return A new {@link Outgredient}.
     */
    static Outgredient<FluidStack> ofFluid(TagKey<Fluid> tagKey, int amount) {
        return new DefaultedFluidTagOutgredient(tagKey, Optional.empty(), amount);
    }

    /**
     * Resolves the outgredient into a specific {@code T}.
     *
     * @return The resolved outgredient.
     */
    T resolve();

    /**
     * @return The registered {@link OutgredientType}.
     */
    OutgredientType<? extends Outgredient<T>> type();

    /**
     * Returns the associated {@link SlotDisplay} for the outgredient. The {@link SlotDisplay} should,
     * in similar fashion to the outgredient itself, only be resolved when actually required.
     *
     * @return A {@link SlotDisplay}.
     */
    SlotDisplay display();
}
