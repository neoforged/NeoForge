package net.neoforged.neoforge.debug.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

import java.util.Objects;
import java.util.Random;

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
