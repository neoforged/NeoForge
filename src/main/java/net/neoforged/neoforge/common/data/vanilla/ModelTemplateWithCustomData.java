/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.vanilla;

import com.google.gson.JsonObject;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class ModelTemplateWithCustomData extends ModelTemplate {
    @Nullable
    public ResourceLocation renderType = null;
    @Nullable
    public Boolean ambientOcclusion = null; // BlockModel.DEFAULT_AMBIENT_OCCLUSION
    @Nullable
    public BlockModel.GuiLight guiLight = null;

    public ModelTemplateWithCustomData(ModelTemplate template) {
        super(template.model, template.suffix, template.requiredSlots.toArray(TextureSlot[]::new));

        if (template instanceof ModelTemplateWithCustomData customData) {
            renderType = customData.renderType;
            ambientOcclusion = customData.ambientOcclusion;
            guiLight = customData.guiLight;
        }
    }

    @Override
    public JsonObject createBaseTemplate(ResourceLocation modelPath, Map<TextureSlot, ResourceLocation> textureMap, @Nullable ExistingFileHelper fileHelper) {
        var json = super.createBaseTemplate(modelPath, textureMap, fileHelper);

        if (renderType != null) {
            json.addProperty("render_type", renderType.toString());
        }

        if (ambientOcclusion != null) {
            json.addProperty("ambientocclusion", ambientOcclusion.toString());
        }

        if (guiLight != null) {
            json.addProperty("gui_light", guiLight.toString());
        }

        return json;
    }

    @Override
    public JsonObject createBaseTemplate(ResourceLocation modelPath, Map<TextureSlot, ResourceLocation> textureMap) {
        return createBaseTemplate(modelPath, textureMap, null);
    }
}
