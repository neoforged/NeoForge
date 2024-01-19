/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(side = Dist.CLIENT, groups = { "client.event", "event" })
public class ClientEventTests {
    @TestHolder(description = { "Tests if the client chat event allows message modifications", "Will delete 'Cancel' and replace 'Replace this text'" })
    static void playerClientChatEvent(final ClientChatEvent event, final DynamicTest test) {
        if (event.getMessage().equals("Cancel")) {
            event.setCanceled(true);
            Minecraft.getInstance().tell(() -> test.requestConfirmation(Minecraft.getInstance().player, Component.literal("Was your message deleted?")));
        } else if (event.getMessage().equals("Replace this text")) {
            event.setMessage("Text replaced.");
            Minecraft.getInstance().tell(() -> test.requestConfirmation(Minecraft.getInstance().player, Component.literal("Was your message modified?")));
        }
    }

    @TestHolder(description = { "Tests if the ClientPlayerChangeGameTypeEvent event is fired", "Will ask the player for confirmation when the player changes their gamemode" })
    static void clientPlayerChangeGameTypeEvent(final ClientPlayerChangeGameTypeEvent event, final DynamicTest test) {
        test.requestConfirmation(Minecraft.getInstance().player, Component.literal("Did you just change your game mode from " + event.getCurrentGameType() + " to " + event.getNewGameType() + "?"));
    }

    @TestHolder(description = { "Tests if the RegisterRenderBuffersEvent event is fired and whether the registered render buffer is represented within a fixed render buffer map" }, enabledByDefault = true)
    static void registerRenderBuffersEvent(final DynamicTest test) {
        test.framework().modEventBus().addListener((final RegisterRenderBuffersEvent event) -> {
            event.registerRenderBuffer(RenderType.lightning());
        });
        test.framework().modEventBus().addListener((final RenderLevelStageEvent.RegisterStageEvent event) -> {
            Map<RenderType, BufferBuilder> fixedBuffers = ObfuscationReflectionHelper.getPrivateValue(MultiBufferSource.BufferSource.class, Minecraft.getInstance().renderBuffers().bufferSource(), "fixedBuffers");

            if (fixedBuffers != null && fixedBuffers.containsKey(RenderType.lightning())) {
                test.pass();
            } else {
                test.fail("The render buffer for the specified render type was not registered");
            }
        });
    }
}
