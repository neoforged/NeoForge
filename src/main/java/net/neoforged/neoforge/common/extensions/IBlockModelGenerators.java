/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;

public interface IBlockModelGenerators {
    default BlockModelBuilder generateCustom(String modelPath, Consumer<BlockModelBuilder> action) {
        return generateCustom(ModelLocationUtils.decorateBlockModelLocation(modelPath), action);
    }

    default BlockModelBuilder generateCustom(Block block, String suffix, Consumer<BlockModelBuilder> action) {
        return generateCustom(ModelLocationUtils.getModelLocation(block, suffix), action);
    }

    default BlockModelBuilder generateCustom(Block block, Consumer<BlockModelBuilder> action) {
        return generateCustom(ModelLocationUtils.getModelLocation(block), action);
    }

    default ModelFile getExistingModel(String modelPath) {
        // ExistingFileHelper is nullable for backwards compat with vanilla data gen
        // should never/rarely ever be null in modded data gen
        var fileHelper = Objects.requireNonNull(self().fileHelper, "Looking up models requires a nonnull ExistingFileHelper");
        return new ModelFile.ExistingModelFile(ModelLocationUtils.decorateBlockModelLocation(modelPath), fileHelper);
    }

    private BlockModelBuilder generateCustom(ResourceLocation modelPath, Consumer<BlockModelBuilder> action) {
        // ExistingFileHelper is nullable for backwards compat with vanilla data gen
        // should never/rarely ever be null in modded data gen
        var fileHelper = Objects.requireNonNull(self().fileHelper, "Custom models require a nonnull ExistingFileHelper");
        var builder = new BlockModelBuilder(modelPath, fileHelper);
        self().modelOutput.accept(modelPath, builder::toJson);
        return builder;
    }

    private BlockModelGenerators self() {
        return (BlockModelGenerators) this;
    }
}
