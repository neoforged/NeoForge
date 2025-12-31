/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.quad;

import java.util.Arrays;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * A mutable representation of a {@link BakedQuad}.
 *
 * <p>This class can be used for constructing quads from scratch, or for loading and modifying existing quads.
 */
public class MutableQuad {
    private final Vector3f[] positions = new Vector3f[] {
            new Vector3f(),
            new Vector3f(),
            new Vector3f(),
            new Vector3f()
    };
    private final long[] uvs = new long[4];
    private final int[] normals = new int[4];
    private final int[] colors = new int[4];

    private int tintIndex = -1;
    private Direction direction = Direction.DOWN;
    @Nullable
    private TextureAtlasSprite sprite;
    private boolean shade;
    private int lightEmission;
    private boolean hasAmbientOcclusion;

    public MutableQuad setNormal(int vertexIndex, float x, float y, float z) {
        normals[vertexIndex] = BakedNormals.pack(x, y, z);
        return this;
    }

    public MutableQuad setNormal(int vertexIndex, Vector3fc normal) {
        normals[vertexIndex] = BakedNormals.pack(normal);
        return this;
    }

    /**
     * @see BakedNormals
     */
    public MutableQuad setPackedNormal(int vertexIndex, int packedNormal) {
        normals[vertexIndex] = packedNormal;
        return this;
    }

    /**
     * @see ARGB
     */
    public MutableQuad setColor(int vertexIndex, int packedColor) {
        colors[vertexIndex] = packedColor;
        return this;
    }

    public MutableQuad setColor(int vertexIndex, int r, int g, int b, int a) {
        return setColor(vertexIndex, ARGB.color(a, r, g, b));
    }

    public MutableQuad setUv(int vertexIndex, float u, float v) {
        uvs[vertexIndex] = UVPair.pack(u, v);
        return this;
    }

    public MutableQuad setUv(int vertexIndex, Vector2fc uv) {
        return setUv(vertexIndex, uv.x(), uv.y());
    }

    /**
     * @see UVPair
     */
    public MutableQuad setPackedUv(int vertexIndex, long packedUv) {
        uvs[vertexIndex] = packedUv;
        return this;
    }

    public int getTintIndex() {
        return tintIndex;
    }

    public MutableQuad setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
        return this;
    }

    public Direction getDirection() {
        return direction;
    }

    public MutableQuad setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    @Nullable
    public TextureAtlasSprite getSprite() {
        return sprite;
    }

    public MutableQuad setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
        return this;
    }

    public float getU(int vertexIndex) {
        return UVPair.unpackU(uvs[vertexIndex]);
    }

    public float getV(int vertexIndex) {
        return UVPair.unpackV(uvs[vertexIndex]);
    }

    public long getPackedUv(int vertexIndex) {
        return uvs[vertexIndex];
    }

    public Vector2f copyUv(int vertexIndex) {
        return copyUv(vertexIndex, new Vector2f());
    }

    public Vector2f copyUv(int vertexIndex, Vector2f dest) {
        var packedUv = uvs[vertexIndex];
        dest.x = UVPair.unpackU(packedUv);
        dest.y = UVPair.unpackV(packedUv);
        return dest;
    }

    /**
     * Assigns UV coordinates to a vertex of the current quad based on its {@linkplain #getSprite() sprite} and the
     * given UV coordinates within that sprite.
     */
    public MutableQuad setUvFromSprite(int vertexIndex, float u, float v) {
        var sprite = getRequiredSprite();
        return setUv(vertexIndex, sprite.getU(u), sprite.getV(v));
    }

    /**
     * Assigns UV coordinates to a vertex of the current quad based on its {@linkplain #getSprite() sprite} and the
     * given UV coordinates within that sprite.
     */
    public MutableQuad setUvFromSprite(int vertexIndex, Vector2fc uv) {
        return setUvFromSprite(vertexIndex, uv.x(), uv.y());
    }

    /**
     * Sets the texture coordinates of the current quad to use the entire {@linkplain #getSprite() current sprite}.
     * <p>
     * The first vertex will use the top-left of the sprite, while the third vertex uses the lower right.
     */
    public MutableQuad setUvFromFullSprite() {
        setUvFromSprite(0, 0, 0);
        setUvFromSprite(1, 0, 1);
        setUvFromSprite(2, 1, 1);
        setUvFromSprite(3, 1, 0);
        return this;
    }

    public int getColor(int vertexIndex) {
        return colors[vertexIndex];
    }

    public boolean isShade() {
        return shade;
    }

    public MutableQuad setShade(boolean shade) {
        this.shade = shade;
        return this;
    }

    public int getLightEmission() {
        return lightEmission;
    }

    public MutableQuad setLightEmission(int lightEmission) {
        this.lightEmission = lightEmission;
        return this;
    }

    public boolean isHasAmbientOcclusion() {
        return hasAmbientOcclusion;
    }

    public MutableQuad setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
        this.hasAmbientOcclusion = hasAmbientOcclusion;
        return this;
    }

    /**
     * {@return the x-component of a vertex's position}
     */
    public float getPosX(int vertexIndex) {
        return positions[vertexIndex].x;
    }

    /**
     * {@return the y-component of a vertex's position}
     */
    public float getPosY(int vertexIndex) {
        return positions[vertexIndex].y;
    }

    /**
     * {@return the z-component of a vertex's position}
     */
    public float getPosZ(int vertexIndex) {
        return positions[vertexIndex].z;
    }

    /**
     * {@return a component of a vertex's position}
     *
     * <p>See <a href="#components">components</a> for the mapping of index to component.
     */
    public float getPosComponent(int vertexIndex, int componentIndex) {
        return positions[vertexIndex].get(componentIndex);
    }

    /**
     * {@return a mutable copy of a vertex's position}
     * <p>
     * Pass a non-null destination vector to avoid allocation of a new vector.
     */
    public Vector3f copyPosition(int vertexIndex, @Nullable Vector3f dest) {
        var pos = positions[vertexIndex];
        if (dest == null) {
            dest = new Vector3f(pos);
        } else {
            dest.set(pos);
        }
        return dest;
    }

    public Vector3f copyPosition(int vertexIndex) {
        return copyPosition(vertexIndex, new Vector3f());
    }

    /**
     * Sets the x-component of a vertex's position.
     */
    public MutableQuad setPosX(int vertexIndex, float x) {
        positions[vertexIndex].x = x;
        return this;
    }

    /**
     * Sets the y-component of a vertex's position.
     */
    public MutableQuad setPosY(int vertexIndex, float y) {
        positions[vertexIndex].y = y;
        return this;
    }

    /**
     * Sets the x-component of a vertex's position.
     */
    public MutableQuad setPosZ(int vertexIndex, float z) {
        positions[vertexIndex].z = z;
        return this;
    }

    /**
     * Sets a component of a vertex's position.
     */
    public MutableQuad setPosComponent(int vertexIndex, int componentIndex, float value) {
        positions[vertexIndex].setComponent(componentIndex, value);
        return this;
    }

    /**
     * Sets a vertex's position.
     */
    public MutableQuad setPos(int vertexIndex, float x, float y, float z) {
        positions[vertexIndex].set(x, y, z);
        return this;
    }

    /**
     * Sets a vertex's position.
     */
    public MutableQuad setPos(int vertexIndex, Vector3fc position) {
        positions[vertexIndex].set(position);
        return this;
    }

    /**
     * {@return the x-component of a vertex's normal or NaN if the normal is undefined}
     */
    public float getNormalX(int vertexIndex) {
        return getNormalComponent(vertexIndex, 0);
    }

    /**
     * {@return the y-component of a vertex's normal or NaN if the normal is undefined}
     */
    public float getNormalY(int vertexIndex) {
        return getNormalComponent(vertexIndex, 1);
    }

    /**
     * {@return the z-component of a vertex's normal or NaN if the normal is undefined}
     */
    public float getNormalZ(int vertexIndex) {
        return getNormalComponent(vertexIndex, 2);
    }

    /**
     * {@return a component of a vertex's normal or NaN if the normal is undefined}
     *
     * <p>See <a href="#components">components</a> for the mapping of index to component.
     */
    public float getNormalComponent(int vertexIndex, int componentIndex) {
        var packedNormal = normals[vertexIndex];
        if (BakedNormals.isUnspecified(packedNormal)) {
            return Float.NaN;
        } else {
            return BakedNormals.unpackComponent(packedNormal, componentIndex);
        }
    }

    /**
     * {@return a mutable copy of a vertex's normal}
     * <p>
     * Pass a non-null destination vector to avoid allocation of a new vector.
     */
    public Vector3f copyNormal(int vertexIndex, @Nullable Vector3f dest) {
        if (dest == null) {
            dest = new Vector3f();
        }
        return BakedNormals.unpack(normals[vertexIndex], dest);
    }

    public Vector3f copyNormal(int vertexIndex) {
        return copyNormal(vertexIndex, new Vector3f());
    }

    public MutableQuad setFrom(BakedQuad quad) {
        for (int i = 0; i < 4; i++) {
            positions[i] = new Vector3f(quad.position(i));
            normals[i] = quad.bakedNormals().normal(i);
            colors[i] = quad.bakedColors().color(i);
            uvs[i] = quad.packedUV(i);
        }
        tintIndex = quad.tintIndex();
        direction = quad.direction();
        sprite = quad.sprite();
        shade = quad.shade();
        lightEmission = quad.lightEmission();
        hasAmbientOcclusion = quad.hasAmbientOcclusion();
        return this;
    }

    /**
     * Sets the positions of this quad to form a rectangle on the given block side using a coordinate-system matching
     * the default orientation of sprites in Vanilla block-models.
     * <p>
     * Inspired by the Fabric Rendering API method {@code square}.
     * <p>
     * The left, bottom, right and top parameters correspond to the default sprite orientation in Vanilla block models.
     * For {@link Direction#UP} the "up" direction is facing {@link Direction#NORTH}, while for {@link Direction#DOWN},
     * it faces {@link Direction#SOUTH}.
     * <p>All coordinates use a normalized [0,1] range.
     * <p>Passing left=0, bottom=0, right=1, top=1, depth=0 will produce a face on the blocks {@code side}.
     */
    public MutableQuad setCubeFaceFromSpriteCoords(Direction side,
            float left,
            float bottom,
            float right,
            float top,
            float depth) {
        this.direction = side;

        switch (side) {
            case NORTH -> {
                // -Z (looking south at north face)
                // left is +X, bottom is -Y
                positions[0].set(1 - left, top, depth);
                positions[1].set(1 - left, bottom, depth);
                positions[2].set(1 - right, bottom, depth);
                positions[3].set(1 - right, top, depth);
            }
            case SOUTH -> {
                // +Z (looking north at south face)
                // left is +X, bottom is -Y
                positions[0].set(left, top, 1 - depth);
                positions[1].set(left, bottom, 1 - depth);
                positions[2].set(right, bottom, 1 - depth);
                positions[3].set(right, top, 1 - depth);
            }
            case EAST -> {
                // -X (looking west at east face)
                // left is +Z, bottom is -Y
                positions[0].set(depth, top, 1 - left);
                positions[1].set(depth, bottom, 1 - left);
                positions[2].set(depth, bottom, 1 - right);
                positions[3].set(depth, top, 1 - right);
            }
            case WEST -> {
                // +X (looking east at west face)
                // left is -Z, bottom is -Y
                positions[0].set(depth, top, left);
                positions[1].set(depth, bottom, left);
                positions[2].set(depth, bottom, right);
                positions[3].set(depth, top, right);
            }
            case UP -> {
                // -Y (looking down at up face)
                // left is -X, bottom is +Z
                positions[0].set(left, 1 - depth, 1 - top);
                positions[1].set(left, 1 - depth, 1 - bottom);
                positions[2].set(right, 1 - depth, 1 - bottom);
                positions[3].set(right, 1 - depth, 1 - top);
            }
            case DOWN -> {
                // +Y (looking up at down face)
                // left is -X, bottom is -Z
                positions[0].set(left, depth, top);
                positions[1].set(left, depth, bottom);
                positions[2].set(right, depth, bottom);
                positions[3].set(right, depth, top);
            }
            default -> {
                throw new IllegalStateException();
            }
        }
        return this;
    }

    /**
     * Same as {@link #setCubeFace(Direction, float, float, float, float, float, float)}, but takes the from and to
     * positions from vectors.
     */
    public MutableQuad setCubeFace(Direction side, Vector3fc from, Vector3fc to) {
        return setCubeFace(side, from.x(), from.y(), from.z(), to.x(), to.y(), to.z());
    }

    /**
     * Sets the positions of this quad to the face of a cube as it would be defined in a Vanilla block model.
     * <p>
     * Inspired by the Fabric Rendering API method {@code square}.
     * <p>
     * The left, bottom, right and top parameters correspond to the default sprite orientation in Vanilla block models.
     * For {@link Direction#UP} the "up" direction is facing {@link Direction#NORTH}, while for {@link Direction#DOWN},
     * it faces {@link Direction#SOUTH}.
     * <p>All coordinates use a normalized [0,1] range.
     * <p>Passing left=0, bottom=0, right=1, top=1, depth=0 will produce a face on the blocks {@code side}.
     */
    public MutableQuad setCubeFace(Direction side,
            float fromX,
            float fromY,
            float fromZ,
            float toX,
            float toY,
            float toZ) {
        this.direction = side;

        for (int i = 0; i < 4; i++) {
            var vertexInfo = FaceInfo.fromFacing(side).getVertexInfo(i);
            positions[i].set(
                    vertexInfo.xFace().select(fromX, fromY, fromZ, toX, toY, toZ),
                    vertexInfo.yFace().select(fromX, fromY, fromZ, toX, toY, toZ),
                    vertexInfo.zFace().select(fromX, fromY, fromZ, toX, toY, toZ));
        }
        return this;
    }

    /**
     * This method simply projects each vertex onto the cube face the quad is sourcing its block lighting from,
     * and derives the vertex UV that way.
     */
    public MutableQuad bakeUvsFromPosition() {
        return bakeUvsFromPosition(UVTransform.IDENTITY);
    }

    /**
     * Same as {@link #bakeUvsFromPosition()}, but applies a transform to the generated UVs before baking.
     */
    public MutableQuad bakeUvsFromPosition(UVTransform transform) {
        switch (direction) {
            case DOWN -> {
                for (int i = 0; i < 4; i++) {
                    uvs[i] = UVPair.pack(positions[i].x, 1 - positions[i].z);
                }
            }
            case UP -> {
                for (int i = 0; i < 4; i++) {
                    uvs[i] = UVPair.pack(positions[i].x, positions[i].z);
                }
            }
            case NORTH -> {
                for (int i = 0; i < 4; i++) {
                    uvs[i] = UVPair.pack(1 - positions[i].x, 1 - positions[i].y);
                }
            }
            case SOUTH -> {
                for (int i = 0; i < 4; i++) {
                    uvs[i] = UVPair.pack(positions[i].x, 1 - positions[i].y);
                }
            }
            case WEST -> {
                for (int i = 0; i < 4; i++) {
                    uvs[i] = UVPair.pack(positions[i].z, 1 - positions[i].y);
                }
            }
            case EAST -> {
                for (int i = 0; i < 4; i++) {
                    uvs[i] = UVPair.pack(1 - positions[i].z, 1 - positions[i].y);
                }
            }
        }

        if (!transform.isIdentity()) {
            for (int i = 0; i < 4; i++) {
                uvs[i] = transform.transformPacked(uvs[i]);
            }
        }

        transformUvsFromSpriteToAtlas();
        return this;
    }

    /**
     * Assumes that the UV coordinates are in sprite-space and transforms
     * them to atlas-space.
     */
    private void transformUvsFromSpriteToAtlas() {
        getRequiredSprite();
        for (int i = 0; i < 4; i++) {
            long packedUv = getPackedUv(i);
            setUvFromSprite(i, UVPair.unpackU(packedUv), UVPair.unpackV(packedUv));
        }
    }

    public BakedQuad toBakedQuad() {
        var sprite = getRequiredSprite();
        return new BakedQuad(
                new Vector3f(positions[0]),
                new Vector3f(positions[1]),
                new Vector3f(positions[2]),
                new Vector3f(positions[3]),
                uvs[0],
                uvs[1],
                uvs[2],
                uvs[3],
                tintIndex,
                direction,
                sprite,
                shade,
                lightEmission,
                BakedNormals.of(normals[0], normals[1], normals[2], normals[3]),
                BakedColors.of(colors[0], colors[1], colors[2], colors[3]),
                hasAmbientOcclusion);
    }

    public MutableQuad reset() {
        for (int i = 0; i < 4; i++) {
            positions[i].set(0, 0, 0);
        }
        Arrays.fill(uvs, 0L);
        Arrays.fill(normals, 0);
        Arrays.fill(colors, 0xFFFFFFFF);
        direction = Direction.DOWN;
        sprite = null;
        lightEmission = 0;
        return this;
    }

    private TextureAtlasSprite getRequiredSprite() {
        if (sprite == null) {
            throw new IllegalStateException("A sprite has to be set on this quad before UVs are manipulated");
        }
        return sprite;
    }
}
