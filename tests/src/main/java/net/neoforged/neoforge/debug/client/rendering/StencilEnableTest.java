/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client.rendering;

import net.minecraft.client.Minecraft;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod("stencil_enable_test")
public class StencilEnableTest {
    public static boolean ENABLED = true;

    public StencilEnableTest(ModContainer modContainer) {
        modContainer.getEventBus().addListener(this::clientSetup);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        if (ENABLED)
            event.enqueueWork(() -> Minecraft.getInstance().getMainRenderTarget().enableStencil());
    }
}
