/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.client;

import com.google.common.reflect.TypeToken;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Axis;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.AbstractHoglinRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.PigRenderer;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.hoglin.HoglinBase;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import net.neoforged.neoforge.client.event.ClientChatEvent;
import net.neoforged.neoforge.client.event.ClientPlayerChangeGameTypeEvent;
import net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
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
            Minecraft.getInstance().schedule(() -> test.requestConfirmation(Minecraft.getInstance().player, Component.literal("Was your message deleted?")));
        } else if (event.getMessage().equals("Replace this text")) {
            event.setMessage("Text replaced.");
            Minecraft.getInstance().schedule(() -> test.requestConfirmation(Minecraft.getInstance().player, Component.literal("Was your message modified?")));
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
        test.framework().modEventBus().addListener((final AddClientReloadListenersEvent event) -> {
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
                        var parts = Minecraft.getInstance().getBlockRenderer().getBlockModel(Blocks.DIAMOND_BLOCK.defaultBlockState())
                                .collectParts(EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO, Blocks.DIAMOND_BLOCK.defaultBlockState(), new SingleThreadedRandomSource(0));
                        Minecraft.getInstance().getBlockRenderer().renderBatched(
                                Blocks.DIAMOND_BLOCK.defaultBlockState(),
                                testBlockAt,
                                context.getRegion(),
                                poseStack,
                                context::getOrCreateChunkBuffer,
                                false,
                                parts);
                        poseStack.popPose();
                    });
                }
            });
            Minecraft.getInstance().levelRenderer.setSectionDirty(section.x(), section.y(), section.z());
            test.requestConfirmation(player, Component.literal("Is a diamond block rendered above you?"));
        }
    }

    @TestHolder(description = { "Tests that RenderPlayerEvent is fired correctly and functions as expected" })
    static void renderPlayerEvent(final DynamicTest test) {
        test.whenEnabled(listeners -> {
            var item = Items.IRON_BLOCK;
            var itemStack = item.getDefaultInstance();
            listeners.forge().addListener((final RenderPlayerEvent.Post event) -> {
                event.getPoseStack().pushPose();
                event.getPoseStack().translate(0, 2, 0);

                ItemStackRenderState renderState = new ItemStackRenderState();
                Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, itemStack, ItemDisplayContext.GROUND, null, null, 0);
                renderState.submit(event.getPoseStack(), event.getSubmitNodeCollector(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);

                event.getPoseStack().popPose();
            });
            test.requestConfirmation(Minecraft.getInstance().player, Component.literal("Is an iron block rendered above you in third-person?"));
        });
    }

    @TestHolder(description = { "Test render state modifier system and registration event" })
    static void updateRenderState(final DynamicTest test) {
        var rotationKey = new ContextKey<Float>(ResourceLocation.fromNamespaceAndPath(test.createModId(), "rotation"));
        var numRenderAttachmentKey = new ContextKey<Integer>(ResourceLocation.fromNamespaceAndPath(test.createModId(), "times_to_render"));
        var testAttachment = test.registrationHelper().attachments().registerSimpleAttachment("test", () -> 3);
        test.framework().modEventBus().addListener((RegisterRenderStateModifiersEvent event) -> {
            event.registerEntityModifier(PigRenderer.class, (entity, renderState) -> {
                renderState.setRenderData(rotationKey, 45f);
            });
            event.registerEntityModifier(new TypeToken<LivingEntityRenderer<? extends LivingEntity, LivingEntityRenderState, ?>>() {}, (entity, renderState) -> {
                renderState.setRenderData(numRenderAttachmentKey, entity.getData(testAttachment));
            });
            // Test other type parameters for safety
            // This call requires explicit typing to satisfy ECJ. Without it, the wildcard on AbstractHoglinRenderer is invalid.
            event.<Entity, HoglinRenderState>registerEntityModifier(new TypeToken<AbstractHoglinRenderer<?>>() {}, (entity, renderState) -> {});
            event.registerEntityModifier(new TypeToken<MobRenderer<Mob, LivingEntityRenderState, ?>>() {}, (entity, renderState) -> {});
            try {
                class TestBrokenHoglinRendererTypeToken<T extends Mob & HoglinBase> extends TypeToken<AbstractHoglinRenderer<T>> {}
                event.registerEntityModifier(new TestBrokenHoglinRendererTypeToken<>(), (entity, renderState) -> {});
                test.fail("Unsafe type parameter succeeded. Cannot assume T can be ?.");
            } catch (IllegalArgumentException ignored) {}
        });
        test.whenEnabled(listeners -> {
            listeners.forge().addListener((RenderLivingEvent.Post<?, ?, ?> event) -> {
                int numRender = event.getRenderState().getRenderDataOrDefault(numRenderAttachmentKey, -1);
                if (numRender == -1) {
                    test.fail("Attachment render data not set");
                    return;
                }
                float xRotation = event.getRenderState().getRenderDataOrDefault(rotationKey, 0f);
                if (event.getRenderer() instanceof PigRenderer && numRender == 0) {
                    test.fail("Custom render data not set for player");
                    return;
                }

                var poseStack = event.getPoseStack();
                poseStack.pushPose();
                poseStack.scale(0.3f, 0.3f, 0.3f);
                for (int i = 0; i < numRender; i++) {
                    poseStack.translate(0, 1, 0);
                    poseStack.pushPose();
                    poseStack.mulPose(Axis.XP.rotation(xRotation));
                    event.getSubmitNodeCollector().submitBlock(
                            poseStack,
                            Blocks.CALCITE.defaultBlockState(),
                            event.getRenderState().lightCoords,
                            OverlayTexture.NO_OVERLAY,
                            -1);
                    poseStack.popPose();
                }
                poseStack.popPose();
                test.pass();
            });
        });
    }

    @TestHolder(description = { "Tests if rendering custom geometry on visible chunks works", "When the message \"gold block\" is sent in chat, gold blocks should render at the origin of every visible section with blocks" })
    static void renderLevelStageWithSectionData(final DynamicTest test) {
        test.whenEnabled(listeners -> {
            listeners.forge().addListener((final ClientChatEvent chatEvent) -> {
                if (chatEvent.getMessage().equalsIgnoreCase("gold block")) {
                    NeoForge.EVENT_BUS.addListener((final RenderLevelStageEvent.AfterOpaqueBlocks event) -> {
                        var randomSource = new SingleThreadedRandomSource(0);
                        var state = Blocks.GOLD_BLOCK.defaultBlockState();
                        var stack = event.getPoseStack();
                        var camera = event.getLevelRenderState().cameraRenderState.pos;
                        event.getRenderableSections().forEach(section -> {
                            if (section.isEmpty()) {
                                return;
                            }

                            stack.pushPose();
                            stack.translate(
                                    section.getRenderOrigin().getX() - camera.x,
                                    section.getRenderOrigin().getY() - camera.y,
                                    section.getRenderOrigin().getZ() - camera.z);

                            var parts = Minecraft.getInstance().getBlockRenderer().getBlockModel(state).collectParts(EmptyBlockAndTintGetter.INSTANCE, BlockPos.ZERO, state, randomSource);
                            Minecraft.getInstance().getBlockRenderer().renderBatched(
                                    state,
                                    section.getRenderOrigin(),
                                    EmptyBlockAndTintGetter.INSTANCE,
                                    stack,
                                    csl -> Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderTypeHelper.getEntityRenderType(csl)),
                                    false,
                                    parts);
                            stack.popPose();

                            test.pass();
                        });
                    });
                }
            });
        });
    }
}
