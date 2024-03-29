/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import java.util.Objects;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(groups = "client.gui", side = Dist.CLIENT)
public class GuiTests {
    @TestHolder(description = "Adds a button to containers that prompts the user a layered GUI and asks them whether they saw it")
    static void testGuiLayering(final DynamicTest test) {
        test.eventListeners().forge().addListener((final ScreenEvent.Init.Post event) -> {
            if (event.getScreen() instanceof AbstractContainerScreen) {
                event.addListener(Button.builder(Component.literal("Test Gui Layering"), btn -> {
                    Minecraft.getInstance().pushGuiLayer(new TestLayer(Component.literal("LayerScreen")));
                    test.requestConfirmation(Minecraft.getInstance().player, Component.literal("Did the layered GUIs work?"));
                }).pos(2, 2).size(150, 20).build());

                event.addListener(Button.builder(Component.literal("Test Gui Normal"), btn -> {
                    Minecraft.getInstance().setScreen(new TestLayer(Component.literal("LayerScreen")));
                    test.requestConfirmation(Minecraft.getInstance().player, Component.literal("Did the layered GUIs work?"));
                }).pos(2, 25).size(150, 20).build());
            }
        });
    }

    @TestHolder(description = "Tests if the potion size event is fired", groups = "event")
    static void testPotionSizeEvent(final ScreenEvent.RenderInventoryMobEffects event, final DynamicTest test) {
        final LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);

        if (player.getActiveEffects().size() <= 3) {
            event.setCompact(true); // Force compact mode for 3 or less active effects
        } else {
            event.setCompact(false); // Force classic mode for 4 or more active effects
        }
        if (player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            event.addHorizontalOffset(20); // Move the effect rendering to the right when slowness is enabled
        }

        test.pass();
    }

    private static class TestLayer extends Screen {
        private static final Random RANDOM = new Random();

        protected TestLayer(Component titleIn) {
            super(titleIn);
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            super.renderBackground(graphics, mouseX, mouseY, partialTicks);
            graphics.drawString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        }

        @Override
        protected void init() {
            int buttonWidth = 150;
            int buttonHeight = 30;
            int buttonGap = 4;
            int buttonSpacing = (buttonHeight + buttonGap);
            int buttons = 3;

            int xoff = (this.width - buttonWidth);
            int yoff = (this.height - buttonHeight - buttonSpacing * (buttons - 1));
            int cnt = 0;

            xoff = RANDOM.nextInt(xoff);
            yoff = RANDOM.nextInt(yoff);

            this.addRenderableWidget(Button.builder(Component.literal("Push New Layer"), this::pushLayerButton).pos(xoff, yoff + buttonSpacing * (cnt++)).size(buttonWidth, buttonHeight).build(ExtendedButton::new));
            this.addRenderableWidget(Button.builder(Component.literal("Pop Current Layer"), this::popLayerButton).pos(xoff, yoff + buttonSpacing * (cnt++)).size(buttonWidth, buttonHeight).build(ExtendedButton::new));
            this.addRenderableWidget(Button.builder(Component.literal("Close entire stack"), this::closeStack).pos(xoff, yoff + buttonSpacing * (cnt++)).size(buttonWidth, buttonHeight).build(ExtendedButton::new));

            this.addRenderableWidget(new ExtendedSlider(xoff, yoff + buttonSpacing * cnt, 50, 25, Component.literal("Val: ").withStyle(ChatFormatting.GOLD), Component.literal("some text which will be cut off"), 5, 55, 6, true));
        }

        private void closeStack(Button button) {
            this.minecraft.setScreen(null);
        }

        private void popLayerButton(Button button) {
            this.minecraft.popGuiLayer();
        }

        private void pushLayerButton(Button button) {
            this.minecraft.pushGuiLayer(new TestLayer(Component.literal("LayerScreen")));
        }
    }

    @TestHolder(description = "Checks that GUI layers can move hearts, air bubbles, etc")
    static void testGuiLayerLeftRightHeight(DynamicTest test) {
        test.framework().modEventBus().addListener((RegisterGuiLayersEvent event) -> {
            event.registerBelow(VanillaGuiLayers.PLAYER_HEALTH, new ResourceLocation(test.createModId(), "left1"), makeLeftOverlay(test, 3, 0x80FF0000));
            event.registerBelow(VanillaGuiLayers.ARMOR_LEVEL, new ResourceLocation(test.createModId(), "left2"), makeLeftOverlay(test, 3, 0x80CC0000));
            event.registerAbove(VanillaGuiLayers.ARMOR_LEVEL, new ResourceLocation(test.createModId(), "left3"), makeLeftOverlay(test, 3, 0x80990000));

            event.registerBelow(VanillaGuiLayers.FOOD_LEVEL, new ResourceLocation(test.createModId(), "right1"), makeRightOverlay(test, 2, 0x8000FF00));
            event.registerBelow(VanillaGuiLayers.VEHICLE_HEALTH, new ResourceLocation(test.createModId(), "right2"), makeRightOverlay(test, 2, 0x8000DD00));
            event.registerBelow(VanillaGuiLayers.AIR_LEVEL, new ResourceLocation(test.createModId(), "right3"), makeRightOverlay(test, 2, 0x8000BB00));
            event.registerAbove(VanillaGuiLayers.AIR_LEVEL, new ResourceLocation(test.createModId(), "right4"), makeRightOverlay(test, 2, 0x80009900));
        });

        test.eventListeners().forge().addListener((ClientChatEvent chatEvent) -> {
            if (chatEvent.getMessage().equalsIgnoreCase("gui layer test")) {
                test.requestConfirmation(Minecraft.getInstance().player, Component.literal(
                        """
                                Do you see green rectangles on the right and red rectangles on the left?
                                Do the vanilla hearts, armor, food, vehicle health and air overlays move accordingly?
                                """));
            }
        });
    }

    private static LayeredDraw.Layer makeRightOverlay(DynamicTest test, int height, int color) {
        return (guiGraphics, partialTick) -> {
            if (!test.framework().tests().isEnabled(test.id())) {
                return;
            }
            var gui = Minecraft.getInstance().gui;
            guiGraphics.fill(
                    guiGraphics.guiWidth() / 2 + 91 - 80,
                    guiGraphics.guiHeight() - gui.rightHeight + 9 - height,
                    guiGraphics.guiWidth() / 2 + 91,
                    guiGraphics.guiHeight() - gui.rightHeight + 9,
                    color);
            gui.rightHeight += height + 1;
        };
    }

    private static LayeredDraw.Layer makeLeftOverlay(DynamicTest test, int height, int color) {
        return (guiGraphics, partialTick) -> {
            if (!test.framework().tests().isEnabled(test.id())) {
                return;
            }
            var gui = Minecraft.getInstance().gui;
            guiGraphics.fill(
                    guiGraphics.guiWidth() / 2 - 91,
                    guiGraphics.guiHeight() - gui.leftHeight + 9 - height,
                    guiGraphics.guiWidth() / 2 - 91 + 80,
                    guiGraphics.guiHeight() - gui.leftHeight + 9,
                    color);
            gui.leftHeight += height + 1;
        };
    }
}
