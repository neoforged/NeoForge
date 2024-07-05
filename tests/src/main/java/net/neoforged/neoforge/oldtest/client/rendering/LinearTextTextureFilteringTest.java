/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.event.ScreenEvent.Render;

@Mod(LinearTextTextureFilteringTest.MODID)
@EventBusSubscriber(value = Dist.CLIENT)
public class LinearTextTextureFilteringTest {
    public static final String MODID = "text_linear_filtering_test";
    static final boolean ENABLED = false;

    @SubscribeEvent
    public static void onGuiRenderPre(Render.Pre event) {
        if (ENABLED && event.getScreen() instanceof TitleScreen) {
            NeoForgeRenderTypes.enableTextTextureLinearFiltering = true;
        }
    }

    @SubscribeEvent
    public static void onGuiRenderPost(Render.Post event) {
        if (ENABLED && event.getScreen() instanceof TitleScreen) {
            NeoForgeRenderTypes.enableTextTextureLinearFiltering = false;
        }
    }
}
