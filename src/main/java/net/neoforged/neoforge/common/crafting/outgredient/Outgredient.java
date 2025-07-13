/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import net.minecraft.world.item.crafting.display.SlotDisplay;

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
