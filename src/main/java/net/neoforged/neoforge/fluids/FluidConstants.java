/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

/**
 * Defines fluid amount constants for common resources.
 * All amounts are given in millibuckets (mb), the standard fluid unit used by NeoForge.
 */
public final class FluidConstants {
    /**
     * One bucket is by definition 1000 millibuckets.
     */
    public static final int BUCKET = 1000;
    /**
     * A molten block, if the block can be divided into 4 parts.
     *
     * @see #GEM_4
     */
    public static final int BLOCK_4 = 1000;
    /**
     * A molten block, if the block can be divided into 9 parts.
     *
     * @see #GEM_9
     * @see #INGOT
     * @see #NUGGET
     */
    public static final int BLOCK_9 = 810;
    /**
     * A molten gem, if the gem is equivalent to 1/4th of a block of some resource.
     *
     * @see #BLOCK_4 for the corresponding block
     */
    public static final int GEM_4 = 250;
    /**
     * A molten gem, if the gem is equivalent to 1/9th of a block of some resource.
     *
     * @see #BLOCK_9 for the corresponding block
     */
    public static final int GEM_9 = 90;
    /**
     * A molten ingot, i.e. 1/9th of a block of some resource that can be divided into 9 parts.
     *
     * @see #BLOCK_9 for the corresponding block
     */
    public static final int INGOT = 90;
    /**
     * A molten nugget, i.e. 1/81th of a block of some resource that can be divided into 81 parts.
     *
     * @see #BLOCK_9 for the corresponding block
     */
    public static final int NUGGET = 10;

    private FluidConstants() {}
}
