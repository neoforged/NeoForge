/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.entity;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingFreezeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFrozenEvent;
import org.slf4j.Logger;

@Mod("living_freeze_event_test")
public class LivingFreezeEventTest {
    public static final boolean ENABLE = true;
    public static final Logger LOGGER = LogUtils.getLogger();

    public LivingFreezeEventTest() {
        if (ENABLE) {
            NeoForge.EVENT_BUS.addListener(LivingFreezeEventTest::onFreezeEvent);
            NeoForge.EVENT_BUS.addListener(LivingFreezeEventTest::onFrozenEvent);
        }
    }

    public static void onFreezeEvent(LivingFreezeEvent event) {
        event.setTicksRequiredToFreeze(10);
    }

    public static void onFrozenEvent(LivingFrozenEvent event) {
        //event.setCanceled(true);
        event.setDamageTickRate(1);
        event.setDamageAmount(4);
    }
}
