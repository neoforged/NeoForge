/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import net.neoforged.neoforge.client.textures.UnitTextureAtlasSprite;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;

/**
 * Vertex consumer that outputs {@linkplain BakedQuad baked quads}.
 * <p>
 * This consumer accepts data in {@link com.mojang.blaze3d.vertex.DefaultVertexFormat#BLOCK} and is not picky about
 * ordering or missing elements, but will not automatically populate missing data (color will be black, for example).
 * <p>
 * Built quads must be retrieved after building four vertices
 */
public class QuadBakingVertexConsumer implements VertexConsumer {
    private final Vector3f[] positions = new Vector3f[4];
    private final long[] uvs = new long[4];
    private final int[] normals = new int[4];
    private final int[] colors = new int[4];
    private int vertexIndex = 0;
    private boolean building = false;

    private int tintIndex = -1;
    private Direction direction = Direction.DOWN;
    private TextureAtlasSprite sprite = UnitTextureAtlasSprite.INSTANCE;
    private boolean shade;
    private int lightEmission;
    private boolean hasAmbientOcclusion;

    public QuadBakingVertexConsumer() {
        clear();
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        if (building) {
            if (++vertexIndex > 4) {
                throw new IllegalStateException("Expected quad export after fourth vertex");
            }
        }
        building = true;

        positions[vertexIndex].set(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        normals[vertexIndex] = BakedNormals.pack(x, y, z);
        return this;
    }

    public VertexConsumer setNormal(int vertexIndex, float x, float y, float z) {
        normals[vertexIndex] = BakedNormals.pack(x, y, z);
        return this;
    }

    public VertexConsumer setNormal(int vertexIndex, Vector3fc normal) {
        normals[vertexIndex] = BakedNormals.pack(normal);
        return this;
    }

    /**
     * @see BakedNormals
     */
    public VertexConsumer setPackedNormal(int vertexIndex, int packedNormal) {
        normals[vertexIndex] = packedNormal;
        return this;
    }

    @Override
    public VertexConsumer setColor(int packedColor) {
        colors[vertexIndex] = packedColor;
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return setColor(ARGB.color(a, r, g, b));
    }

    /**
     * @see ARGB
     */
    public VertexConsumer setColor(int vertexIndex, int packedColor) {
        colors[vertexIndex] = packedColor;
        return this;
    }

    public VertexConsumer setColor(int vertexIndex, int r, int g, int b, int a) {
        return setColor(vertexIndex, ARGB.color(a, r, g, b));
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        uvs[vertexIndex] = UVPair.pack(u, v);
        return this;
    }

    public VertexConsumer setUv(int vertexIndex, float u, float v) {
        uvs[vertexIndex] = UVPair.pack(u, v);
        return this;
    }

    public VertexConsumer setUv(int vertexIndex, Vector2fc uv) {
        return setUv(vertexIndex, uv.x(), uv.y());
    }

    /**
     * @see UVPair
     */
    public VertexConsumer setPackedUv(int vertexIndex, int packedUv) {
        uvs[vertexIndex] = packedUv;
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this;
    }

    @Override
    public VertexConsumer misc(VertexFormatElement element, int... rawData) {
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float lineWidth) {
        return this;
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

        building = true;
        vertexIndex = 3;
    }

    public BakedQuad bakeQuad() {
        if (!building || ++vertexIndex != 4) {
            throw new IllegalStateException("Not enough vertices available. Vertices in buffer: " + vertexIndex);
        }

        BakedQuad quad = new BakedQuad(
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
        clear();
        return quad;
    }

    private void clear() {
        vertexIndex = 0;
        building = false;
        Arrays.setAll(positions, $ -> new Vector3f());
        Arrays.fill(uvs, 0L);
        Arrays.fill(normals, 0);
        Arrays.fill(colors, 0xFFFFFFFF);
        direction = Direction.DOWN;
        sprite = UnitTextureAtlasSprite.INSTANCE;
        lightEmission = 0;
    }
}
