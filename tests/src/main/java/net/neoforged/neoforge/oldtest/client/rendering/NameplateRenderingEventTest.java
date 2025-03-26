/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

@Mod(NameplateRenderingEventTest.MODID)
@EventBusSubscriber(value = Dist.CLIENT)
public class NameplateRenderingEventTest {
    public static final String MODID = "nameplate_render_test";
    static final boolean ENABLED = false;

    @SubscribeEvent
    public static void onNameplateRender(RenderNameTagEvent.CanRender event) {
        if (!ENABLED) {
            return;
        }

        if (event.getEntityRenderer() instanceof CowRenderer) {
            event.setContent(Component.literal("Evil Cow").withStyle(ChatFormatting.RED));
            event.setCanRender(TriState.TRUE);
        }

        if (event.getEntity() instanceof Player) {
            event.setContent(event.getEntity().getDisplayName().copy().withStyle(ChatFormatting.GOLD));
        }
    }
}
