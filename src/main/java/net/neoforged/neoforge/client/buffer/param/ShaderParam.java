/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;

public record ShaderParam(Optional<Supplier<ShaderInstance>> shaderSupplier) implements IBufferDefinitionParam<Optional<Supplier<ShaderInstance>>> {

    public ShaderParam(Supplier<ShaderInstance> shaderSupplier) {
        this(Optional.of(shaderSupplier));
    }

    public ShaderParam() {
        this(Optional.empty());
    }

    @Override
    public Optional<Supplier<ShaderInstance>> getValue() {
        return shaderSupplier;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypeManager.SHADER;
    }
    public static final class Vanilla {
        public static final ShaderParam NO_SHADER = new ShaderParam();
        public static final ShaderParam POSITION_COLOR_LIGHTMAP_SHADER = new ShaderParam(GameRenderer::getPositionColorLightmapShader);
        public static final ShaderParam POSITION_SHADER = new ShaderParam(GameRenderer::getPositionShader);
        public static final ShaderParam POSITION_TEX_SHADER = new ShaderParam(GameRenderer::getPositionTexShader);
        public static final ShaderParam POSITION_COLOR_TEX_LIGHTMAP_SHADER = new ShaderParam(GameRenderer::getPositionColorTexLightmapShader);
        public static final ShaderParam POSITION_COLOR_SHADER = new ShaderParam(GameRenderer::getPositionColorShader);
        public static final ShaderParam RENDERTYPE_SOLID_SHADER = new ShaderParam(GameRenderer::getRendertypeSolidShader);
        public static final ShaderParam RENDERTYPE_CUTOUT_MIPPED_SHADER = new ShaderParam(GameRenderer::getRendertypeCutoutMippedShader);
        public static final ShaderParam RENDERTYPE_CUTOUT_SHADER = new ShaderParam(GameRenderer::getRendertypeCutoutShader);
        public static final ShaderParam RENDERTYPE_TRANSLUCENT_SHADER = new ShaderParam(GameRenderer::getRendertypeTranslucentShader);
        public static final ShaderParam RENDERTYPE_TRANSLUCENT_MOVING_BLOCK_SHADER = new ShaderParam(GameRenderer::getRendertypeTranslucentMovingBlockShader);
        public static final ShaderParam RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER = new ShaderParam(GameRenderer::getRendertypeArmorCutoutNoCullShader);
        public static final ShaderParam RENDERTYPE_ENTITY_SOLID_SHADER = new ShaderParam(GameRenderer::getRendertypeEntitySolidShader);
        public static final ShaderParam RENDERTYPE_ENTITY_CUTOUT_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityCutoutShader);
        public static final ShaderParam RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityCutoutNoCullShader);
        public static final ShaderParam RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityCutoutNoCullZOffsetShader);
        public static final ShaderParam RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER = new ShaderParam(GameRenderer::getRendertypeItemEntityTranslucentCullShader);
        public static final ShaderParam RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityTranslucentCullShader);
        public static final ShaderParam RENDERTYPE_ENTITY_TRANSLUCENT_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityTranslucentShader);
        public static final ShaderParam RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityTranslucentEmissiveShader);
        public static final ShaderParam RENDERTYPE_ENTITY_SMOOTH_CUTOUT_SHADER = new ShaderParam(GameRenderer::getRendertypeEntitySmoothCutoutShader);
        public static final ShaderParam RENDERTYPE_BEACON_BEAM_SHADER = new ShaderParam(GameRenderer::getRendertypeBeaconBeamShader);
        public static final ShaderParam RENDERTYPE_ENTITY_DECAL_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityDecalShader);
        public static final ShaderParam RENDERTYPE_ENTITY_NO_OUTLINE_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityNoOutlineShader);
        public static final ShaderParam RENDERTYPE_ENTITY_SHADOW_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityShadowShader);
        public static final ShaderParam RENDERTYPE_ENTITY_ALPHA_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityAlphaShader);
        public static final ShaderParam RENDERTYPE_EYES_SHADER = new ShaderParam(GameRenderer::getRendertypeEyesShader);
        public static final ShaderParam RENDERTYPE_ENERGY_SWIRL_SHADER = new ShaderParam(GameRenderer::getRendertypeEnergySwirlShader);
        public static final ShaderParam RENDERTYPE_LEASH_SHADER = new ShaderParam(GameRenderer::getRendertypeLeashShader);
        public static final ShaderParam RENDERTYPE_WATER_MASK_SHADER = new ShaderParam(GameRenderer::getRendertypeWaterMaskShader);
        public static final ShaderParam RENDERTYPE_OUTLINE_SHADER = new ShaderParam(GameRenderer::getRendertypeOutlineShader);
        public static final ShaderParam RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER = new ShaderParam(GameRenderer::getRendertypeArmorEntityGlintShader);
        public static final ShaderParam RENDERTYPE_GLINT_TRANSLUCENT_SHADER = new ShaderParam(GameRenderer::getRendertypeGlintTranslucentShader);
        public static final ShaderParam RENDERTYPE_GLINT_SHADER = new ShaderParam(GameRenderer::getRendertypeGlintShader);
        public static final ShaderParam RENDERTYPE_ENTITY_GLINT_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityGlintShader);
        public static final ShaderParam RENDERTYPE_ENTITY_GLINT_DIRECT_SHADER = new ShaderParam(GameRenderer::getRendertypeEntityGlintDirectShader);
        public static final ShaderParam RENDERTYPE_CRUMBLING_SHADER = new ShaderParam(GameRenderer::getRendertypeCrumblingShader);
        public static final ShaderParam RENDERTYPE_TEXT_SHADER = new ShaderParam(GameRenderer::getRendertypeTextShader);
        public static final ShaderParam RENDERTYPE_TEXT_BACKGROUND_SHADER = new ShaderParam(GameRenderer::getRendertypeTextBackgroundShader);
        public static final ShaderParam RENDERTYPE_TEXT_INTENSITY_SHADER = new ShaderParam(GameRenderer::getRendertypeTextIntensityShader);
        public static final ShaderParam RENDERTYPE_TEXT_SEE_THROUGH_SHADER = new ShaderParam(GameRenderer::getRendertypeTextSeeThroughShader);
        public static final ShaderParam RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH_SHADER = new ShaderParam(GameRenderer::getRendertypeTextBackgroundSeeThroughShader);
        public static final ShaderParam RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH_SHADER = new ShaderParam(GameRenderer::getRendertypeTextIntensitySeeThroughShader);
        public static final ShaderParam RENDERTYPE_LIGHTNING_SHADER = new ShaderParam(GameRenderer::getRendertypeLightningShader);
        public static final ShaderParam RENDERTYPE_TRIPWIRE_SHADER = new ShaderParam(GameRenderer::getRendertypeTripwireShader);
        public static final ShaderParam RENDERTYPE_END_PORTAL_SHADER = new ShaderParam(GameRenderer::getRendertypeEndPortalShader);
        public static final ShaderParam RENDERTYPE_END_GATEWAY_SHADER = new ShaderParam(GameRenderer::getRendertypeEndGatewayShader);
        public static final ShaderParam RENDERTYPE_CLOUDS_SHADER = new ShaderParam(GameRenderer::getRendertypeCloudsShader);
        public static final ShaderParam RENDERTYPE_LINES_SHADER = new ShaderParam(GameRenderer::getRendertypeLinesShader);
        public static final ShaderParam RENDERTYPE_GUI_SHADER = new ShaderParam(GameRenderer::getRendertypeGuiShader);
        public static final ShaderParam RENDERTYPE_GUI_OVERLAY_SHADER = new ShaderParam(GameRenderer::getRendertypeGuiOverlayShader);
        public static final ShaderParam RENDERTYPE_GUI_TEXT_HIGHLIGHT_SHADER = new ShaderParam(GameRenderer::getRendertypeGuiTextHighlightShader);
        public static final ShaderParam RENDERTYPE_GUI_GHOST_RECIPE_OVERLAY_SHADER = new ShaderParam(GameRenderer::getRendertypeGuiGhostRecipeOverlayShader);
        public static final ShaderParam RENDERTYPE_BREEZE_WIND_SHADER = new ShaderParam(GameRenderer::getRendertypeBreezeWindShader);
    }
}
