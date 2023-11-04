/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client.rendering;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;

@Mod("render_level_stages_test")
public class RenderLevelStagesTest {
    public static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();

    public RenderLevelStagesTest() {
        if (FMLLoader.getDist().isClient()) {
            NeoForge.EVENT_BUS.addListener(this::onRenderLevelStages);
        }
    }

    private int count = 0;

    public void onRenderLevelStages(RenderLevelStageEvent event) {
        if (count < 10) {
            LOGGER.info("Render Stage: " + event.getStage());
            if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER)
                count++;
        }
    }
}
