/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.buffer.param;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.renderer.CoreShaders;
import net.minecraft.client.renderer.ShaderProgram;

public record ShaderParam(Optional<Supplier<ShaderProgram>> shaderSupplier) implements IBufferDefinitionParam<Optional<Supplier<ShaderProgram>>> {

    public ShaderParam(Supplier<ShaderProgram> shaderSupplier) {
        this(Optional.of(shaderSupplier));
    }

    public ShaderParam() {
        this(Optional.empty());
    }

    @Override
    public Optional<Supplier<ShaderProgram>> getValue() {
        return shaderSupplier;
    }

    @Override
    public IBufferDefinitionParamType<?, ?> getType() {
        return BufferDefinitionParamTypeManager.SHADER;
    }
    public static final class Vanilla {
        public static final ShaderParam NO_SHADER = new ShaderParam();
        public static final ShaderParam POSITION_COLOR_LIGHTMAP_SHADER = new ShaderParam(() -> CoreShaders.POSITION_COLOR_LIGHTMAP);
        public static final ShaderParam POSITION_SHADER = new ShaderParam(() -> CoreShaders.POSITION);
        public static final ShaderParam POSITION_TEX_SHADER = new ShaderParam(() -> CoreShaders.POSITION_TEX);
        public static final ShaderParam POSITION_COLOR_TEX_LIGHTMAP_SHADER = new ShaderParam(() -> CoreShaders.POSITION_COLOR_TEX_LIGHTMAP);
        public static final ShaderParam POSITION_COLOR_SHADER = new ShaderParam(() -> CoreShaders.POSITION_COLOR);
        public static final ShaderParam RENDERTYPE_SOLID_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_SOLID);
        public static final ShaderParam RENDERTYPE_CUTOUT_MIPPED_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_CUTOUT_MIPPED);
        public static final ShaderParam RENDERTYPE_CUTOUT_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_CUTOUT);
        public static final ShaderParam RENDERTYPE_TRANSLUCENT_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_TRANSLUCENT);
        public static final ShaderParam RENDERTYPE_TRANSLUCENT_MOVING_BLOCK_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_TRANSLUCENT_MOVING_BLOCK);
        public static final ShaderParam RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ARMOR_CUTOUT_NO_CULL);
        public static final ShaderParam RENDERTYPE_ENTITY_SOLID_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_SOLID);
        public static final ShaderParam RENDERTYPE_ENTITY_CUTOUT_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_CUTOUT);
        public static final ShaderParam RENDERTYPE_ENTITY_CUTOUT_NO_CULL_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_CUTOUT_NO_CULL);
        public static final ShaderParam RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_CUTOUT_NO_CULL_Z_OFFSET);
        public static final ShaderParam RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL);
        public static final ShaderParam RENDERTYPE_ENTITY_TRANSLUCENT_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_TRANSLUCENT);
        public static final ShaderParam RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE);
        public static final ShaderParam RENDERTYPE_ENTITY_SMOOTH_CUTOUT_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_SMOOTH_CUTOUT);
        public static final ShaderParam RENDERTYPE_BEACON_BEAM_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_BEACON_BEAM);
        public static final ShaderParam RENDERTYPE_ENTITY_DECAL_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_DECAL);
        public static final ShaderParam RENDERTYPE_ENTITY_NO_OUTLINE_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_NO_OUTLINE);
        public static final ShaderParam RENDERTYPE_ENTITY_SHADOW_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_SHADOW);
        public static final ShaderParam RENDERTYPE_ENTITY_ALPHA_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_ALPHA);
        public static final ShaderParam RENDERTYPE_EYES_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_EYES);
        public static final ShaderParam RENDERTYPE_ENERGY_SWIRL_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENERGY_SWIRL);
        public static final ShaderParam RENDERTYPE_LEASH_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_LEASH);
        public static final ShaderParam RENDERTYPE_WATER_MASK_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_WATER_MASK);
        public static final ShaderParam RENDERTYPE_OUTLINE_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_OUTLINE);
        public static final ShaderParam RENDERTYPE_ARMOR_ENTITY_GLINT_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ARMOR_ENTITY_GLINT);
        public static final ShaderParam RENDERTYPE_GLINT_TRANSLUCENT_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_GLINT_TRANSLUCENT);
        public static final ShaderParam RENDERTYPE_GLINT_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_GLINT);
        public static final ShaderParam RENDERTYPE_ENTITY_GLINT_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_ENTITY_GLINT);
        public static final ShaderParam RENDERTYPE_CRUMBLING_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_CRUMBLING);
        public static final ShaderParam RENDERTYPE_TEXT_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_TEXT);
        public static final ShaderParam RENDERTYPE_TEXT_BACKGROUND_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_TEXT_BACKGROUND);
        public static final ShaderParam RENDERTYPE_TEXT_INTENSITY_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_TEXT_INTENSITY);
        public static final ShaderParam RENDERTYPE_TEXT_SEE_THROUGH_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_TEXT_SEE_THROUGH);
        public static final ShaderParam RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_TEXT_BACKGROUND_SEE_THROUGH);
        public static final ShaderParam RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_TEXT_INTENSITY_SEE_THROUGH);
        public static final ShaderParam RENDERTYPE_LIGHTNING_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_LIGHTNING);
        public static final ShaderParam RENDERTYPE_TRIPWIRE_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_TRIPWIRE);
        public static final ShaderParam RENDERTYPE_END_PORTAL_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_END_PORTAL);
        public static final ShaderParam RENDERTYPE_END_GATEWAY_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_END_GATEWAY);
        public static final ShaderParam RENDERTYPE_CLOUDS_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_CLOUDS);
        public static final ShaderParam RENDERTYPE_LINES_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_LINES);
        public static final ShaderParam RENDERTYPE_GUI_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_GUI);
        public static final ShaderParam RENDERTYPE_GUI_OVERLAY_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_GUI_OVERLAY);
        public static final ShaderParam RENDERTYPE_GUI_TEXT_HIGHLIGHT_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_GUI_TEXT_HIGHLIGHT);
        public static final ShaderParam RENDERTYPE_GUI_GHOST_RECIPE_OVERLAY_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_GUI_GHOST_RECIPE_OVERLAY);
        public static final ShaderParam RENDERTYPE_BREEZE_WIND_SHADER = new ShaderParam(() -> CoreShaders.RENDERTYPE_BREEZE_WIND);
    }
}
