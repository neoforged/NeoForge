/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities.color.vanilla;

import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.capabilities.color.ColorApplicationResult;
import net.neoforged.neoforge.capabilities.color.IColorable;

public record SheepDyeConsumer(Sheep sheep) implements IColorable {
    public ColorApplicationResult apply(DyeColor dye) {
        if (!sheep.isAlive() || sheep.isSheared()) {
            return ColorApplicationResult.CANNOT_APPLY;
        }

        if (sheep.getColor() != dye) {
            sheep.setColor(dye);
            return ColorApplicationResult.APPLIED;
        } else {
            return ColorApplicationResult.ALREADY_APPLIED;
        }
    }
}
