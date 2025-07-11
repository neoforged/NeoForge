/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.result;

import net.minecraft.world.item.crafting.display.SlotDisplay;

/**
 * Superinterface for {@link Result}-dependent {@link SlotDisplay}s.
 *
 * @param <T> The generic type of the {@link Result}.
 */
public interface ResultSlotDisplay<T> extends SlotDisplay {
    /**
     * @return The {@link Result} this display depends on.
     */
    Result<T> result();
}
