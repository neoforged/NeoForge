/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.mixins;

import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Villager.class)
public interface VillagerAccessor {
    /**
     * For {@link net.neoforged.neoforge.debug.entity.TradeTests} to be able to test for presence of all villager trades.
     */
    @Invoker("increaseMerchantCareer")
    void neoforge$callIncreaseMerchantCareer();
}
