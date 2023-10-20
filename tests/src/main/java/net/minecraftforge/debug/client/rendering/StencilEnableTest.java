/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.debug.client.rendering;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.DeferredWorkQueue;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("stencil_enable_test")
public class StencilEnableTest {
    public static boolean ENABLED = true;

    public StencilEnableTest() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        if (ENABLED)
            event.enqueueWork(() -> Minecraft.getInstance().getMainRenderTarget().enableStencil());
    }
}
