/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.resources.ResourceLocation;

public interface IModelProviderExtension {
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
