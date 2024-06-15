/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.fluids;

/**
 * Constants for fluid amounts. All values are in "millibuckets", which are 1/1000 of a bucket.
 */
public class FluidConstants {
    /**
     * Represents the amount of fluid in a bucket.
     */
    public static final int BUCKET = 1000;

    /**
     * Represents the amount of fluid in a bottle. This value applies to all bottles that hold a liquid in game, though
     * not all bottles will return a handler to manipulate their fluid.
     */
    public static final int BOTTLE = 250;

    /**
     * Represents the amount of fluid a storage blocks melt into. Blocks like iron, gold, diamond and most other blocks
     * made up of 9 items will melt into this amount (assuming the item melts into {@link #INGOT}).
     */
    public static final int NINE_X_STORAGE_BLOCK = 810;

    /**
     * Represents the amount of fluid a standard ingot melts into. Iron and gold ingots will melt into this amount.
     * This value is 1/9th of {@link #NINE_X_STORAGE_BLOCK}.
     */
    public static final int INGOT = 90;

    /**
     * Represents the amount of fluid a standard nugget melts into. Iron and gold nuggets will melt into this amount.
     * This value is 1/9th of {@link #INGOT}.
     */
    public static final int NUGGET = 10;
}
