/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.living;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.npc.Villager;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingConversionEvent;

@Mod("living_conversion_event_test")
public class LivingConversionEventTest {
    public LivingConversionEventTest() {
        NeoForge.EVENT_BUS.addListener(this::canLivingConversion);
        NeoForge.EVENT_BUS.addListener(this::onLivingConversion);
    }

    public void canLivingConversion(LivingConversionEvent.Pre event) {
        if (event.getEntity() instanceof Piglin) {
            event.setCanceled(true);
            event.setConversionTimer(0);
        }
    }

    public void onLivingConversion(LivingConversionEvent.Post event) {
        if (event.getEntity() instanceof Villager)
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.LUCK, 20));
    }
}
