/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class TrimmableArmorModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> TrimmableArmorModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new TrimmableArmorModelBuilder<>(parent, existingFileHelper);
    }

    private T untrimmed;
    private T trimmed;
    private String textureTarget;
    private ResourceLocation textureBase;
    private final Map<String, T> materialOverrides = new LinkedHashMap<>();

    protected TrimmableArmorModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(new ResourceLocation("neoforge:trimmable_armor"), parent, existingFileHelper, false);
    }

    public TrimmableArmorModelBuilder<T> untrimmed(T modelBuilder) {
        Preconditions.checkNotNull(modelBuilder, "modelBuilder must not be null");
        untrimmed = modelBuilder;
        return this;
    }

    public TrimmableArmorModelBuilder<T> trimmed(T modelBuilder, String textureTarget, ResourceLocation textureBase) {
        Preconditions.checkNotNull(modelBuilder, "modelBuilder must not be null");
        Preconditions.checkNotNull(textureTarget, "textureTarget must not be null");
        Preconditions.checkNotNull(textureBase, "textureBase must not be null");
        trimmed = modelBuilder;
        this.textureTarget = textureTarget;
        this.textureBase = textureBase;
        return this;
    }

    public TrimmableArmorModelBuilder<T> override(String assetName, T override) {
        Preconditions.checkNotNull(assetName, "assetName must not be null");
        Preconditions.checkNotNull(override, "override must not be null");
        Preconditions.checkArgument(ResourceLocation.isValidPath(assetName), "Invalid asset name: " + assetName);
        materialOverrides.put(assetName, override);
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        Preconditions.checkNotNull(untrimmed, "untrimmed must not be null");
        Preconditions.checkNotNull(trimmed, "trimmed must not be null");

        json = super.toJson(json);

        JsonObject trimmedJson = new JsonObject();

        trimmedJson.add("model", trimmed.toJson());
        trimmedJson.addProperty("target_texture", textureTarget);
        trimmedJson.addProperty("base_asset", textureBase.toString());

        if (!materialOverrides.isEmpty()) {
            JsonObject overrideJson = new JsonObject();
            for (Map.Entry<String, T> entry : materialOverrides.entrySet()) {
                overrideJson.add(entry.getKey(), entry.getValue().toJson());
            }

            trimmedJson.add("overrides", overrideJson);
        }

        json.add("base", untrimmed.toJson());
        json.add("trimmed", trimmedJson);

        return json;
    }
}
