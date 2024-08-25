/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterDimensionTransitionScreenEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(side = Dist.CLIENT, groups = { DimensionTransitionScreenTests.GROUP, "manual" })
public class DimensionTransitionScreenTests {
    public static final String GROUP = "dimension_transition";
    public static final ResourceLocation NETHER_BG = ResourceLocation.withDefaultNamespace("textures/block/netherrack.png");
    public static final ResourceLocation END_BG = ResourceLocation.withDefaultNamespace("textures/block/end_stone.png");

    @EmptyTemplate
    @TestHolder(description = "Tests if a custom dimension transition screen is properly displayed when exiting the Nether")
    static void netherOutgoingTransition(DynamicTest test) {
        test.framework().modEventBus().addListener((RegisterDimensionTransitionScreenEvent event) -> event.registerOutgoingEffect(Level.NETHER, (supplier, reason) -> new CustomLevelScreen(supplier, reason, NETHER_BG, Component.literal("This displays when returning from the nether!"))));

        test.eventListeners().forge().addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> {
            Player player = event.getEntity();
            if (event.getFrom() == Level.NETHER) {
                test.requestConfirmation(player, Component.literal("Did the screen display a netherrack background when traveling through the portal?"));
            }
        });
    }

    @EmptyTemplate
    @TestHolder(description = "Tests if a custom dimension transition screen is properly displayed when entering the End")
    static void endIncomingTransition(DynamicTest test) {
        test.framework().modEventBus().addListener((RegisterDimensionTransitionScreenEvent event) -> event.registerIncomingEffect(Level.END, (supplier, reason) -> new CustomLevelScreen(supplier, reason, END_BG, Component.literal("This displays when going to the end!"))));

        test.eventListeners().forge().addListener((PlayerEvent.PlayerChangedDimensionEvent event) -> {
            Player player = event.getEntity();
            if (event.getTo() == Level.END) {
                test.requestConfirmation(player, Component.literal("Did the screen display an end stone background when traveling through the portal?"));
            }
        });
    }

    public static class CustomLevelScreen extends ReceivingLevelScreen {
        private final ResourceLocation bgTexture;
        private final Component message;

        public CustomLevelScreen(BooleanSupplier supplier, Reason reason, ResourceLocation bgTexture, Component message) {
            super(supplier, reason);
            this.bgTexture = bgTexture;
            this.message = message;
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            this.renderBackground(graphics, mouseX, mouseY, partialTick);
            graphics.drawCenteredString(this.font, this.message, this.width / 2, this.height / 2 - 50, 0xFFFFFF);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(this.bgTexture, 0, 0, 0, 0.0F, 0.0F, this.width, this.height, 32, 32);
        }
    }
}
