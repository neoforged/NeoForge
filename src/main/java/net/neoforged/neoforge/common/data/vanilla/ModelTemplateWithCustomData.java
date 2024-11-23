/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.vanilla;

import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModelTemplateWithCustomData extends ModelTemplate {
    @Nullable
    private ResourceLocation renderType;

    public ModelTemplateWithCustomData(Optional<ResourceLocation> parent, Optional<String> pathSuffix, TextureSlot... requiredSlots) {
        super(parent, pathSuffix, requiredSlots);
    }

    public ModelTemplateWithCustomData(ModelTemplate template) {
        this(template.model, template.suffix, template.requiredSlots.toArray(TextureSlot[]::new));
    }

    @Override
    public ModelTemplate withRenderType(ResourceLocation renderType) {
        this.renderType = renderType;
        return this;
    }

    @Override
    public JsonObject createBaseTemplate(ResourceLocation modelPath, Map<TextureSlot, ResourceLocation> textureMap, @Nullable ExistingFileHelper fileHelper) {
        var json = super.createBaseTemplate(modelPath, textureMap, fileHelper);

        if (renderType != null) {
            json.addProperty("render_type", renderType.toString());
        }

        return json;
    }

    @Override
    public JsonObject createBaseTemplate(ResourceLocation modelPath, Map<TextureSlot, ResourceLocation> textureMap) {
        return createBaseTemplate(modelPath, textureMap, null);
    }
}
