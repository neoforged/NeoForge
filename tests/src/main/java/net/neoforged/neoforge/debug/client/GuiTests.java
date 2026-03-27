/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Objects;
import java.util.Random;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.TestListener;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import org.jspecify.annotations.Nullable;

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
        if (player.hasEffect(MobEffects.SLOWNESS)) {
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
        public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
            super.extractBackground(graphics, mouseX, mouseY, partialTicks);
            graphics.text(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
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
            event.registerBelow(VanillaGuiLayers.PLAYER_HEALTH, Identifier.fromNamespaceAndPath(test.createModId(), "left1"), makeLeftOverlay(test, 3, 0x80FF0000));
            event.registerBelow(VanillaGuiLayers.ARMOR_LEVEL, Identifier.fromNamespaceAndPath(test.createModId(), "left2"), makeLeftOverlay(test, 3, 0x80CC0000));
            event.registerAbove(VanillaGuiLayers.ARMOR_LEVEL, Identifier.fromNamespaceAndPath(test.createModId(), "left3"), makeLeftOverlay(test, 3, 0x80990000));

            event.registerBelow(VanillaGuiLayers.FOOD_LEVEL, Identifier.fromNamespaceAndPath(test.createModId(), "right1"), makeRightOverlay(test, 2, 0x8000FF00));
            event.registerBelow(VanillaGuiLayers.VEHICLE_HEALTH, Identifier.fromNamespaceAndPath(test.createModId(), "right2"), makeRightOverlay(test, 2, 0x8000DD00));
            event.registerBelow(VanillaGuiLayers.AIR_LEVEL, Identifier.fromNamespaceAndPath(test.createModId(), "right3"), makeRightOverlay(test, 2, 0x8000BB00));
            event.registerAbove(VanillaGuiLayers.AIR_LEVEL, Identifier.fromNamespaceAndPath(test.createModId(), "right4"), makeRightOverlay(test, 2, 0x80009900));
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

    private static GuiLayer makeRightOverlay(DynamicTest test, int height, int color) {
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

    private static GuiLayer makeLeftOverlay(DynamicTest test, int height, int color) {
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

    @TestHolder(description = "Tests if gui layers can be wrapped to apply pose stack transformations")
    static void testGuiLayerWrapping(DynamicTest test) {
        test.framework().modEventBus().addListener((RegisterGuiLayersEvent event) -> {
            event.wrapLayer(VanillaGuiLayers.FOOD_LEVEL, guiLayer -> (guiGraphics, deltaTracker) -> {
                if (test.framework().tests().isEnabled(test.id())) {
                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().translate(-91 / 2f, -11);
                    guiLayer.render(guiGraphics, deltaTracker);
                    guiGraphics.pose().popMatrix();
                } else {
                    guiLayer.render(guiGraphics, deltaTracker);
                }
            });
        });

        test.framework().tests().addListener(new TestListener() {
            @Override
            public void onEnabled(TestFramework framework, Test enabledTest, @Nullable Entity changer) {
                if (test == enabledTest && changer instanceof Player player) {
                    test.requestConfirmation(player, Component.literal("Is the food bar rendering above the health bar, in the centre of the screen?"));
                }
            }
        });
    }

    @TestHolder(description = "Checks that the depth budget for GUI layers gets adjusted as necessary")
    static void testGuiLayerDepthBudget(DynamicTest test) {
        test.framework().modEventBus().addListener((RegisterGuiLayersEvent event) -> {
            // Register some placeholder layers
            for (int i = 0; i < 50; i++) {
                event.registerAboveAll(Identifier.fromNamespaceAndPath(test.createModId(), "fake_" + i), (guiGraphics, deltaTracker) -> {});
            }
            // Register the real layer
            event.registerAboveAll(Identifier.fromNamespaceAndPath(test.createModId(), "high_depth_test"), (guiGraphics, deltaTracker) -> {
                if (test.framework().tests().isEnabled(test.id())) {
                    guiGraphics.fill(guiGraphics.guiWidth() - 50, 0, guiGraphics.guiWidth(), 50, 0xFFFF0000);
                }
            });
        });

        test.eventListeners().forge().addListener((ClientChatEvent chatEvent) -> {
            if (chatEvent.getMessage().equalsIgnoreCase("gui layer depth test")) {
                test.requestConfirmation(Minecraft.getInstance().player, Component.literal(
                        "Do you see a red square in the top right corner of the screen?"));
            }
        });
    }

    @TestHolder(description = "Checks that InventoryScreen.renderEntityInInventory() can render multiple entities of the same type within a single frame")
    static void testInventoryEntityRenderMulti(DynamicTest test) {
        Lazy<ArmorStand> entityOne = Lazy.of(() -> makeTestArmorStand(Items.DIAMOND_CHESTPLATE));
        Lazy<ArmorStand> entityTwo = Lazy.of(() -> makeTestArmorStand(Items.LEATHER_CHESTPLATE));
        test.framework().modEventBus().addListener((RegisterGuiLayersEvent event) -> {
            event.registerAboveAll(Identifier.fromNamespaceAndPath(test.createModId(), "inv_entities"), (graphics, deltaTracker) -> {
                if (!test.framework().tests().isEnabled(test.id())) return;

                var armorStandAngleX = 0.43633232F;
                int maxX = graphics.guiWidth();
                InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, maxX - 100, 20, maxX - 60, 80, 25, 1, 0, armorStandAngleX, entityOne.get());
                InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, maxX - 50, 20, maxX - 10, 80, 25, 1, 0, armorStandAngleX, entityTwo.get());
            });
        });
        test.eventListeners().forge().addListener((ClientPlayerNetworkEvent.LoggingOut event) -> {
            entityOne.invalidate();
            entityTwo.invalidate();
        });
    }

    private static ArmorStand makeTestArmorStand(Item armor) {
        ArmorStand armorStand = new ArmorStand(Minecraft.getInstance().level, 0.0, 0.0, 0.0);
        armorStand.setNoBasePlate(true);
        armorStand.setShowArms(true);
        armorStand.setXRot(25.0F);
        armorStand.yHeadRot = armorStand.getYRot();
        armorStand.yHeadRotO = armorStand.getYRot();
        armorStand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(armor));
        return armorStand;
    }

    @TestHolder(description = "Checks that non-quad vertex format modes can be used in UI rendering by drawing an octahedron in the top-right corner", enabledByDefault = true)
    static void testNonQuadUiGeometry(DynamicTest test) {
        String modId = test.createModId();

        RenderPipeline triStripPipeline = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(modId, "tri_strip"))
                .withVertexShader("core/position_color")
                .withFragmentShader("core/position_color")
                .withCull(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
                .build();

        record CircleGuiElementRenderState(
                int centerX,
                int centerY,
                int radius,
                int divisions,
                int color,
                RenderPipeline pipeline,
                TextureSetup textureSetup,
                ScreenRectangle bounds,
                @Nullable ScreenRectangle scissorArea) implements GuiElementRenderState {
            static CircleGuiElementRenderState create(
                    int centerX,
                    int centerY,
                    int radius,
                    int divisions,
                    int color,
                    RenderPipeline pipeline) {
                ScreenRectangle bounds = new ScreenRectangle(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
                return new CircleGuiElementRenderState(centerX, centerY, radius, divisions, color, pipeline, TextureSetup.noTexture(), bounds, null);
            }

            @Override
            public void buildVertices(VertexConsumer builder) {
                builder.addVertex(centerX, centerY, 0).setColor(color);
                for (int i = 0; i <= divisions; i++) {
                    float vx = centerX + (radius * Mth.cos(i * Mth.TWO_PI / divisions));
                    float vy = centerY + (radius * Mth.sin(i * Mth.TWO_PI / divisions));
                    builder.addVertex(vx, vy, 0).setColor(color);
                }
            }
        }

        test.framework().modEventBus().addListener(RegisterGuiLayersEvent.class, event -> {
            Identifier id = Identifier.fromNamespaceAndPath(modId, "test");
            event.registerAboveAll(id, (graphics, deltaTracker) -> {
                int centerX = graphics.guiWidth() - 20;
                graphics.submitGuiElementRenderState(CircleGuiElementRenderState.create(centerX, 20, 15, 8, 0xFFAA0000, triStripPipeline));
                graphics.submitGuiElementRenderState(CircleGuiElementRenderState.create(centerX, 60, 15, 8, 0xFFAA0000, triStripPipeline));
            });
        });
    }
}
