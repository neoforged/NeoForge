/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.ao;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.config.NeoForgeClientConfig;
import net.neoforged.neoforge.client.model.IQuadTransformer;
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
public class EnhancedAoRenderStorage extends ModelBlockRenderer.AmbientOcclusionRenderStorage {
    public static ModelBlockRenderer.AmbientOcclusionRenderStorage newInstance() {
        if (NeoForgeClientConfig.INSTANCE.enhancedLighting.getAsBoolean()) {
            return new EnhancedAoRenderStorage();
        } else {
            return new ModelBlockRenderer.AmbientOcclusionRenderStorage();
        }
    }

    /**
     * "Enhanced" flat shading logic.
     */
    public static void applyFlatQuadBrightness(BlockAndTintGetter level, BakedQuad quad, ModelBlockRenderer.CommonRenderStorage storage) {
        if (NeoForgeClientConfig.INSTANCE.enhancedLighting.getAsBoolean()) {
            int quadNormal = -1;

            for (int vertex = 0; vertex < 4; ++vertex) {
                // Handle each vertex separately to apply vertex normals.

                int normal = quad.vertices()[IQuadTransformer.STRIDE * vertex + IQuadTransformer.NORMAL];
                // The ignored byte is padding and may be filled with user data
                if ((normal & 0x00FFFFFF) == 0) {
                    // No normal! Try to use the quad normal.
                    if (quadNormal == -1) {
                        quadNormal = ClientHooks.computeQuadNormal(quad.vertices());
                    }
                    normal = quadNormal;
                }

                storage.brightness[vertex] = level.getShade(
                        normalComponent(normal, 0),
                        normalComponent(normal, 1),
                        normalComponent(normal, 2),
                        quad.shade());
            }
        } else {
            float f = level.getShade(quad.direction(), quad.shade());
            storage.brightness[0] = f;
            storage.brightness[1] = f;
            storage.brightness[2] = f;
            storage.brightness[3] = f;
        }
    }

    /**
     * Debug option to compare the emulated vanilla AO with the actual vanilla AO.
     * Only does something if emulated AO is enabled.
     */
    private static final boolean COMPARE_WITH_VANILLA = Boolean.getBoolean("neoforge.ao.compareWithVanilla");
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Cache these objects so that they don't need to be reallocated for every {@link EnhancedAoRenderStorage}.
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

    private BakedQuad currentQuad;

    public EnhancedAoRenderStorage() {
        var cache = AO_OBJECT_CACHE.get();
        this.calculator = cache.calculator;
        this.weights = cache.weights;
        // Reset AO Face cache
        this.calculator.startBlock(this.cache);
    }

    @Override
    public void captureQuad(BakedQuad quad) {
        this.currentQuad = quad;
    }

    @Override
    public void calculate(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction, boolean shade) {
        if (this.currentQuad == null) {
            throw new IllegalStateException("Make sure to pass the quad via captureQuad before calling calculate.");
        }

        // Enhanced calculation
        // Vanilla uses ==. We could add an epsilon to use the cheaper axis-aligned logic for almost axis-aligned faces.
        boolean isAxisAligned = switch (direction) {
            case DOWN, UP -> faceShape[ModelBlockRenderer.SizeInfo.DOWN.index] == faceShape[ModelBlockRenderer.SizeInfo.UP.index];
            case NORTH, SOUTH -> faceShape[ModelBlockRenderer.SizeInfo.NORTH.index] == faceShape[ModelBlockRenderer.SizeInfo.SOUTH.index];
            case WEST, EAST -> faceShape[ModelBlockRenderer.SizeInfo.WEST.index] == faceShape[ModelBlockRenderer.SizeInfo.EAST.index];
        };

        if (isAxisAligned) {
            calculateAxisAligned(level, state, pos, direction, shade);
        } else {
            calculateIrregular(level, state, pos, shade);
        }
    }

    /**
     * Computes AO for an axis-aligned quad.
     *
     * <p>This is similar to vanilla in how we select whether to use the inside or outside light.
     * However, we still use our own interpolation logic which does not make any assumption about vertex winding order.
     */
    private void calculateAxisAligned(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction, boolean shade) {
        // Same logic as vanilla: sample outside if the depth is small, or force outside if we are a full block.
        // This is already stored in the faceCubic field.
        var fullFace = this.calculator.calculateFace(level, state, pos, direction, shade, this.faceCubic);

        // Perform bilinear interpolation to map a full AO face to actual vertex brightness and lightmap.
        // This will work regardless of the vertex order or position
        AoFace aoFace = AoFace.fromDirection(direction);
        int[] vertices = this.currentQuad.vertices();
        float[] weights = this.weights;
        for (int vertex = 0; vertex < 4; ++vertex) {
            aoFace.computeCornerWeights(weights, vertexPos(vertices, vertex, 0), vertexPos(vertices, vertex, 1), vertexPos(vertices, vertex, 2));
            brightness[vertex] = interpolateBrightness(fullFace, weights);
            lightmap[vertex] = interpolateLightmap(fullFace, weights);
        }

        // Debug option to compare emulated vanilla AO with actual vanilla AO.
        // Since we make changes compared to vanilla's AO, many quads will trigger the warning.
        if (COMPARE_WITH_VANILLA) {
            // This is a debug option, so allocations are fine
            float[] emulatedBrightness = brightness.clone();
            int[] emulatedLightmap = lightmap.clone();

            super.calculate(level, state, pos, direction, shade);

            for (int vertex = 0; vertex < 4; ++vertex) {
                if (!Mth.equal(emulatedBrightness[vertex], brightness[vertex]) || emulatedLightmap[vertex] != lightmap[vertex]) {
                    LOGGER.warn("Emulated vanilla AO differs from actual AO at vertex {} of face {}, while lighting {}@{}\n"
                            + "Vanilla: lightmap = {}, brightness = {}\n"
                            + "Emulated: lightmap = {}, brightness = {}\n",
                            vertex, direction, state.getBlock(), pos, lightmap[vertex], brightness[vertex], emulatedLightmap[vertex], emulatedBrightness[vertex]);
                    break;
                }
            }

            // Revert to our AO
            System.arraycopy(emulatedBrightness, 0, brightness, 0, 4);
            System.arraycopy(emulatedLightmap, 0, lightmap, 0, 4);
        }
    }

    private static final float AO_EPS = 1e-4f;
    private static final float AVERAGE_WEIGHT = 0.75f;
    private static final float MAX_WEIGHT = 1 - AVERAGE_WEIGHT;

    /**
     * Computes AO for a general quad.
     * Projects onto each axis, computes the AO, then combines proportionally to the square of each normal component.
     */
    private void calculateIrregular(BlockAndTintGetter level, BlockState state, BlockPos pos, boolean shade) {
        int[] vertices = currentQuad.vertices();
        int quadNormal = -1;

        for (int vertex = 0; vertex < 4; ++vertex) {
            // Handle each vertex separately to apply vertex normals.

            int normal = vertices[IQuadTransformer.STRIDE * vertex + IQuadTransformer.NORMAL];
            // The ignored byte is padding and may be filled with user data
            if ((normal & 0x00FFFFFF) == 0) {
                // No normal! Try to use the quad normal.
                if (quadNormal == -1) {
                    quadNormal = ClientHooks.computeQuadNormal(vertices);
                }
                normal = quadNormal;
            }

            float weightedBrightness = 0;
            int weightedLightmap = 0;
            float maxBrightness = 0;
            int maxLightmap = 0;

            for (int axis = 0; axis < 3; ++axis) {
                float normalComponent = normalComponent(normal, axis);
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
                float depth = aoFace.computeDepth(
                        vertexPos(vertices, vertex, 0),
                        vertexPos(vertices, vertex, 1),
                        vertexPos(vertices, vertex, 2));
                // Same logic as vanilla: sample outside if the depth is small, or force outside if we are a full block.
                boolean sampleOutside = depth < AO_EPS || state.isCollisionShapeFullBlock(level, pos);
                AoCalculatedFace fullFace = this.calculator.calculateFace(level, state, pos, direction, shade, sampleOutside);

                // Perform bilinear interpolation to map full AO face to this vertex.
                float[] weights = this.weights;
                aoFace.computeCornerWeights(weights, vertexPos(vertices, vertex, 0), vertexPos(vertices, vertex, 1), vertexPos(vertices, vertex, 2));
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
            brightness[vertex] = Math.clamp(weightedBrightness * AVERAGE_WEIGHT + maxBrightness * MAX_WEIGHT, 0.0F, 1.0F);
            lightmap[vertex] = lerpLightmap(weightedLightmap, AVERAGE_WEIGHT, maxLightmap, MAX_WEIGHT);
        }
    }

    /**
     * Extracts the position of a vertex from quad data.
     *
     * @param vertices quad data
     * @param vertex   vertex index, from 0 to 3 included
     * @param axis     axis index, for 0 to 2 included
     */
    private static float vertexPos(int[] vertices, int vertex, int axis) {
        return Float.intBitsToFloat(vertices[vertex * IQuadTransformer.STRIDE + IQuadTransformer.POSITION + axis]);
    }

    /**
     * Unpacks a normal component.
     */
    private static float normalComponent(int normal, int axis) {
        int encodedNormalComponent = (normal >> (axis * 8)) & 0xFF;
        // Casting to byte will cast to a signed int.
        // This is really important, otherwise negative values will lead to above 1.0 normal components.
        return ((byte) encodedNormalComponent) / 127.0f;
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
        return blend(in.lightmap0, in.lightmap1, in.lightmap2, in.lightmap3, weights[0], weights[1], weights[2], weights[3]);
    }

    /**
     * Interpolates two lightmaps linearly.
     */
    private static int lerpLightmap(int lightmap1, float w1, int lightmap2, float w2) {
        // Interpolate the two components separately
        int block1 = LightTexture.blockWithFraction(lightmap1);
        int block2 = LightTexture.blockWithFraction(lightmap2);
        int block = 0xFF & Math.round(block1 * w1 + block2 * w2);

        int sky1 = LightTexture.skyWithFraction(lightmap1);
        int sky2 = LightTexture.skyWithFraction(lightmap2);
        int sky = 0xFF & Math.round(sky1 * w1 + sky2 * w2);

        return LightTexture.packWithFraction(block, sky);
    }

    static int maxLightmap(int lightmap1, int lightmap2) {
        return LightTexture.packWithFraction(
                Math.max(LightTexture.blockWithFraction(lightmap1), LightTexture.blockWithFraction(lightmap2)),
                Math.max(LightTexture.skyWithFraction(lightmap1), LightTexture.skyWithFraction(lightmap2)));
    }
}
