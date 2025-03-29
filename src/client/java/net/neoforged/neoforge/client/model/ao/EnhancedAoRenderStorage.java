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
import net.neoforged.neoforge.client.model.LightingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entrypoint for our enhanced ambient occlusion pipeline.
 *
 * TODO explain what's going on
 */
public class EnhancedAoRenderStorage extends ModelBlockRenderer.AmbientOcclusionRenderStorage {
    /**
     * Debug option to compare the emulated vanilla AO with the actual vanilla AO.
     * Only does something if emulated AO is enabled.
     */
    private static final boolean COMPARE_WITH_VANILLA = Boolean.getBoolean("neoforge.ao.compareWithVanilla");
    private static final Logger LOGGER = LogUtils.getLogger();

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
        boolean vanillaRequested = currentQuad.lightingMode() == LightingMode.VANILLA;
        AoConfig config = NeoForgeClientConfig.INSTANCE.ambientOcclusion.get();

        if (config == AoConfig.VANILLA) {
            super.calculate(level, state, pos, direction, shade);
        } else if (config == AoConfig.EMULATE || (config == AoConfig.HYBRID && vanillaRequested)) {
            calculateEmulatedVanilla(level, state, pos, direction, shade);
        } else {
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
    }

    private final float[] weights = new float[4];

    private void calculateEmulatedVanilla(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction, boolean shade) {
        // Vanilla will always compute a single face; outside is decided by this.faceCubic
        var fullFace = this.calculator.calculateFace(level, state, pos, direction, shade, this.faceCubic);
        interpolateFace(fullFace, direction);

        // Debug option to compare emulated vanilla AO with actual vanilla AO.
        // We are not interested in assuming a quad winding order,
        // so quads that have the wrong winding might trigger the warning.
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
        }
    }

    /**
     * Performs bilinear interpolation to map a full AO face to actual vertex brightness and lightmap.
     * This will work regardless of the vertex order and positions.
     */
    private void interpolateFace(AoCalculatedFace fullFace, Direction direction) {
        AoFace aoFace = AoFace.fromDirection(direction);
        int[] vertices = this.currentQuad.vertices();
        float[] weights = this.weights;
        for (int vertex = 0; vertex < 4; ++vertex) {
            aoFace.computeCornerWeights(weights, vertexPos(vertices, vertex, 0), vertexPos(vertices, vertex, 1), vertexPos(vertices, vertex, 2));
            brightness[vertex] = interpolateBrightness(fullFace, weights);
            lightmap[vertex] = interpolateLightmap(fullFace, weights);
        }
    }

    private static final float AO_EPS = 1e-4f;
    // Avoid repeated allocations
    private final AoCalculatedFace temp = new AoCalculatedFace();

    private AoCalculatedFace gatherAxisAligned(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction, boolean shade, int vertex) {
        int[] vertices = currentQuad.vertices();
        float depth = AoFace.fromDirection(direction).computeDepth(
                vertexPos(vertices, vertex, 0),
                vertexPos(vertices, vertex, 1),
                vertexPos(vertices, vertex, 2));

        // Interpolate between inside and outside light, depending on depth
        if (depth < AO_EPS) { // Avoid linear interpolation if we are exactly on one of the faces
            return this.calculator.calculateFace(level, state, pos, direction, shade, true);
        } else if (depth > 1 - AO_EPS) {
            return this.calculator.calculateFace(level, state, pos, direction, shade, false);
        } else {
            AoCalculatedFace faceInner = this.calculator.calculateFace(level, state, pos, direction, shade, false);
            AoCalculatedFace faceOuter = this.calculator.calculateFace(level, state, pos, direction, shade, true);
            return combineLinearly(temp, faceInner, depth, faceOuter, 1 - depth);
        }
    }

    /**
     * Calculate AO for a face that is axis-aligned, but not coplanar with the light face.
     * This means that it is parallel to the light face, but inside the block.
     */
    private void calculateAxisAligned(BlockAndTintGetter level, BlockState state, BlockPos pos, Direction direction, boolean shade) {
        // Pass vertex 0: it's only used for depth and since the face is axis-aligned all vertices have the same depth.
        var fullFace = gatherAxisAligned(level, state, pos, direction, shade, 0);

        interpolateFace(fullFace, direction);
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
                AoCalculatedFace fullFace = gatherAxisAligned(level, state, pos, lightFace, shade, vertex);

                // Perform bilinear interpolation to map full AO face to this vertex.
                AoFace aoFace = AoFace.fromDirection(lightFace);
                int[] vertices = this.currentQuad.vertices();
                float[] weights = this.weights;
                aoFace.computeCornerWeights(weights, vertexPos(vertices, vertex, 0), vertexPos(vertices, vertex, 1), vertexPos(vertices, vertex, 2));
                float brightness = interpolateBrightness(fullFace, weights);
                int lightmap = interpolateLightmap(fullFace, weights);

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

    private static float vertexPos(int[] vertices, int vertex, int axis) {
        return Float.intBitsToFloat(vertices[vertex * 8 + axis]);
    }

    private float interpolateBrightness(AoCalculatedFace in, float[] weights) {
        return Math.clamp(in.brightness0 * weights[0] + in.brightness1 * weights[1] + in.brightness2 * weights[2] + in.brightness3 * weights[3], 0.0F, 1.0F);
    }

    private int interpolateLightmap(AoCalculatedFace in, float[] weights) {
        return blend(in.lightmap0, in.lightmap1, in.lightmap2, in.lightmap3, weights[0], weights[1], weights[2], weights[3]);
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
