/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.mojang.math.Transformation;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
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
        var elements = ITEM_MODEL_GENERATOR.processFrames(layerIndex, "layer" + layerIndex, sprite.contents());
        ClientHooks.fixItemModelSeams(elements, sprite);
        if (faceData != null) {
            elements.forEach(element -> element.setFaceData(faceData));
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
        int width = spriteContents.width(), height = spriteContents.height();
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

                    // Create element
                    BlockElement element = new BlockElement(
                            new Vector3f(16 * xStart / (float) width, 16 - 16 * yEnd / (float) height, 7.5F),
                            new Vector3f(16 * x / (float) width, 16 - 16 * y / (float) height, 8.5F),
                            Map.of(
                                    Direction.NORTH, new BlockElementFace(null, layerIndex, "layer" + layerIndex, new BlockFaceUV(null, 0)),
                                    Direction.SOUTH, new BlockElementFace(null, layerIndex, "layer" + layerIndex, new BlockFaceUV(null, 0))),
                            null,
                            true,
                            0);
                    // Expand coordinates to match the shrunk UVs of the front/back face on a standard generated model (done after to not affect the auto-generated UVs)
                    element.from.x = Mth.clamp(Mth.lerp(expand, element.from.x, 8F), 0F, 16F);
                    element.from.y = Mth.clamp(Mth.lerp(expand, element.from.y, 8F), 0F, 16F);
                    element.to.x = Mth.clamp(Mth.lerp(expand, element.to.x, 8F), 0F, 16F);
                    element.to.y = Mth.clamp(Mth.lerp(expand, element.to.y, 8F), 0F, 16F);
                    // Counteract sprite expansion to ensure pixel alignment
                    element.faces.forEach((dir, face) -> {
                        float[] uv = face.uv().uvs;
                        float centerU = (uv[0] + uv[0] + uv[2] + uv[2]) / 4.0F;
                        uv[0] = Mth.clamp(Mth.lerp(expand, uv[0], centerU), 0F, 16F);
                        uv[2] = Mth.clamp(Mth.lerp(expand, uv[2], centerU), 0F, 16F);
                        float centerV = (uv[1] + uv[1] + uv[3] + uv[3]) / 4.0F;
                        uv[1] = Mth.clamp(Mth.lerp(expand, uv[1], centerV), 0F, 16F);
                        uv[3] = Mth.clamp(Mth.lerp(expand, uv[3], centerV), 0F, 16F);
                    });
                    elements.add(element);

                    // Reset xStart
                    xStart = -1;
                }
            }
        }
        return elements;
    }

    /**
     * Bakes a list of {@linkplain BlockElement block elements} and feeds the baked quads to a {@linkplain IModelBuilder model builder}.
     */
    public static void bakeElements(IModelBuilder<?> builder, List<BlockElement> elements, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState) {
        for (BlockElement element : elements) {
            element.faces.forEach((side, face) -> {
                var sprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.parse(face.texture())));
                BakedQuad quad = SimpleBakedModel.bakeFace(element, face, sprite, side, modelState);
                if (face.cullForDirection() == null)
                    builder.addUnculledFace(quad);
                else
                    builder.addCulledFace(Direction.rotate(modelState.getRotation().getMatrix(), face.cullForDirection()), quad);
            });
        }
    }

    /**
     * Bakes a list of {@linkplain BlockElement block elements} and returns the list of baked quads.
     */
    public static List<BakedQuad> bakeElements(List<BlockElement> elements, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState) {
        if (elements.isEmpty())
            return List.of();
        var list = new ArrayList<BakedQuad>();
        bakeElements(IModelBuilder.collecting(list), elements, spriteGetter, modelState);
        return list;
    }

    /**
     * {@return a {@link ModelState} that combines the existing model state and the {@linkplain Transformation root transform}}
     */
    public static ModelState composeRootTransformIntoModelState(ModelState modelState, Transformation rootTransform) {
        // Move the origin of the root transform as if the negative corner were the block center to match the way the
        // ModelState transform is applied in the FaceBakery by moving the vertices to be centered on that corner
        rootTransform = rootTransform.applyOrigin(new Vector3f(-.5F, -.5F, -.5F));
        return new SimpleModelState(modelState.getRotation().compose(rootTransform), modelState.isUvLocked());
    }
}
