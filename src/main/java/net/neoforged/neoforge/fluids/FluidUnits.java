/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import net.minecraft.resources.ResourceLocation;

public class FluidUnits {
    /**
     * Unit representing one bucket. Value must always be {@link FluidType#BUCKET_VOLUME}.
     */
    public static final FluidUnit BUCKET = c("bucket");
    /**
     * Unit representing one millibucket. Value must always be 1.
     */
    public static final FluidUnit MB = c("mb");

    /**
     * Amount of fluid for one bottle of fluids such as potions, honey, etc.
     */
    public static final FluidUnit BOTTLE = c("bottle");
    /**
     * Amount of fluid for one bowl of stew-like fluids.
     */
    public static final FluidUnit BOWL = c("bowl");

    /**
     * Amount of fluid for one block of various molten materials.
     * Expected to be an appropriate multiple of the other units,
     * such as 9 ingots or 4 or 9 gems
     */
    public static final FluidUnit BLOCK = c("block");
    /**
     * Amount of fluid for one ingot of molten metals
     */
    public static final FluidUnit INGOT = c("ingot");
    /**
     * Amount of fluid for one nugget of molten metals
     */
    public static final FluidUnit NUGGET = c("nugget");
    /**
     * Amount of fluid for one gem of molten gem-like materials
     */
    public static final FluidUnit GEM = c("gem");

    /**
     * Amount of fluid for one slime ball for slime-like fluids
     */
    public static final FluidUnit SLIMEBALL = c("slime_ball");
    /**
     * Amount of fluid for one brick for molten clay-like fluids
     */
    public static final FluidUnit BRICK = c("brick");
    /**
     * Amount of fluid for one pane for molten glass-like fluids.
     */
    public static final FluidUnit PANE = c("pane");
    /**
     * Amount of fluid for one experience point for experience fluids
     */
    public static final FluidUnit EXPERIENCE_POINT = c("experience_point");

    private static FluidUnit c(String name) {
        return FluidUnit.get(ResourceLocation.fromNamespaceAndPath("c", name));
    }
}
