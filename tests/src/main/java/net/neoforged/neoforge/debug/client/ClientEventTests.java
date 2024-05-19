/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForge;
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
            try {
                var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                var field = bufferSource.getClass().getDeclaredField("fixedBuffers");

                field.setAccessible(true);

                var fixedBuffers = (Map<RenderType, BufferBuilder>) field.get(bufferSource);

                if (fixedBuffers != null && fixedBuffers.containsKey(RenderType.lightning())) {
                    test.pass();
                } else {
                    test.fail("The render buffer for the specified render type was not registered");
                }
            } catch (Exception e) {
                test.fail("Failed to access fixed buffers map");
            }
        });
    }

    @TestHolder(description = { "Tests if adding custom geometry to chunks works", "When the message \"diamond block\" is sent in chat, this should render a fake diamond block above the player's position" })
    static void addSectionGeometryTest(final ClientChatEvent chatEvent, final DynamicTest test) {
        if (chatEvent.getMessage().equalsIgnoreCase("diamond block")) {
            var player = Minecraft.getInstance().player;
            var testBlockAt = player.blockPosition().above(3);
            var section = SectionPos.of(testBlockAt);
            var sectionOrigin = section.origin();
            NeoForge.EVENT_BUS.addListener((final AddSectionGeometryEvent event) -> {
                if (event.getSectionOrigin().equals(sectionOrigin)) {
                    event.addRenderer(context -> {
                        var poseStack = context.getPoseStack();
                        poseStack.pushPose();
                        poseStack.translate(
                                testBlockAt.getX() - sectionOrigin.getX(),
                                testBlockAt.getY() - sectionOrigin.getY(),
                                testBlockAt.getZ() - sectionOrigin.getZ());
                        var renderType = RenderType.solid();
                        Minecraft.getInstance().getBlockRenderer().renderBatched(
                                Blocks.DIAMOND_BLOCK.defaultBlockState(),
                                testBlockAt,
                                context.getRegion(),
                                poseStack,
                                context.getOrCreateChunkBuffer(renderType),
                                false,
                                new SingleThreadedRandomSource(0),
                                ModelData.EMPTY,
                                renderType);
                        poseStack.popPose();
                    });
                }
            });
            Minecraft.getInstance().levelRenderer.setSectionDirty(section.x(), section.y(), section.z());
            test.requestConfirmation(player, Component.literal("Is a diamond block rendered above you?"));
        }
    }
}
