/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.vanilla;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Map;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ModelTemplateWithCustomData extends ModelTemplate {
    @Nullable
    public ResourceLocation renderType = null;
    @Nullable
    public Boolean ambientOcclusion = null; // BlockModel.DEFAULT_AMBIENT_OCCLUSION
    @Nullable
    public BlockModel.GuiLight guiLight = null;
    public Map<ItemDisplayContext, ItemTransform> transforms = Maps.newHashMap();

    public ModelTemplateWithCustomData(ModelTemplate template) {
        super(template.model, template.suffix, template.requiredSlots.toArray(TextureSlot[]::new));

        if (template instanceof ModelTemplateWithCustomData customData) {
            renderType = customData.renderType;
            ambientOcclusion = customData.ambientOcclusion;
            guiLight = customData.guiLight;
            transforms = Maps.newHashMap(customData.transforms);
        }
    }

    @Override
    public JsonObject createBaseTemplate(ResourceLocation modelPath, Map<TextureSlot, ResourceLocation> textureMap, @Nullable ExistingFileHelper fileHelper) {
        var json = super.createBaseTemplate(modelPath, textureMap, fileHelper);

        if (renderType != null) {
            json.addProperty("render_type", renderType.toString());
        }

        if (ambientOcclusion != null) {
            json.addProperty("ambientocclusion", ambientOcclusion);
        }

        if (guiLight != null) {
            json.addProperty("gui_light", guiLight.getSerializedName());
        }

        var transformsJson = toJson(transforms);

        if (!transformsJson.isEmpty()) {
            json.add("display", transformsJson);
        }

        return json;
    }

    @Override
    public JsonObject createBaseTemplate(ResourceLocation modelPath, Map<TextureSlot, ResourceLocation> textureMap) {
        return createBaseTemplate(modelPath, textureMap, null);
    }

    public static JsonObject toJson(ItemTransform transform) {
        var json = new JsonObject();
        var hasRightRotation = !transform.rightRotation.equals(ItemTransform.Deserializer.DEFAULT_ROTATION);

        if (!transform.translation.equals(ItemTransform.Deserializer.DEFAULT_TRANSLATION))
            json.add("translation", toJson(transform.translation));
        if (!transform.rotation.equals(ItemTransform.Deserializer.DEFAULT_ROTATION))
            json.add(hasRightRotation ? "left_rotation" : "rotation", toJson(transform.rotation));
        if (!transform.scale.equals(ItemTransform.Deserializer.DEFAULT_SCALE))
            json.add("scale", toJson(transform.scale));
        if (hasRightRotation)
            json.add("right_rotation", toJson(transform.rightRotation));

        return json;
    }

    public static JsonObject toJson(Map<ItemDisplayContext, ItemTransform> transforms) {
        var json = new JsonObject();

        transforms.forEach((context, transform) -> {
            if (transform.equals(ItemTransform.NO_TRANSFORM))
                return;

            var transformJson = toJson(transform);

            if (!transformJson.isEmpty())
                json.add(context.getSerializedName(), transformJson);
        });

        return json;
    }

    public static JsonArray toJson(Vector3f vec) {
        var array = new JsonArray();
        array.add(vec.x());
        array.add(vec.y());
        array.add(vec.z());
        return array;
    }
}
