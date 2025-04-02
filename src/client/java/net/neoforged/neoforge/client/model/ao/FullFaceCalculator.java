/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.ao;

import net.minecraft.client.renderer.LightTexture;
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
 * with a few fixes applied:
 * <ul>
 *     <li>Fix vanilla sampling adjacent blocks 2 blocks away instead of 1 block away.</li>
 *     <li>Fix vanilla using the wrong edges when computing some corners when both `sideClear`s are false.</li>
 *     <li>Replace vanilla lightmap blending formula which can cause seams by an improved formula.</li>
 *     <li>Always use the sampling position to compute the inner light, even if the block outside of the face
 *     is not solid. This is not guaranteed to be an improvement in all cases, but it does at least fix some cases.</li>
 * </ul>
 */
class FullFaceCalculator {
    /**
     * Debug option to disable the lightmap blending formula fix. See below for an explanation.
     */
    private static final boolean DISABLE_LIGHTMAP_BLENDING_FIX = Boolean.getBoolean("neoforge.ao.disableLightmapBlendingFix");

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
        boolean cornerClear0;
        if (!sideClear2 && !sideClear0) {
            cornerBrightness0 = sideBrightness0;
            cornerLightmap0 = sideLightmap0;
            cornerClear0 = false;
        } else {
            blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[0]).move(modelblockrenderer$adjacencyinfo.corners[2]);
            BlockState blockstate8 = level.getBlockState(blockpos$mutableblockpos);
            cornerBrightness0 = this.cache.getShadeBrightness(blockstate8, level, blockpos$mutableblockpos);
            cornerLightmap0 = this.cache.getLightColor(blockstate8, level, blockpos$mutableblockpos);
            cornerClear0 = !blockstate8.isViewBlocking(level, blockpos$mutableblockpos) || blockstate8.getLightBlock() == 0;
        }

        float cornerBrightness1;
        int cornerLightmap1;
        boolean cornerClear1;
        if (!sideClear3 && !sideClear0) {
            cornerBrightness1 = sideBrightness0;
            cornerLightmap1 = sideLightmap0;
            cornerClear1 = false;
        } else {
            blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[0]).move(modelblockrenderer$adjacencyinfo.corners[3]);
            BlockState blockstate10 = level.getBlockState(blockpos$mutableblockpos);
            cornerBrightness1 = this.cache.getShadeBrightness(blockstate10, level, blockpos$mutableblockpos);
            cornerLightmap1 = this.cache.getLightColor(blockstate10, level, blockpos$mutableblockpos);
            cornerClear1 = !blockstate10.isViewBlocking(level, blockpos$mutableblockpos) || blockstate10.getLightBlock() == 0;
        }

        float cornerBrightness2;
        int cornerLightmap2;
        boolean cornerClear2;
        if (!sideClear2 && !sideClear1) {
            // Vanilla used side0 here, which is not adjacent to this face. Use 1 instead, which is.
            cornerBrightness2 = sideBrightness1;
            cornerLightmap2 = sideLightmap1;
            cornerClear2 = false;
        } else {
            blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[1]).move(modelblockrenderer$adjacencyinfo.corners[2]);
            BlockState blockstate11 = level.getBlockState(blockpos$mutableblockpos);
            cornerBrightness2 = this.cache.getShadeBrightness(blockstate11, level, blockpos$mutableblockpos);
            cornerLightmap2 = this.cache.getLightColor(blockstate11, level, blockpos$mutableblockpos);
            cornerClear2 = !blockstate11.isViewBlocking(level, blockpos$mutableblockpos) || blockstate11.getLightBlock() == 0;
        }

        float cornerBrightness3;
        int cornerLightmap3;
        boolean cornerClear3;
        if (!sideClear3 && !sideClear1) {
            // Vanilla used side0 here, which is not adjacent to this face. Use 1 instead, which is.
            cornerBrightness3 = sideBrightness1;
            cornerLightmap3 = sideLightmap1;
            cornerClear3 = false;
        } else {
            blockpos$mutableblockpos.setWithOffset(samplePos, modelblockrenderer$adjacencyinfo.corners[1]).move(modelblockrenderer$adjacencyinfo.corners[3]);
            BlockState blockstate12 = level.getBlockState(blockpos$mutableblockpos);
            cornerBrightness3 = this.cache.getShadeBrightness(blockstate12, level, blockpos$mutableblockpos);
            cornerLightmap3 = this.cache.getLightColor(blockstate12, level, blockpos$mutableblockpos);
            cornerClear3 = !blockstate12.isViewBlocking(level, blockpos$mutableblockpos) || blockstate12.getLightBlock() == 0;
        }

        // Process the inside of the block
        // This here is changed compare to vanilla which would use the offset position if
        // sampleOutside || !outsideState.isSolidRender
        // which causes seams e.g. when a slab is placed below an active sculk sensor
        BlockState insideState = sampleOutside ? level.getBlockState(samplePos) : renderedState;
        float insideBrightness = this.cache.getShadeBrightness(insideState, level, samplePos);
        int insideLightmap = this.cache.getLightColor(insideState, level, samplePos);
        boolean insideClear = !insideState.isViewBlocking(level, samplePos) || insideState.getLightBlock() == 0;

        // Wrap up
        float levelBrightness = level.getShade(direction, shade);

        out.brightness0 = ((sideBrightness3 + sideBrightness0 + cornerBrightness1 + insideBrightness) * 0.25F) * levelBrightness;
        out.brightness1 = ((sideBrightness2 + sideBrightness0 + cornerBrightness0 + insideBrightness) * 0.25F) * levelBrightness;
        out.brightness2 = ((sideBrightness2 + sideBrightness1 + cornerBrightness2 + insideBrightness) * 0.25F) * levelBrightness;
        out.brightness3 = ((sideBrightness3 + sideBrightness1 + cornerBrightness3 + insideBrightness) * 0.25F) * levelBrightness;
        out.lightmap0 = blend(sideLightmap3, sideLightmap0, cornerLightmap1, insideLightmap, sideClear3, sideClear0, cornerClear1, insideClear);
        out.lightmap1 = blend(sideLightmap2, sideLightmap0, cornerLightmap0, insideLightmap, sideClear2, sideClear0, cornerClear0, insideClear);
        out.lightmap2 = blend(sideLightmap2, sideLightmap1, cornerLightmap2, insideLightmap, sideClear2, sideClear1, cornerClear2, insideClear);
        out.lightmap3 = blend(sideLightmap3, sideLightmap1, cornerLightmap3, insideLightmap, sideClear3, sideClear1, cornerClear3, insideClear);
    }

    /**
     * Computes the lightmap of the corner of an AO face,
     * by combining the lightmap values on the two neighbor blocks, on the corner block, and inside the block.
     */
    private static int blend(
            int sideLightmapA, int sideLightmapB, int cornerLightmap, int insideLightmap,
            boolean sideClearA, boolean sideClearB, boolean cornerClear, boolean insideClear) {
        if (DISABLE_LIGHTMAP_BLENDING_FIX) {
            // This is the vanilla lightmap blending for each AO face corner.
            // - It special-cases 0 to prevent solid blocks from making adjacent blocks too dark.
            //   This special casing is bad because it does not distinguish natural light levels of 0 and solid blocks.
            // - The second problem is that the formula gives special treatment to the lightmap inside the block.
            //   The same corner of two adjacent faces receives the same 4 lightmaps to blend, but in a different order.
            //   When the 4 values are not treated equally, seams can appear.

            if (sideLightmapA == 0) {
                sideLightmapA = insideLightmap;
            }

            if (sideLightmapB == 0) {
                sideLightmapB = insideLightmap;
            }

            if (cornerLightmap == 0) {
                cornerLightmap = insideLightmap;
            }
        } else {
            // This is the improved lightmap blending, which fixes both issues:
            // - It properly ignores lightmaps coming from solid blocks, but it does not ignore 0 values otherwise,
            //   which means that a natural 0 value will not get ignored in the blending.
            // - It treats all 4 lightmaps equally.

            int sideBlockA = LightTexture.block(sideLightmapA);
            int sideBlockB = LightTexture.block(sideLightmapB);
            int cornerBlock = LightTexture.block(cornerLightmap);
            int insideBlock = LightTexture.block(insideLightmap);
            int sideSkyA = LightTexture.sky(sideLightmapA);
            int sideSkyB = LightTexture.sky(sideLightmapB);
            int cornerSky = LightTexture.sky(cornerLightmap);
            int insideSky = LightTexture.sky(insideLightmap);

            // Compute per-component minimum light, only including values from clear positions
            int minBlock = 0x10000;
            int minSky = 0x10000;

            if (sideClearA) {
                minBlock = sideBlockA;
                minSky = sideSkyA;
            }
            if (sideClearB) {
                minBlock = Math.min(minBlock, sideBlockB);
                minSky = Math.min(minSky, sideSkyB);
            }
            if (cornerClear) {
                minBlock = Math.min(minBlock, cornerBlock);
                minSky = Math.min(minSky, cornerSky);
            }
            if (insideClear) {
                minBlock = Math.min(minBlock, insideBlock);
                minSky = Math.min(minSky, insideSky);
            }

            // Ensure that if no positions were clear, minimum is 0
            minBlock &= 0xFFFF;
            minSky &= 0xFFFF;

            // Increase all components of non-clear blocks to the minimum light value
            sideLightmapA = LightTexture.pack(Math.max(minBlock, sideBlockA), Math.max(minSky, sideSkyA));
            sideLightmapB = LightTexture.pack(Math.max(minBlock, sideBlockB), Math.max(minSky, sideSkyB));
            cornerLightmap = LightTexture.pack(Math.max(minBlock, cornerBlock), Math.max(minSky, cornerSky));
            insideLightmap = LightTexture.pack(Math.max(minBlock, insideBlock), Math.max(minSky, insideSky));
        }

        return sideLightmapA + sideLightmapB + cornerLightmap + insideLightmap >> 2 & 0xFF00FF;
    }
}
