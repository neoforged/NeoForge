/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.ForgeRegistries;
import net.neoforged.neoforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Checks that {@link RegistryObject} works correctly, specifically that get() functions immediately
 * after construction, if registries are already populated.
 */
@Mod(RegistryObjectTest.MODID)
public class RegistryObjectTest {

    static final String MODID = "registry_object_test";

    private static final boolean ENABLED = true;

    private static final Logger LOGGER = LogManager.getLogger();

    public RegistryObjectTest() {
        if (!ENABLED) return;

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::commonSetup);
    }

    public void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Stone 1: {}", RegistryObject.create(new ResourceLocation("minecraft", "stone"), ForgeRegistries.BLOCKS).get());
        LOGGER.info("Stone 2: {}", RegistryObject.create(new ResourceLocation("minecraft", "stone"), ForgeRegistries.Keys.BLOCKS, MODID).get());
    }
}
