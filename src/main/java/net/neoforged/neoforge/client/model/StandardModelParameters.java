/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import java.util.Map;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.RenderTypeGroup;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper around all standard top-level model parameters added by vanilla and NeoForge except elements.
 * <p>
 * For use in custom model loaders which want to respect these properties but create the quads from
 * something other than the vanilla elements spec.
 */
public record StandardModelParameters(
        @Nullable ResourceLocation parent,
        TextureSlots.Data textures,
        @Nullable ItemTransforms itemTransforms,
        @Nullable Boolean ambientOcclusion,
        @Nullable UnbakedModel.GuiLight guiLight,
        @Nullable Transformation rootTransform,
        RenderTypeGroup renderTypeGroup,
        Map<String, Boolean> partVisibility) {
    public static StandardModelParameters parse(JsonObject jsonObject, JsonDeserializationContext context) {
        String parentName = GsonHelper.getAsString(jsonObject, "parent", "");
        ResourceLocation parent = parentName.isEmpty() ? null : ResourceLocation.parse(parentName);

        TextureSlots.Data textures = TextureSlots.Data.EMPTY;
        if (jsonObject.has("textures")) {
            JsonObject jsonobject = GsonHelper.getAsJsonObject(jsonObject, "textures");
            textures = TextureSlots.parseTextureMap(jsonobject, TextureAtlas.LOCATION_BLOCKS);
        }

        ItemTransforms itemTransforms = null;
        if (jsonObject.has("display")) {
            JsonObject jsonobject1 = GsonHelper.getAsJsonObject(jsonObject, "display");
            itemTransforms = context.deserialize(jsonobject1, ItemTransforms.class);
        }

        Boolean ambientOcclusion = null;
        if (jsonObject.has("ambientocclusion")) {
            ambientOcclusion = GsonHelper.getAsBoolean(jsonObject, "ambientocclusion");
        }

        UnbakedModel.GuiLight guiLight = null;
        if (jsonObject.has("gui_light")) {
            guiLight = UnbakedModel.GuiLight.getByName(GsonHelper.getAsString(jsonObject, "gui_light"));
        }

        Transformation rootTransform = NeoForgeModelProperties.deserializeRootTransform(jsonObject, context);
        RenderTypeGroup renderTypeGroup = NeoForgeModelProperties.deserializeRenderType(jsonObject);
        Map<String, Boolean> partVisibility = NeoForgeModelProperties.deserializePartVisibility(jsonObject);

        return new StandardModelParameters(parent, textures, itemTransforms, ambientOcclusion, guiLight, rootTransform, renderTypeGroup, partVisibility);
    }
}
