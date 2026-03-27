/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
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
}
