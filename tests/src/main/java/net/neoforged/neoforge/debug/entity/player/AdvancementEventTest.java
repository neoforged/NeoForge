/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.player;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent.AdvancementEarnEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent.AdvancementProgressEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent.AdvancementProgressEvent.ProgressType;
import net.neoforged.fml.common.Mod;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

@Mod(AdvancementEventTest.MOD_ID)
public class AdvancementEventTest
{
    static final String MOD_ID = "advancement_event_test";
    private static final boolean ENABLED = false;
    private static final Logger LOGGER = LogUtils.getLogger();

    public AdvancementEventTest()
    {
        if (ENABLED)
        {
            NeoForge.EVENT_BUS.addListener(this::onAdvancementEarnEvent);
            NeoForge.EVENT_BUS.addListener(this::onAdvancementProgressEvent);
        }
    }

    public void onAdvancementEarnEvent(AdvancementEarnEvent event)
    {
        AdvancementHolder advancement = event.getAdvancement();
        Player player = event.getEntity();
        LOGGER.info("Player {} earned advancement {} and was awarded {}", player, advancement.id(), advancement.value().rewards());
    }

    public void onAdvancementProgressEvent(AdvancementProgressEvent event)
    {
        AdvancementHolder advancement = event.getAdvancement();
        Player player = event.getEntity();
        AdvancementProgress advancementProgress = event.getAdvancementProgress();
        String criterionName = event.getCriterionName();
        AdvancementEvent.AdvancementProgressEvent.ProgressType progressType = event.getProgressType();
        String action;
        if (progressType == ProgressType.GRANT)
        {
            action = "granted";
        }
        else
        {
            action = "revoked";
        }
        LOGGER.info("Player {} progressed advancement {}. They were {} progress on {}. They have completed {}% of the achievement",
                player, advancement.id(), action, criterionName, advancementProgress.getPercent()*100);
    }
}
