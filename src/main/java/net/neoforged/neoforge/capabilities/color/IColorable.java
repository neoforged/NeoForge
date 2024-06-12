/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities.color;

import net.minecraft.world.item.DyeColor;

/**
 * Represents a colorable object in-game.
 */
public interface IColorable {
    /**
     * Apply a packed RGB value to the object.
     * For implementations that support alpha values, see {@link #apply(int, int)}.
     *
     * @param rgb Packed RGB color value.
     * @return An application result.
     */
    default ColorApplicationResult apply(int rgb) {
        return apply(rgb, 0xFF);
    }

    /**
     * Implementers that support alpha can use FastColor.ARGB#color(int, int) to re-pack the alpha component.
     *
     * @param rgb   Packed RGB color value.
     * @param alpha Alpha color value.
     * @return An application result.
     */
    default ColorApplicationResult apply(int rgb, int alpha) {
        return ColorApplicationResult.CANNOT_APPLY;
    }

    /**
     * Apply a vanilla dye color to the object.
     *
     * @param dye Dye color to apply.
     * @return An application result.
     */
    ColorApplicationResult apply(DyeColor dye);
}
