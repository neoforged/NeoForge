/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import net.neoforged.neoforge.client.model.quad.MutableQuad;
import net.neoforged.neoforge.client.textures.UnitTextureAtlasSprite;
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
        mutableQuad.setSprite(new MockSprite());
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
                UnitTextureAtlasSprite.INSTANCE,
                true,
                0);

        var mutableQuad = new MutableQuad();
        mutableQuad.setSprite(new MockSprite());
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
        mutableQuad.setSprite(new MockSprite());
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
        mutableQuad.setSprite(new MockSprite());
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
            assertEquals(mutableQuad.getU(i), UVPair.unpackU(refQuad.packedUV(i)), "uv[" + i + "].u");
            assertEquals(mutableQuad.getV(i), UVPair.unpackV(refQuad.packedUV(i)), "uv[" + i + "].v");
            assertEquals(mutableQuad.getColor(i), refQuad.bakedColors().color(i), "color[" + i + "]");
        }

        assertEquals(mutableQuad.getTintIndex(), refQuad.tintIndex());
        assertEquals(mutableQuad.getDirection(), refQuad.direction());
        assertEquals(mutableQuad.getSprite(), refQuad.sprite());
        assertEquals(mutableQuad.isShade(), refQuad.shade());
        assertEquals(mutableQuad.getLightEmission(), refQuad.lightEmission());
        assertEquals(mutableQuad.isHasAmbientOcclusion(), refQuad.hasAmbientOcclusion());
    }

    @Test
    void testToBakedQuadRoundtrip() {
        var refQuad = buildReferenceQuads().get(Direction.NORTH);
        var mutableQuad = new MutableQuad().setFrom(refQuad);
        assertThat(mutableQuad.toBakedQuad()).usingRecursiveComparison().isEqualTo(refQuad);
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

    /**
     * This test relies on baking a cube in the same way a Vanilla JSON blockmodel cube would be baked,
     * and then using the resulting quads as the reference quads in terms of winding and UV.
     */
    private static Map<Direction, BakedQuad> buildReferenceQuads() {
        var blockModelJson = """
                {
                "textures": {
                  "t": "x:y"
                },
                "elements": [
                    {
                        "from": [1, 2, 3],
                        "to": [8, 13, 16],
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

    static class MockSprite extends TextureAtlasSprite {
        private static final Identifier ID = Identifier.parse("x:y");

        public MockSprite() {
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

    static class MockModelBaker implements ModelBaker, SpriteGetter, ModelBaker.PartCache {
        @Override
        public ResolvedModel getModel(Identifier location) {
            throw new UnsupportedOperationException();
        }

        @Override
        public BlockModelPart missingBlockModelPart() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SpriteGetter sprites() {
            return this;
        }

        @Override
        public PartCache parts() {
            return this;
        }

        @Override
        public <T> T compute(SharedOperationKey<T> key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TextureAtlasSprite get(Material material, ModelDebugName name) {
            return new MockSprite();
        }

        @Override
        public TextureAtlasSprite reportMissingReference(String reference, ModelDebugName name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public TextureAtlasSprite resolveSlot(TextureSlots slots, String id, ModelDebugName name) {
            return new MockSprite();
        }

        @Override
        public ResolvedModel resolveInlineModel(UnbakedModel inlineModel, ModelDebugName debugName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Vector3fc vector(Vector3fc vector) {
            return vector;
        }
    }
}
