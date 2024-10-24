/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities.color;

/**
 * Represents a colorable object in-game.
 */
public interface IColorable {
    /**
     * Apply a packed ARGB value to the object.
     *
     * @param argb Packed ARGB color value.
     * @return An application result.
     */
    ColorApplicationResult apply(int argb);
}
