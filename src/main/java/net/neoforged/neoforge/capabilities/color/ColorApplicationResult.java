/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities.color;

/**
 * For usage with {@link IColorable}; represents the result of trying to apply color to something.
 */
public enum ColorApplicationResult {
    /**
     * Implies that the color was read by the consuming object, and determined that
     * application will not change state.
     * Ex: Tried to use black dye on an already-black sheep
     */
    ALREADY_APPLIED,

    /**
     * Implies that the application of color was successful, and something about the
     * consuming object has changed.
     * Ex: Dyed a white sheep red.
     */
    APPLIED,

    /**
     * The consuming object has rejected the application of color, as it either cannot accept
     * the level of detail, or color cannot currently be applied to the object.
     * Ex: Destination only allows dye colors, full ARGB not supported
     */
    CANNOT_APPLY
}
