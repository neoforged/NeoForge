/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public class CompositeModelBuilder extends CustomLoaderBuilder {
    private final Map<String, Either<ResourceLocation, InlineChild>> childModels = new LinkedHashMap<>();
    private final List<String> itemRenderOrder = new ArrayList<>();

    public CompositeModelBuilder() {
        super(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "composite"), false);
    }

    /**
     * Add a child model by reference. The child model will be loaded from a separate file at the given location
     * 
     * @param name  The part name of the child
     * @param model The child model's path relative to the models folder
     */
    public CompositeModelBuilder child(String name, ResourceLocation model) {
        Preconditions.checkNotNull(name, "name must not be null");
        Preconditions.checkNotNull(model, "model must not be null");
        childModels.put(name, Either.left(model));
        itemRenderOrder.add(name);
        return this;
    }

    /**
     * Add an inline child model. The child model will be loaded from a nested object in the same JSON file
     *
     * @param name     The part name of the child
     * @param template The {@link ModelTemplate} to create the child model from
     * @param textures The {@link TextureMapping} this child model uses
     */
    public CompositeModelBuilder inlineChild(String name, ModelTemplate template, TextureMapping textures) {
        Preconditions.checkNotNull(name, "name must not be null");
        Preconditions.checkNotNull(template, "model template must not be null");
        Preconditions.checkNotNull(textures, "textures must not be null");
        childModels.put(name, Either.right(new InlineChild(template, textures)));
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
    protected CustomLoaderBuilder copyInternal() {
        CompositeModelBuilder builder = new CompositeModelBuilder();
        builder.childModels.putAll(this.childModels);
        builder.itemRenderOrder.addAll(this.itemRenderOrder);
        return builder;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        JsonObject children = new JsonObject();
        for (Map.Entry<String, Either<ResourceLocation, InlineChild>> entry : childModels.entrySet()) {
            entry.getValue()
                    .ifLeft(reference -> children.addProperty(entry.getKey(), reference.toString()))
                    .ifRight(inline -> serializeNestedTemplate(inline.template, inline.textures, inlineJson -> children.add(entry.getKey(), inlineJson)));
        }
        json.add("children", children);

        JsonArray itemRenderOrder = new JsonArray();
        for (String name : this.itemRenderOrder) {
            itemRenderOrder.add(name);
        }
        json.add("item_render_order", itemRenderOrder);

        return json;
    }

    private record InlineChild(ModelTemplate template, TextureMapping textures) {}
}
