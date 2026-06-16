/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.client.resources.model.cuboid.ItemModelGenerator;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextMap;
import org.jspecify.annotations.Nullable;

/**
 * Properties that NeoForge adds for {@link CuboidModel}s and {@link UnbakedModel}s.
 */
public final class NeoForgeModelProperties {
    private NeoForgeModelProperties() {}

    /**
     * Root transform. For block models, this can be specified under the {@code transform} JSON key.
     */
    public static final ContextKey<Transformation> TRANSFORM = ContextKey.vanilla("transform");

    /**
     * Part visibilities. For models with named parts (i.e. OBJ and composite), this can be specified under the {@code visibility} JSON key
     */
    public static final ContextKey<Map<String, Boolean>> PART_VISIBILITY = ContextKey.vanilla("part_visibility");

    /// Item layer model face data. For item layer models, the values can be specified in the entries of the {@code textures}
    /// object via the {@code neoforge_data} JSON key
    public static final ContextKey<Int2ObjectMap<ExtraFaceData>> ITEM_LAYER_FACE_DATA = ContextKey.vanilla("item_layer_face_data");

    /**
     * {@return a {@link Transformation} if the {@code transform} key is present, otherwise {@code null}}
     */
    @Nullable
    public static Transformation deserializeRootTransform(JsonObject jsonObject, JsonDeserializationContext context) {
        if (jsonObject.has("transform")) {
            JsonElement transform = jsonObject.get("transform");
            return context.deserialize(transform, Transformation.class);
        }
        return null;
    }

    /**
     * {@return a map of part visibilities if the {@code visibility} key is present, otherwise an empty map}
     */
    public static Map<String, Boolean> deserializePartVisibility(JsonObject jsonObject) {
        Map<String, Boolean> partVisibility = new HashMap<>();
        if (jsonObject.has("visibility")) {
            JsonObject visibility = GsonHelper.getAsJsonObject(jsonObject, "visibility");
            for (Map.Entry<String, JsonElement> part : visibility.entrySet()) {
                partVisibility.put(part.getKey(), part.getValue().getAsBoolean());
            }
        }
        return Map.copyOf(partVisibility);
    }

    /// {@return a map of item layer face data entries if the {@code neoforge_data} key is present on at least one entry of the {@code textures} object, otherwise an empty map}
    public static Int2ObjectMap<ExtraFaceData> deserializeItemLayerFaceData(JsonObject jsonObject) {
        if (!jsonObject.has("textures")) {
            return Int2ObjectMaps.emptyMap();
        }

        JsonObject textures = GsonHelper.getAsJsonObject(jsonObject, "textures");
        Int2ObjectMap<ExtraFaceData> layerData = new Int2ObjectOpenHashMap<>();
        for (int layerIndex = 0; layerIndex < ItemModelGenerator.LAYERS.size(); layerIndex++) {
            String key = ItemModelGenerator.LAYERS.get(layerIndex);
            if (!textures.has(key) || !textures.get(key).isJsonObject()) {
                continue;
            }

            JsonObject textureEntry = textures.getAsJsonObject(key);
            if (textureEntry.has("neoforge_data")) {
                JsonObject dataEntry = GsonHelper.getAsJsonObject(textureEntry, "neoforge_data");
                ExtraFaceData faceData = ExtraFaceData.read(dataEntry, ExtraFaceData.DEFAULT);
                if (!faceData.equals(ExtraFaceData.DEFAULT)) {
                    layerData.put(layerIndex, faceData);
                }
            }
        }
        return layerData.isEmpty() ? Int2ObjectMaps.emptyMap() : Int2ObjectMaps.unmodifiable(layerData);
    }

    /**
     * Puts the given {@linkplain Transformation root transform} into the given builder if present, overwriting any value specified in a parent model
     */
    public static void fillRootTransformProperty(ContextMap.Builder propertiesBuilder, @Nullable Transformation rootTransform) {
        if (rootTransform != null) {
            propertiesBuilder.withParameter(NeoForgeModelProperties.TRANSFORM, rootTransform);
        }
    }

    /**
     * Puts the given part visibility into the given builder if present, merging the with values from parent models
     * on a per-key basis and overwriting existing keys
     */
    public static void fillPartVisibilityProperty(ContextMap.Builder propertiesBuilder, Map<String, Boolean> partVisibility) {
        if (!partVisibility.isEmpty()) {
            Map<String, Boolean> visibility = propertiesBuilder.getOptionalParameter(NeoForgeModelProperties.PART_VISIBILITY);
            if (visibility != null) {
                visibility = new HashMap<>(visibility);
                visibility.putAll(partVisibility);
            } else {
                visibility = partVisibility;
            }
            visibility = Map.copyOf(visibility);
            propertiesBuilder.withParameter(NeoForgeModelProperties.PART_VISIBILITY, visibility);
        }
    }

    /// Puts the given item layer face data into the given builder if present, merging with values from parent models
    /// on a per-layer basis and overwriting existing layers
    public static void fillItemLayerFaceData(ContextMap.Builder propertiesBuilder, Int2ObjectMap<ExtraFaceData> layerFaceData) {
        if (!layerFaceData.isEmpty()) {
            Int2ObjectMap<ExtraFaceData> faceData = propertiesBuilder.getOptionalParameter(ITEM_LAYER_FACE_DATA);
            if (faceData != null) {
                faceData = new Int2ObjectOpenHashMap<>(faceData);
                faceData.putAll(layerFaceData);
                faceData = Int2ObjectMaps.unmodifiable(faceData);
            } else {
                faceData = layerFaceData;
            }
            propertiesBuilder.withParameter(ITEM_LAYER_FACE_DATA, faceData);
        }
    }

    /// Retrieves the [ExtraFaceData] for the given layer index from the given properties map
    public static ExtraFaceData getItemLayerFaceData(ContextMap additionalProperties, int layer) {
        return additionalProperties.getOrDefault(ITEM_LAYER_FACE_DATA, Int2ObjectMaps.emptyMap()).getOrDefault(layer, ExtraFaceData.DEFAULT);
    }
}
