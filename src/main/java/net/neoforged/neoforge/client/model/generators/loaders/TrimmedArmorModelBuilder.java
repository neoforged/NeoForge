/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.generators.loaders;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class TrimmedArmorModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {
    public static <T extends ModelBuilder<T>> TrimmedArmorModelBuilder<T> begin(T parent, ExistingFileHelper existingFileHelper) {
        return new TrimmedArmorModelBuilder<>(parent, existingFileHelper);
    }

    private @Nullable ModelBuilder<?> baseModel;
    private @Nullable ResourceLocation baseTrimTexture;

    protected TrimmedArmorModelBuilder(T parent, ExistingFileHelper existingFileHelper) {
        super(ResourceLocation.fromNamespaceAndPath("neoforge", "trimmed_armor"), parent, existingFileHelper, false);
    }

    public TrimmedArmorModelBuilder<T> baseModel(ModelBuilder<?> baseModel) {
        Preconditions.checkNotNull(baseModel, "Base model must not be null");
        this.baseModel = baseModel;
        return this;
    }

    public TrimmedArmorModelBuilder<T> baseTrimTexture(ResourceLocation baseTrimTexture) {
        Preconditions.checkNotNull(baseTrimTexture, "Base trim texture must not be null");
        this.baseTrimTexture = baseTrimTexture;
        return this;
    }

    @Override
    public JsonObject toJson(JsonObject json) {
        json = super.toJson(json);

        Preconditions.checkNotNull(baseModel, "Base model must not be null");

        json.add("base_model", baseModel.toJson());

        Preconditions.checkNotNull(baseTrimTexture, "Base trim texture must not be null");

        json.addProperty("base_trim_texture", baseTrimTexture.toString());

        return json;
    }
}
