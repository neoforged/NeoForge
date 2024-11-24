/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.data.models.BlockModelGenerators;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ModelProvider;
import org.apache.commons.lang3.StringUtils;

public interface IBlockModelGenerators {
    // TODO: reimplement these
    /*default BlockModelBuilder generateCustom(String modelPath, Consumer<BlockModelBuilder> action) {
        return generateCustom(modLocation(modelPath), action);
    }
    
    default BlockModelBuilder generateCustom(Block block, String suffix, Consumer<BlockModelBuilder> action) {
        return generateCustom(ModelLocationUtils.getModelLocation(block, suffix), action);
    }
    
    default BlockModelBuilder generateCustom(Block block, Consumer<BlockModelBuilder> action) {
        return generateCustom(ModelLocationUtils.getModelLocation(block), action);
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
    }*/
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
