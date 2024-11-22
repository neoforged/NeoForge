/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import com.google.gson.JsonElement;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public interface ITexturedModelExtension {
    default ResourceLocation create(Block block, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, @Nullable ExistingFileHelper fileHelper) {
        return self().getTemplate().create(block, self().getMapping(), modelOutput, fileHelper);
    }

    default ResourceLocation createWithSuffix(Block block, String suffix, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, @Nullable ExistingFileHelper fileHelper) {
        return self().getTemplate().createWithSuffix(block, suffix, self().getMapping(), modelOutput, fileHelper);
    }

    private TexturedModel self() {
        return (TexturedModel) this;
    }

    interface Provider {
        default ResourceLocation create(Block block, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, @Nullable ExistingFileHelper fileHelper) {
            return self().get(block).create(block, modelOutput, fileHelper);
        }

        default ResourceLocation createWithSuffix(Block block, String suffix, BiConsumer<ResourceLocation, Supplier<JsonElement>> modelOutput, @Nullable ExistingFileHelper fileHelper) {
            return self().get(block).createWithSuffix(block, suffix, modelOutput, fileHelper);
        }

        private TexturedModel.Provider self() {
            return (TexturedModel.Provider) this;
        }
    }
}
