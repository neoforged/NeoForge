/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.pipeline;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.Arrays;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import net.neoforged.neoforge.client.textures.UnitTextureAtlasSprite;
import org.joml.Vector3f;

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

    @Override
    public VertexConsumer setColor(int packedColor) {
        colors[vertexIndex] = packedColor;
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        return setColor(ARGB.color(a, r, g, b));
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        uvs[vertexIndex] = UVPair.pack(u, v);
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

    public void setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setSprite(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    public void setShade(boolean shade) {
        this.shade = shade;
    }

    public void setLightEmission(int lightEmission) {
        this.lightEmission = lightEmission;
    }

    public void setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
        this.hasAmbientOcclusion = hasAmbientOcclusion;
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
