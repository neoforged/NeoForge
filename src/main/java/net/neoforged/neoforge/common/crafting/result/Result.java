/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.result;

import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jetbrains.annotations.Nullable;

/**
 * This interface represents a generic recipe result. The result can be resolved to a {@code T} when required.
 *
 * @param <T> The type of the recipe result, i.e. what it ultimately resolves to.
 */
public interface Result<T> {
    /**
     * Resolves the result into a specific {@code T}.
     *
     * @return The resolved result.
     */
    T resolve();

    /**
     * Returns whether the result is considered to be "from vanilla".
     * A result that is "from vanilla" will not be subject to a type-dependent registry lookup.
     * As such, if this method returns {@code true}, {@link Result#type()} is expected to be {@code null}.
     *
     * @return Whether the result is considered to be "from vanilla".
     * @see ResultCodecs#ITEM_STACK_RESULT_CODEC
     */
    default boolean isVanilla() {
        return false;
    }

    /**
     * @return The registered {@link ResultType}. This is expected to be {@code null} if and only if {@link Result#isVanilla()} is {@code true}.
     */
    @Nullable
    ResultType<? extends Result<T>> type();

    /**
     * Returns the associated {@link SlotDisplay} for the result. The {@link SlotDisplay} should,
     * in similar fashion to the result itself, only be resolved when actually required.
     *
     * @return A {@link SlotDisplay}.
     */
    SlotDisplay display();
}
