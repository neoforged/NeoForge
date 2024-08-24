/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.DimensionTransitionScreen;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(side = Dist.CLIENT, groups = DimensionTransitionScreenTests.GROUP)
public class DimensionTransitionScreenTests {
    public static final String GROUP = "dimension_transition";
    public static final ResourceLocation NETHER_BG = ResourceLocation.withDefaultNamespace("textures/block/netherrack.png");
    public static final ResourceLocation END_BG = ResourceLocation.withDefaultNamespace("textures/block/end_stone.png");

    @EmptyTemplate
    @TestHolder(description = "Tests if a custom dimension transition screen is properly displayed when exiting the Nether", enabledByDefault = true)
    static void netherOutgoingTransition(DynamicTest test) {
        test.framework().modEventBus().addListener((RegisterDimensionTransitionScreenEvent event) -> {
            event.registerOutgoingEffect(Level.NETHER, new DimensionTransitionScreen() {
                @Override
                public boolean renderScreenEffect(GuiGraphics graphics, float partialTicks, int screenWidth, int screenHeight) {
                    graphics.blit(NETHER_BG, 0, 0, 0, 0.0F, 0.0F, screenWidth, screenHeight, 32, 32);
                    return true;
                }

                @Override
                public void renderTransitionText(Font font, GuiGraphics graphics, int screenWidth, int screenHeight) {
                    graphics.drawCenteredString(font, "This displays when returning from the nether!", screenWidth / 2, screenHeight / 2 - 50, 0xFFFFFF);
                }
            });
        });

        test.eventListeners().forge().addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> {
            Player player = event.getEntity();
            if (event.getFrom() == Level.NETHER) {
                test.requestConfirmation(player, Component.literal("Did the screen display a netherrack background when traveling through the portal?"));
            }
        });
    }

    @EmptyTemplate
    @TestHolder(description = "Tests if a custom dimension transition screen is properly displayed when entering the End", enabledByDefault = true)
    static void endIncomingTransition(DynamicTest test) {
        test.framework().modEventBus().addListener((RegisterDimensionTransitionScreenEvent event) -> {
            event.registerIncomingEffect(Level.END, new DimensionTransitionScreen() {
                @Override
                public boolean renderScreenEffect(GuiGraphics graphics, float partialTicks, int screenWidth, int screenHeight) {
                    graphics.blit(END_BG, 0, 0, 0, 0.0F, 0.0F, screenWidth, screenHeight, 32, 32);
                    return true;
                }

                @Override
                public void renderTransitionText(Font font, GuiGraphics graphics, int screenWidth, int screenHeight) {
                    graphics.drawCenteredString(font, "This displays when going to the end!", screenWidth / 2, screenHeight / 2 - 50, 0xFFFFFF);
                }
            });
        });

        test.eventListeners().forge().addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> {
            Player player = event.getEntity();
            if (event.getTo() == Level.END) {
                test.requestConfirmation(player, Component.literal("Did the screen display an end stone background when traveling through the portal?"));
            }
        });
    }
}
