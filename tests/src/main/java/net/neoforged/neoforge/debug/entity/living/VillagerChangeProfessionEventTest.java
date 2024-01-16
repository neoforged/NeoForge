/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.living;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.village.VillagerChangeProfessionEvent;
import org.slf4j.Logger;

@Mod("villager_change_profession_event_test")
public class VillagerChangeProfessionEventTest {
    private static final Logger LOGGER = LogUtils.getLogger();

    public VillagerChangeProfessionEventTest() {
        NeoForge.EVENT_BUS.addListener(this::onEvent);
    }

    public void onEvent(VillagerChangeProfessionEvent event) {
        if (event.getOldProfession() == event.getNewProfession())
            return;

        if (event.getOldProfession() == VillagerProfession.NITWIT) {
            event.setNewProfession(VillagerProfession.BUTCHER);
        }
    }
}
