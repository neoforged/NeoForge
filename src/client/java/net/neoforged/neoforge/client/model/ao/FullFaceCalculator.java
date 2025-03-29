/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.ao;

import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Calculates AO for a full cube face.
 * There are 24 possible configurations for each block, depending on: the direction,
 * whether the quad wants to be shaded, and whether the sample is taken outside the block.
 *
 * <p>The {@link EnhancedAoRenderStorage} then works by combining the results of multiple configurations,
 * using various interpolation schemes depending on the quad.
 *
 * <p>The logic is mostly contained in {@link #calculateFaceUncached},
 * and derives from vanilla's {@link ModelBlockRenderer.AmbientOcclusionRenderStorage#calculate},
 * with a number of fixes applied.
 * TODO list fixes if any
 */
class FullFaceCalculator {
    final BlockPos.MutableBlockPos scratchPos = new BlockPos.MutableBlockPos();
    private ModelBlockRenderer.Cache cache;

    private final AoCalculatedFace[] aoFaces = new AoCalculatedFace[24];
    {
        for (int i = 0; i < 24; ++i) {
            aoFaces[i] = new AoCalculatedFace();
        }
    }
    private int calculatedAoFaces = 0;

    void startBlock(ModelBlockRenderer.Cache cache) {
        this.calculatedAoFaces = 0;
        this.cache = cache;
    }

    AoCalculatedFace calculateFace(BlockAndTintGetter level, BlockState renderedState, BlockPos renderedPos, Direction direction, boolean shade, boolean sampleOutside) {
        int cacheIndex = direction.get3DDataValue();
        if (sampleOutside) {
            cacheIndex += 6;
        }
        if (shade) {
            cacheIndex += 12;
        }

        if ((this.calculatedAoFaces & (1 << cacheIndex)) != 0) {
            return this.aoFaces[cacheIndex];
        }

        var fullFace = this.aoFaces[cacheIndex];
        calculateFaceUncached(fullFace, level, renderedState, renderedPos, direction, shade, sampleOutside);
        this.calculatedAoFaces |= 1 << cacheIndex;
        return fullFace;
    }

    /**
     * Computes the AO for a full face.
     *
     * @param out           storage for the computed lightmap and brightness.
     * @param sampleOutside {@code true} to sample the light outside the block, {@code false} to sample the light inside the block.
     *                      In vanilla, this is equivalent to {@code faceCubic}.
     */
    private void calculateFaceUncached(AoCalculatedFace out, BlockAndTintGetter level, BlockState renderedState, BlockPos renderedPos, Direction direction, boolean shade, boolean sampleOutside) {
        BlockPos samplePos = sampleOutside ? renderedPos.relative(direction) : renderedPos;
        ModelBlockRenderer.AdjacencyInfo modelblockrenderer$adjacencyinfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(direction);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = this.scratchPos;

        // Sample light and brightness for each side of the face
        // AdjacencyInfo calls them corners, but they are actually sides
        blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[0]);
        BlockState blockstate = level.getBlockState(blockpos$mutableblockpos);
        int sideLightmap0 = this.cache.getLightColor(blockstate, level, blockpos$mutableblockpos);
        float sideBrightness0 = this.cache.getShadeBrightness(blockstate, level, blockpos$mutableblockpos);

        blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[1]);
        BlockState blockstate1 = level.getBlockState(blockpos$mutableblockpos);
        int sideLightmap1 = this.cache.getLightColor(blockstate1, level, blockpos$mutableblockpos);
        float sideBrightness1 = this.cache.getShadeBrightness(blockstate1, level, blockpos$mutableblockpos);

        blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[2]);
        BlockState blockstate2 = level.getBlockState(blockpos$mutableblockpos);
        int sideLightmap2 = this.cache.getLightColor(blockstate2, level, blockpos$mutableblockpos);
        float sideBrightness2 = this.cache.getShadeBrightness(blockstate2, level, blockpos$mutableblockpos);

        blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[3]);
        BlockState blockstate3 = level.getBlockState(blockpos$mutableblockpos);
        int sideLightmap3 = this.cache.getLightColor(blockstate3, level, blockpos$mutableblockpos);
        float sideBrightness3 = this.cache.getShadeBrightness(blockstate3, level, blockpos$mutableblockpos);

        // Check if sides are clear
        BlockState blockstate4 = level.getBlockState(
                blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[0]) // Neo: remove move() to avoid oversampling (MC-43968)
        );
        boolean sideClear0 = !blockstate4.isViewBlocking(level, blockpos$mutableblockpos) || blockstate4.getLightBlock() == 0;
        BlockState blockstate5 = level.getBlockState(
                blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[1]) // Neo: remove move() to avoid oversampling (MC-43968)
        );
        boolean sideClear1 = !blockstate5.isViewBlocking(level, blockpos$mutableblockpos) || blockstate5.getLightBlock() == 0;
        BlockState blockstate6 = level.getBlockState(
                blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[2]) // Neo: remove move() to avoid oversampling (MC-43968)
        );
        boolean sideClear2 = !blockstate6.isViewBlocking(level, blockpos$mutableblockpos) || blockstate6.getLightBlock() == 0;
        BlockState blockstate7 = level.getBlockState(
                blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[3]) // Neo: remove move() to avoid oversampling (MC-43968)
        );
        boolean sideClear3 = !blockstate7.isViewBlocking(level, blockpos$mutableblockpos) || blockstate7.getLightBlock() == 0;

        // Process corners
        // Note that corners[0] and corners[1] are on the same axis, and same for corners[2] and corners[3].

        float cornerBrightness0;
        int cornerLightmap0;
        if (!sideClear2 && !sideClear0) {
            cornerBrightness0 = sideBrightness0;
            cornerLightmap0 = sideLightmap0;
        } else {
            blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[0]).move(modelblockrenderer$adjacencyinfo.corners[2]);
            BlockState blockstate8 = level.getBlockState(blockpos$mutableblockpos);
            cornerBrightness0 = this.cache.getShadeBrightness(blockstate8, level, blockpos$mutableblockpos);
            cornerLightmap0 = this.cache.getLightColor(blockstate8, level, blockpos$mutableblockpos);
        }

        float cornerBrightness1;
        int cornerLightmap1;
        if (!sideClear3 && !sideClear0) {
            cornerBrightness1 = sideBrightness0;
            cornerLightmap1 = sideLightmap0;
        } else {
            blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[0]).move(modelblockrenderer$adjacencyinfo.corners[3]);
            BlockState blockstate10 = level.getBlockState(blockpos$mutableblockpos);
            cornerBrightness1 = this.cache.getShadeBrightness(blockstate10, level, blockpos$mutableblockpos);
            cornerLightmap1 = this.cache.getLightColor(blockstate10, level, blockpos$mutableblockpos);
        }

        float cornerBrightness2;
        int cornerLightmap2;
        if (!sideClear2 && !sideClear1) {
            cornerBrightness2 = sideBrightness0;
            cornerLightmap2 = sideLightmap0;
        } else {
            blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[1]).move(modelblockrenderer$adjacencyinfo.corners[2]);
            BlockState blockstate11 = level.getBlockState(blockpos$mutableblockpos);
            cornerBrightness2 = this.cache.getShadeBrightness(blockstate11, level, blockpos$mutableblockpos);
            cornerLightmap2 = this.cache.getLightColor(blockstate11, level, blockpos$mutableblockpos);
        }

        float cornerBrightness3;
        int cornerLightmap3;
        if (!sideClear3 && !sideClear1) {
            cornerBrightness3 = sideBrightness0;
            cornerLightmap3 = sideLightmap0;
        } else {
            blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[1]).move(modelblockrenderer$adjacencyinfo.corners[3]);
            BlockState blockstate12 = level.getBlockState(blockpos$mutableblockpos);
            cornerBrightness3 = this.cache.getShadeBrightness(blockstate12, level, blockpos$mutableblockpos);
            cornerLightmap3 = this.cache.getLightColor(blockstate12, level, blockpos$mutableblockpos);
        }

        // Process the inside of the block
        int insideLightmap = this.cache.getLightColor(renderedState, level, renderedPos);
        blockpos$mutableblockpos.setWithOffset(renderedPos, direction);
        BlockState blockstate9 = level.getBlockState(blockpos$mutableblockpos);
        if (sampleOutside || !blockstate9.isSolidRender()) {
            insideLightmap = this.cache.getLightColor(blockstate9, level, blockpos$mutableblockpos);
        }

        float insideBrightness = sampleOutside
                ? this.cache.getShadeBrightness(level.getBlockState(samplePos), level, samplePos)
                : this.cache.getShadeBrightness(level.getBlockState(renderedPos), level, renderedPos);

        // Wrap up
        float levelBrightness = level.getShade(direction, shade);

        out.brightness0 = ((sideBrightness3 + sideBrightness0 + cornerBrightness1 + insideBrightness) * 0.25F) * levelBrightness;
        out.brightness1 = ((sideBrightness2 + sideBrightness0 + cornerBrightness0 + insideBrightness) * 0.25F) * levelBrightness;
        out.brightness2 = ((sideBrightness2 + sideBrightness1 + cornerBrightness2 + insideBrightness) * 0.25F) * levelBrightness;
        out.brightness3 = ((sideBrightness3 + sideBrightness1 + cornerBrightness3 + insideBrightness) * 0.25F) * levelBrightness;
        out.lightmap0 = blend(sideLightmap3, sideLightmap0, cornerLightmap1, insideLightmap);
        out.lightmap1 = blend(sideLightmap2, sideLightmap0, cornerLightmap0, insideLightmap);
        out.lightmap2 = blend(sideLightmap2, sideLightmap1, cornerLightmap2, insideLightmap);
        out.lightmap3 = blend(sideLightmap3, sideLightmap1, cornerLightmap3, insideLightmap);
    }

    private static int blend(int sideLightmapA, int sideLightmapB, int cornerLightmap, int faceLightmap) {
        if (true) {
            if (sideLightmapA == 0) {
                sideLightmapA = faceLightmap;
            }

            if (sideLightmapB == 0) {
                sideLightmapB = faceLightmap;
            }

            if (cornerLightmap == 0) {
                cornerLightmap = faceLightmap;
            }

            return sideLightmapA + sideLightmapB + cornerLightmap + faceLightmap >> 2 & 16711935;
        } else {
            // Taken from Indigo
            if (sideLightmapA == 0 || sideLightmapB == 0 || cornerLightmap == 0 || faceLightmap == 0) {
                // Compute non-zero min
                int min = Math.max(Math.max(sideLightmapA, sideLightmapB), Math.max(cornerLightmap, faceLightmap));
                if (sideLightmapA != 0) min = Math.min(min, sideLightmapA);
                if (sideLightmapB != 0) min = Math.min(min, sideLightmapB);
                if (cornerLightmap != 0) min = Math.min(min, cornerLightmap);
                if (faceLightmap != 0) min = Math.min(min, faceLightmap);

                // Apply min
                sideLightmapA = Math.max(min, sideLightmapA);
                sideLightmapB = Math.max(min, sideLightmapB);
                cornerLightmap = Math.max(min, cornerLightmap);
                faceLightmap = Math.max(min, faceLightmap);
            }

            return sideLightmapA + sideLightmapB + cornerLightmap + faceLightmap >> 2 & 16711935;
        }
    }
}
