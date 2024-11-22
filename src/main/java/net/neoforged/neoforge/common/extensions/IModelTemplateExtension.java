/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public interface IModelTemplateExtension {
    default ResourceLocation create(Block block, TextureMapping textures, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, @Nullable ExistingFileHelper fileHelper) {
        return create(ModelLocationUtils.getModelLocation(block, suffix().orElse("")), textures, modelOutput, fileHelper);
    }

    default ResourceLocation createWithSuffix(Block block, String suffix, TextureMapping textures, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, @Nullable ExistingFileHelper fileHelper) {
        return create(ModelLocationUtils.getModelLocation(block, suffix + suffix().orElse("")), textures, modelOutput, fileHelper);
    }

    default ResourceLocation createWithOverride(Block block, String suffix, TextureMapping textures, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, @Nullable ExistingFileHelper fileHelper) {
        return create(ModelLocationUtils.getModelLocation(block, suffix), textures, modelOutput, fileHelper);
    }

    default ResourceLocation create(ResourceLocation modelPath, TextureMapping textures, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, @Nullable ExistingFileHelper fileHelper) {
        return self().create(modelPath, textures, modelOutput, (path, output) -> createBaseTemplate(path, output, fileHelper));
    }

    default JsonObject createBaseTemplate(ResourceLocation modelPath, Map<TextureSlot, ResourceLocation> modelOutput, @Nullable ExistingFileHelper fileHelper) {
        var modelJson = new JsonObject();

        model().ifPresent(parentPath -> {
            modelJson.addProperty("parent", parentPath.toString());

            if (fileHelper != null) {
                Preconditions.checkState(fileHelper.exists(parentPath, ModelProvider.MODEL), "Model at %s does not exist", parentPath);
            }
        });

        if (!modelOutput.isEmpty()) {
            var texturesJson = new JsonObject();

            modelOutput.forEach((slot, texturePath) -> {
                texturesJson.addProperty(slot.getId(), texturePath.toString());

                if (fileHelper != null) {
                    Preconditions.checkState(fileHelper.exists(texturePath, ModelProvider.TEXTURE), "Texture %s does not exist in any known resource pack", texturePath);
                }
            });

            modelJson.add("textures", texturesJson);
        }

        return modelJson;
    }

    private ModelTemplate self() {
        return (ModelTemplate) this;
    }

    // TODO: Potentially replace these with AT
    Optional<ResourceLocation> model();

    Optional<String> suffix();
}
