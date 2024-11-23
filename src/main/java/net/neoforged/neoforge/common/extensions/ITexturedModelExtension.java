/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

public interface ITexturedModelExtension {
    default TexturedModel withRenderType(String renderType) {
        return new TexturedModel(self().getMapping(), self().getTemplate().withRenderType(renderType));
    }

    default TexturedModel withRenderType(ResourceLocation renderType) {
        return new TexturedModel(self().getMapping(), self().getTemplate().withRenderType(renderType));
    }

    default TexturedModel withAmbientOcclusion(boolean ambientOcclusion) {
        return new TexturedModel(self().getMapping(), self().getTemplate().withAmbientOcclusion(ambientOcclusion));
    }

    default TexturedModel withGuiLight(BlockModel.GuiLight guiLight) {
        return new TexturedModel(self().getMapping(), self().getTemplate().withGuiLight(guiLight));
    }

    default TexturedModel withItemTransform(ItemDisplayContext displayContext, ItemTransform transform) {
        return new TexturedModel(self().getMapping(), self().getTemplate().withItemTransform(displayContext, transform));
    }

    private TexturedModel self() {
        return (TexturedModel) this;
    }

    interface Provider {
        default TexturedModel.Provider withRenderType(String renderType) {
            return block -> self().get(block).withRenderType(renderType);
        }

        default TexturedModel.Provider withRenderType(ResourceLocation renderType) {
            return block -> self().get(block).withRenderType(renderType);
        }

        default TexturedModel.Provider withAmbientOcclusion(boolean ambientOcclusion) {
            return block -> self().get(block).withAmbientOcclusion(ambientOcclusion);
        }

        default TexturedModel.Provider withGuiLight(BlockModel.GuiLight guiLight) {
            return block -> self().get(block).withGuiLight(guiLight);
        }

        default TexturedModel.Provider withItemTransform(ItemDisplayContext displayContext, ItemTransform transform) {
            return block -> self().get(block).withItemTransform(displayContext, transform);
        }

        private TexturedModel.Provider self() {
            return (TexturedModel.Provider) this;
        }
    }
}
