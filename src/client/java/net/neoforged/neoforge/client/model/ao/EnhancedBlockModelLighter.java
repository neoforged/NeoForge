/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.ao;

import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelLighter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.config.NeoForgeClientConfig;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3fc;
import org.slf4j.Logger;

/**
 * Entrypoint and main class of our enhanced AO pipeline.
 *
 * <p>Vanilla's AO logic works well for faces that are axis-aligned.
 * That computation is replicated in {@link FullFaceCalculator}, with some bug fixes.
 * The job of the enhanced pipeline is to handle faces that are more complicated,
 * by combining multiple full faces as needed using interpolation.
 *
 * <p>Compared to vanilla, we also remove any assumption about vertex order in the quad.
 */
public class EnhancedBlockModelLighter extends BlockModelLighter {
    public static BlockModelLighter newInstance() {
        if (NeoForgeClientConfig.INSTANCE.enhancedLighting.getAsBoolean()) {
            return new EnhancedBlockModelLighter();
        } else {
            return new BlockModelLighter();
        }
    }

    /**
     * Debug option to compare the emulated vanilla AO with the actual vanilla AO.
     * Only does something if emulated AO is enabled.
     */
    private static final boolean COMPARE_WITH_VANILLA = Boolean.getBoolean("neoforge.ao.compareWithVanilla");
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Cache these objects so that they don't need to be reallocated for every {@link EnhancedBlockModelLighter}.
     */
    private record AoObjectCache(FullFaceCalculator calculator, float[] weights) {}

    private static final ThreadLocal<AoObjectCache> AO_OBJECT_CACHE = ThreadLocal.withInitial(() -> new AoObjectCache(
            new FullFaceCalculator(),
            new float[4]));

    /**
     * Calculator for full faces.
     */
    private final FullFaceCalculator calculator;
    // Avoid repeated allocations of this array
    private final float[] weights;

    public EnhancedBlockModelLighter() {
        var cache = AO_OBJECT_CACHE.get();
        this.calculator = cache.calculator;
        this.weights = cache.weights;
    }

    @Override
    public void reset() {
        // Reset AO Face cache
        this.calculator.startBlock(this.cache);
    }

    @Override
    public void prepareQuadAmbientOcclusion(BlockAndTintGetter level, BlockState state, BlockPos pos, BakedQuad quad, QuadInstance outputInstance) {
        this.prepareQuadShape(level, state, pos, quad, true);

        // Enhanced calculation
        // Vanilla uses ==. We could add an epsilon to use the cheaper axis-aligned logic for almost axis-aligned faces.
        Direction direction = quad.direction();
        boolean isAxisAligned = switch (direction) {
            case DOWN, UP -> faceShape[SizeInfo.DOWN.index] == faceShape[SizeInfo.UP.index];
            case NORTH, SOUTH -> faceShape[SizeInfo.NORTH.index] == faceShape[SizeInfo.SOUTH.index];
            case WEST, EAST -> faceShape[SizeInfo.WEST.index] == faceShape[SizeInfo.EAST.index];
        };

        if (isAxisAligned) {
            calculateAxisAligned(level, state, pos, direction, quad, outputInstance);
        } else {
            calculateIrregular(level, state, pos, quad, outputInstance);
        }
    }

    /**
     * Computes AO for an axis-aligned quad.
     *
     * <p>This is similar to vanilla in how we select whether to use the inside or outside light.
     * However, we still use our own interpolation logic which does not make any assumption about vertex winding order.
     */
    private void calculateAxisAligned(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction, BakedQuad quad, QuadInstance outputInstance) {
        // Same logic as vanilla: sample outside if the depth is small, or force outside if we are a full block.
        // This is already stored in the faceCubic field.
        var fullFace = this.calculator.calculateFace(level, state, pos, direction, quad.materialInfo().shade(), this.faceCubic);

        // Perform bilinear interpolation to map a full AO face to actual vertex brightness and lightmap.
        // This will work regardless of the vertex order or position
        AoFace aoFace = AoFace.fromDirection(direction);
        float[] weights = this.weights;
        for (int vertex = 0; vertex < 4; ++vertex) {
            Vector3fc vertPos = quad.position(vertex);
            aoFace.computeCornerWeights(weights, vertPos.x(), vertPos.y(), vertPos.z());
            outputInstance.setColor(vertex, ARGB.gray(interpolateBrightness(fullFace, weights)));
            outputInstance.setLightCoords(vertex, interpolateLightmap(fullFace, weights));
        }

        // Debug option to compare emulated vanilla AO with actual vanilla AO.
        // Since we make changes compared to vanilla's AO, many quads will trigger the warning.
        if (COMPARE_WITH_VANILLA) {
            // This is a debug option, so allocations are fine
            QuadInstance vanilla = new QuadInstance();

            super.prepareQuadAmbientOcclusion(level, state, pos, quad, vanilla);

            for (int vertex = 0; vertex < 4; ++vertex) {
                if (!Mth.equal(vanilla.getColor(vertex), outputInstance.getColor(vertex)) || vanilla.getLightCoords(vertex) != outputInstance.getLightCoords(vertex)) {
                    LOGGER.warn("Emulated vanilla AO differs from actual AO at vertex {} of face {}, while lighting {}@{}\n"
                            + "Vanilla: lightmap = {}, brightness = {}\n"
                            + "Emulated: lightmap = {}, brightness = {}\n",
                            vertex,
                            direction,
                            state.getBlock(),
                            pos,
                            Integer.toHexString(vanilla.getLightCoords(vertex)),
                            Integer.toHexString(vanilla.getColor(vertex)),
                            Integer.toHexString(outputInstance.getLightCoords(vertex)),
                            Integer.toHexString(outputInstance.getColor(vertex)));
                    break;
                }
            }
        }
    }

    private static final float AO_EPS = 1e-4f;
    private static final float AVERAGE_WEIGHT = 0.75f;
    private static final float MAX_WEIGHT = 1 - AVERAGE_WEIGHT;

    /**
     * Computes AO for a general quad.
     * Projects onto each axis, computes the AO, then combines proportionally to the square of each normal component.
     */
    private void calculateIrregular(BlockAndTintGetter level, BlockState state, BlockPos pos, BakedQuad quad, QuadInstance outputInstance) {
        boolean shade = quad.materialInfo().shade();
        int quadNormal = -1;

        for (int vertex = 0; vertex < 4; ++vertex) {
            // Handle each vertex separately to apply vertex normals.

            int normal = quad.bakedNormals().normal(vertex);
            // The ignored byte is padding and may be filled with user data
            if (BakedNormals.isUnspecified(normal)) {
                // No normal! Try to use the quad normal.
                if (quadNormal == -1) {
                    quadNormal = BakedNormals.computeQuadNormal(quad.position0(), quad.position1(), quad.position2(), quad.position3());
                }
                normal = quadNormal;
            }

            float weightedBrightness = 0;
            int weightedLightmap = 0;
            float maxBrightness = 0;
            int maxLightmap = 0;

            for (int axis = 0; axis < 3; ++axis) {
                float normalComponent = BakedNormals.unpackComponent(normal, axis);
                if (normalComponent == 0) {
                    continue;
                }

                // Choose AO face based on normal sign
                Direction direction = switch (axis) {
                    case 0 -> normalComponent > 0 ? Direction.EAST : Direction.WEST;
                    case 1 -> normalComponent > 0 ? Direction.UP : Direction.DOWN;
                    case 2 -> normalComponent > 0 ? Direction.SOUTH : Direction.NORTH;
                    default -> throw new AssertionError();
                };

                // Compute full face
                AoFace aoFace = AoFace.fromDirection(direction);
                Vector3fc vertPos = quad.position(vertex);
                float depth = aoFace.computeDepth(vertPos.x(), vertPos.y(), vertPos.z());
                // Same logic as vanilla: sample outside if the depth is small, or force outside if we are a full block.
                boolean sampleOutside = depth < AO_EPS || state.isCollisionShapeFullBlock(level, pos);
                AoCalculatedFace fullFace = this.calculator.calculateFace(level, state, pos, direction, shade, sampleOutside);

                // Perform bilinear interpolation to map full AO face to this vertex.
                float[] weights = this.weights;
                aoFace.computeCornerWeights(weights, vertPos.x(), vertPos.y(), vertPos.z());
                float brightness = interpolateBrightness(fullFace, weights);
                int lightmap = interpolateLightmap(fullFace, weights);

                // Blend proportionally to the square of the normal component
                float axisWeight = normalComponent * normalComponent;
                weightedBrightness += brightness * axisWeight;
                weightedLightmap = lerpLightmap(weightedLightmap, 1, lightmap, axisWeight);

                // Also keep track of the max, which will be used later
                // to make sure the quad does not get too dark.
                maxBrightness = Math.max(maxBrightness, brightness);
                maxLightmap = maxLightmap(maxLightmap, lightmap);
            }

            // Do an average between the max and the weighted average.
            // Using only the weighted average looks a bit too dark.
            outputInstance.setColor(vertex, ARGB.gray(Math.clamp(weightedBrightness * AVERAGE_WEIGHT + maxBrightness * MAX_WEIGHT, 0.0F, 1.0F)));
            outputInstance.setLightCoords(vertex, lerpLightmap(weightedLightmap, AVERAGE_WEIGHT, maxLightmap, MAX_WEIGHT));
        }
    }

    @Override
    public void prepareQuadFlat(BlockAndTintGetter level, BlockState state, BlockPos pos, int lightCoords, BakedQuad quad, QuadInstance outputInstance) {
        if (!quad.materialInfo().shade()) {
            super.prepareQuadFlat(level, state, pos, lightCoords, quad, outputInstance);
            return;
        }

        if (lightCoords == -1) {
            this.prepareQuadShape(level, state, pos, quad, false);
            BlockPos lightPos = this.faceCubic ? this.scratchPos.setWithOffset(pos, quad.direction()) : pos;
            outputInstance.setLightCoords(this.cache.getLightCoords(state, level, lightPos));
        } else {
            outputInstance.setLightCoords(lightCoords);
        }

        CardinalLighting cardinalLighting = level.cardinalLighting();
        int quadNormal = -1;
        for (int vertex = 0; vertex < 4; ++vertex) {
            // Handle each vertex separately to apply vertex normals.

            int normal = quad.bakedNormals().normal(vertex);
            // The ignored byte is padding and may be filled with user data
            if (BakedNormals.isUnspecified(normal)) {
                // No normal! Try to use the quad normal.
                if (quadNormal == -1) {
                    quadNormal = BakedNormals.computeQuadNormal(quad.position0(), quad.position1(), quad.position2(), quad.position3());
                }
                normal = quadNormal;
            }

            float weightedBrightness = 0F;
            for (int axis = 0; axis < 3; ++axis) {
                float normalComponent = BakedNormals.unpackComponent(normal, axis);
                if (normalComponent == 0) {
                    continue;
                }

                // Choose AO face based on normal sign
                Direction direction = switch (axis) {
                    case 0 -> normalComponent > 0 ? Direction.EAST : Direction.WEST;
                    case 1 -> normalComponent > 0 ? Direction.UP : Direction.DOWN;
                    case 2 -> normalComponent > 0 ? Direction.SOUTH : Direction.NORTH;
                    default -> throw new AssertionError();
                };

                // Blend proportionally to the square of the normal component
                float axisWeight = normalComponent * normalComponent;
                weightedBrightness += cardinalLighting.byFace(direction) * axisWeight;
            }
            outputInstance.setColor(vertex, ARGB.gray(Math.min(weightedBrightness, 1F)));
        }
    }

    /**
     * Interpolates brightness from the 4 corners of a face.
     */
    private static float interpolateBrightness(AoCalculatedFace in, float[] weights) {
        return Math.clamp(in.brightness0 * weights[0] + in.brightness1 * weights[1] + in.brightness2 * weights[2] + in.brightness3 * weights[3], 0.0F, 1.0F);
    }

    /**
     * Interpolates lightmap from the 4 corners of a face.
     */
    private static int interpolateLightmap(AoCalculatedFace in, float[] weights) {
        return LightCoordsUtil.smoothWeightedBlend(in.lightmap0, in.lightmap1, in.lightmap2, in.lightmap3, weights[0], weights[1], weights[2], weights[3]);
    }

    /**
     * Interpolates two lightmaps linearly.
     */
    private static int lerpLightmap(int lightmap1, float w1, int lightmap2, float w2) {
        // Interpolate the two components separately
        int block1 = LightCoordsUtil.smoothBlock(lightmap1);
        int block2 = LightCoordsUtil.smoothBlock(lightmap2);
        int block = 0xFF & Math.round(block1 * w1 + block2 * w2);

        int sky1 = LightCoordsUtil.smoothSky(lightmap1);
        int sky2 = LightCoordsUtil.smoothSky(lightmap2);
        int sky = 0xFF & Math.round(sky1 * w1 + sky2 * w2);

        return LightCoordsUtil.smoothPack(block, sky);
    }

    static int maxLightmap(int lightmap1, int lightmap2) {
        return LightCoordsUtil.smoothPack(
                Math.max(LightCoordsUtil.smoothBlock(lightmap1), LightCoordsUtil.smoothBlock(lightmap2)),
                Math.max(LightCoordsUtil.smoothSky(lightmap1), LightCoordsUtil.smoothSky(lightmap2)));
    }
}
