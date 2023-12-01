/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Checks that {@link DeferredHolder} works correctly, specifically that get() functions immediately
 * after construction, if registries are already populated.
 */
@Mod(DeferredHolderTest.MODID)
public class DeferredHolderTest {

    static final String MODID = "deferred_holder_test";

    private static final boolean ENABLED = true;

    private static final Logger LOGGER = LogManager.getLogger();

    public DeferredHolderTest(ModContainer modContainer) {
        if (!ENABLED) return;

        modContainer.getEventBus().addListener(this::commonSetup);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Stone 1: {}", DeferredHolder.create(Registries.BLOCK, new ResourceLocation("minecraft", "stone")).get());
    }
}
