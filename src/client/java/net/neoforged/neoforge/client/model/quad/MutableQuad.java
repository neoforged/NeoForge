/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.quad;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.textures.UnitTextureAtlasSprite;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * A mutable representation of a {@link BakedQuad}.
 *
 * <p>This class can be used for constructing quads from scratch, or for loading and modifying existing quads.
 */
public class MutableQuad {
    private final Vector3f[] positions = new Vector3f[]{
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
    private TextureAtlasSprite sprite = UnitTextureAtlasSprite.INSTANCE;
    private boolean shade;
    private int lightEmission;
    private boolean hasAmbientOcclusion;

    public void setNormal(int vertexIndex, float x, float y, float z) {
        normals[vertexIndex] = BakedNormals.pack(x, y, z);
    }

    public void setNormal(int vertexIndex, Vector3fc normal) {
        normals[vertexIndex] = BakedNormals.pack(normal);
    }

    /**
     * @see BakedNormals
     */
    public void setPackedNormal(int vertexIndex, int packedNormal) {
        normals[vertexIndex] = packedNormal;
    }

    /**
     * @see ARGB
     */
    public void setColor(int vertexIndex, int packedColor) {
        colors[vertexIndex] = packedColor;
    }

    public void setColor(int vertexIndex, int r, int g, int b, int a) {
        setColor(vertexIndex, ARGB.color(a, r, g, b));
    }

    public void setUv(int vertexIndex, float u, float v) {
        uvs[vertexIndex] = UVPair.pack(u, v);
    }

    public void setUv(int vertexIndex, Vector2fc uv) {
        setUv(vertexIndex, uv.x(), uv.y());
    }

    /**
     * @see UVPair
     */
    public void setPackedUv(int vertexIndex, int packedUv) {
        uvs[vertexIndex] = packedUv;
    }

    public int getTintIndex() {
        return tintIndex;
    }

    public void setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public TextureAtlasSprite getSprite() {
        return sprite;
    }

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    /**
     * Assigns UV coordinates to a vertex of the current quad based on its {@linkplain #getSprite() sprite} and the
     * given UV coordinates within that sprite.
     */
    public void setUvFromSprite(int vertexIndex, float u, float v) {
        setUv(vertexIndex, sprite.getU(u), sprite.getV(v));
    }

    /**
     * Assigns UV coordinates to a vertex of the current quad based on its {@linkplain #getSprite() sprite} and the
     * given UV coordinates within that sprite.
     */
    public void setUvFromSprite(int vertexIndex, Vector2fc uv) {
        setUvFromSprite(vertexIndex, uv.x(), uv.y());
    }

    /**
     * Sets the texture coordinates of the current quad to use the entire {@linkplain #getSprite() current sprite}.
     * <p>
     * The first vertex will use the top-left of the sprite, while the third vertex uses the lower right.
     */
    public void setUvFromFullSprite() {
        setUvFromSprite(0, 0, 0);
        setUvFromSprite(1, 0, 1);
        setUvFromSprite(2, 1, 1);
        setUvFromSprite(3, 1, 0);
    }

    public boolean isShade() {
        return shade;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    public int getLightEmission() {
        return lightEmission;
    }

    public void setLightEmission(int lightEmission) {
        this.lightEmission = lightEmission;
    }

    public boolean isHasAmbientOcclusion() {
        return hasAmbientOcclusion;
    }

    public void setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
        this.hasAmbientOcclusion = hasAmbientOcclusion;
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
    public void setPosX(int vertexIndex, float x) {
        positions[vertexIndex].x = x;
    }

    /**
     * Sets the y-component of a vertex's position.
     */
    public void setPosY(int vertexIndex, float y) {
        positions[vertexIndex].y = y;
    }

    /**
     * Sets the x-component of a vertex's position.
     */
    public void setPosZ(int vertexIndex, float z) {
        positions[vertexIndex].z = z;
    }

    /**
     * Sets a component of a vertex's position.
     */
    public void setPosComponent(int vertexIndex, int componentIndex, float value) {
        positions[vertexIndex].setComponent(componentIndex, value);
    }

    /**
     * Sets a vertex's position.
     */
    public void setPos(int vertexIndex, float x, float y, float z) {
        positions[vertexIndex].set(x, y, z);
    }

    /**
     * Sets a vertex's position.
     */
    public void setPos(int vertexIndex, Vector3fc position) {
        positions[vertexIndex].set(position);
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

    public void setFrom(BakedQuad quad) {
        for (int i = 0; i < 4; i++) {
            positions[i] = new Vector3f(quad.position(i));
            normals[i] = quad.bakedNormals().normal(i);
            colors[i] = quad.bakedColors().color(i);
        }
        tintIndex = quad.tintIndex();
        direction = quad.direction();
        sprite = quad.sprite();
        shade = quad.shade();
        lightEmission = quad.lightEmission();
        hasAmbientOcclusion = quad.hasAmbientOcclusion();
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
    public void setCubeFaceFromSpriteCoords(Direction side,
                                            float left,
                                            float bottom,
                                            float right,
                                            float top,
                                            float depth) {

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
    }

    /**
     * Same as {@link #setCubeFace(Direction, float, float, float, float, float, float)}, but takes the from and to
     * positions from vectors.
     */
    public void setCubeFace(Direction side, Vector3fc from, Vector3fc to) {
        setCubeFace(side, from.x(), from.y(), from.z(), to.x(), to.y(), to.z());
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
    public void setCubeFace(Direction side,
                            float fromX,
                            float fromY,
                            float fromZ,
                            float toX,
                            float toY,
                            float toZ) {
        for (int i = 0; i < 4; i++) {
            var vertexInfo = FaceInfo.fromFacing(side).getVertexInfo(i);
            positions[i].set(
                    vertexInfo.xFace().select(fromX, fromY, fromZ, toX, toY, toZ),
                    vertexInfo.yFace().select(fromX, fromY, fromZ, toX, toY, toZ),
                    vertexInfo.zFace().select(fromX, fromY, fromZ, toX, toY, toZ)
            );
        }
    }

    public BakedQuad toBakedQuad() {
        return new BakedQuad(
                positions[0],
                positions[1],
                positions[2],
                positions[3],
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

    public void reset() {
        for (int i = 0; i < 4; i++) {
            positions[i].set(0, 0, 0);
        }
        Arrays.fill(uvs, 0L);
        Arrays.fill(normals, 0);
        Arrays.fill(colors, 0xFFFFFFFF);
        direction = Direction.DOWN;
        sprite = UnitTextureAtlasSprite.INSTANCE;
        lightEmission = 0;
    }
}
