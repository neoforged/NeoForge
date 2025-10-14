/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.Lazy;

@SuppressWarnings("deprecation")
public enum NeoForgeRenderTypes {
    ITEM_LAYERED_SOLID(() -> getItemLayeredSolid(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_LAYERED_CUTOUT(() -> getItemLayeredCutout(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_LAYERED_CUTOUT_MIPPED(() -> getItemLayeredCutoutMipped(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_LAYERED_TRANSLUCENT(() -> getItemLayeredTranslucent(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_UNSORTED_TRANSLUCENT(() -> getUnsortedTranslucent(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_UNLIT_TRANSLUCENT(() -> getUnlitTranslucent(TextureAtlas.LOCATION_BLOCKS)),
    ITEM_UNSORTED_UNLIT_TRANSLUCENT(() -> getUnlitTranslucent(TextureAtlas.LOCATION_BLOCKS, false));

    /**
     * @return A RenderType fit for multi-layer solid item rendering.
     */
    public static RenderType getItemLayeredSolid(ResourceLocation textureLocation) {
        return Internal.LAYERED_ITEM_SOLID.apply(textureLocation);
    }

    /**
     * @return A RenderType fit for multi-layer cutout item item rendering.
     */
    public static RenderType getItemLayeredCutout(ResourceLocation textureLocation) {
        return Internal.LAYERED_ITEM_CUTOUT.apply(textureLocation);
    }

    /**
     * @return A RenderType fit for multi-layer cutout-mipped item rendering.
     */
    public static RenderType getItemLayeredCutoutMipped(ResourceLocation textureLocation) {
        return Internal.LAYERED_ITEM_CUTOUT_MIPPED.apply(textureLocation);
    }

    /**
     * @return A RenderType fit for multi-layer translucent item rendering.
     */
    public static RenderType getItemLayeredTranslucent(ResourceLocation textureLocation) {
        return Internal.LAYERED_ITEM_TRANSLUCENT.apply(textureLocation);
    }

    /**
     * @return A RenderType fit for translucent item/entity rendering, but with depth sorting disabled.
     */
    public static RenderType getUnsortedTranslucent(ResourceLocation textureLocation) {
        return Internal.UNSORTED_TRANSLUCENT.apply(textureLocation);
    }

    /**
     * @return A RenderType fit for translucent item/entity rendering, but with diffuse lighting disabled
     *         so that fullbright quads look correct.
     */
    public static RenderType getUnlitTranslucent(ResourceLocation textureLocation) {
        return Internal.UNLIT_TRANSLUCENT_SORTED.apply(textureLocation);
    }

    /**
     * @return A RenderType fit for translucent item/entity rendering, but with diffuse lighting disabled
     *         so that fullbright quads look correct.
     * @param sortingEnabled If false, depth sorting will not be performed.
     */
    public static RenderType getUnlitTranslucent(ResourceLocation textureLocation, boolean sortingEnabled) {
        return (sortingEnabled ? Internal.UNLIT_TRANSLUCENT_SORTED : Internal.UNLIT_TRANSLUCENT_UNSORTED).apply(textureLocation);
    }

    /**
     * @return Same as {@link RenderType#entityCutout(ResourceLocation)}, but with mipmapping enabled.
     */
    public static RenderType getEntityCutoutMipped(ResourceLocation textureLocation) {
        return Internal.LAYERED_ITEM_CUTOUT_MIPPED.apply(textureLocation);
    }

    /**
     * @return Replacement of {@link RenderType#text(ResourceLocation)}, but with linear texture filtering.
     */
    public static RenderType getTextFiltered(ResourceLocation locationIn) {
        return Internal.TEXT_FILTERED.apply(locationIn);
    }

    /**
     * @return Replacement of {@link RenderType#textIntensity(ResourceLocation)}, but with linear texture filtering.
     */
    public static RenderType getTextIntensityFiltered(ResourceLocation locationIn) {
        return Internal.TEXT_INTENSITY_FILTERED.apply(locationIn);
    }

    /**
     * @return Replacement of {@link RenderType#textPolygonOffset(ResourceLocation)}, but with linear texture filtering.
     */
    public static RenderType getTextPolygonOffsetFiltered(ResourceLocation locationIn) {
        return Internal.TEXT_POLYGON_OFFSET_FILTERED.apply(locationIn);
    }

    /**
     * @return Replacement of {@link RenderType#textIntensityPolygonOffset(ResourceLocation)}, but with linear texture filtering.
     */
    public static RenderType getTextIntensityPolygonOffsetFiltered(ResourceLocation locationIn) {
        return Internal.TEXT_INTENSITY_POLYGON_OFFSET_FILTERED.apply(locationIn);
    }

    /**
     * @return Replacement of {@link RenderType#textSeeThrough(ResourceLocation)}, but with linear texture filtering.
     */
    public static RenderType getTextSeeThroughFiltered(ResourceLocation locationIn) {
        return Internal.TEXT_SEETHROUGH_FILTERED.apply(locationIn);
    }

    /**
     * @return Replacement of {@link RenderType#textIntensitySeeThrough(ResourceLocation)}, but with linear texture filtering.
     */
    public static RenderType getTextIntensitySeeThroughFiltered(ResourceLocation locationIn) {
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
        public static Function<ResourceLocation, RenderType> UNSORTED_TRANSLUCENT = Util.memoize(Internal::unsortedTranslucent);

        private static RenderType unsortedTranslucent(ResourceLocation textureLocation) {
            final boolean sortingEnabled = false;
            var renderState = RenderType.CompositeState.builder()
                    .setTextureState(new TextureStateShard(textureLocation, false))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setOverlayState(RenderType.OVERLAY)
                    .createCompositeState(true);
            return RenderType.create("neoforge_entity_unsorted_translucent", 256, true, sortingEnabled, RenderPipelines.ENTITY_TRANSLUCENT, renderState);
        }

        public static Function<ResourceLocation, RenderType> UNLIT_TRANSLUCENT_SORTED = Util.memoize(tex -> Internal.unlitTranslucent(tex, true));
        public static Function<ResourceLocation, RenderType> UNLIT_TRANSLUCENT_UNSORTED = Util.memoize(tex -> Internal.unlitTranslucent(tex, false));

        private static RenderType unlitTranslucent(ResourceLocation textureLocation, boolean sortingEnabled) {
            var renderState = RenderType.CompositeState.builder()
                    .setTextureState(new TextureStateShard(textureLocation, false))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setOverlayState(RenderType.OVERLAY)
                    .createCompositeState(true);
            return RenderType.create("neoforge_entity_unlit_translucent", 256, true, sortingEnabled, NeoForgeRenderPipelines.ENTITY_UNLIT_TRANSLUCENT, renderState);
        }

        public static Function<ResourceLocation, RenderType> LAYERED_ITEM_SOLID = Util.memoize(Internal::layeredItemSolid);

        private static RenderType layeredItemSolid(ResourceLocation locationIn) {
            var rendertype$state = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setOverlayState(RenderType.OVERLAY)
                    .createCompositeState(true);
            return RenderType.create("neoforge_item_entity_solid", 256, true, false, RenderPipelines.ENTITY_SOLID, rendertype$state);
        }

        public static Function<ResourceLocation, RenderType> LAYERED_ITEM_CUTOUT = Util.memoize(Internal::layeredItemCutout);

        private static RenderType layeredItemCutout(ResourceLocation locationIn) {
            var rendertype$state = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setOverlayState(RenderType.OVERLAY)
                    .createCompositeState(true);
            return RenderType.create("neoforge_item_entity_cutout", 256, true, false, RenderPipelines.ENTITY_CUTOUT, rendertype$state);
        }

        public static Function<ResourceLocation, RenderType> LAYERED_ITEM_CUTOUT_MIPPED = Util.memoize(Internal::layeredItemCutoutMipped);

        private static RenderType layeredItemCutoutMipped(ResourceLocation locationIn) {
            var rendertype$state = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, true))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setOverlayState(RenderType.OVERLAY)
                    .createCompositeState(true);
            return RenderType.create("neoforge_item_entity_cutout_mipped", 256, true, false, NeoForgeRenderPipelines.ENTITY_SMOOTH_CUTOUT_CULL, rendertype$state);
        }

        public static Function<ResourceLocation, RenderType> LAYERED_ITEM_TRANSLUCENT = Util.memoize(Internal::layeredItemTranslucent);

        private static RenderType layeredItemTranslucent(ResourceLocation locationIn) {
            var rendertype$state = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setOverlayState(RenderType.OVERLAY)
                    .createCompositeState(true);
            return RenderType.create("neoforge_item_entity_translucent_cull", 256, true, true, NeoForgeRenderPipelines.ENTITY_TRANSLUCENT_CULL, rendertype$state);
        }

        public static Function<ResourceLocation, RenderType> TEXT_FILTERED = Util.memoize(Internal::getTextFiltered);

        private static RenderType getTextFiltered(ResourceLocation locationIn) {
            var rendertype$state = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false))
                    .setTexturingState(new LinearFilteredTexturing(locationIn))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .createCompositeState(false);
            return RenderType.create("neoforge_text", 256, false, false, RenderPipelines.TEXT, rendertype$state);
        }

        public static Function<ResourceLocation, RenderType> TEXT_INTENSITY_FILTERED = Util.memoize(Internal::getTextIntensityFiltered);

        private static RenderType getTextIntensityFiltered(ResourceLocation locationIn) {
            var rendertype$state = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false))
                    .setTexturingState(new LinearFilteredTexturing(locationIn))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .createCompositeState(false);
            return RenderType.create("neoforge_text_intensity", 256, false, false, RenderPipelines.TEXT_INTENSITY, rendertype$state);
        }

        public static Function<ResourceLocation, RenderType> TEXT_POLYGON_OFFSET_FILTERED = Util.memoize(Internal::getTextPolygonOffsetFiltered);

        private static RenderType getTextPolygonOffsetFiltered(ResourceLocation locationIn) {
            var rendertype$state = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false))
                    .setTexturingState(new LinearFilteredTexturing(locationIn))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .createCompositeState(false);
            return RenderType.create("neoforge_text_polygon_offset", 256, false, false, RenderPipelines.TEXT_POLYGON_OFFSET, rendertype$state);
        }

        public static Function<ResourceLocation, RenderType> TEXT_INTENSITY_POLYGON_OFFSET_FILTERED = Util.memoize(Internal::getTextIntensityPolygonOffsetFiltered);

        private static RenderType getTextIntensityPolygonOffsetFiltered(ResourceLocation locationIn) {
            var rendertype$state = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false))
                    .setTexturingState(new LinearFilteredTexturing(locationIn))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .createCompositeState(false);
            return RenderType.create("neoforge_text_intensity_polygon_offset", 256, false, false, RenderPipelines.TEXT_INTENSITY, rendertype$state);
        }

        public static Function<ResourceLocation, RenderType> TEXT_SEETHROUGH_FILTERED = Util.memoize(Internal::getTextSeeThroughFiltered);

        private static RenderType getTextSeeThroughFiltered(ResourceLocation locationIn) {
            var rendertype$state = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false))
                    .setTexturingState(new LinearFilteredTexturing(locationIn))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .createCompositeState(false);
            return RenderType.create("neoforge_text_see_through", 256, false, false, RenderPipelines.TEXT_SEE_THROUGH, rendertype$state);
        }

        public static Function<ResourceLocation, RenderType> TEXT_INTENSITY_SEETHROUGH_FILTERED = Util.memoize(Internal::getTextIntensitySeeThroughFiltered);

        private static RenderType getTextIntensitySeeThroughFiltered(ResourceLocation locationIn) {
            var rendertype$state = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, false))
                    .setTexturingState(new LinearFilteredTexturing(locationIn))
                    .setLightmapState(RenderType.LIGHTMAP)
                    .createCompositeState(false);
            return RenderType.create("neoforge_text_intensity_see_through", 256, false, false, RenderPipelines.TEXT_INTENSITY_SEE_THROUGH, rendertype$state);
        }

        private static final class LinearFilteredTexturing extends RenderStateShard.TexturingStateShard {
            public LinearFilteredTexturing(ResourceLocation textureLoc) {
                super("neoforge:linear_filtered", () -> {
                    TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
                    AbstractTexture texture = texturemanager.getTexture(textureLoc);
                    texture.setFilter(true, false);
                }, () -> {
                    TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
                    AbstractTexture texture = texturemanager.getTexture(textureLoc);
                    texture.setFilter(false, false);
                });
            }
        }
    }
}
