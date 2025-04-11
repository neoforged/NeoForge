/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.ao;

/**
 * Calculated AO values for a full face. Mutable so that instances can be reused.
 */
class AoCalculatedFace {
    float brightness0;
    float brightness1;
    float brightness2;
    float brightness3;
    int lightmap0;
    int lightmap1;
    int lightmap2;
    int lightmap3;
}
