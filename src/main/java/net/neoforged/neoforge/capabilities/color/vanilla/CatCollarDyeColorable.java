/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities.color.vanilla;

import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.capabilities.color.ColorApplicationResult;
import net.neoforged.neoforge.capabilities.color.IDyeColorable;

public record CatCollarDyeColorable(Cat cat) implements IDyeColorable {
    @Override
    public ColorApplicationResult apply(DyeColor dyecolor) {
        if (dyecolor != cat.getCollarColor()) {
            cat.setCollarColor(dyecolor);
            cat.setPersistenceRequired();

            return ColorApplicationResult.APPLIED;
        } else {
            return ColorApplicationResult.ALREADY_APPLIED;
        }
    }
}
