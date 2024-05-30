/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.commands.Commands;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME, modid = NeoForgeVersion.MOD_ID)
public final class ParticleBoundsDebugRenderer {
    private static boolean enabled = false;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!enabled || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        var camPos = event.getCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();

        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());

        Minecraft.getInstance().particleEngine.iterateParticles(particle -> {
            var bb = particle.getRenderBoundingBox(event.getPartialTick());
            if (!bb.isInfinite() && event.getFrustum().isVisible(bb)) {
                poseStack.pushPose();
                var offset = particle.getPos().subtract(camPos);
                poseStack.translate(offset.x, offset.y, offset.z);
                bb = bb.move(-particle.getPos().x, -particle.getPos().y, -particle.getPos().z);
                LevelRenderer.renderLineBox(poseStack, consumer, bb, 1F, 0F, 0F, 1F);
                poseStack.popPose();
            }
        });

        Minecraft.getInstance().renderBuffers().bufferSource().endBatch(RenderType.lines());

        poseStack.popPose();
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("neoforge")
                        .then(Commands.literal("debug_particle_renderbounds")
                                .requires(src -> src.hasPermission(Commands.LEVEL_ADMINS))
                                .then(Commands.argument("enable", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            enabled = BoolArgumentType.getBool(ctx, "enable");
                                            return Command.SINGLE_SUCCESS;
                                        }))));
    }

    private ParticleBoundsDebugRenderer() {}
}
