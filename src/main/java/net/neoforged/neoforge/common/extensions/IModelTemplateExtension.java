/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.function.Consumer;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.data.models.model.ModelTemplate;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.vanilla.ModelTemplateWithCustomData;

public interface IModelTemplateExtension {
    default ModelTemplate withRenderType(String renderType) {
        return withRenderType(ResourceLocation.withDefaultNamespace(renderType));
    }

    default ModelTemplate withRenderType(ResourceLocation renderType) {
        return withCustomData(customData -> customData.renderType = renderType);
    }

    default ModelTemplate withAmbientOcclusion(boolean ambientOcclusion) {
        return withCustomData(customData -> customData.ambientOcclusion = ambientOcclusion);
    }

    default ModelTemplate withGuiLight(BlockModel.GuiLight guiLight) {
        return withCustomData(customData -> customData.guiLight = guiLight);
    }

    private ModelTemplate self() {
        return (ModelTemplate) this;
    }

    default ModelTemplate withCustomData(Consumer<ModelTemplateWithCustomData> mutator) {
        var template = new ModelTemplateWithCustomData(self());
        mutator.accept(template);
        return template;
    }
}
