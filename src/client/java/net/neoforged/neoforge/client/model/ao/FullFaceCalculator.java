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
 * <li>Fix vanilla sampling adjacent blocks 2 blocks away instead of 1 block away.</li>
 * <li>Fix vanilla using the wrong edges when computing some corners when both `sideClear`s are false.</li>
 * <li>Replace vanilla lightmap blending formula which can cause seams by an improved formula.</li>
 * <li>Always use the sampling position to compute the inner light, even if the block outside of the face
 * is not solid. This is not guaranteed to be an improvement in all cases, but it does at least fix some cases.</li>
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
        ModelBlockRenderer.AdjacencyInfo adjacencyInfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(direction);
        BlockPos.MutableBlockPos scratchPos = this.scratchPos;

        // Sample light and brightness for each side of the face
        // Also store clear here, whereas vanilla does it later
        // AdjacencyInfo calls them corners, but they are actually sides
        scratchPos.setWithOffset(samplePos, adjacencyInfo.corners[0]);
        BlockState sideState0 = level.getBlockState(scratchPos);
        int sideLightmap0 = this.cache.getLightColor(sideState0, level, scratchPos);
        float sideBrightness0 = this.cache.getShadeBrightness(sideState0, level, scratchPos);
        boolean sideClear0 = !sideState0.isViewBlocking(level, scratchPos) || sideState0.getLightBlock() == 0;

        scratchPos.setWithOffset(samplePos, adjacencyInfo.corners[1]);
        BlockState sideState1 = level.getBlockState(scratchPos);
        int sideLightmap1 = this.cache.getLightColor(sideState1, level, scratchPos);
        float sideBrightness1 = this.cache.getShadeBrightness(sideState1, level, scratchPos);
        boolean sideClear1 = !sideState1.isViewBlocking(level, scratchPos) || sideState1.getLightBlock() == 0;

        scratchPos.setWithOffset(samplePos, adjacencyInfo.corners[2]);
        BlockState sideState2 = level.getBlockState(scratchPos);
        int sideLightmap2 = this.cache.getLightColor(sideState2, level, scratchPos);
        float sideBrightness2 = this.cache.getShadeBrightness(sideState2, level, scratchPos);
        boolean sideClear2 = !sideState2.isViewBlocking(level, scratchPos) || sideState2.getLightBlock() == 0;

        scratchPos.setWithOffset(samplePos, adjacencyInfo.corners[3]);
        BlockState sideState3 = level.getBlockState(scratchPos);
        int sideLightmap3 = this.cache.getLightColor(sideState3, level, scratchPos);
        float sideBrightness3 = this.cache.getShadeBrightness(sideState3, level, scratchPos);
        boolean sideClear3 = !sideState3.isViewBlocking(level, scratchPos) || sideState3.getLightBlock() == 0;

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
            scratchPos.setWithOffset(samplePos, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[2]);
            BlockState cornerState0 = level.getBlockState(scratchPos);
            cornerBrightness0 = this.cache.getShadeBrightness(cornerState0, level, scratchPos);
            cornerLightmap0 = this.cache.getLightColor(cornerState0, level, scratchPos);
            cornerClear0 = !cornerState0.isViewBlocking(level, scratchPos) || cornerState0.getLightBlock() == 0;
        }

        float cornerBrightness1;
        int cornerLightmap1;
        boolean cornerClear1;
        if (!sideClear3 && !sideClear0) {
            cornerBrightness1 = sideBrightness0;
            cornerLightmap1 = sideLightmap0;
            cornerClear1 = false;
        } else {
            scratchPos.setWithOffset(samplePos, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[3]);
            BlockState cornerState1 = level.getBlockState(scratchPos);
            cornerBrightness1 = this.cache.getShadeBrightness(cornerState1, level, scratchPos);
            cornerLightmap1 = this.cache.getLightColor(cornerState1, level, scratchPos);
            cornerClear1 = !cornerState1.isViewBlocking(level, scratchPos) || cornerState1.getLightBlock() == 0;
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
            scratchPos.setWithOffset(samplePos, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[2]);
            BlockState cornerState2 = level.getBlockState(scratchPos);
            cornerBrightness2 = this.cache.getShadeBrightness(cornerState2, level, scratchPos);
            cornerLightmap2 = this.cache.getLightColor(cornerState2, level, scratchPos);
            cornerClear2 = !cornerState2.isViewBlocking(level, scratchPos) || cornerState2.getLightBlock() == 0;
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
            scratchPos.setWithOffset(samplePos, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[3]);
            BlockState cornerState3 = level.getBlockState(scratchPos);
            cornerBrightness3 = this.cache.getShadeBrightness(cornerState3, level, scratchPos);
            cornerLightmap3 = this.cache.getLightColor(cornerState3, level, scratchPos);
            cornerClear3 = !cornerState3.isViewBlocking(level, scratchPos) || cornerState3.getLightBlock() == 0;
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

            int sideBlockA = LightTexture.blockWithFraction(sideLightmapA);
            int sideBlockB = LightTexture.blockWithFraction(sideLightmapB);
            int cornerBlock = LightTexture.blockWithFraction(cornerLightmap);
            int insideBlock = LightTexture.blockWithFraction(insideLightmap);
            int sideSkyA = LightTexture.skyWithFraction(sideLightmapA);
            int sideSkyB = LightTexture.skyWithFraction(sideLightmapB);
            int cornerSky = LightTexture.skyWithFraction(cornerLightmap);
            int insideSky = LightTexture.skyWithFraction(insideLightmap);

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
            sideLightmapA = LightTexture.packWithFraction(Math.max(minBlock, sideBlockA), Math.max(minSky, sideSkyA));
            sideLightmapB = LightTexture.packWithFraction(Math.max(minBlock, sideBlockB), Math.max(minSky, sideSkyB));
            cornerLightmap = LightTexture.packWithFraction(Math.max(minBlock, cornerBlock), Math.max(minSky, cornerSky));
            insideLightmap = LightTexture.packWithFraction(Math.max(minBlock, insideBlock), Math.max(minSky, insideSky));
        }

        return sideLightmapA + sideLightmapB + cornerLightmap + insideLightmap >> 2 & 0xFF00FF;
    }
}
