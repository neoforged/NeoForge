/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.vanilla.ModelTemplateWithCustomData;

public interface IModelTemplateExtension {
    default ModelTemplate withRenderType(ResourceLocation renderType) {
        return new ModelTemplateWithCustomData(self(), renderType);
    }

    private ModelTemplate self() {
        return (ModelTemplate) this;
    }
}
