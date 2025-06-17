/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.ClientHooks;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class UnbakedElementsHelper {
    private UnbakedElementsHelper() {}

    private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

    /**
     * @see #createUnbakedItemElements(int, TextureAtlasSprite, ExtraFaceData)
     */
    public static List<BlockElement> createUnbakedItemElements(int layerIndex, TextureAtlasSprite sprite) {
        return createUnbakedItemElements(layerIndex, sprite, null);
    }

    /**
     * Creates a list of {@linkplain BlockElement block elements} in the shape of the specified sprite contents.
     * These can later be baked using the same, or another texture.
     * <p>
     * The {@link Direction#NORTH} and {@link Direction#SOUTH} faces take up the whole surface.
     */
    public static List<BlockElement> createUnbakedItemElements(int layerIndex, TextureAtlasSprite sprite, @Nullable ExtraFaceData faceData) {
        var elements = ItemModelGenerator.processFrames(layerIndex, "layer" + layerIndex, sprite.contents());
        ClientHooks.fixItemModelSeams(elements, sprite);
        if (faceData != null) {
            elements.replaceAll(elem -> new BlockElement(elem.from(), elem.to(), elem.faces(), elem.rotation(), elem.shade(), elem.lightEmission(), faceData));
        }
        return elements;
    }

    /**
     * @see #createUnbakedItemMaskElements(int, TextureAtlasSprite, ExtraFaceData)
     */
    public static List<BlockElement> createUnbakedItemMaskElements(int layerIndex, TextureAtlasSprite sprite) {
        return createUnbakedItemMaskElements(layerIndex, sprite, null);
    }

    /**
     * Creates a list of {@linkplain BlockElement block elements} in the shape of the specified sprite contents.
     * These can later be baked using the same, or another texture.
     * <p>
     * The {@link Direction#NORTH} and {@link Direction#SOUTH} faces take up only the pixels the texture uses.
     */
    public static List<BlockElement> createUnbakedItemMaskElements(int layerIndex, TextureAtlasSprite sprite, @Nullable ExtraFaceData faceData) {
        List<BlockElement> elements = createUnbakedItemElements(layerIndex, sprite, faceData);
        elements.removeFirst(); // Remove north and south faces

        float expand = -sprite.uvShrinkRatio();
        SpriteContents spriteContents = sprite.contents();
        int width = spriteContents.width();
        int height = spriteContents.height();
        BitSet bits = new BitSet(width * height);

        // For every frame in the texture, mark all the opaque pixels (this is what vanilla does too)
        spriteContents.getUniqueFrames().forEach(frame -> {
            for (int x = 0; x < width; x++)
                for (int y = 0; y < height; y++)
                    if (!spriteContents.isTransparent(frame, x, y))
                        bits.set(x + y * width);
        });

        // Scan in search of opaque pixels
        for (int y = 0; y < height; y++) {
            int xStart = -1;
            for (int x = 0; x < width; x++) {
                boolean opaque = bits.get(x + y * width);
                if (opaque == (xStart == -1)) { // (opaque && -1) || (!opaque && !-1)
                    if (xStart == -1) {
                        // We have found the start of a new segment, continue
                        xStart = x;
                        continue;
                    }

                    // The segment is over, expand down as far as possible
                    int yEnd = y + 1;
                    expand:
                    for (; yEnd < height; yEnd++)
                        for (int x2 = xStart; x2 <= x; x2++)
                            if (!bits.get(x2 + yEnd * width))
                                break expand;

                    // Mark all pixels in the area as visited
                    for (int i = xStart; i < x; i++)
                        for (int j = y; j < yEnd; j++)
                            bits.clear(i + j * width);

                    Vector3f from = new Vector3f(16 * xStart / (float) width, 16 - 16 * yEnd / (float) height, 7.5F);
                    Vector3f to = new Vector3f(16 * x / (float) width, 16 - 16 * y / (float) height, 8.5F);
                    // Create initial default UVs
                    BlockElementFace.UVs northUvs = FaceBakery.defaultFaceUV(from, to, Direction.NORTH);
                    BlockElementFace.UVs southUvs = FaceBakery.defaultFaceUV(from, to, Direction.SOUTH);
                    // Expand coordinates to match the shrunk UVs of the front/back face on a standard generated model
                    from.x = Mth.clamp(Mth.lerp(expand, from.x, 8F), 0F, 16F);
                    from.y = Mth.clamp(Mth.lerp(expand, from.y, 8F), 0F, 16F);
                    to.x = Mth.clamp(Mth.lerp(expand, to.x, 8F), 0F, 16F);
                    to.y = Mth.clamp(Mth.lerp(expand, to.y, 8F), 0F, 16F);
                    // Counteract sprite expansion to ensure pixel alignment
                    northUvs = expandUVs(northUvs, expand);
                    southUvs = expandUVs(southUvs, expand);
                    // Create faces
                    Map<Direction, BlockElementFace> faces = Map.of(
                            Direction.NORTH, new BlockElementFace(null, layerIndex, "layer" + layerIndex, northUvs, Quadrant.R0),
                            Direction.SOUTH, new BlockElementFace(null, layerIndex, "layer" + layerIndex, southUvs, Quadrant.R0));
                    // Create element
                    elements.add(new BlockElement(from, to, faces, null, true, 0));

                    // Reset xStart
                    xStart = -1;
                }
            }
        }
        return elements;
    }

    private static BlockElementFace.UVs expandUVs(BlockElementFace.UVs uvs, float expand) {
        float centerU = (uvs.minU() + uvs.minU() + uvs.maxU() + uvs.maxU()) / 4.0F;
        float centerV = (uvs.minV() + uvs.minV() + uvs.maxV() + uvs.maxV()) / 4.0F;
        return new BlockElementFace.UVs(
                Mth.clamp(Mth.lerp(expand, uvs.minU(), centerU), 0F, 16F),
                Mth.clamp(Mth.lerp(expand, uvs.minV(), centerV), 0F, 16F),
                Mth.clamp(Mth.lerp(expand, uvs.maxU(), centerU), 0F, 16F),
                Mth.clamp(Mth.lerp(expand, uvs.maxV(), centerV), 0F, 16F));
    }

    /**
     * Bakes a list of {@linkplain BlockElement block elements} and feeds the baked quads to a {@linkplain QuadCollection.Builder quad collection builder}.
     */
    public static void bakeElements(QuadCollection.Builder builder, List<BlockElement> elements, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState) {
        for (BlockElement element : elements) {
            element.faces().forEach((side, face) -> {
                var sprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(face.texture())));
                BakedQuad quad = SimpleUnbakedGeometry.bakeFace(element, face, sprite, side, modelState);
                if (face.cullForDirection() == null)
                    builder.addUnculledFace(quad);
                else
                    builder.addCulledFace(Direction.rotate(modelState.transformation().getMatrix(), face.cullForDirection()), quad);
            });
        }
    }

    /**
     * Bakes a list of {@linkplain BlockElement block elements} and returns the list of baked quads.
     */
    public static List<BakedQuad> bakeElements(List<BlockElement> elements, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState) {
        if (elements.isEmpty())
            return List.of();
        var builder = new QuadCollection.Builder();
        bakeElements(builder, elements, spriteGetter, modelState);
        return builder.build().getAll();
    }

    /**
     * {@return a {@link ModelState} that combines the existing model state and the {@linkplain Transformation root transform}}
     */
    public static ModelState composeRootTransformIntoModelState(ModelState modelState, Transformation rootTransform) {
        if (rootTransform.isIdentity()) {
            return modelState;
        }

        // Move the origin of the root transform as if the negative corner were the block center to match the way the
        // ModelState transform is applied in the FaceBakery by moving the vertices to be centered on that corner
        rootTransform = rootTransform.applyOrigin(new Vector3f(-.5F, -.5F, -.5F));
        return new ComposedModelState(modelState, rootTransform);
    }
}
