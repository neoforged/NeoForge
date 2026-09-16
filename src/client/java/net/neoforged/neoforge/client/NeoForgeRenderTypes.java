/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import java.util.function.Function;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.TextureTransform;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

@SuppressWarnings("deprecation")
public final class NeoForgeRenderTypes {
    /// Solid equivalent to [Sheets#cutoutBlockItemSheet()] and [Sheets#translucentBlockItemSheet()]
    public static final RenderType SOLID_BLOCK_SHEET = RenderTypes.entitySolid(TextureAtlas.LOCATION_BLOCKS);

    public static RenderType getItemCutoutUnlit(Identifier texture) {
        return Internal.ITEM_CUTOUT_UNLIT.apply(texture);
    }

    public static RenderType getItemGlintCutoutUnlit(Identifier texture) {
        return Internal.ITEM_GLINT_CUTOUT_UNLIT.apply(texture);
    }

    public static RenderType getItemGlintSpecialCutoutUnlit(Identifier texture) {
        return Internal.ITEM_GLINT_SPECIAL_CUTOUT_UNLIT.apply(texture);
    }

    public static RenderType getItemTranslucentUnlit(Identifier texture) {
        return Internal.ITEM_TRANSLUCENT_UNLIT.apply(texture);
    }

    public static RenderType getItemGlintTranslucentUnlit(Identifier texture) {
        return Internal.ITEM_GLINT_TRANSLUCENT_UNLIT.apply(texture);
    }

    public static RenderType getItemGlintSpecialTranslucentUnlit(Identifier texture) {
        return Internal.ITEM_GLINT_SPECIAL_TRANSLUCENT_UNLIT.apply(texture);
    }

    public static RenderType getEntityUnlitTranslucent(Identifier texture) {
        return Internal.ENTITY_UNLIT_TRANSLUCENT.apply(texture);
    }

    // ----------------------------------------
    //  Implementation details below this line
    // ----------------------------------------

    private static final class Internal {
        private static final Function<Identifier, RenderType> ITEM_CUTOUT_UNLIT = Util.memoize(Internal::itemCutoutUnlit);
        private static final Function<Identifier, RenderType> ITEM_GLINT_CUTOUT_UNLIT = Util.memoize(Internal::itemGlintCutoutUnlit);
        private static final Function<Identifier, RenderType> ITEM_GLINT_SPECIAL_CUTOUT_UNLIT = Util.memoize(Internal::itemGlintSpecialCutoutUnlit);
        private static final Function<Identifier, RenderType> ITEM_TRANSLUCENT_UNLIT = Util.memoize(Internal::itemTranslucentUnlit);
        private static final Function<Identifier, RenderType> ITEM_GLINT_TRANSLUCENT_UNLIT = Util.memoize(Internal::itemGlintTranslucentUnlit);
        private static final Function<Identifier, RenderType> ITEM_GLINT_SPECIAL_TRANSLUCENT_UNLIT = Util.memoize(Internal::itemGlintSpecialTranslucentUnlit);

        private static RenderType itemCutoutUnlit(Identifier texture) {
            RenderSetup state = RenderSetup.builder(NeoForgeRenderPipelines.ITEM_CUTOUT_UNLIT)
                    .withTexture("Sampler0", texture)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .createRenderSetup();
            return RenderType.create("neoforge_item_cutout_unlit", state);
        }

        private static RenderType itemGlintCutoutUnlit(Identifier texture) {
            RenderSetup state = RenderSetup.builder(NeoForgeRenderPipelines.ITEM_CUTOUT_UNLIT_GLINT)
                    .withTexture("Sampler0", texture)
                    .withTexture("GlintSampler", ItemFeatureRenderer.ENCHANTED_GLINT_ITEM)
                    .setTextureTransform(TextureTransform.GLINT_TEXTURING)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .createRenderSetup();
            return RenderType.create("neoforge_item_cutout_unlit", state);
        }

        private static RenderType itemGlintSpecialCutoutUnlit(Identifier texture) {
            RenderSetup state = RenderSetup.builder(NeoForgeRenderPipelines.ITEM_CUTOUT_UNLIT_GLINT_SPECIAL)
                    .withTexture("Sampler0", texture)
                    .withTexture("GlintSampler", ItemFeatureRenderer.ENCHANTED_GLINT_ITEM)
                    .setTextureTransform(TextureTransform.GLINT_TEXTURING)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .createRenderSetup();
            return RenderType.create("neoforge_item_cutout_unlit", state);
        }

        private static RenderType itemTranslucentUnlit(Identifier texture) {
            RenderSetup state = RenderSetup.builder(NeoForgeRenderPipelines.ITEM_TRANSLUCENT_UNLIT)
                    .setOitPipelines(NeoForgeRenderPipelines.OIT_ITEM_UNLIT)
                    .withTexture("Sampler0", texture)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .sortOnUpload()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .createRenderSetup();
            return RenderType.create("neoforge_item_translucent_unlit", state);
        }

        private static RenderType itemGlintTranslucentUnlit(Identifier texture) {
            RenderSetup state = RenderSetup.builder(NeoForgeRenderPipelines.ITEM_TRANSLUCENT_UNLIT_GLINT)
                    .setOitPipelines(NeoForgeRenderPipelines.OIT_ITEM_UNLIT_GLINT)
                    .withTexture("Sampler0", texture)
                    .withTexture("GlintSampler", ItemFeatureRenderer.ENCHANTED_GLINT_ITEM)
                    .setTextureTransform(TextureTransform.GLINT_TEXTURING)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .sortOnUpload()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .createRenderSetup();
            return RenderType.create("neoforge_item_translucent_unlit", state);
        }

        private static RenderType itemGlintSpecialTranslucentUnlit(Identifier texture) {
            RenderSetup state = RenderSetup.builder(NeoForgeRenderPipelines.ITEM_TRANSLUCENT_UNLIT_GLINT_SPECIAL)
                    .setOitPipelines(NeoForgeRenderPipelines.OIT_ITEM_UNLIT_GLINT_SPECIAL)
                    .withTexture("Sampler0", texture)
                    .withTexture("GlintSampler", ItemFeatureRenderer.ENCHANTED_GLINT_ITEM)
                    .setTextureTransform(TextureTransform.GLINT_TEXTURING)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .sortOnUpload()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .createRenderSetup();
            return RenderType.create("neoforge_item_translucent_unlit", state);
        }

        public static Function<Identifier, RenderType> ENTITY_UNLIT_TRANSLUCENT = Util.memoize(Internal::unlitTranslucent);

        private static RenderType unlitTranslucent(Identifier textureLocation) {
            var renderStateBuilder = RenderSetup.builder(NeoForgeRenderPipelines.ENTITY_UNLIT_TRANSLUCENT)
                    .setOitPipelines(NeoForgeRenderPipelines.OIT_ENTITY_UNLIT)
                    .withTexture("Sampler0", textureLocation)
                    .useLightmap()
                    .useOverlay()
                    .affectsCrumbling()
                    .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                    .sortOnUpload();
            return RenderType.create("neoforge_entity_unlit_translucent", renderStateBuilder.createRenderSetup());
        }
    }

    private NeoForgeRenderTypes() {}
}
