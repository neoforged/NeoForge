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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.template.CustomLoaderBuilder;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

public class CompositeModelBuilder extends CustomLoaderBuilder {
    private final Map<String, ResourceLocation> childModels = new LinkedHashMap<>();
    private final List<String> itemRenderOrder = new ArrayList<>();

    public CompositeModelBuilder() {
        super(ResourceLocation.fromNamespaceAndPath(NeoForgeVersion.MOD_ID, "composite"), false);
    }

    public CompositeModelBuilder child(String name, ResourceLocation model) {
        Preconditions.checkNotNull(name, "name must not be null");
        Preconditions.checkNotNull(model, "model must not be null");
        childModels.put(name, model);
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
        for (Map.Entry<String, ResourceLocation> entry : childModels.entrySet()) {
            children.addProperty(entry.getKey(), entry.getValue().toString());
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
