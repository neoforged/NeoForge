/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.extensions;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public interface IBlockModelGeneratorsExtension {
    /**
     * Copied from {@link BlockModelGenerators#createScaffolding()} to allow generation for modded blocks.
     */
    default void createScaffolding(Block scaffolding) {
        var stableModel = ModelLocationUtils.getModelLocation(scaffolding, "_stable");
        var unstableModel = ModelLocationUtils.getModelLocation(scaffolding, "_unstable");
        self().registerSimpleItemModel(scaffolding, stableModel);
        self().blockStateOutput.accept(MultiVariantGenerator.multiVariant(scaffolding).with(BlockModelGenerators.createBooleanModelDispatch(BlockStateProperties.BOTTOM, unstableModel, stableModel)));
    }

    private BlockModelGenerators self() {
        return (BlockModelGenerators) this;
    }
}
