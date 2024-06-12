/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities.color;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a colorable object in-game.
 */
public interface IColorable {
    /**
     * Suggests to an applicator whether applying color to the target should consume some quantity
     * of dye/ink/etc. Implementation of consumption is left as an exercise to the applicator.
     *
     * @return True if dye should be considered consumed on success; false otherwise.
     */
    default boolean consumesDye(@Nullable LivingEntity entity, ItemStack source) {
        return source.is(Tags.Items.DYES);
    }

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
