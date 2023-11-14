/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.lighting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.NeoForgeConfig;
import org.jetbrains.annotations.ApiStatus;

/**
 * Wrapper around {@link ModelBlockRenderer} to allow rendering blocks via Forge's lighting pipeline.
 */
@ApiStatus.Internal
public class LightPipelineAwareModelBlockRenderer extends ModelBlockRenderer {
    private static final Direction[] SIDES = Direction.values();

    private final ThreadLocal<QuadLighter> flatLighter, smoothLighter;

    public LightPipelineAwareModelBlockRenderer(BlockColors colors) {
        super(colors);
        this.flatLighter = ThreadLocal.withInitial(() -> new FlatQuadLighter(colors));
        this.smoothLighter = ThreadLocal.withInitial(() -> new SmoothQuadLighter(colors));
    }

    @Override
    public void tesselateWithoutAO(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean checkSides, RandomSource rand, long seed, int packedOverlay, ModelData modelData, RenderType renderType) {
        if (NeoForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.get()) {
            render(vertexConsumer, flatLighter.get(), level, model, state, pos, poseStack, checkSides, rand, seed, packedOverlay, modelData, renderType);
        } else {
            super.tesselateWithoutAO(level, model, state, pos, poseStack, vertexConsumer, checkSides, rand, seed, packedOverlay, modelData, renderType);
        }
    }

    @Override
    public void tesselateWithAO(BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean checkSides, RandomSource rand, long seed, int packedOverlay, ModelData modelData, RenderType renderType) {
        if (NeoForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.get()) {
            render(vertexConsumer, smoothLighter.get(), level, model, state, pos, poseStack, checkSides, rand, seed, packedOverlay, modelData, renderType);
        } else {
            super.tesselateWithAO(level, model, state, pos, poseStack, vertexConsumer, checkSides, rand, seed, packedOverlay, modelData, renderType);
        }
    }

    public static boolean render(VertexConsumer vertexConsumer, QuadLighter lighter, BlockAndTintGetter level, BakedModel model, BlockState state, BlockPos pos, PoseStack poseStack, boolean checkSides, RandomSource rand, long seed, int packedOverlay, ModelData modelData, RenderType renderType) {
        LightPipelineAwareModelBlockRenderer renderer = (LightPipelineAwareModelBlockRenderer) Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        var pose = poseStack.last();
        var empty = true;
        var smoothLighter = lighter instanceof SmoothQuadLighter;
        QuadLighter flatLighter = null;

        rand.setSeed(seed);
        List<BakedQuad> quads = model.getQuads(state, null, rand, modelData, renderType);
        if (!quads.isEmpty()) {
            empty = false;
            lighter.setup(level, pos, state);
            for (BakedQuad quad : quads) {
                if (smoothLighter && !quad.hasAmbientOcclusion()) {
                    if (flatLighter == null) {
                        flatLighter = renderer.flatLighter.get();
                        flatLighter.setup(level, pos, state);
                    }
                    flatLighter.process(vertexConsumer, pose, quad, packedOverlay);
                } else {
                    lighter.process(vertexConsumer, pose, quad, packedOverlay);
                }
            }
        }

        for (Direction side : SIDES) {
            if (checkSides && !Block.shouldRenderFace(state, level, pos, side, pos.relative(side))) {
                continue;
            }
            rand.setSeed(seed);
            quads = model.getQuads(state, side, rand, modelData, renderType);
            if (!quads.isEmpty()) {
                if (empty) {
                    empty = false;
                    lighter.setup(level, pos, state);
                }
                for (BakedQuad quad : quads) {
                    if (smoothLighter && !quad.hasAmbientOcclusion()) {
                        if (flatLighter == null) {
                            flatLighter = renderer.flatLighter.get();
                            flatLighter.setup(level, pos, state);
                        }
                        flatLighter.process(vertexConsumer, pose, quad, packedOverlay);
                    } else {
                        lighter.process(vertexConsumer, pose, quad, packedOverlay);
                    }
                }
            }
        }
        lighter.reset();
        if (flatLighter != null)
            flatLighter.reset();
        return !empty;
    }
}
