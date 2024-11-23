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
    default ModelFile getExistingModel(ResourceLocation modelPath) {
        // ExistingFileHelper is nullable for backwards compat with vanilla data gen
        // should never/rarely ever be null in modded data gen
        var fileHelper = Objects.requireNonNull(self().fileHelper, "Looking up models requires a nonnull ExistingFileHelper");
        return new ModelFile.ExistingModelFile(modelPath, fileHelper);
    }

    private ModelProvider self() {
        return (ModelProvider) this;
    }
}
