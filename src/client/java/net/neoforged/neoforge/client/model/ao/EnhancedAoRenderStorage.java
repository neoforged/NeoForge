package net.neoforged.neoforge.client.model.ao;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.LightingMode;

/**
 * Entrypoint for our enhanced ambient occlusion pipeline.
 *
 * <p>Important terminology:
 * <ul>
 *     <li>Light face: the face of a perfect cube indicated by {@link BakedQuad#direction()}.</li>
 *     <li>A face is partial if: the orthogonal projection of the face onto the light face is not a perfect square.</li>
 *     <li>A face is axis-aligned if: the face is parallel to its light face.</li>
 *     <li>A face is coordinate cubic if: the face is coplanar with the light face.
 *     This is not called simply "cubic" because vanilla forces axis-aligned faces to be cubic if the block is solid.</li>
 * </ul>
 */
public class EnhancedAoRenderStorage extends ModelBlockRenderer.AmbientOcclusionRenderStorage {
    private final AoCalculator calculator;
    private BakedQuad currentQuad;

    public EnhancedAoRenderStorage() {
        this.calculator = new AoCalculator(this.cache);
    }

    @Override
    public void captureQuad(BakedQuad quad) {
        this.currentQuad = quad;
    }

    @Override
    public void calculate(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction, boolean shade) {
//        if (currentQuad.lightingMode() == LightingMode.VANILLA) {
//            super.calculate(level, state, pos, direction, shade);
//            return;
//        }

        if (this.faceAxisAligned) {
            if (this.faceCoordinateCubic) {
                calculateVanilla(level, state, pos, direction, shade, true);
            } else {
                calculateAxisAligned(level, state, pos, direction, shade);
            }
        } else {
            calculateIrregular(level, state, pos, shade);
        }
    }

    private final AoCalculatedFace temp = new AoCalculatedFace();
    private final AoCalculatedFace temp2 = new AoCalculatedFace();
    private final AoCalculatedFace temp3 = new AoCalculatedFace();

    private void calculateVanilla(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction, boolean shade, boolean sampleOutside) {
        var face = this.calculator.calculateFace(level, state, pos, direction, shade, sampleOutside);
        face = this.facePartial ? interpolatePartial(temp, face, direction) : face;
        face.copyToResult(this.brightness, this.lightmap, direction);
    }

    /**
     * Calculate AO for a face that is axis-aligned, but not coplanar with the light face.
     * This means that it is parallel to the light face, but inside the block.
     */
    private void calculateAxisAligned(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction, boolean shade) {
        float depth = switch (direction) {
            case DOWN -> faceShape[ModelBlockRenderer.SizeInfo.DOWN.index];
            case UP -> faceShape[ModelBlockRenderer.SizeInfo.FLIP_UP.index];
            case NORTH -> faceShape[ModelBlockRenderer.SizeInfo.NORTH.index];
            case SOUTH -> faceShape[ModelBlockRenderer.SizeInfo.FLIP_SOUTH.index];
            case WEST -> faceShape[ModelBlockRenderer.SizeInfo.WEST.index];
            case EAST -> faceShape[ModelBlockRenderer.SizeInfo.FLIP_EAST.index];
        };
        depth = Math.clamp(depth, 0, 1);

        var faceInner = this.calculator.calculateFace(level, state, pos, direction, shade, false);
        var faceOuter = this.calculator.calculateFace(level, state, pos, direction, shade, true);

        var faceCombined = combineLinearly(temp, faceInner, depth, faceOuter, 1 - depth);
        var face = this.facePartial ? interpolatePartial(temp2, faceCombined, direction) : faceCombined;
        face.copyToResult(this.brightness, this.lightmap, direction);
    }

    private AoCalculatedFace gatherIrregularProjectedFullFace(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction, boolean shade) {
        float depth = switch (direction) {
            case DOWN -> tempShape[ModelBlockRenderer.SizeInfo.DOWN.index];
            case UP -> tempShape[ModelBlockRenderer.SizeInfo.FLIP_UP.index];
            case NORTH -> tempShape[ModelBlockRenderer.SizeInfo.NORTH.index];
            case SOUTH -> tempShape[ModelBlockRenderer.SizeInfo.FLIP_SOUTH.index];
            case WEST -> tempShape[ModelBlockRenderer.SizeInfo.WEST.index];
            case EAST -> tempShape[ModelBlockRenderer.SizeInfo.FLIP_EAST.index];
        };
        depth = Math.clamp(depth, 0, 1);

        var faceInner = this.calculator.calculateFace(level, state, pos, direction, shade, false);
        var faceOuter = this.calculator.calculateFace(level, state, pos, direction, shade, true);

        return combineLinearly(temp, faceInner, depth, faceOuter, 1 - depth);
    }

    /**
     * Non axis-aligned face. Project onto each axis, compute the AO, then combine proportionally to the square of each normal component.
     */
    private void calculateIrregular(BlockAndTintGetter level, BlockState state, BlockPos pos, boolean shade) {
        int quadNormal = -1;

        for (int vertex = 0; vertex < 4; ++vertex) {
            // Handle each vertex separately to apply vertex normals.

            int normal = currentQuad.vertices()[IQuadTransformer.STRIDE * vertex + IQuadTransformer.NORMAL];
            // The ignored byte is padding and may be filled with user data
            if ((normal & 0x00FFFFFF) == 0) {
                // No normal! Try to use the quad normal.
                if (quadNormal == -1) {
                    quadNormal = ClientHooks.computeQuadNormal(currentQuad.vertices());
                }
                normal = quadNormal;
            }

            float vertexBrightness = 0;
            float maxBrightness = 0;
            int vertexLightmap = 0;
            int maxBlock = 0;
            int maxSky = 0;

            for (int axis = 0; axis < 3; ++axis) {
                int encodedNormalComponent = (normal >> (axis * 8)) & 0xFF;
                // Casting to byte will cast to a signed int.
                float normalComponent = ((byte) encodedNormalComponent) / 127.0f;
                if (normalComponent == 0) {
                    continue;
                }

                Direction lightFace = switch (axis) {
                    case 0 -> normalComponent > 0 ? Direction.EAST : Direction.WEST;
                    case 1 -> normalComponent > 0 ? Direction.UP : Direction.DOWN;
                    case 2 -> normalComponent > 0 ? Direction.SOUTH : Direction.NORTH;
                    default -> throw new AssertionError();
                };
                fillVertexShape(vertex);
                AoCalculatedFace face = gatherIrregularProjectedFullFace(level, state, pos, lightFace, shade);

                // Now we need to find the corner of that face that corresponds to the current vertex
                ModelBlockRenderer.AmbientVertexRemap vertexRemap = ModelBlockRenderer.AmbientVertexRemap.fromFacing(lightFace);
                if (vertex == vertexRemap.vert0) {
                    interpolateCorner(tempWeights, lightFace, 0, tempShape);
                } else if (vertex == vertexRemap.vert1) {
                    interpolateCorner(tempWeights, lightFace, 1, tempShape);
                } else if (vertex == vertexRemap.vert2) {
                    interpolateCorner(tempWeights, lightFace, 2, tempShape);
                } else if (vertex == vertexRemap.vert3) {
                    interpolateCorner(tempWeights, lightFace, 3, tempShape);
                } else {
                    throw new AssertionError();
                }

                float brightness = interpolateBrightness(face, tempWeights);
                int lightmap = interpolateLightmap(face, tempWeights);

                // TODO: indigo does an average between the max and the average; maybe it looks better?

                // Blend proportionally to the square of the normal component
                float axisWeight = normalComponent * normalComponent;
                vertexBrightness += brightness * axisWeight;
                maxBrightness = Math.max(maxBrightness, brightness);
                vertexLightmap = lerpLightmap(vertexLightmap, 1, lightmap, axisWeight);
                maxBlock = Math.max(maxBlock, LightTexture.block(lightmap));
                maxSky = Math.max(maxSky, LightTexture.sky(lightmap));
            }

            brightness[vertex] = vertexBrightness; // (vertexBrightness + maxBrightness) * 0.5f;
            lightmap[vertex] = vertexLightmap; // lerpLightmap(vertexLightmap, 0.5f, LightTexture.pack(maxBlock, maxSky), 0.5f);
        }
    }

    private final float[] tempShape = new float[12];
    private final float[] tempWeights = new float[4];

    private void fillVertexShape(int vertex) {
        // TODO: not a huge fan of this... :P
        float f6 = Float.intBitsToFloat(currentQuad.vertices()[vertex * 8]);
        float f7 = Float.intBitsToFloat(currentQuad.vertices()[vertex * 8 + 1]);
        float f8 = Float.intBitsToFloat(currentQuad.vertices()[vertex * 8 + 2]);
        float f = f6;
        float f1 = f7;
        float f2 = f8;
        float f3 = f6;
        float f4 = f7;
        float f5 = f8;

        tempShape[ModelBlockRenderer.SizeInfo.WEST.index] = f;
        tempShape[ModelBlockRenderer.SizeInfo.EAST.index] = f3;
        tempShape[ModelBlockRenderer.SizeInfo.DOWN.index] = f1;
        tempShape[ModelBlockRenderer.SizeInfo.UP.index] = f4;
        tempShape[ModelBlockRenderer.SizeInfo.NORTH.index] = f2;
        tempShape[ModelBlockRenderer.SizeInfo.SOUTH.index] = f5;
        tempShape[ModelBlockRenderer.SizeInfo.FLIP_WEST.index] = 1.0F - f;
        tempShape[ModelBlockRenderer.SizeInfo.FLIP_EAST.index] = 1.0F - f3;
        tempShape[ModelBlockRenderer.SizeInfo.FLIP_DOWN.index] = 1.0F - f1;
        tempShape[ModelBlockRenderer.SizeInfo.FLIP_UP.index] = 1.0F - f4;
        tempShape[ModelBlockRenderer.SizeInfo.FLIP_NORTH.index] = 1.0F - f2;
        tempShape[ModelBlockRenderer.SizeInfo.FLIP_SOUTH.index] = 1.0F - f5;
    }

    private static void interpolateCorner(float[] out, Direction lightFace, int corner, float[] tempShape) {
        var modelblockrenderer$adjacencyinfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(lightFace);
        var weights = switch (corner) {
            case 0 -> modelblockrenderer$adjacencyinfo.vert0Weights;
            case 1 -> modelblockrenderer$adjacencyinfo.vert1Weights;
            case 2 -> modelblockrenderer$adjacencyinfo.vert2Weights;
            case 3 -> modelblockrenderer$adjacencyinfo.vert3Weights;
            default -> throw new AssertionError();
        };

        out[0] = tempShape[weights[0].index] * tempShape[weights[1].index];
        out[1] = tempShape[weights[2].index] * tempShape[weights[3].index];
        out[2] = tempShape[weights[4].index] * tempShape[weights[5].index];
        out[3] = tempShape[weights[6].index] * tempShape[weights[7].index];
    }

    private float interpolateBrightness(AoCalculatedFace in, float[] weights) {
        return Math.clamp(in.brightness0 * weights[0] + in.brightness1 * weights[1] + in.brightness2 * weights[2] + in.brightness3 * weights[3], 0.0F, 1.0F);
    }

    private int interpolateLightmap(AoCalculatedFace in, float[] weights) {
        return blend(in.lightmap0, in.lightmap1, in.lightmap2, in.lightmap3, weights[0], weights[1], weights[2], weights[3]);
    }

    private AoCalculatedFace interpolatePartial(AoCalculatedFace out, AoCalculatedFace in, Direction direction) {
        var modelblockrenderer$adjacencyinfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(direction);

        float f29 = in.brightness0;
        float f31 = in.brightness1;
        float f32 = in.brightness2;
        float f33 = in.brightness3;
        float f13 = this.faceShape[modelblockrenderer$adjacencyinfo.vert0Weights[0].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert0Weights[1].index];
        float f14 = this.faceShape[modelblockrenderer$adjacencyinfo.vert0Weights[2].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert0Weights[3].index];
        float f15 = this.faceShape[modelblockrenderer$adjacencyinfo.vert0Weights[4].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert0Weights[5].index];
        float f16 = this.faceShape[modelblockrenderer$adjacencyinfo.vert0Weights[6].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert0Weights[7].index];
        float f17 = this.faceShape[modelblockrenderer$adjacencyinfo.vert1Weights[0].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert1Weights[1].index];
        float f18 = this.faceShape[modelblockrenderer$adjacencyinfo.vert1Weights[2].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert1Weights[3].index];
        float f19 = this.faceShape[modelblockrenderer$adjacencyinfo.vert1Weights[4].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert1Weights[5].index];
        float f20 = this.faceShape[modelblockrenderer$adjacencyinfo.vert1Weights[6].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert1Weights[7].index];
        float f21 = this.faceShape[modelblockrenderer$adjacencyinfo.vert2Weights[0].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert2Weights[1].index];
        float f22 = this.faceShape[modelblockrenderer$adjacencyinfo.vert2Weights[2].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert2Weights[3].index];
        float f23 = this.faceShape[modelblockrenderer$adjacencyinfo.vert2Weights[4].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert2Weights[5].index];
        float f24 = this.faceShape[modelblockrenderer$adjacencyinfo.vert2Weights[6].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert2Weights[7].index];
        float f25 = this.faceShape[modelblockrenderer$adjacencyinfo.vert3Weights[0].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert3Weights[1].index];
        float f26 = this.faceShape[modelblockrenderer$adjacencyinfo.vert3Weights[2].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert3Weights[3].index];
        float f27 = this.faceShape[modelblockrenderer$adjacencyinfo.vert3Weights[4].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert3Weights[5].index];
        float f28 = this.faceShape[modelblockrenderer$adjacencyinfo.vert3Weights[6].index]
                * this.faceShape[modelblockrenderer$adjacencyinfo.vert3Weights[7].index];
        out.brightness0 = Math.clamp(f29 * f13 + f31 * f14 + f32 * f15 + f33 * f16, 0.0F, 1.0F);
        out.brightness1 = Math.clamp(f29 * f17 + f31 * f18 + f32 * f19 + f33 * f20, 0.0F, 1.0F);
        out.brightness2 = Math.clamp(f29 * f21 + f31 * f22 + f32 * f23 + f33 * f24, 0.0F, 1.0F);
        out.brightness3 = Math.clamp(f29 * f25 + f31 * f26 + f32 * f27 + f33 * f28, 0.0F, 1.0F);

        int i2 = in.lightmap0;
        int j2 = in.lightmap1;
        int k2 = in.lightmap2;
        int l2 = in.lightmap3;
        out.lightmap0 = blend(i2, j2, k2, l2, f13, f14, f15, f16);
        out.lightmap1 = blend(i2, j2, k2, l2, f17, f18, f19, f20);
        out.lightmap2 = blend(i2, j2, k2, l2, f21, f22, f23, f24);
        out.lightmap3 = blend(i2, j2, k2, l2, f25, f26, f27, f28);

        return out;
    }

    private AoCalculatedFace combineLinearly(AoCalculatedFace out, AoCalculatedFace in1, float w1, AoCalculatedFace in2, float w2) {
        out.brightness0 = in1.brightness0 * w1 + in2.brightness0 * w2;
        out.brightness1 = in1.brightness1 * w1 + in2.brightness1 * w2;
        out.brightness2 = in1.brightness2 * w1 + in2.brightness2 * w2;
        out.brightness3 = in1.brightness3 * w1 + in2.brightness3 * w2;

        out.lightmap0 = lerpLightmap(in1.lightmap0, w1, in2.lightmap0, w2);
        out.lightmap1 = lerpLightmap(in1.lightmap1, w1, in2.lightmap1, w2);
        out.lightmap2 = lerpLightmap(in1.lightmap2, w1, in2.lightmap2, w2);
        out.lightmap3 = lerpLightmap(in1.lightmap3, w1, in2.lightmap3, w2);

        return out;
    }

    private int lerpLightmap(int lightmap1, float w1, int lightmap2, float w2) {
        // Interpolate the two components separately
        int block1 = LightTexture.block(lightmap1);
        int block2 = LightTexture.block(lightmap2);
        int block = (int) (block1 * w1 + block2 * w2);

        int sky1 = LightTexture.sky(lightmap1);
        int sky2 = LightTexture.sky(lightmap2);
        int sky = (int) (sky1 * w1 + sky2 * w2);

        return LightTexture.pack(block, sky);
    }
}
