/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import net.minecraft.world.item.crafting.display.SlotDisplay;

/**
 * Superinterface for {@link Outgredient}-dependent {@link SlotDisplay}s.
 *
 * @param <T> The generic type of the {@link Outgredient}.
 */
public interface OutgredientSlotDisplay<T> extends SlotDisplay {
    /**
     * @return The {@link Outgredient} this display depends on.
     */
    Outgredient<T> outgredient();
}
