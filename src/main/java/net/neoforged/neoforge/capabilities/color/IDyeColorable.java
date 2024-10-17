/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities.color;

import net.minecraft.world.item.DyeColor;

/**
 * Represents a dye-colorable object in-game.
 */
public interface IDyeColorable {
    /**
     * Apply a vanilla dye color to the object.
     *
     * @param dye Dye color to apply.
     * @return An application result.
     */
    ColorApplicationResult apply(DyeColor dye);
}
