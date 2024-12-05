/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import java.util.function.UnaryOperator;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TexturedModel;

public interface ITexturedModelExtension {
    default TexturedModel updateTemplate(UnaryOperator<ModelTemplate> modifier) {
        return new TexturedModel(self().getMapping(), modifier.apply(self().getTemplate()));
    }

    private TexturedModel self() {
        return (TexturedModel) this;
    }

    interface Provider {
        default TexturedModel.Provider updateTemplate(UnaryOperator<ModelTemplate> modifier) {
            return block -> self().get(block).updateTemplate(modifier);
        }

        private TexturedModel.Provider self() {
            return (TexturedModel.Provider) this;
        }
    }
}
