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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = NeoForgeVersion.MOD_ID)
public final class BlockEntityRenderBoundsDebugRenderer {
    private static boolean enabled = false;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (!enabled || event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }

        LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());

        levelRenderer.iterateVisibleBlockEntities(be -> drawRenderBoundingBox(poseStack, consumer, camera, be));
    }

    private static void drawRenderBoundingBox(PoseStack poseStack, VertexConsumer consumer, Vec3 camera, BlockEntity be) {
        BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(be);
        if (renderer != null) {
            BlockPos pos = be.getBlockPos();
            AABB aabb = renderer.getRenderBoundingBox(be).move(-pos.getX(), -pos.getY(), -pos.getZ());
            Vec3 offset = Vec3.atLowerCornerOf(pos).subtract(camera);

            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y, offset.z);
            LevelRenderer.renderLineBox(poseStack, consumer, aabb, 1F, 0F, 0F, 1F);
            poseStack.popPose();
        }
    }

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("neoforge")
                        .then(Commands.literal("debug_blockentity_renderbounds")
                                .requires(src -> src.hasPermission(Commands.LEVEL_ADMINS))
                                .then(Commands.argument("enable", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            enabled = BoolArgumentType.getBool(ctx, "enable");
                                            return Command.SINGLE_SUCCESS;
                                        }))));
    }

    private BlockEntityRenderBoundsDebugRenderer() {}
}
