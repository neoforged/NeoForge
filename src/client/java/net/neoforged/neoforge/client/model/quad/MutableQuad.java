/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.quad;

import com.mojang.blaze3d.platform.Transparency;
import java.util.Arrays;
import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Contract;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * A mutable representation of a {@link BakedQuad}.
 *
 * <p>This class can be used for constructing quads from scratch, or for loading and modifying existing quads.
 * <p>It provides several utility methods that go beyond simply manipulating the attributes of the quad:
 * <ul>
 * <li>{@link #setCubeFaceFromSpriteCoords(Direction, float, float, float, float, float)} generates the positions of a face by using a 2D coordinate system as if you were looking at the sprite textured on that face.</li>
 * <li>{@link #setCubeFace(Direction, Vector3fc, Vector3fc)} generates the positions of a 3D cube face by giving the cubes extent.</li>
 * <li>{@link #bakeUvsFromPosition(UVTransform)} generates the texture coordinates of the quad similar to how Vanilla block models do, with optional transformations.</li>
 * <li>{@link #recalculateWinding()} can reorder the vertices of the quad to match the vertex order expected by Vanilla ambient occlusion for axis-aligned quads.</li>
 * <li>{@link #setSpriteAndMoveUv(Material.Baked, Transparency)} and {@link #setSpriteAndMoveUv(TextureAtlasSprite, ChunkSectionLayer, RenderType)} can change the sprite used by a quad while remapping the atlas uv automatically.</li>
 * </ul>
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

    private Direction direction = Direction.DOWN;
    @Nullable
    private TextureAtlasSprite sprite;
    @Nullable
    private ChunkSectionLayer chunkLayer;
    @Nullable
    private RenderType itemRenderType;
    private int tintIndex = -1;
    private boolean shade = true;
    private int lightEmission;
    private boolean ambientOcclusion;
    /**
     * This is only used to reuse position vectors when possible.
     */
    @Nullable
    private BakedQuad lastSourceQuad;

    public MutableQuad() {
        reset();
    }

    /**
     * {@return the x-component of a vertex's position}
     */
    @Contract(pure = true)
    public float x(int vertexIndex) {
        return positions[vertexIndex].x;
    }

    /**
     * {@return the y-component of a vertex's position}
     */
    @Contract(pure = true)
    public float y(int vertexIndex) {
        return positions[vertexIndex].y;
    }

    /**
     * {@return the z-component of a vertex's position}
     */
    @Contract(pure = true)
    public float z(int vertexIndex) {
        return positions[vertexIndex].z;
    }

    /**
     * {@return a component of a vertex's position}
     *
     * @see Vector3f#get(int)
     */
    @Contract(pure = true)
    public float positionComponent(int vertexIndex, int componentIndex) {
        return positions[vertexIndex].get(componentIndex);
    }

    /**
     * Copies a vertex's position into a new vector and returns it.
     */
    @Contract(pure = true)
    public Vector3f copyPosition(int vertexIndex) {
        return new Vector3f(positions[vertexIndex]);
    }

    /**
     * Copies a vertex's position into the given vector and returns it.
     */
    public Vector3f copyPosition(int vertexIndex, Vector3f dest) {
        var pos = positions[vertexIndex];
        dest.set(pos);
        return dest;
    }

    /**
     * Sets the x-component of a vertex's position.
     */
    public MutableQuad setX(int vertexIndex, float x) {
        positions[vertexIndex].x = x;
        return this;
    }

    /**
     * Sets the y-component of a vertex's position.
     */
    public MutableQuad setY(int vertexIndex, float y) {
        positions[vertexIndex].y = y;
        return this;
    }

    /**
     * Sets the x-component of a vertex's position.
     */
    public MutableQuad setZ(int vertexIndex, float z) {
        positions[vertexIndex].z = z;
        return this;
    }

    /**
     * Sets a component of a vertex's position.
     *
     * @see Vector3f#setComponent(int, float)
     */
    public MutableQuad setPositionComponent(int vertexIndex, int componentIndex, float value) {
        positions[vertexIndex].setComponent(componentIndex, value);
        return this;
    }

    /**
     * Sets a vertex's position.
     */
    public MutableQuad setPosition(int vertexIndex, float x, float y, float z) {
        positions[vertexIndex].set(x, y, z);
        return this;
    }

    /**
     * Sets a vertex's position.
     */
    public MutableQuad setPosition(int vertexIndex, Vector3fc position) {
        positions[vertexIndex].set(position);
        return this;
    }

    /**
     * Sets the positions of this quad to form a rectangle on the given block side using a coordinate-system matching
     * the default orientation of sprites in Vanilla block-models.
     * <p>
     * Inspired by the Fabric Renderer API method {@code square}.
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
                positions[0].set(1 - depth, top, 1 - left);
                positions[1].set(1 - depth, bottom, 1 - left);
                positions[2].set(1 - depth, bottom, 1 - right);
                positions[3].set(1 - depth, top, 1 - right);
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
        }
        return this;
    }

    /**
     * Same as {@link #setCubeFace(Direction, float, float, float, float, float, float)}, but uses the full cube.
     */
    public MutableQuad setFullCubeFace(Direction side) {
        return setCubeFace(side, 0, 0, 0, 1, 1, 1);
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
     * <p>All coordinates use a normalized [0,1] range.
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
     * {@return the horizontal texture coordinate in atlas-space for a vertex}
     */
    @Contract(pure = true)
    public float u(int vertexIndex) {
        return UVPair.unpackU(uvs[vertexIndex]);
    }

    /**
     * {@return the vertical texture coordinate in atlas-space for a vertex}
     */
    @Contract(pure = true)
    public float v(int vertexIndex) {
        return UVPair.unpackV(uvs[vertexIndex]);
    }

    /**
     * {@return the a texture coordinate in atlas-space for a vertex}
     */
    @Contract(pure = true)
    public float uvComponent(int vertexIndex, int componentIndex) {
        return switch (componentIndex) {
            case 0 -> u(vertexIndex);
            case 1 -> v(vertexIndex);
            default -> throw new IllegalArgumentException("Invalid UV index: " + componentIndex);
        };
    }

    /**
     * {@return the texture coordinates in atlas-space for a vertex in packed form}
     *
     * @see UVPair#unpackU(long)
     * @see UVPair#unpackV(long)
     */
    @Contract(pure = true)
    public long packedUv(int vertexIndex) {
        return uvs[vertexIndex];
    }

    /**
     * Same as {@link #copyUv(int, Vector2f)}, but constructs a destination vector automatically.
     */
    @Contract(pure = true)
    public Vector2f copyUv(int vertexIndex) {
        return copyUv(vertexIndex, new Vector2f());
    }

    /**
     * Copies the texture coordinates of a vertex into a given vector and returns it.
     */
    public Vector2f copyUv(int vertexIndex, Vector2f dest) {
        var packedUv = uvs[vertexIndex];
        dest.x = UVPair.unpackU(packedUv);
        dest.y = UVPair.unpackV(packedUv);
        return dest;
    }

    /**
     * Sets the texture coordinate of a vertex.
     *
     * <p>Note that this method expects texture coordinates in the coordinate space of the atlas, not the sprite.
     *
     * @see #setUvFromSprite(int, float, float)
     */
    public MutableQuad setUv(int vertexIndex, float u, float v) {
        uvs[vertexIndex] = UVPair.pack(u, v);
        return this;
    }

    /**
     * Sets the texture coordinate of a vertex.
     *
     * <p>Note that this method expects texture coordinates in the coordinate space of the atlas, not the sprite.
     *
     * @see #setUvFromSprite(int, Vector2fc)
     */
    public MutableQuad setUv(int vertexIndex, Vector2fc uv) {
        return setUv(vertexIndex, uv.x(), uv.y());
    }

    /**
     * Sets a component of the texture coordinate of a vertex.
     *
     * <p>Note that this method expects texture coordinates in the coordinate space of the atlas, not the sprite.
     *
     * @see #setUvFromSprite(int, float, float)
     */
    public MutableQuad setUvComponent(int vertexIndex, int componentIndex, float value) {
        return switch (componentIndex) {
            case 0 -> setUv(vertexIndex, value, v(vertexIndex));
            case 1 -> setUv(vertexIndex, u(vertexIndex), value);
            default -> throw new IllegalArgumentException("Invalid UV index: " + componentIndex);
        };
    }

    /**
     * Sets the texture coordinate of a vertex from their packed representation.
     *
     * <p>Note that this method expects texture coordinates in the coordinate space of the atlas, not the sprite.
     *
     * @see UVPair
     */
    public MutableQuad setPackedUv(int vertexIndex, long packedUv) {
        uvs[vertexIndex] = packedUv;
        return this;
    }

    /**
     * Assigns UV coordinates to a vertex of the current quad based on its {@linkplain #sprite() sprite} and the
     * given UV coordinates within that sprite.
     */
    public MutableQuad setUvFromSprite(int vertexIndex, float u, float v) {
        TextureAtlasSprite sprite = requiredSprite();
        return setUv(vertexIndex, sprite.getU(u), sprite.getV(v));
    }

    /**
     * Assigns UV coordinates to a vertex of the current quad based on its {@linkplain #sprite() sprite} and the
     * given UV coordinates within that sprite.
     */
    public MutableQuad setUvFromSprite(int vertexIndex, Vector2fc uv) {
        return setUvFromSprite(vertexIndex, uv.x(), uv.y());
    }

    /**
     * Projects each vertex onto the cube face the quad is sourcing its block lighting from,
     * and derives the vertex UV that way.
     *
     * <p>Requires {@link #sprite()} to be set.
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

    @Contract(pure = true)
    public int tintIndex() {
        return tintIndex;
    }

    public MutableQuad setTintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
        return this;
    }

    @Contract(pure = true)
    public Direction direction() {
        return direction;
    }

    public MutableQuad setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    /**
     * {@return the sprite associated with the quad or null if no sprite has been set yet}
     *
     * <p>Note that {@link BakedQuad} must have an associated sprite.
     */
    @Contract(pure = true)
    @Nullable
    public TextureAtlasSprite sprite() {
        return sprite;
    }

    /**
     * Same as {@link #sprite()}, but throws an exception if no sprite is set on the quad yet.
     *
     * @throws IllegalStateException If no sprite is set yet.
     */
    @Contract(pure = true)
    public TextureAtlasSprite requiredSprite() {
        if (sprite == null) {
            throw new IllegalStateException("A sprite has to be set on this quad before UVs are manipulated");
        }
        return sprite;
    }

    /// {@return the chunk layer associated with the quad or null if no chunk layer has been set yet}
    ///
    /// Note that [BakedQuad] must have an associated chunk layer
    @Contract(pure = true)
    @Nullable
    public ChunkSectionLayer chunkLayer() {
        return chunkLayer;
    }

    /// Same as [#chunkLayer()], but throws an exception if no chunk layer is set on the quad yet.
    ///
    /// @throws IllegalStateException If no chunk layer is set yet
    @Contract(pure = true)
    public ChunkSectionLayer requiredChunkLayer() {
        if (chunkLayer == null) {
            throw new IllegalStateException("A ChunkSectionLayer has to be set on this quad before baking");
        }
        return chunkLayer;
    }

    /// {@return the item render type associated with the quad or null if no item render type has been set yet}
    ///
    /// Note that [BakedQuad] must have an associated item render type
    @Contract(pure = true)
    @Nullable
    public RenderType itemRenderType() {
        return itemRenderType;
    }

    /// Same as [#itemRenderType()], but throws an exception if no item render type is set on the quad yet.
    ///
    /// @throws IllegalStateException If no item render type is set yet
    @Contract(pure = true)
    public RenderType requiredItemRenderType() {
        if (itemRenderType == null) {
            throw new IllegalStateException("An item RenderType has to be set on this quad before baking");
        }
        return itemRenderType;
    }

    /**
     * Changes the material used by this quad and sets the {@link #itemRenderType()} and {@link #chunkLayer()}
     * based on the material.
     *
     * <p>Note that changing the sprite does not automatically translate the current UV coordinates within the atlas
     * to be within this new sprite. Use {@link #setSpriteAndMoveUv(Material.Baked, Transparency)}
     * to change sprites and remap them, {@link #bakeUvsFromPosition()} to generate texture coordinates from scratch, or set them manually.
     */
    public MutableQuad setSprite(Material.Baked material) {
        return setSprite(material, getTransparency(material));
    }

    /**
     * Changes the texture atlas sprite used by this quad.
     *
     * <p>Note that changing the sprite does not automatically translate the current UV coordinates within the atlas
     * to be within this new sprite. Use {@link #setSpriteAndMoveUv(Material.Baked, Transparency)}
     * to change sprites and remap them, {@link #bakeUvsFromPosition()} to generate texture coordinates from scratch, or set them manually.
     */
    @SuppressWarnings("deprecation")
    public MutableQuad setSprite(Material.Baked material, Transparency transparency) {
        RenderType itemRenderType;
        if (material.sprite().atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
            itemRenderType = transparency.hasTranslucent() ? Sheets.translucentBlockItemSheet() : Sheets.cutoutBlockItemSheet();
        } else {
            itemRenderType = transparency.hasTranslucent() ? Sheets.translucentItemSheet() : Sheets.cutoutItemSheet();
        }
        setSprite(material.sprite(), ChunkSectionLayer.byTransparency(transparency), itemRenderType);
        return this;
    }

    /**
     * Changes the texture atlas sprite used by this quad.
     *
     * <p>Note that changing the sprite does not automatically translate the current UV coordinates within the atlas
     * to be within this new sprite. Use {@link #setSpriteAndMoveUv(TextureAtlasSprite, ChunkSectionLayer, RenderType)}
     * to change sprites and remap them, {@link #bakeUvsFromPosition()} to generate texture coordinates from scratch, or set them manually.
     */
    public MutableQuad setSprite(TextureAtlasSprite sprite, ChunkSectionLayer chunkLayer, RenderType itemRenderType) {
        this.sprite = sprite;
        this.chunkLayer = chunkLayer;
        this.itemRenderType = itemRenderType;
        return this;
    }

    /**
     * Changes the sprite and remaps the UV to the new sprites position in the texture atlas.
     *
     * @throws IllegalStateException If no sprite is currently set. There would be nothing to remap from.
     */
    public MutableQuad setSpriteAndMoveUv(Material.Baked material) {
        return setSpriteAndMoveUv(material, getTransparency(material));
    }

    /**
     * Changes the sprite and remaps the UV to the new sprites position in the texture atlas.
     *
     * @throws IllegalStateException If no sprite is currently set. There would be nothing to remap from.
     */
    public MutableQuad setSpriteAndMoveUv(Material.Baked material, Transparency transparency) {
        transformUvsFromAtlasToSprite();
        setSprite(material, transparency);
        transformUvsFromSpriteToAtlas();
        return this;
    }

    /**
     * Changes the sprite and remaps the UV to the new sprites position in the texture atlas.
     *
     * @throws IllegalStateException If no sprite is currently set. There would be nothing to remap from.
     */
    public MutableQuad setSpriteAndMoveUv(TextureAtlasSprite sprite, ChunkSectionLayer chunkLayer, RenderType itemRenderType) {
        transformUvsFromAtlasToSprite();
        setSprite(sprite, chunkLayer, itemRenderType);
        transformUvsFromSpriteToAtlas();
        return this;
    }

    @Contract(pure = true)
    public boolean shade() {
        return shade;
    }

    public MutableQuad setShade(boolean shade) {
        this.shade = shade;
        return this;
    }

    @Contract(pure = true)
    public int lightEmission() {
        return lightEmission;
    }

    public MutableQuad setLightEmission(int lightEmission) {
        this.lightEmission = lightEmission;
        return this;
    }

    /**
     * {@return the x-component of a vertex's normal or NaN if the normal is undefined}
     */
    @Contract(pure = true)
    public float normalX(int vertexIndex) {
        return normalComponent(vertexIndex, 0);
    }

    /**
     * {@return the y-component of a vertex's normal or NaN if the normal is undefined}
     */
    @Contract(pure = true)
    public float normalY(int vertexIndex) {
        return normalComponent(vertexIndex, 1);
    }

    /**
     * {@return the z-component of a vertex's normal or NaN if the normal is undefined}
     */
    @Contract(pure = true)
    public float normalZ(int vertexIndex) {
        return normalComponent(vertexIndex, 2);
    }

    /**
     * {@return a component of a vertex's normal or NaN if the normal is undefined}
     *
     * @see Vector3f#get(int)
     */
    @Contract(pure = true)
    public float normalComponent(int vertexIndex, int componentIndex) {
        var packedNormal = normals[vertexIndex];
        if (BakedNormals.isUnspecified(packedNormal)) {
            return Float.NaN;
        } else {
            return BakedNormals.unpackComponent(packedNormal, componentIndex);
        }
    }

    /**
     * {@return a vertex normal in packed form}
     *
     * @see BakedNormals#unpack(int, Vector3f)
     * @see BakedNormals#pack(float, float, float)
     */
    @Contract(pure = true)
    public int packedNormal(int vertexIndex) {
        return normals[vertexIndex];
    }

    /**
     * Same as {@link #copyNormal(int, Vector3f)}, but constructs a new destination vector automatically.
     */
    @Contract(pure = true)
    public Vector3f copyNormal(int vertexIndex) {
        return copyNormal(vertexIndex, new Vector3f());
    }

    /**
     * Copies the normal vector of a vertex into a given vector and returns it.
     */
    public Vector3f copyNormal(int vertexIndex, Vector3f dest) {
        return BakedNormals.unpack(normals[vertexIndex], dest);
    }

    /**
     * Sets the normal vector of a vertex.
     */
    public MutableQuad setNormal(int vertexIndex, float x, float y, float z) {
        normals[vertexIndex] = BakedNormals.pack(x, y, z);
        return this;
    }

    /**
     * Sets the normal vector of a vertex.
     */
    public MutableQuad setNormal(int vertexIndex, Vector3fc normal) {
        normals[vertexIndex] = BakedNormals.pack(normal);
        return this;
    }

    /**
     * Sets a component of a vertex's normal.
     *
     * <p>If a normal was unspecified before this method was called, its other components will be set to 0.
     *
     * @see Vector3f#setComponent(int, float)
     */
    public MutableQuad setNormalComponent(int vertexIndex, int componentIndex, float value) {
        int normal = normals[vertexIndex];
        float x, y, z;
        if (BakedNormals.isUnspecified(normal)) {
            x = 0;
            y = 0;
            z = 0;
        } else {
            x = BakedNormals.unpackX(normal);
            y = BakedNormals.unpackY(normal);
            z = BakedNormals.unpackZ(normal);
        }

        switch (componentIndex) {
            case 0 -> x = value;
            case 1 -> y = value;
            case 2 -> z = value;
            default -> throw new IllegalArgumentException();
        }

        normals[vertexIndex] = BakedNormals.pack(x, y, z);
        return this;
    }

    /**
     * Sets the normal vector of a vertex from its packed representation.
     *
     * @see BakedNormals
     */
    public MutableQuad setPackedNormal(int vertexIndex, int packedNormal) {
        normals[vertexIndex] = packedNormal;
        return this;
    }

    /**
     * Sets the normal vector of all vertices to the normal vectors encoded in the given baked normals object.
     */
    public MutableQuad setNormal(BakedNormals bakedNormals) {
        normals[0] = bakedNormals.normal(0);
        normals[1] = bakedNormals.normal(1);
        normals[2] = bakedNormals.normal(2);
        normals[3] = bakedNormals.normal(3);
        return this;
    }

    /**
     * Recomputes the quad normal from the vertex positions.
     *
     * <p>Assumes that the quad is planar.
     */
    public MutableQuad recomputeNormals(boolean updateDirection) {
        int normal = BakedNormals.computeQuadNormal(positions[0], positions[1], positions[2], positions[3]);
        Arrays.fill(normals, normal);
        if (updateDirection) {
            float nX = BakedNormals.unpackX(normal);
            float nY = BakedNormals.unpackY(normal);
            float nZ = BakedNormals.unpackZ(normal);
            setDirection(Direction.getApproximateNearest(nX, nY, nZ));
        }
        return this;
    }

    /**
     * {@return the color of a given vertex in ARGB form}
     *
     * @see ARGB
     */
    @Contract(pure = true)
    public int color(int vertexIndex) {
        return colors[vertexIndex];
    }

    /**
     * Sets the color of all vertices to a packed ARGB color.
     *
     * @see ARGB
     */
    public MutableQuad setColor(int packedColor) {
        colors[0] = packedColor;
        colors[1] = packedColor;
        colors[2] = packedColor;
        colors[3] = packedColor;
        return this;
    }

    /**
     * Sets the color of a vertex to a packed ARGB color.
     *
     * @see ARGB
     */
    public MutableQuad setColor(int vertexIndex, int packedColor) {
        colors[vertexIndex] = packedColor;
        return this;
    }

    /**
     * Sets the color of a vertex from integer components (0-255).
     *
     * @see ARGB
     */
    public MutableQuad setColor(int vertexIndex, int r, int g, int b, int a) {
        return setColor(vertexIndex, ARGB.color(a, r, g, b));
    }

    /**
     * Sets the color of all vertices to a packed ARGB color.
     */
    public MutableQuad setColor(BakedColors bakedColors) {
        colors[0] = bakedColors.color(0);
        colors[1] = bakedColors.color(1);
        colors[2] = bakedColors.color(2);
        colors[3] = bakedColors.color(3);
        return this;
    }

    @Contract(pure = true)
    public boolean hasAmbientOcclusion() {
        return ambientOcclusion;
    }

    public MutableQuad setAmbientOcclusion(boolean ambientOcclusion) {
        this.ambientOcclusion = ambientOcclusion;
        return this;
    }

    public MutableQuad setFrom(BakedQuad quad) {
        lastSourceQuad = quad;
        for (int i = 0; i < 4; i++) {
            positions[i].set(quad.position(i));
            normals[i] = quad.bakedNormals().normal(i);
            colors[i] = quad.bakedColors().color(i);
            uvs[i] = quad.packedUV(i);
        }
        direction = quad.direction();
        BakedQuad.MaterialInfo materialInfo = quad.materialInfo();
        sprite = materialInfo.sprite();
        chunkLayer = materialInfo.layer();
        itemRenderType = materialInfo.itemRenderType();
        tintIndex = materialInfo.tintIndex();
        shade = materialInfo.shade();
        lightEmission = materialInfo.lightEmission();
        ambientOcclusion = materialInfo.ambientOcclusion();
        return this;
    }

    /**
     * Assumes that the UV coordinates are in sprite-space and transforms
     * them to atlas-space.
     */
    private void transformUvsFromSpriteToAtlas() {
        TextureAtlasSprite sprite = requiredSprite();
        for (int i = 0; i < 4; i++) {
            long packedUv = packedUv(i);
            setUv(i, sprite.getU(UVPair.unpackU(packedUv)), sprite.getV(UVPair.unpackV(packedUv)));
        }
    }

    /**
     * Assumes that the UV coordinates are in atlas-space and transforms
     * them to sprite-space.
     */
    private void transformUvsFromAtlasToSprite() {
        TextureAtlasSprite sprite = requiredSprite();
        float uOrigin = sprite.getU0();
        float vOrigin = sprite.getV0();
        float uWidth = sprite.getU1() - uOrigin;
        float vWidth = sprite.getV1() - vOrigin;

        for (int i = 0; i < 4; i++) {
            long packedUv = packedUv(i);
            float u = (UVPair.unpackU(packedUv) - uOrigin) / uWidth;
            float v = (UVPair.unpackV(packedUv) - vOrigin) / vWidth;
            setUv(i, u, v);
        }
    }

    @Contract(pure = true)
    public BakedQuad toBakedQuad() {
        TextureAtlasSprite sprite = requiredSprite();
        ChunkSectionLayer chunkLayer = requiredChunkLayer();
        RenderType itemRenderType = requiredItemRenderType();

        // Try to reuse objects from the last baked quad that we copied from to reduce allocations if
        // the quad was only partially transformed.
        Vector3fc pos0;
        Vector3fc pos1;
        Vector3fc pos2;
        Vector3fc pos3;
        BakedQuad.MaterialInfo materialInfo;
        BakedNormals bakedNormals;
        BakedColors bakedColors;
        if (lastSourceQuad != null) {
            pos0 = reuseVector(lastSourceQuad, positions[0]);
            pos1 = reuseVector(lastSourceQuad, positions[1]);
            pos2 = reuseVector(lastSourceQuad, positions[2]);
            pos3 = reuseVector(lastSourceQuad, positions[3]);

            BakedQuad.MaterialInfo srcInfo = lastSourceQuad.materialInfo();
            boolean canReuseMaterialInfo = sprite == srcInfo.sprite() &&
                    chunkLayer == srcInfo.layer() &&
                    itemRenderType == srcInfo.itemRenderType() &&
                    tintIndex == srcInfo.tintIndex() &&
                    shade == srcInfo.shade() &&
                    lightEmission == srcInfo.lightEmission() &&
                    ambientOcclusion == srcInfo.ambientOcclusion();
            if (canReuseMaterialInfo) {
                materialInfo = srcInfo;
            } else {
                materialInfo = new BakedQuad.MaterialInfo(sprite, chunkLayer, itemRenderType, tintIndex, shade, lightEmission, ambientOcclusion);
            }

            // If the normals did not change, reuse the old object
            bakedNormals = lastSourceQuad.bakedNormals();
            for (int i = 0; i < 4; i++) {
                if (bakedNormals.normal(i) != normals[i]) {
                    // At least one normal is different -> copy
                    bakedNormals = BakedNormals.of(normals[0], normals[1], normals[2], normals[3]);
                    break;
                }
            }

            // If the colors did not change, reuse the old object
            bakedColors = lastSourceQuad.bakedColors();
            for (int i = 0; i < 4; i++) {
                if (bakedColors.color(i) != colors[i]) {
                    // The color for at least one vertex is different -> copy
                    bakedColors = BakedColors.of(colors[0], colors[1], colors[2], colors[3]);
                    break;
                }
            }
        } else {
            pos0 = new Vector3f(positions[0]);
            pos1 = new Vector3f(positions[1]);
            pos2 = new Vector3f(positions[2]);
            pos3 = new Vector3f(positions[3]);
            materialInfo = new BakedQuad.MaterialInfo(sprite, chunkLayer, itemRenderType, tintIndex, shade, lightEmission, ambientOcclusion);
            bakedNormals = BakedNormals.of(normals[0], normals[1], normals[2], normals[3]);
            bakedColors = BakedColors.of(colors[0], colors[1], colors[2], colors[3]);
        }

        return new BakedQuad(
                pos0,
                pos1,
                pos2,
                pos3,
                uvs[0],
                uvs[1],
                uvs[2],
                uvs[3],
                direction,
                materialInfo,
                bakedNormals,
                bakedColors);
    }

    /**
     * Tries to reuse the position vectors of the last quad we sourced any data from.
     * This avoids unnecessary allocations if the positions of the quad were not transformed,
     * or if a rotation simply rotated the order of positions.
     */
    private static Vector3fc reuseVector(BakedQuad quad, Vector3f position) {
        for (int i = 0; i < 4; i++) {
            if (quad.position(i).equals(position)) {
                return quad.position(i);
            }
        }
        return new Vector3f(position); // Copy if reuse is not possible
    }

    /**
     * Applies the given matrix to this quads position and normals (if specified).
     * <p>Note that the {@linkplain #direction()} is not transformed.
     */
    public MutableQuad transform(Matrix4f rotation) {
        Vector3f tmp = null;
        for (int i = 0; i < 4; i++) {
            rotation.transformPosition(positions[i]);
            var normal = normals[i];
            if (!BakedNormals.isUnspecified(normal)) {
                tmp = BakedNormals.unpack(normal, tmp);
                rotation.transformDirection(tmp);
                normals[i] = BakedNormals.pack(tmp);
            }
        }

        return this;
    }

    /**
     * Recalculates the order of vertices to conform to the order expected by the Vanilla AO algorithm.
     *
     * <p>It uses the current {@linkplain #direction() direction} and <strong>only works correctly for axis-aligned quads</strong>.
     *
     * @see FaceBakery#recalculateWinding
     */
    public MutableQuad recalculateWinding() {
        FaceBakery.recalculateWinding(positions, uvs, direction, colors, normals);
        return this;
    }

    /**
     * {@return a copy of this mutable quad}
     */
    public MutableQuad copy() {
        return copyInto(new MutableQuad());
    }

    /**
     * Copies the contents of this mutable quad into the provided mutable quad and returns it.
     */
    public MutableQuad copyInto(MutableQuad dest) {
        for (int i = 0; i < 4; i++) {
            dest.positions[i].set(positions[i]);
        }
        System.arraycopy(uvs, 0, dest.uvs, 0, uvs.length);
        System.arraycopy(normals, 0, dest.normals, 0, normals.length);
        System.arraycopy(colors, 0, dest.colors, 0, colors.length);
        dest.direction = direction;
        dest.sprite = sprite;
        dest.chunkLayer = chunkLayer;
        dest.itemRenderType = itemRenderType;
        dest.tintIndex = tintIndex;
        dest.shade = shade;
        dest.lightEmission = lightEmission;
        dest.ambientOcclusion = ambientOcclusion;
        dest.lastSourceQuad = lastSourceQuad;
        return dest;
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
        chunkLayer = null;
        itemRenderType = null;
        tintIndex = -1;
        shade = true;
        lightEmission = 0;
        ambientOcclusion = false;
        lastSourceQuad = null;

        return this;
    }

    private static Transparency getTransparency(Material.Baked material) {
        var spriteTransparency = material.sprite().contents().transparency();
        if (material.forceTranslucent()) {
            return Transparency.TRANSLUCENT;
        }
        return spriteTransparency;
    }
}
