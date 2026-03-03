/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import java.util.Map;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import org.jspecify.annotations.Nullable;

/**
 * Wrapper around all standard top-level model parameters added by vanilla and NeoForge except elements.
 * <p>
 * For use in custom model loaders which want to respect these properties but create the quads from
 * something other than the vanilla elements spec.
 */
public record StandardModelParameters(
        @Nullable Identifier parent,
        TextureSlots.Data textures,
        @Nullable ItemTransforms itemTransforms,
        @Nullable Boolean ambientOcclusion,
        UnbakedModel.@Nullable GuiLight guiLight,
        @Nullable Transformation rootTransform,
        Map<String, Boolean> partVisibility) {
    public static StandardModelParameters parse(JsonObject jsonObject, JsonDeserializationContext context) {
        String parentName = GsonHelper.getAsString(jsonObject, "parent", "");
        Identifier parent = parentName.isEmpty() ? null : Identifier.parse(parentName);

        TextureSlots.Data textures = TextureSlots.Data.EMPTY;
        if (jsonObject.has("textures")) {
            JsonObject jsonobject = GsonHelper.getAsJsonObject(jsonObject, "textures");
            textures = TextureSlots.parseTextureMap(jsonobject);
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
        Map<String, Boolean> partVisibility = NeoForgeModelProperties.deserializePartVisibility(jsonObject);

        return new StandardModelParameters(parent, textures, itemTransforms, ambientOcclusion, guiLight, rootTransform, partVisibility);
    }
}
