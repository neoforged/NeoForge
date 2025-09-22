/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

@EventBusSubscriber(value = Dist.CLIENT, modid = NeoForgeVersion.MOD_ID)
public final class BlockEntityRenderBoundsDebugRenderer {
    private static final ContextKey<List<BlockEntityRenderBoundsRenderState>> DATA_KEY = new ContextKey<>(
            ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "block_entity_render_bounds"));
    private static boolean enabled = false;

    @SubscribeEvent
    public static void onExtractLevelRenderState(ExtractLevelRenderStateEvent event) {
        if (!enabled) {
            return;
        }

        List<BlockEntityRenderBoundsRenderState> renderStates = new ArrayList<>();
        BlockEntityRenderDispatcher dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        event.getLevelRenderer().iterateVisibleBlockEntities(be -> {
            BlockEntityRenderer<BlockEntity, ?> renderer = dispatcher.getRenderer(be);
            if (renderer != null) {
                AABB aabb = renderer.getRenderBoundingBox(be);
                if (!aabb.isInfinite()) {
                    BlockPos pos = be.getBlockPos();
                    aabb = aabb.move(-pos.getX(), -pos.getY(), -pos.getZ());
                    renderStates.add(new BlockEntityRenderBoundsRenderState(pos, aabb));
                }
            }
        });
        if (!renderStates.isEmpty()) {
            event.getRenderState().setRenderData(DATA_KEY, renderStates);
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterEntities event) {
        if (!enabled) {
            return;
        }

        List<BlockEntityRenderBoundsRenderState> renderStates = event.getLevelRenderState().getRenderData(DATA_KEY);
        if (renderStates == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        VertexConsumer consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(RenderType.lines());

        for (BlockEntityRenderBoundsRenderState be : renderStates) {
            Vec3 offset = Vec3.atLowerCornerOf(be.pos).subtract(camera);

            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y, offset.z);
            ShapeRenderer.renderLineBox(poseStack.last(), consumer, be.bounds, 1F, 0F, 0F, 1F);
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

    private record BlockEntityRenderBoundsRenderState(BlockPos pos, AABB bounds) {}

    private BlockEntityRenderBoundsDebugRenderer() {}
}
