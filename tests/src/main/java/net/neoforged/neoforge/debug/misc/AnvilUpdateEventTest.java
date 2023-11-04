/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.misc;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import org.slf4j.Logger;

/**
 * This class provides a way to test the AnvilUpdateEvent. <br>
 * The event is now fired as described in the javadoc (of AnvilUpdateEvent). <br>
 * "AnvilUpdateEvent is fired whenever the input stacks (left or right) or the name in an anvil changed."
 */
@Mod(AnvilUpdateEventTest.MOD_ID)
public class AnvilUpdateEventTest {
    private static final boolean ENABLED = false;
    private static final Logger LOGGER = LogUtils.getLogger();
    static final String MOD_ID = "anvil_update_event_fix";

    public AnvilUpdateEventTest() {
        if (ENABLED) {
            NeoForge.EVENT_BUS.addListener(this::anvilUpdate);
        }
    }

    private void anvilUpdate(AnvilUpdateEvent event) {
        LOGGER.info("Anvil input or name changed!");
    }
}
