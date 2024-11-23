/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Objects;
import net.minecraft.data.models.ModelProvider;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public interface IModelProviderExtension {
    default ModelFile getExistingModel(String modelPath) {
        return getExistingModel(mcLocation(modelPath));
    }

    default ModelFile getExistingModel(ResourceLocation modelPath) {
        // due to being in ModelProvider this is unaware of which model type the path is for
        // prepend either 'block/' or 'item/' or use helpers in 'BlockModelGenerators' or 'ItemModelGenerators'
        //
        // ExistingFileHelper is nullable for backwards compat with vanilla data gen
        // should never/rarely ever be null in modded data gen
        var fileHelper = Objects.requireNonNull(self().fileHelper, "Looking up models requires a nonnull ExistingFileHelper");
        return new ModelFile.ExistingModelFile(modelPath, fileHelper);
    }

    default ResourceLocation modLocation(String modelPath) {
        return ResourceLocation.fromNamespaceAndPath(self().modId, modelPath);
    }

    default ResourceLocation mcLocation(String modelPath) {
        return ResourceLocation.withDefaultNamespace(modelPath);
    }

    private ModelProvider self() {
        return (ModelProvider) this;
    }
}
