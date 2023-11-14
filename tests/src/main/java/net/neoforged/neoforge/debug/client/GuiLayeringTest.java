/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

@Mod(GuiLayeringTest.MODID)
public class GuiLayeringTest {
    private static final boolean ENABLED = false;

    private static final Random RANDOM = new Random();
    public static final String MODID = "gui_layer_test";

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ClientEvents {
        @SubscribeEvent
        public static void guiOpen(ScreenEvent.Init.Post event) {
            if (event.getScreen() instanceof AbstractContainerScreen && ENABLED) {
                event.addListener(Button.builder(Component.literal("Test Gui Layering"), btn -> {
                    Minecraft.getInstance().pushGuiLayer(new TestLayer(Component.literal("LayerScreen")));
                }).pos(2, 2).size(150, 20).build());

                event.addListener(Button.builder(Component.literal("Test Gui Normal"), btn -> {
                    Minecraft.getInstance().setScreen(new TestLayer(Component.literal("LayerScreen")));
                }).pos(2, 25).size(150, 20).build());
            }
        }

        public static class TestLayer extends Screen {
            protected TestLayer(Component titleIn) {
                super(titleIn);
            }

            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                this.renderTransparentBackground(graphics);
                graphics.drawString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
                super.render(graphics, mouseX, mouseY, partialTicks);
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
    }
}
