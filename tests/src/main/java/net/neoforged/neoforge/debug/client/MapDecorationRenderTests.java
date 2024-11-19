/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.gui.map.IMapDecorationRenderer;
import net.neoforged.neoforge.client.gui.map.RegisterMapDecorationRenderersEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import org.joml.Matrix4f;

@ForEachTest(side = Dist.CLIENT, groups = MapDecorationRenderTests.GROUP)
public class MapDecorationRenderTests {
    public static final String GROUP = "map_decoration_render";

    @EmptyTemplate
    @TestHolder(description = "Tests if custom map decoration renderers work", enabledByDefault = true)
    static void customRenderer(DynamicTest test) {
        var decorationType = test.registrationHelper().registrar(Registries.MAP_DECORATION_TYPE).register(
                "test",
                () -> new MapDecorationType(
                        ResourceLocation.withDefaultNamespace("target_x"),
                        false,
                        -1,
                        false,
                        false));

        test.framework().modEventBus().addListener((RegisterMapDecorationRenderersEvent event) -> {
            event.register(decorationType.value(), new TestDecorationRenderer(null));
        });

        test.eventListeners().forge().addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            Player player = event.getEntity();
            ItemStack mapItem = MapItem.create(player.level(), player.getBlockX(), player.getBlockZ(), (byte) 0, true, false);
            MapItemSavedData data = MapItem.getSavedData(mapItem, player.level());
            if (data == null) {
                test.fail("Map data missing for new map");
                return;
            }
            String markerName = player.getName().getString() + "_test_marker";
            data.addDecoration(decorationType, player.level(), markerName, data.centerX, data.centerZ, 0, null);
            player.getInventory().add(mapItem);
            test.requestConfirmation(player, Component.literal("Does the map show a color-cycling cross?"));
        });
    }

    @EmptyTemplate
    @TestHolder(description = "Tests if custom map decoration render state data works")
    static void customRenderData(DynamicTest test) {
        var key = new ContextKey<Integer>(ResourceLocation.fromNamespaceAndPath(test.createModId(), "custom_color"));
        var decorationType = test.registrationHelper().registrar(Registries.MAP_DECORATION_TYPE).register(
                "test",
                () -> new MapDecorationType(
                        ResourceLocation.withDefaultNamespace("target_x"),
                        false,
                        -1,
                        false,
                        false));

        test.framework().modEventBus().addListener((RegisterMapDecorationRenderersEvent event) -> {
            event.register(decorationType.value(), new TestDecorationRenderer(key));
        });
        test.framework().modEventBus().addListener((RegisterRenderStateModifiersEvent event) -> {
            event.registerMapDecorationModifier(decorationType.getKey(), (mapItemSavedData, mapRenderState, mapDecorationRenderState) -> {
                mapDecorationRenderState.setRenderData(key, 0xFFFFAABB);
            });
        });

        test.eventListeners().forge().addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
            Player player = event.getEntity();
            ItemStack mapItem = MapItem.create(player.level(), player.getBlockX(), player.getBlockZ(), (byte) 0, true, false);
            MapItemSavedData data = MapItem.getSavedData(mapItem, player.level());
            if (data == null) {
                test.fail("Map data missing for new map");
                return;
            }
            String markerName = player.getName().getString() + "_test_marker";
            data.addDecoration(decorationType, player.level(), markerName, data.centerX, data.centerZ, 0, null);
            player.getInventory().add(mapItem);
            test.requestConfirmation(player, Component.literal("Does the map show a pink cross?"));
        });
    }

    private static final class TestDecorationRenderer implements IMapDecorationRenderer {
        private final ContextKey<Integer> customColorKey;

        TestDecorationRenderer(ContextKey<Integer> customColorKey) {
            this.customColorKey = customColorKey;
        }

        @Override
        public boolean render(
                MapRenderState.MapDecorationRenderState decoration,
                PoseStack poseStack,
                MultiBufferSource bufferSource,
                MapRenderState mapRenderState,
                MapDecorationTextureManager decorationTextures,
                boolean inItemFrame,
                int packedLight,
                int index) {
            poseStack.pushPose();
            poseStack.translate(decoration.x / 2.0F + 64.0F, decoration.y / 2.0F + 64.0F, -0.02F);
            poseStack.mulPose(Axis.ZP.rotationDegrees((float) (decoration.rot * 360) / 16.0F));
            poseStack.scale(4.0F, 4.0F, 3.0F);
            poseStack.translate(-0.125F, 0.125F, 0.0F);
            Matrix4f pose = poseStack.last().pose();

            TextureAtlasSprite sprite = decoration.atlasSprite;
            float u0 = sprite.getU0();
            float v0 = sprite.getV0();
            float u1 = sprite.getU1();
            float v1 = sprite.getV1();

            float hue = (System.currentTimeMillis() % 3000L) / 3000F;
            int color = Mth.hsvToArgb(hue, 1F, 1F, 0xFF);
            if (decoration.getRenderData(customColorKey) != null) {
                color = decoration.getRenderDataOrThrow(customColorKey);
            }

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.text(sprite.atlasLocation()));
            buffer.addVertex(pose, -1.0F, 1.0F, index * -0.001F).setColor(color).setUv(u0, v0).setLight(packedLight);
            buffer.addVertex(pose, 1.0F, 1.0F, index * -0.001F).setColor(color).setUv(u1, v0).setLight(packedLight);
            buffer.addVertex(pose, 1.0F, -1.0F, index * -0.001F).setColor(color).setUv(u1, v1).setLight(packedLight);
            buffer.addVertex(pose, -1.0F, -1.0F, index * -0.001F).setColor(color).setUv(u0, v1).setLight(packedLight);

            poseStack.popPose();

            return true;
        }
    }
}
