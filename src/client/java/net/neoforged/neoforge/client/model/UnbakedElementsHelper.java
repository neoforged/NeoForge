/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.mojang.math.Quadrant;
import com.mojang.math.Transformation;
import java.util.BitSet;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.cuboid.CuboidFace;
import net.minecraft.client.resources.model.cuboid.CuboidModelElement;
import net.minecraft.client.resources.model.cuboid.FaceBakery;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public final class UnbakedElementsHelper {
    private UnbakedElementsHelper() {}

    /**
     * @see #bakeItemMaskQuads(ModelBaker, int, Material.Baked, Material.Baked, ModelState, ExtraFaceData)
     */
    public static QuadCollection bakeItemMaskQuads(ModelBaker baker, int layerIndex, Material.Baked maskMaterial, Material.Baked outputMaterial, ModelState modelState) {
        return bakeItemMaskQuads(baker, layerIndex, maskMaterial, outputMaterial, modelState, ExtraFaceData.DEFAULT);
    }

    /**
     * Bakes quads in the shape of the specified mask texture with the specified output texture applied to them.
     * <p>
     * The {@link Direction#NORTH} and {@link Direction#SOUTH} faces take up only the pixels the mask texture uses.
     */
    public static QuadCollection bakeItemMaskQuads(ModelBaker baker, int layerIndex, Material.Baked maskMaterial, Material.Baked outputMaterial, ModelState modelState, ExtraFaceData faceData) {
        return bakeItemMaskQuads(baker, layerIndex, maskMaterial, outputMaterial, modelState, faceData, UnaryOperator.identity());
    }

    /**
     * Bakes quads in the shape of the specified mask texture with the specified output texture applied to them.
     * <p>
     * The {@link Direction#NORTH} and {@link Direction#SOUTH} faces take up only the pixels the mask texture uses.
     */
    public static QuadCollection bakeItemMaskQuads(ModelBaker baker, int layerIndex, Material.Baked maskMaterial, Material.Baked outputMaterial, ModelState modelState, ExtraFaceData faceData, UnaryOperator<BakedQuad.MaterialInfo> materialModifier) {
        QuadCollection.Builder builder = new QuadCollection.Builder();
        ModelBaker.Interner interner = baker.interner();
        BakedQuad.MaterialInfo maskMaterialInfo = interner.materialInfo(BakedQuad.MaterialInfo.of(maskMaterial, maskMaterial.sprite().transparency(), layerIndex, true, 0, true));
        BakedQuad.MaterialInfo outMaterialInfo = interner.materialInfo(materialModifier.apply(BakedQuad.MaterialInfo.of(outputMaterial, outputMaterial.sprite().transparency(), layerIndex, true, faceData.lightEmission(), faceData.ambientOcclusion())));

        // TODO 26.1: why are the side faces included at all?
        ItemModelGenerator.bakeSideFaces(builder, interner, modelState, maskMaterialInfo);

        SpriteContents spriteContents = maskMaterial.sprite().contents();
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
                    // Create UVs
                    CuboidFace.UVs northUvs = FaceBakery.defaultFaceUV(from, to, Direction.NORTH);
                    CuboidFace.UVs southUvs = FaceBakery.defaultFaceUV(from, to, Direction.SOUTH);
                    // Create quads
                    builder.addUnculledFace(FaceBakery.bakeQuad(interner, from, to, northUvs, Quadrant.R0, outMaterialInfo, Direction.SOUTH, modelState, null, faceData));
                    builder.addUnculledFace(FaceBakery.bakeQuad(interner, from, to, southUvs, Quadrant.R0, outMaterialInfo, Direction.NORTH, modelState, null, faceData));

                    // Reset xStart
                    xStart = -1;
                }
            }
        }
        return builder.build();
    }

    /**
     * Bakes a list of {@linkplain CuboidModelElement block elements} and feeds the baked quads to a {@linkplain QuadCollection.Builder quad collection builder}.
     */
    public static void bakeElements(ModelBaker baker, QuadCollection.Builder builder, List<CuboidModelElement> elements, Function<String, Material.Baked> materialGetter, ModelState modelState) {
        for (CuboidModelElement element : elements) {
            element.faces().forEach((side, face) -> {
                Material.Baked material = materialGetter.apply(face.texture());
                BakedQuad quad = FaceBakery.bakeQuad(
                        baker,
                        element.from(),
                        element.to(),
                        face,
                        material,
                        side,
                        modelState,
                        element.rotation(),
                        element.shade(),
                        element.lightEmission());
                if (face.cullForDirection() == null)
                    builder.addUnculledFace(quad);
                else
                    builder.addCulledFace(Direction.rotate(modelState.transformation().getMatrix(), face.cullForDirection()), quad);
            });
        }
    }

    /**
     * Bakes a list of {@linkplain CuboidModelElement block elements} and returns the list of baked quads.
     */
    public static List<BakedQuad> bakeElements(ModelBaker baker, List<CuboidModelElement> elements, Function<String, Material.Baked> materialGetter, ModelState modelState) {
        if (elements.isEmpty())
            return List.of();
        var builder = new QuadCollection.Builder();
        bakeElements(baker, builder, elements, materialGetter, modelState);
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
