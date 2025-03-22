/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.lighting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
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
    public void tesselateWithoutAO(BlockAndTintGetter level, List<BlockModelPart> modelParts, BlockState state, BlockPos pos, PoseStack poseStack, Function<RenderType, VertexConsumer> bufferLookup, boolean checkSides, int packedOverlay) {
        if (NeoForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.get()) {
            render(bufferLookup, flatLighter.get(), level, modelParts, state, pos, poseStack, checkSides, packedOverlay);
        } else {
            super.tesselateWithoutAO(level, modelParts, state, pos, poseStack, bufferLookup, checkSides, packedOverlay);
        }
    }

    @Override
    public void tesselateWithAO(BlockAndTintGetter level, List<BlockModelPart> modelParts, BlockState state, BlockPos pos, PoseStack poseStack, Function<RenderType, VertexConsumer> bufferLookup, boolean checkSides, int packedOverlay) {
        if (NeoForgeConfig.CLIENT.experimentalForgeLightPipelineEnabled.get()) {
            render(bufferLookup, smoothLighter.get(), level, modelParts, state, pos, poseStack, checkSides, packedOverlay);
        } else {
            super.tesselateWithAO(level, modelParts, state, pos, poseStack, bufferLookup, checkSides, packedOverlay);
        }
    }

    public static boolean render(Function<RenderType, VertexConsumer> bufferLookup, QuadLighter lighter, BlockAndTintGetter level, List<BlockModelPart> modelParts, BlockState state, BlockPos pos, PoseStack poseStack, boolean checkSides, int packedOverlay) {
        LightPipelineAwareModelBlockRenderer renderer = (LightPipelineAwareModelBlockRenderer) Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        ModelBlockRenderer.Cache cache = ModelBlockRenderer.CACHE.get();
        var pose = poseStack.last();
        var empty = true;
        var smoothLighter = lighter instanceof SmoothQuadLighter;
        QuadLighter flatLighter = null;
        BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();
        int checkedSides = 0;
        int visibleSides = 0;

        for (BlockModelPart part : modelParts) {
            VertexConsumer vertexConsumer = bufferLookup.apply(part.getRenderType(state));

            List<BakedQuad> quads = part.getQuads(null);
            if (!quads.isEmpty()) {
                empty = false;
                lighter.setup(level, pos, state, cache);
                for (BakedQuad quad : quads) {
                    if (smoothLighter && !quad.hasAmbientOcclusion()) {
                        if (flatLighter == null) {
                            flatLighter = renderer.flatLighter.get();
                            flatLighter.setup(level, pos, state, cache);
                        }
                        flatLighter.process(vertexConsumer, pose, quad, packedOverlay);
                    } else {
                        lighter.process(vertexConsumer, pose, quad, packedOverlay);
                    }
                }
            }

            for (Direction side : SIDES) {
                int sideOrdinal = 1 << side.ordinal();
                boolean sideChecked = (checkedSides & sideOrdinal) == 1;
                boolean sideVisible = (visibleSides & sideOrdinal) == 1;
                if (sideChecked && !sideVisible) {
                    continue;
                }

                if (!sideChecked) {
                    checkedSides |= sideOrdinal;
                    if (!shouldRenderFace(level, pos, state, checkSides, side, scratchPos.setWithOffset(pos, side))) {
                        continue;
                    }
                    visibleSides |= sideOrdinal;
                }

                quads = part.getQuads(side);
                if (!quads.isEmpty()) {
                    if (empty) {
                        empty = false;
                        lighter.setup(level, pos, state, cache);
                    }
                    for (BakedQuad quad : quads) {
                        if (smoothLighter && !quad.hasAmbientOcclusion()) {
                            if (flatLighter == null) {
                                flatLighter = renderer.flatLighter.get();
                                flatLighter.setup(level, pos, state, cache);
                            }
                            flatLighter.process(vertexConsumer, pose, quad, packedOverlay);
                        } else {
                            lighter.process(vertexConsumer, pose, quad, packedOverlay);
                        }
                    }
                }
            }
        }
        lighter.reset();
        if (flatLighter != null)
            flatLighter.reset();
        return !empty;
    }

    public QuadLighter getQuadLighter(boolean smooth) {
        return (smooth ? smoothLighter : flatLighter).get();
    }
}
