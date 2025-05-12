/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.internal;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.SelfTest;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class SelfTestClient {
    public static void initClient() {
        var clientSelfTestDestination = System.getenv("NEOFORGE_CLIENT_SELFTEST");
        if (clientSelfTestDestination != null) {
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre e) -> {
                if (Minecraft.getInstance().getOverlay() instanceof LoadingOverlay) {
                    return;
                }
                if (Minecraft.getInstance().isRunning()) {
                    SelfTest.writeSelfTestReport(clientSelfTestDestination);
                    Minecraft.getInstance().stop();
                }
            });
        }
    }
}
