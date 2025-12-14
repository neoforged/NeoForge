/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.neoforged.neoforge.common.util.Lazy;

@SuppressWarnings("deprecation")
public enum NeoForgeRenderTypes {
    BLOCK_ITEM_LAYERED_SOLID(() -> getItemLayeredSolid(TextureAtlas.LOCATION_BLOCKS)),
    BLOCK_ITEM_LAYERED_CUTOUT(() -> getItemLayeredCutout(TextureAtlas.LOCATION_BLOCKS)),
    BLOCK_ITEM_LAYERED_TRANSLUCENT(() -> getItemLayeredTranslucent(TextureAtlas.LOCATION_BLOCKS)),
    BLOCK_ITEM_UNSORTED_TRANSLUCENT(() -> getUnsortedTranslucent(TextureAtlas.LOCATION_BLOCKS)),
    BLOCK_ITEM_UNLIT_TRANSLUCENT(() -> getUnlitTranslucent(TextureAtlas.LOCATION_BLOCKS)),
    BLOCK_ITEM_UNSORTED_UNLIT_TRANSLUCENT(() -> getUnlitUnsortedTranslucent(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_LAYERED_SOLID(() -> getItemLayeredSolid(TextureAtlas.LOCATION_ITEMS)),
    ITEM_LAYERED_CUTOUT(() -> getItemLayeredCutout(TextureAtlas.LOCATION_ITEMS)),
    ITEM_LAYERED_TRANSLUCENT(() -> getItemLayeredTranslucent(TextureAtlas.LOCATION_ITEMS)),
    ITEM_UNSORTED_TRANSLUCENT(() -> getUnsortedTranslucent(TextureAtlas.LOCATION_ITEMS)),
    ITEM_UNLIT_TRANSLUCENT(() -> getUnlitTranslucent(TextureAtlas.LOCATION_ITEMS)),
    ITEM_UNSORTED_UNLIT_TRANSLUCENT(() -> getUnlitUnsortedTranslucent(TextureAtlas.LOCATION_ITEMS));

    // TODO 1.21.11: Some render types previously enabled linear filtering, this intends to be equivalent, but was not checked. Also check the mipmap flag.
    private static final Supplier<GpuSampler> LINEAR_FILTERING_SAMPLER = () -> RenderSystem.getSamplerCache()
            .getSampler(AddressMode.CLAMP_TO_EDGE, AddressMode.CLAMP_TO_EDGE, FilterMode.LINEAR, FilterMode.NEAREST, false);

    /**
     * @return A RenderType fit for multi-layer solid item rendering.
     */
    public static RenderType getItemLayeredSolid(Identifier textureLocation) {
        return Internal.LAYERED_ITEM_SOLID.apply(textureLocation);
    }

    /**
     * @return A RenderType fit for multi-layer cutout item item rendering.
     */
    public static RenderType getItemLayeredCutout(Identifier textureLocation) {
        return Internal.LAYERED_ITEM_CUTOUT.apply(textureLocation);
    }

    /**
     * @return A RenderType fit for multi-layer translucent item rendering.
     */
    public static RenderType getItemLayeredTranslucent(Identifier textureLocation) {
        return Internal.LAYERED_ITEM_TRANSLUCENT.apply(textureLocation);
    }

    /**
     * @return A RenderType fit for translucent item/entity rendering, but with depth sorting disabled.
     */
    public static RenderType getUnsortedTranslucent(Identifier textureLocation) {
        return Internal.UNSORTED_TRANSLUCENT.apply(textureLocation);
    }

    /**
     * @return A RenderType fit for translucent item/entity rendering, but with diffuse lighting disabled
     *         so that fullbright quads look correct.
     */
    public static RenderType getUnlitTranslucent(Identifier textureLocation) {
        return Internal.UNLIT_TRANSLUCENT_SORTED.apply(textureLocation);
    }

    /**
     * @return A RenderType fit for translucent item/entity rendering, but with diffuse lighting disabled
     *         so that fullbright quads look correct.
     */
    public static RenderType getUnlitUnsortedTranslucent(Identifier textureLocation) {
        return Internal.UNLIT_TRANSLUCENT_UNSORTED.apply(textureLocation);
    }

    /**
     * @return Same as {@link RenderTypes#entityCutout(Identifier)}, but with mipmapping enabled.
     */
    public static RenderType getEntityCutoutMipped(Identifier textureLocation) {
        return Internal.LAYERED_ITEM_CUTOUT_MIPPED.apply(textureLocation);
    }

    /**
     * @return Replacement of {@link RenderTypes#text(Identifier)}, but with linear texture filtering.
     */
    public static RenderType getTextFiltered(Identifier locationIn) {
        return Internal.TEXT_FILTERED.apply(locationIn);
    }

    /**
     * @return Replacement of {@link RenderTypes#textIntensity(Identifier)}, but with linear texture filtering.
     */
    public static RenderType getTextIntensityFiltered(Identifier locationIn) {
        return Internal.TEXT_INTENSITY_FILTERED.apply(locationIn);
    }

    /**
     * @return Replacement of {@link RenderTypes#textPolygonOffset(Identifier)}, but with linear texture filtering.
     */
    public static RenderType getTextPolygonOffsetFiltered(Identifier locationIn) {
        return Internal.TEXT_POLYGON_OFFSET_FILTERED.apply(locationIn);
    }

    /**
     * @return Replacement of {@link RenderTypes#textIntensityPolygonOffset(Identifier)}, but with linear texture filtering.
     */
    public static RenderType getTextIntensityPolygonOffsetFiltered(Identifier locationIn) {
        return Internal.TEXT_INTENSITY_POLYGON_OFFSET_FILTERED.apply(locationIn);
    }

    /**
     * @return Replacement of {@link RenderTypes#textSeeThrough(Identifier)}, but with linear texture filtering.
     */
    public static RenderType getTextSeeThroughFiltered(Identifier locationIn) {
        return Internal.TEXT_SEETHROUGH_FILTERED.apply(locationIn);
    }

    /**
     * @return Replacement of {@link RenderTypes#textIntensitySeeThrough(Identifier)}, but with linear texture filtering.
     */
    public static RenderType getTextIntensitySeeThroughFiltered(Identifier locationIn) {
        return Internal.TEXT_INTENSITY_SEETHROUGH_FILTERED.apply(locationIn);
    }

    // ----------------------------------------
    //  Implementation details below this line
    // ----------------------------------------

    private final Supplier<RenderType> renderTypeSupplier;

    NeoForgeRenderTypes(Supplier<RenderType> renderTypeSupplier) {
        // Wrap in a Lazy<> to avoid running the supplier more than once.
        this.renderTypeSupplier = Lazy.of(renderTypeSupplier);
    }

    public RenderType get() {
        return renderTypeSupplier.get();
    }

    private static final class Internal {
        public static Function<Identifier, RenderType> UNSORTED_TRANSLUCENT = Util.memoize(Internal::unsortedTranslucent);

        private static RenderType unsortedTranslucent(Identifier textureLocation) {
            var renderState = RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
                    .withTexture("Sampler0", textureLocation) // TODO 1.21.11: This disabled mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .useOverlay()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .affectsCrumbling()
                    .createRenderSetup();
            return RenderType.create("neoforge_entity_unsorted_translucent", renderState);
        }

        public static Function<Identifier, RenderType> UNLIT_TRANSLUCENT_SORTED = Util.memoize(tex -> Internal.unlitTranslucent(tex, true));
        public static Function<Identifier, RenderType> UNLIT_TRANSLUCENT_UNSORTED = Util.memoize(tex -> Internal.unlitTranslucent(tex, false));

        private static RenderType unlitTranslucent(Identifier textureLocation, boolean sortingEnabled) {
            var renderStateBuilder = RenderSetup.builder(NeoForgeRenderPipelines.ENTITY_UNLIT_TRANSLUCENT)
                    .withTexture("Sampler0", textureLocation) // TODO 1.21.11: This disabled mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE);
            if (sortingEnabled) {
                renderStateBuilder.sortOnUpload();
            }
            return RenderType.create("neoforge_entity_unlit_translucent", renderStateBuilder.createRenderSetup());
        }

        public static Function<Identifier, RenderType> LAYERED_ITEM_SOLID = Util.memoize(Internal::layeredItemSolid);

        private static RenderType layeredItemSolid(Identifier locationIn) {
            var rendertype$state = RenderSetup.builder(RenderPipelines.ENTITY_SOLID)
                    .withTexture("Sampler0", locationIn) // TODO 1.21.11: This disabled mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .useOverlay()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .affectsCrumbling()
                    .createRenderSetup();
            return RenderType.create("neoforge_item_entity_solid", rendertype$state);
        }

        public static Function<Identifier, RenderType> LAYERED_ITEM_CUTOUT = Util.memoize(Internal::layeredItemCutout);

        private static RenderType layeredItemCutout(Identifier locationIn) {
            var rendertype$state = RenderSetup.builder(RenderPipelines.ENTITY_CUTOUT)
                    .withTexture("Sampler0", locationIn) // TODO 1.21.11: This disabled mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .useOverlay()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .affectsCrumbling()
                    .createRenderSetup();
            return RenderType.create("neoforge_item_entity_cutout", rendertype$state);
        }

        public static Function<Identifier, RenderType> LAYERED_ITEM_CUTOUT_MIPPED = Util.memoize(Internal::layeredItemCutoutMipped);

        private static RenderType layeredItemCutoutMipped(Identifier locationIn) {
            var rendertype$state = RenderSetup.builder(NeoForgeRenderPipelines.ENTITY_SMOOTH_CUTOUT_CULL)
                    .withTexture("Sampler0", locationIn) // TODO 1.21.11: This ENABLED mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .useOverlay()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .affectsCrumbling()
                    .createRenderSetup();
            return RenderType.create("neoforge_item_entity_cutout_mipped", rendertype$state);
        }

        public static Function<Identifier, RenderType> LAYERED_ITEM_TRANSLUCENT = Util.memoize(Internal::layeredItemTranslucent);

        private static RenderType layeredItemTranslucent(Identifier locationIn) {
            var rendertype$state = RenderSetup.builder(NeoForgeRenderPipelines.ENTITY_TRANSLUCENT_CULL)
                    .withTexture("Sampler0", locationIn) // TODO 1.21.11: This disabled mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .useOverlay()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .affectsCrumbling()
                    .sortOnUpload()
                    .createRenderSetup();
            return RenderType.create("neoforge_item_entity_translucent_cull", rendertype$state);
        }

        public static Function<Identifier, RenderType> TEXT_FILTERED = Util.memoize(Internal::getTextFiltered);

        private static RenderType getTextFiltered(Identifier locationIn) {
            var rendertype$state = RenderSetup.builder(RenderPipelines.TEXT)
                    .withTexture("Sampler0", locationIn, LINEAR_FILTERING_SAMPLER) // TODO 1.21.11: This disabled mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .createRenderSetup();
            return RenderType.create("neoforge_text", rendertype$state);
        }

        public static Function<Identifier, RenderType> TEXT_INTENSITY_FILTERED = Util.memoize(Internal::getTextIntensityFiltered);

        private static RenderType getTextIntensityFiltered(Identifier locationIn) {
            var rendertype$state = RenderSetup.builder(RenderPipelines.TEXT_INTENSITY)
                    .withTexture("Sampler0", locationIn, LINEAR_FILTERING_SAMPLER) // TODO 1.21.11: This disabled mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .createRenderSetup();
            return RenderType.create("neoforge_text_intensity", rendertype$state);
        }

        public static Function<Identifier, RenderType> TEXT_POLYGON_OFFSET_FILTERED = Util.memoize(Internal::getTextPolygonOffsetFiltered);

        private static RenderType getTextPolygonOffsetFiltered(Identifier locationIn) {
            var rendertype$state = RenderSetup.builder(RenderPipelines.TEXT_POLYGON_OFFSET)
                    .withTexture("Sampler0", locationIn, LINEAR_FILTERING_SAMPLER) // TODO 1.21.11: This disabled mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .createRenderSetup();
            return RenderType.create("neoforge_text_polygon_offset", rendertype$state);
        }

        public static Function<Identifier, RenderType> TEXT_INTENSITY_POLYGON_OFFSET_FILTERED = Util.memoize(Internal::getTextIntensityPolygonOffsetFiltered);

        private static RenderType getTextIntensityPolygonOffsetFiltered(Identifier locationIn) {
            var rendertype$state = RenderSetup.builder(RenderPipelines.TEXT_INTENSITY)
                    .withTexture("Sampler0", locationIn, LINEAR_FILTERING_SAMPLER) // TODO 1.21.11: This disabled mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .createRenderSetup();
            return RenderType.create("neoforge_text_intensity_polygon_offset", rendertype$state);
        }

        public static Function<Identifier, RenderType> TEXT_SEETHROUGH_FILTERED = Util.memoize(Internal::getTextSeeThroughFiltered);

        private static RenderType getTextSeeThroughFiltered(Identifier locationIn) {
            var rendertype$state = RenderSetup.builder(RenderPipelines.TEXT_SEE_THROUGH)
                    .withTexture("Sampler0", locationIn, LINEAR_FILTERING_SAMPLER) // TODO 1.21.11: This disabled mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .createRenderSetup();
            return RenderType.create("neoforge_text_see_through", rendertype$state);
        }

        public static Function<Identifier, RenderType> TEXT_INTENSITY_SEETHROUGH_FILTERED = Util.memoize(Internal::getTextIntensitySeeThroughFiltered);

        private static RenderType getTextIntensitySeeThroughFiltered(Identifier locationIn) {
            var rendertype$state = RenderSetup.builder(RenderPipelines.TEXT_INTENSITY_SEE_THROUGH)
                    .withTexture("Sampler0", locationIn, LINEAR_FILTERING_SAMPLER) // TODO 1.21.11: This disabled mip-mapping before, no idea how to force that now
                    .useLightmap()
                    .createRenderSetup();
            return RenderType.create("neoforge_text_intensity_see_through", rendertype$state);
        }
    }
}
