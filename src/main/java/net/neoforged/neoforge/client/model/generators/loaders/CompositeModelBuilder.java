/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ExtendedModelTemplate;

public class CompositeModelBuilder extends CustomLoaderBuilder {
    public static CompositeModelBuilder begin(ExtendedModelTemplate.Builder parent) {
        return new CompositeModelBuilder(parent);
    }

    private final Map<String, ModelTemplate> childModels = new LinkedHashMap<>();
    private final Map<String, TextureMapping> childTextures = new LinkedHashMap<>();
    private final List<String> itemRenderOrder = new ArrayList<>();

    protected CompositeModelBuilder(ExtendedModelTemplate.Builder parent) {
        super(ResourceLocation.fromNamespaceAndPath("neoforge", "composite"), parent, false);
    }

    public CompositeModelBuilder child(String name, ModelTemplate modelBuilder, TextureMapping textures) {
        Preconditions.checkNotNull(name, "name must not be null");
        Preconditions.checkNotNull(modelBuilder, "modelBuilder must not be null");
        Preconditions.checkNotNull(textures, "textures must not be null");
        childModels.put(name, modelBuilder);
        childTextures.put(name, textures);
        itemRenderOrder.add(name);
        return this;
    }

    public CompositeModelBuilder itemRenderOrder(String... names) {
        Preconditions.checkNotNull(names, "names must not be null");
        Preconditions.checkArgument(names.length > 0, "names must contain at least one element");
        for (String name : names)
            if (!childModels.containsKey(name))
                throw new IllegalArgumentException("names contains \"" + name + "\", which is not a child of this model");
        itemRenderOrder.clear();
        itemRenderOrder.addAll(Arrays.asList(names));
        return this;
    }

    @Override
    protected CustomLoaderBuilder copyInternal(ExtendedModelTemplate.Builder owner) {
        CompositeModelBuilder builder = new CompositeModelBuilder(owner);
        builder.childModels.putAll(this.childModels);
        this.childTextures.forEach((name, textures) -> builder.childTextures.put(name, textures.copy()));
        builder.itemRenderOrder.addAll(this.itemRenderOrder);
        return builder;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        JsonObject children = new JsonObject();
        for (Map.Entry<String, ModelTemplate> entry : childModels.entrySet()) {
            CustomLoaderBuilder.serializeNestedTemplate(entry.getValue(), childTextures.get(entry.getKey()), child -> children.add(entry.getKey(), child));
        }
        json.add("children", children);

        JsonArray itemRenderOrder = new JsonArray();
        for (String name : this.itemRenderOrder) {
            itemRenderOrder.add(name);
        }
        json.add("item_render_order", itemRenderOrder);

        return json;
    }
}
