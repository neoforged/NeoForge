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
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import org.apache.commons.lang3.StringUtils;

public interface IBlockModelGenerators {
    default BlockModelBuilder generateCustom(String modelPath, Consumer<BlockModelBuilder> action) {
        return generateCustom(modLocation(modelPath), action);
    }

    default BlockModelBuilder generateCustom(Block block, String suffix, Consumer<BlockModelBuilder> action) {
        return generateCustom(ModelLocationUtils.getModelLocation(block, suffix), action);
    }

    default BlockModelBuilder generateCustom(Block block, Consumer<BlockModelBuilder> action) {
        return generateCustom(ModelLocationUtils.getModelLocation(block), action);
    }

    default ModelFile getExistingModel(String modelPath) {
        return getExistingModel(mcLocation(modelPath));
    }

    default ModelFile getExistingModel(ResourceLocation modelPath) {
        // ExistingFileHelper is nullable for backwards compat with vanilla data gen
        // should never/rarely ever be null in modded data gen
        var fileHelper = Objects.requireNonNull(self().fileHelper, "Looking up models requires a nonnull ExistingFileHelper");
        var model = new ModelFile.ExistingModelFile(modelPath.withPath(IBlockModelGenerators::appendBlockFolder), fileHelper);
        model.assertExistence();
        return model;
    }

    default BlockModelBuilder generateCustom(ResourceLocation modelPath, Consumer<BlockModelBuilder> action) {
        // ExistingFileHelper is nullable for backwards compat with vanilla data gen
        // should never/rarely ever be null in modded data gen
        var fileHelper = Objects.requireNonNull(self().fileHelper, "Custom models require a nonnull ExistingFileHelper");
        var corrected = modelPath.withPath(IBlockModelGenerators::appendBlockFolder);
        var builder = new BlockModelBuilder(corrected, fileHelper);
        action.accept(builder);
        self().modelOutput.accept(corrected, builder::toJson);
        return builder;
    }

    default ResourceLocation modLocation(String modelPath) {
        return ResourceLocation.fromNamespaceAndPath(self().modId, modelPath).withPath(IBlockModelGenerators::appendBlockFolder);
    }

    default ResourceLocation mcLocation(String modelPath) {
        return ResourceLocation.withDefaultNamespace(modelPath).withPath(IBlockModelGenerators::appendBlockFolder);
    }

    private BlockModelGenerators self() {
        return (BlockModelGenerators) this;
    }

    static String appendBlockFolder(String path) {
        return StringUtils.prependIfMissing(path, ModelProvider.BLOCK_FOLDER + '/');
    }
}
