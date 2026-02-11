/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.mojang.blaze3d.platform.NativeImage;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.Material;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.MaterialBaker;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import net.neoforged.neoforge.client.model.quad.MutableQuad;
import net.neoforged.neoforge.client.textures.UnitTextureAtlasSprite;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

public class MutableQuadTests {
    private static final float WORLD_SCALE = 1 / 16f; // Factor for converting from "pixels" [0,16] to block space [0,1]
    private static final Vector3fc REFERENCE_BLOCK_MIN = new Vector3f(1, 2, 3);
    private static final Vector3fc REFERENCE_BLOCK_MAX = new Vector3f(8, 13, 16);

    static final String SQUARE_PARAMS = """
            side  | l | b | r  | t  | d
            NORTH | 8 | 2 | 15 | 13 | 3
            SOUTH | 1 | 2 | 8  | 13 | 0
            WEST  | 3 | 2 | 16 | 13 | 1
            EAST  | 0 | 2 | 13 | 13 | 8
            UP    | 1 | 0 | 8  | 13 | 3
            DOWN  | 1 | 3 | 8  | 16 | 2
            """;

    @CsvSource(textBlock = SQUARE_PARAMS, delimiter = '|', useHeadersInDisplayName = true)
    @ParameterizedTest
    public void testSetSquareAgainstVanillaBlockModel(Direction side, float left, float bottom, float right, float top, float depth) {
        var referenceQuads = buildReferenceQuads();

        var mutableQuad = new MutableQuad();
        mutableQuad.setCubeFaceFromSpriteCoords(
                side,
                left * WORLD_SCALE,
                bottom * WORLD_SCALE,
                right * WORLD_SCALE,
                top * WORLD_SCALE,
                depth * WORLD_SCALE);
        mutableQuad.setSpriteInfo(makeMockSpriteInfo());
        mutableQuad.bakeUvsFromPosition();
        assertQuadsEquals(referenceQuads.get(side), mutableQuad);
    }

    @EnumSource(Direction.class)
    @ParameterizedTest
    public void testSetSquareAgainstVanillaBlockModelForFullBlock(Direction side) {
        var referenceQuads = buildReferenceQuads(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16));

        var mutableQuad = new MutableQuad();
        mutableQuad.setCubeFaceFromSpriteCoords(side, 0, 0, 1, 1, 0);
        mutableQuad.setSpriteInfo(makeMockSpriteInfo());
        mutableQuad.bakeUvsFromPosition();
        assertQuadsEquals(referenceQuads.get(side), mutableQuad);
    }

    /**
     * These vertices were created using Fabrics QuadEmitter with baked UV using BAKE_UVLOCK,
     * and a sprite that matches the positions of MockSprite.
     */
    private static final Object[][] FABRIC_REFERENCE_DATA = {
            { Direction.NORTH, new Vector3f(0.5f, 0.8125f, 0.1875f), UVPair.pack(0.53125f, 0.51171875f) },
            { Direction.NORTH, new Vector3f(0.5f, 0.125f, 0.1875f), UVPair.pack(0.53125f, 0.5546875f) },
            { Direction.NORTH, new Vector3f(0.0625f, 0.125f, 0.1875f), UVPair.pack(0.55859375f, 0.5546875f) },
            { Direction.NORTH, new Vector3f(0.0625f, 0.8125f, 0.1875f), UVPair.pack(0.55859375f, 0.51171875f) },
            { Direction.SOUTH, new Vector3f(0.0625f, 0.8125f, 1.0f), UVPair.pack(0.50390625f, 0.51171875f) },
            { Direction.SOUTH, new Vector3f(0.0625f, 0.125f, 1.0f), UVPair.pack(0.50390625f, 0.5546875f) },
            { Direction.SOUTH, new Vector3f(0.5f, 0.125f, 1.0f), UVPair.pack(0.53125f, 0.5546875f) },
            { Direction.SOUTH, new Vector3f(0.5f, 0.8125f, 1.0f), UVPair.pack(0.53125f, 0.51171875f) },
            { Direction.WEST, new Vector3f(0.0625f, 0.8125f, 0.1875f), UVPair.pack(0.51171875f, 0.51171875f) },
            { Direction.WEST, new Vector3f(0.0625f, 0.125f, 0.1875f), UVPair.pack(0.51171875f, 0.5546875f) },
            { Direction.WEST, new Vector3f(0.0625f, 0.125f, 1.0f), UVPair.pack(0.5625f, 0.5546875f) },
            { Direction.WEST, new Vector3f(0.0625f, 0.8125f, 1.0f), UVPair.pack(0.5625f, 0.51171875f) },
            { Direction.EAST, new Vector3f(0.5f, 0.8125f, 1.0f), UVPair.pack(0.5f, 0.51171875f) },
            { Direction.EAST, new Vector3f(0.5f, 0.125f, 1.0f), UVPair.pack(0.5f, 0.5546875f) },
            { Direction.EAST, new Vector3f(0.5f, 0.125f, 0.1875f), UVPair.pack(0.55078125f, 0.5546875f) },
            { Direction.EAST, new Vector3f(0.5f, 0.8125f, 0.1875f), UVPair.pack(0.55078125f, 0.51171875f) },
            { Direction.UP, new Vector3f(0.0625f, 0.8125f, 0.1875f), UVPair.pack(0.50390625f, 0.51171875f) },
            { Direction.UP, new Vector3f(0.0625f, 0.8125f, 1.0f), UVPair.pack(0.50390625f, 0.5625f) },
            { Direction.UP, new Vector3f(0.5f, 0.8125f, 1.0f), UVPair.pack(0.53125f, 0.5625f) },
            { Direction.UP, new Vector3f(0.5f, 0.8125f, 0.1875f), UVPair.pack(0.53125f, 0.51171875f) },
            { Direction.DOWN, new Vector3f(0.0625f, 0.125f, 1.0f), UVPair.pack(0.50390625f, 0.5f) },
            { Direction.DOWN, new Vector3f(0.0625f, 0.125f, 0.1875f), UVPair.pack(0.50390625f, 0.55078125f) },
            { Direction.DOWN, new Vector3f(0.5f, 0.125f, 0.1875f), UVPair.pack(0.53125f, 0.55078125f) },
            { Direction.DOWN, new Vector3f(0.5f, 0.125f, 1.0f), UVPair.pack(0.53125f, 0.5f) },
    };

    @CsvSource(textBlock = SQUARE_PARAMS, delimiter = '|', useHeadersInDisplayName = true)
    @ParameterizedTest
    public void testSetSquareAgainstFabricSquare(Direction side, float left, float bottom, float right, float top, float depth) {
        var f = 1 / 16f; // Factor for converting from "pixels" [0,16] to block space [0,1]
        var refVertices = Arrays.stream(FABRIC_REFERENCE_DATA).filter(d -> d[0] == side).toList();
        assertEquals(4, refVertices.size());
        var referenceQuad = new BakedQuad(
                (Vector3fc) refVertices.get(0)[1],
                (Vector3fc) refVertices.get(1)[1],
                (Vector3fc) refVertices.get(2)[1],
                (Vector3fc) refVertices.get(3)[1],
                (long) refVertices.get(0)[2],
                (long) refVertices.get(1)[2],
                (long) refVertices.get(2)[2],
                (long) refVertices.get(3)[2],
                0,
                side,
                new BakedQuad.SpriteInfo(UnitTextureAtlasSprite.INSTANCE, ChunkSectionLayer.SOLID, RenderTypes.entitySolid(UnitTextureAtlasSprite.LOCATION)),
                true,
                0);

        var mutableQuad = new MutableQuad();
        mutableQuad.setSpriteInfo(makeMockSpriteInfo());
        mutableQuad.setCubeFaceFromSpriteCoords(side, left * f, bottom * f, right * f, top * f, depth * f);
        mutableQuad.bakeUvsFromPosition();
        assertQuadsEquals(referenceQuad, mutableQuad);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    public void testSetCubeFaceFromVectors(Direction side) {
        var referenceQuads = buildReferenceQuads();

        var from = new Vector3f(REFERENCE_BLOCK_MIN).mul(WORLD_SCALE);
        var to = new Vector3f(REFERENCE_BLOCK_MAX).mul(WORLD_SCALE);

        var mutableQuad = new MutableQuad();
        mutableQuad.setSpriteInfo(makeMockSpriteInfo());
        mutableQuad.setCubeFace(side, from, to);
        mutableQuad.bakeUvsFromPosition();
        assertQuadsEquals(referenceQuads.get(side), mutableQuad);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    public void testSetCubeFace(Direction side) {
        var referenceQuads = buildReferenceQuads();

        var from = new Vector3f(REFERENCE_BLOCK_MIN).mul(WORLD_SCALE);
        var to = new Vector3f(REFERENCE_BLOCK_MAX).mul(WORLD_SCALE);

        var mutableQuad = new MutableQuad();
        mutableQuad.setSpriteInfo(makeMockSpriteInfo());
        mutableQuad.setCubeFace(side, from.x, from.y, from.z, to.x, to.y, to.z);
        mutableQuad.bakeUvsFromPosition();
        assertQuadsEquals(referenceQuads.get(side), mutableQuad);
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    public void testSetCubeFaceFullBlock(Direction side) {
        var min = new Vector3f(0, 0, 0);
        var max = new Vector3f(16, 16, 16);
        var referenceQuads = buildReferenceQuads(min, max);

        var from = new Vector3f(min).mul(WORLD_SCALE);
        var to = new Vector3f(max).mul(WORLD_SCALE);

        var mutableQuad = new MutableQuad();
        mutableQuad.setSpriteInfo(makeMockSpriteInfo());
        mutableQuad.setCubeFace(side, from.x, from.y, from.z, to.x, to.y, to.z);
        mutableQuad.bakeUvsFromPosition();
        assertQuadsEquals(referenceQuads.get(side), mutableQuad);
    }

    @Test
    void testSetFromBakedQuad() {
        var refQuad = buildReferenceQuads().get(Direction.NORTH);
        var mutableQuad = new MutableQuad().setFrom(refQuad);
        for (int i = 0; i < 4; i++) {
            assertEquals(mutableQuad.copyPosition(i), refQuad.position(i), "position[" + i + "]");
            assertEquals(mutableQuad.copyNormal(i), BakedNormals.unpack(refQuad.bakedNormals().normal(i), new Vector3f()), "normal[" + i + "]");
            assertEquals(mutableQuad.u(i), UVPair.unpackU(refQuad.packedUV(i)), "uv[" + i + "].u");
            assertEquals(mutableQuad.v(i), UVPair.unpackV(refQuad.packedUV(i)), "uv[" + i + "].v");
            assertEquals(mutableQuad.color(i), refQuad.bakedColors().color(i), "color[" + i + "]");
        }

        assertEquals(mutableQuad.tintIndex(), refQuad.tintIndex());
        assertEquals(mutableQuad.direction(), refQuad.direction());
        assertEquals(mutableQuad.spriteInfo(), refQuad.spriteInfo());
        assertEquals(mutableQuad.shade(), refQuad.shade());
        assertEquals(mutableQuad.lightEmission(), refQuad.lightEmission());
        assertEquals(mutableQuad.hasAmbientOcclusion(), refQuad.hasAmbientOcclusion());
    }

    /**
     * Moves from a sprite to a sprite with different position in the atlas and checks
     * that the UV gets translated correctly.
     */
    @Test
    void testSetSpriteAndMoveUv() {
        var refQuad = buildReferenceQuads().get(Direction.NORTH);
        var oldSprite = makeMockSpriteInfo(192, 192);
        var mutableQuad = new MutableQuad().setFrom(refQuad);
        mutableQuad.setSpriteInfo(oldSprite);

        // Move to some position within the old sprite
        float[] localUv = {
                0.25f, 0.25f,
                0.75f, 0.25f,
                0.75f, 0.75f,
                0.25f, 0.75f
        };
        for (int i = 0; i < 4; i++) {
            float localU = localUv[i * 2];
            float localV = localUv[i * 2 + 1];
            mutableQuad.setUvFromSprite(i, localU, localV);
            assertEquals(oldSprite.sprite().getU(localU), mutableQuad.u(i), "u[" + i + "] == oldSprite.getU(" + localU + ")");
            assertEquals(oldSprite.sprite().getV(localV), mutableQuad.v(i), "v[" + i + "] == oldSprite.getV(" + localV + ")");
        }

        // Now change the sprite and also move the UV
        var newSprite = makeMockSpriteInfo(32, 32);
        mutableQuad.setSpriteInfoAndMoveUv(newSprite);

        for (int i = 0; i < 4; i++) {
            float localU = localUv[i * 2];
            float localV = localUv[i * 2 + 1];
            assertEquals(newSprite.sprite().getU(localU), mutableQuad.u(i), "u[" + i + "] == newSprite.getU(" + localU + ")");
            assertEquals(newSprite.sprite().getV(localV), mutableQuad.v(i), "v[" + i + "] == newSprite.getV(" + localV + ")");
        }
    }

    @Test
    void testToBakedQuadRoundtrip() {
        var refQuad = buildReferenceQuads().get(Direction.NORTH);
        var mutableQuad = new MutableQuad().setFrom(refQuad);
        assertThat(mutableQuad.toBakedQuad()).usingRecursiveComparison().isEqualTo(refQuad);
    }

    @Test
    void testToBakedQuadPositionReuseWithoutChanges() {
        var refQuad = buildReferenceQuads().get(Direction.NORTH);
        var mutatedQuad = new MutableQuad().setFrom(refQuad).toBakedQuad();

        // Without changes, the positions should be the same objects
        assertSame(refQuad.position0(), mutatedQuad.position0());
        assertSame(refQuad.position1(), mutatedQuad.position1());
        assertSame(refQuad.position2(), mutatedQuad.position2());
        assertSame(refQuad.position3(), mutatedQuad.position3());
    }

    @Test
    void testToBakedQuadPositionReuseWithPartialChanges() {
        // Mutate one of the positions, check that all others are reused, but the changed one isn't
        var refQuad = buildReferenceQuads().get(Direction.NORTH);
        for (int i = 0; i < 4; i++) {
            var mutatedQuad = new MutableQuad().setFrom(refQuad).setPosition(i, 1, 2, 3).toBakedQuad();

            // Without changes, the positions should be the same objects
            for (int j = 0; j < 4; j++) {
                if (i == j) {
                    assertNotSame(refQuad.position(j), mutatedQuad.position(j));
                } else {
                    assertSame(refQuad.position(j), mutatedQuad.position(j));
                }
            }
        }
    }

    @Test
    void testTransform() {
        // Apply a really simple transform to a quad on the south face
        var refQuad = new MutableQuad().setCubeFace(Direction.SOUTH, 0, 0, 0, 1, 1, 1).setSpriteInfo(makeMockSpriteInfo()).toBakedQuad();
        var quat = new Quaternionf().fromAxisAngleDeg(Direction.EAST.getUnitVec3f(), 180);
        var rotation = new Matrix4f().rotateAround(quat, 0.5f, 0.5f, 0.5f, new Matrix4f());

        // It should be equivalent to a cube face on the opposite side, incl. normal vectors
        var expectedQuad = new MutableQuad().setCubeFace(Direction.NORTH, 0, 0, 0, 1, 1, 1).setSpriteInfo(makeMockSpriteInfo()).toBakedQuad();

        var transformedQuad = new MutableQuad().setFrom(refQuad).transform(rotation).setDirection(Direction.NORTH).recalculateWinding();
        assertQuadsEquals(expectedQuad, transformedQuad);
    }

    private static void assertQuadsEquals(BakedQuad expected, MutableQuad actual) {
        var actualVertices = IntStream.range(0, 4)
                .mapToObj(actual::copyPosition)
                .map(MutableQuadTests::formatVector)
                .toList();
        var expectedVertices = IntStream.range(0, 4)
                .mapToObj(expected::position)
                .map(MutableQuadTests::formatVector)
                .toList();

        assertThat(actualVertices).as("positions match").containsExactlyElementsOf(expectedVertices);

        var actualUvs = IntStream.range(0, 4)
                .mapToObj(actual::copyUv)
                .map(MutableQuadTests::formatVector)
                .toList();
        var expectedUvs = IntStream.range(0, 4)
                .mapToObj(vertexIdx -> new Vector2f(UVPair.unpackU(expected.packedUV(vertexIdx)), UVPair.unpackV(expected.packedUV(vertexIdx))))
                .map(MutableQuadTests::formatVector)
                .toList();

        assertThat(actualUvs).as("uvs match").containsExactlyElementsOf(expectedUvs);
    }

    private static String formatVector(Vector3fc v) {
        return String.format(Locale.ROOT, "%.03f, %.03f, %.03f", v.x(), v.y(), v.z());
    }

    private static String formatVector(Vector2fc v) {
        return String.format(Locale.ROOT, "%.03f, %.03f", v.x(), v.y());
    }

    private static Map<Direction, BakedQuad> buildReferenceQuads() {
        return buildReferenceQuads(REFERENCE_BLOCK_MIN, REFERENCE_BLOCK_MAX);
    }

    /**
     * This test relies on baking a cube in the same way a Vanilla JSON blockmodel cube would be baked,
     * and then using the resulting quads as the reference quads in terms of winding and UV.
     */
    private static Map<Direction, BakedQuad> buildReferenceQuads(Vector3fc from, Vector3fc to) {
        var blockModelJson = """
                {
                "textures": {
                  "t": "x:y"
                },
                "elements": [
                    {
                        "from": [$x0, $y0, $z0],
                        "to": [$x1, $y1, $z1],
                        "faces": {
                            "north": {"texture": "#t", "cullface": "north"},
                            "east": {"texture": "#t", "cullface": "east"},
                            "south": {"texture": "#t", "cullface": "south"},
                            "west": {"texture": "#t", "cullface": "west"},
                            "up": {"texture": "#t", "cullface": "up"},
                            "down": {"texture": "#t", "cullface": "down"}
                        }
                    }
                ]
                }
                """;
        blockModelJson = blockModelJson.replace("$x0", String.valueOf(from.x()));
        blockModelJson = blockModelJson.replace("$y0", String.valueOf(from.y()));
        blockModelJson = blockModelJson.replace("$z0", String.valueOf(from.z()));
        blockModelJson = blockModelJson.replace("$x1", String.valueOf(to.x()));
        blockModelJson = blockModelJson.replace("$y1", String.valueOf(to.y()));
        blockModelJson = blockModelJson.replace("$z1", String.valueOf(to.z()));
        var blockModel = BlockModel.GSON.fromJson(blockModelJson, BlockModel.class);

        var baked = blockModel.geometry().bake(TextureSlots.EMPTY, new MockModelBaker(), BlockModelRotation.IDENTITY, () -> "");
        return Arrays.stream(Direction.values()).collect(Collectors.toMap(d -> d, d -> {
            var quads = baked.getQuads(d);
            if (quads.size() != 1) {
                throw new IllegalStateException("Expected exactly 1 quad to be baked for side " + d + " but: " + quads);
            }
            return quads.getFirst();
        }));
    }

    private static BakedQuad.SpriteInfo makeMockSpriteInfo() {
        return new BakedQuad.SpriteInfo(new MockSprite(), ChunkSectionLayer.SOLID, RenderTypes.entitySolid(MockSprite.ID));
    }

    private static BakedQuad.SpriteInfo makeMockSpriteInfo(int x, int y) {
        return new BakedQuad.SpriteInfo(new MockSprite(x, y), ChunkSectionLayer.SOLID, RenderTypes.entitySolid(MockSprite.ID));
    }

    static class MockSprite extends TextureAtlasSprite {
        private static final Identifier ID = Identifier.parse("x:y");

        public MockSprite() {
            this(128, 128);
        }

        public MockSprite(int x, int y) {
            super(ID, new SpriteContents(ID, new FrameSize(16, 16), new NativeImage(16, 16, false)), 256, 256, 128, 128, 0);
        }

        @Override
        public float getU(float u) {
            return super.getU(u);
        }

        @Override
        public float getV(float v) {
            return super.getV(v);
        }
    }

    static class MockModelBaker implements ModelBaker, MaterialBaker, ModelBaker.Interner {
        @Override
        public ResolvedModel getModel(Identifier location) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BlockModelPart missingBlockModelPart() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MaterialBaker materials() {
            return this;
        }

        @Override
        public Interner interner() {
            return this;
        }

        @Override
        public <T> T compute(SharedOperationKey<T> key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Material.Baked get(Material material, ModelDebugName name) {
            return new Material.Baked(new MockSprite(), false);
        }

        @Override
        public Material.Baked reportMissingReference(String reference, ModelDebugName name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Material.Baked resolveSlot(TextureSlots slots, String id, ModelDebugName name) {
            return new Material.Baked(new MockSprite(), false);
        }

        @Override
        public ResolvedModel resolveInlineModel(UnbakedModel inlineModel, ModelDebugName debugName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Vector3fc vector(Vector3fc vector) {
            return vector;
        }

        @Override
        public BakedQuad.SpriteInfo spriteInfo(BakedQuad.SpriteInfo sprite) {
            return sprite;
        }
    }
}
