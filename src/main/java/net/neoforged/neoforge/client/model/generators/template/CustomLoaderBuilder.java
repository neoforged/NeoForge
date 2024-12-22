/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.template;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;

public abstract class CustomLoaderBuilder {
    private static final ResourceLocation DUMMY = ResourceLocation.fromNamespaceAndPath("dummy", "dummy");

    protected final ResourceLocation loaderId;
    protected final Map<String, Boolean> visibility = new LinkedHashMap<>();
    protected final boolean allowInlineElements;
    private boolean optional = false;

    /**
     * @param loaderId            The ID of the associated {@link UnbakedModelLoader}
     * @param allowInlineElements Whether the loader supports inline vanilla elements and as such can fall back to vanilla loading
     *                            with some degradation if the loader does not exist and is marked as optional in the model
     */
    protected CustomLoaderBuilder(ResourceLocation loaderId, boolean allowInlineElements) {
        this.loaderId = loaderId;
        this.allowInlineElements = allowInlineElements;
    }

    public CustomLoaderBuilder visibility(String partName, boolean show) {
        Preconditions.checkNotNull(partName, "partName must not be null");
        this.visibility.put(partName, show);
        return this;
    }

    /**
     * Mark the custom loader as optional for this model to allow it to be loaded through vanilla paths
     * if the loader is not present
     */
    public CustomLoaderBuilder optional() {
        Preconditions.checkState(allowInlineElements, "Only loaders with support for inline elements can be marked as optional");
        this.optional = true;
        return this;
    }

    final CustomLoaderBuilder copy() {
        CustomLoaderBuilder builder = copyInternal();
        builder.visibility.putAll(this.visibility);
        builder.optional = this.optional;
        return builder;
    }

    /**
     * Returns a deep-copy of this builder in order to maintain the immutability nature of {@link ModelTemplate}.
     */
    protected abstract CustomLoaderBuilder copyInternal();

    public JsonObject toJson(JsonObject json) {
        if (optional) {
            JsonObject loaderObj = new JsonObject();
            loaderObj.addProperty("id", loaderId.toString());
            loaderObj.addProperty("optional", true);
            json.add("loader", loaderObj);
        } else {
            json.addProperty("loader", loaderId.toString());
        }

        if (!visibility.isEmpty()) {
            JsonObject visibilityObj = new JsonObject();

            for (Map.Entry<String, Boolean> entry : visibility.entrySet()) {
                visibilityObj.addProperty(entry.getKey(), entry.getValue());
            }

            json.add("visibility", visibilityObj);
        }

        return json;
    }

    /**
     * Serializes the given {@link ModelTemplate}/{@link TextureMapping} pair inline using the provided {@code consumer}
     */
    protected static void serializeNestedTemplate(ModelTemplate template, TextureMapping textures, Consumer<JsonElement> consumer) {
        template.create(DUMMY, textures, (id, jsonSup) -> consumer.accept(jsonSup.get()));
    }
}
