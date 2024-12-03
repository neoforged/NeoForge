/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ExtendedModelTemplate;
import org.jetbrains.annotations.Nullable;

public class SeparateTransformsModelBuilder extends CustomLoaderBuilder {
    public static SeparateTransformsModelBuilder begin(ExtendedModelTemplate.Builder parent) {
        return new SeparateTransformsModelBuilder(parent);
    }

    @Nullable
    private ModelTemplate base;
    @Nullable
    private TextureMapping baseTextures;
    private final Map<String, ModelTemplate> childModels = new LinkedHashMap<>();
    private final Map<String, TextureMapping> childTextures = new LinkedHashMap<>();

    protected SeparateTransformsModelBuilder(ExtendedModelTemplate.Builder parent) {
        super(ResourceLocation.fromNamespaceAndPath("neoforge", "separate_transforms"), parent, false);
    }

    public SeparateTransformsModelBuilder base(ModelTemplate modelBuilder, TextureMapping textures) {
        Preconditions.checkNotNull(modelBuilder, "modelBuilder must not be null");
        Preconditions.checkNotNull(textures, "textures must not be null");
        base = modelBuilder;
        baseTextures = textures;
        return this;
    }

    public SeparateTransformsModelBuilder perspective(ItemDisplayContext perspective, ModelTemplate modelBuilder, TextureMapping textures) {
        Preconditions.checkNotNull(perspective, "layer must not be null");
        Preconditions.checkNotNull(modelBuilder, "modelBuilder must not be null");
        Preconditions.checkNotNull(textures, "textures must not be null");
        childModels.put(perspective.getSerializedName(), modelBuilder);
        childTextures.put(perspective.getSerializedName(), textures);
        return this;
    }

    @Override
    protected CustomLoaderBuilder copyInternal(ExtendedModelTemplate.Builder owner) {
        SeparateTransformsModelBuilder builder = new SeparateTransformsModelBuilder(owner);
        builder.base = this.base;
        builder.childModels.putAll(this.childModels);
        this.childTextures.forEach((name, textures) -> builder.childTextures.put(name, textures.copy()));
        return builder;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        JsonObject root = super.toJson(json);

        if (base != null && baseTextures != null) {
            CustomLoaderBuilder.serializeNestedTemplate(base, baseTextures, baseJson -> root.add("bool", baseJson));
        }

        JsonObject parts = new JsonObject();
        for (Map.Entry<String, ModelTemplate> entry : childModels.entrySet()) {
            CustomLoaderBuilder.serializeNestedTemplate(entry.getValue(), childTextures.get(entry.getKey()), child -> parts.add(entry.getKey(), child));
        }
        root.add("perspectives", parts);

        return root;
    }
}
