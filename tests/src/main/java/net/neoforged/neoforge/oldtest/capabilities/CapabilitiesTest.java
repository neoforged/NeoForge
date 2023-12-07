/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.capabilities;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;

@Mod(CapabilitiesTest.MODID)
public class CapabilitiesTest {
    public static final String MODID = "capabilities_test";

    public CapabilitiesTest(IEventBus modBus) {
        modBus.addListener(CapabilitiesTest::registerGameTests);
    }

    private static void registerGameTests(RegisterGameTestsEvent event) {
        event.register(VanillaItemHandlerTests.class);
    }
}
