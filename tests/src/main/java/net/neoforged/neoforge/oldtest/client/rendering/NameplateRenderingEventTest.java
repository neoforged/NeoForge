/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client.rendering;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;

@Mod(NameplateRenderingEventTest.MODID)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class NameplateRenderingEventTest {
    public static final String MODID = "nameplate_render_test";
    static final boolean ENABLED = false;

    @SubscribeEvent
    public static void onNameplateRender(RenderNameTagEvent event) {
        if (!ENABLED) {
            return;
        }

        if (event.getEntity() instanceof Cow) {
            event.setContent(Component.literal("Evil Cow").withStyle(ChatFormatting.RED));
            event.setResult(Event.Result.ALLOW);
        }

        if (event.getEntity() instanceof Player) {
            event.setContent(event.getEntity().getDisplayName().copy().withStyle(ChatFormatting.GOLD));
        }
    }
}
