/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.ao;

import net.neoforged.neoforge.client.model.LightingMode;

public enum AoConfig {
    /**
     * Always use vanilla logic. Effectively disables the enhanced AO pipeline.
     */
    VANILLA,
    /**
     * Always use vanilla-like logic.
     * The differences to {@link #VANILLA} are that this mode will give correct results even
     * if the vertex winding order is wrong, that it will cache AO faces which can provide
     * a benefit if there are many quads per block, and that any other bug fixes will be applied.
     * Faces that are not on the edge of a block will suffer from the same issues that vanilla has.
     */
    EMULATE,
    /**
     * Uses a quad's {@link LightingMode} to select between {@link #EMULATE} and {@link #ENHANCED}.
     * This is the default mode, and provides a good balance between preserving the look of vanilla models
     * and giving the option to opt into more advanced lighting.
     */
    HYBRID,
    /**
     * Use enhanced logic for all quads.
     * Tries to look correct and consistent in all cases, at the cost of some divergence from vanilla.
     */
    ENHANCED;
}
