/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities.color.vanilla;

import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.capabilities.color.ColorApplicationResult;
import net.neoforged.neoforge.capabilities.color.IDyeColorable;

public record WolfCollarDyeColorable(Wolf wolf) implements IDyeColorable {
    @Override
    public ColorApplicationResult apply(DyeColor dyecolor) {
        if (dyecolor != wolf.getCollarColor()) {
            wolf.setCollarColor(dyecolor);
            return ColorApplicationResult.APPLIED;
        }

        return ColorApplicationResult.CANNOT_APPLY;
    }
}
