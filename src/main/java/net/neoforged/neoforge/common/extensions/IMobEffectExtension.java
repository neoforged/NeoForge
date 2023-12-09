/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface IMobEffectExtension {
    private MobEffect self() {
        return (MobEffect) this;
    }

    /***
     * Checks whether this {@link MobEffect} can be cured by the given {@link ItemStack}
     *
     * @param stack The {@link ItemStack} being checked
     * @return true if the given {@link ItemStack} cures this effect, false otherwise
     * @see IMobEffectInstanceExtension#isCuredBy(ItemStack)
     */
    default boolean isCuredBy(ItemStack stack) {
        return stack.is(Items.MILK_BUCKET);
    }

    /**
     * Used for determining {@link MobEffect} sort order in GUIs.
     * Defaults to the {@link MobEffect}'s liquid color.
     * 
     * @param effectInstance The {@link MobEffectInstance} containing this {@link MobEffect}
     * @return a value used to sort {@link MobEffect}s in GUIs
     */
    default int getSortOrder(MobEffectInstance effectInstance) {
        return self().getColor();
    }
}
