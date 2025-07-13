/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Nullable;

/**
 * This interface represents a generic recipe outgredient. The outgredient can be resolved to a {@code T} when required.
 *
 * @param <T> The type of the recipe outgredient, i.e. what it ultimately resolves to.
 */
public interface Outgredient<T> {
    /**
     * Resolves the outgredient into a specific {@code T}.
     *
     * @return The resolved outgredient.
     */
    T resolve();

    /**
     * Returns whether the outgredient is considered to be "from vanilla".
     * An outgredient that is "from vanilla" will not be subject to a type-dependent registry lookup.
     * As such, if this method returns {@code true}, {@link Outgredient#type()} is expected to be {@code null}.
     *
     * @return Whether the outgredient is considered to be "from vanilla".
     * @see OutgredientCodecs#ITEM_STACK_OUTGREDIENT_CODEC
     */
    default boolean isVanilla() {
        return false;
    }

    /**
     * @return The registered {@link OutgredientType}. This is expected to be {@code null} if and only if {@link Outgredient#isVanilla()} is {@code true}.
     */
    @Nullable
    OutgredientType<? extends Outgredient<T>> type();

    /**
     * Returns the associated {@link SlotDisplay} for the outgredient. The {@link SlotDisplay} should,
     * in similar fashion to the outgredient itself, only be resolved when actually required.
     *
     * @return A {@link SlotDisplay}.
     */
    SlotDisplay display();
}
