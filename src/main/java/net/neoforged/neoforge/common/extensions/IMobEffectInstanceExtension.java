/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

public interface IMobEffectInstanceExtension {
    private MobEffectInstance self() {
        return (MobEffectInstance) this;
    }

    /***
     * Checks whether the given {@link ItemStack} can cure the {@link MobEffect} or whether the {@link MobEffect}
     * can be cured by the given {@link ItemStack}
     * 
     * @param stack The ItemStack being checked
     * @return true if the given ItemStack cures the {@link MobEffect}, false otherwise
     * @see IMobEffectExtension#isCuredBy(ItemStack)
     * @see IItemExtension#cures(ItemStack,MobEffectInstance)
     */
    default boolean isCuredBy(ItemStack stack) {
        return self().getEffect().isCuredBy(stack) || stack.cures(self());
    }
}
