/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Custom block model definition to allow completely taking over the loading of a blockstate file
 */
public interface CustomBlockModelDefinition {
    /**
     * Instantiate this definition.
     *
     * @param states         The {@link StateDefinition} of the block this definition is being instantiated for
     * @param sourceSupplier A {@link Supplier} providing the source file and source pack name for debugging
     * @return a map of {@link BlockState}s to {@link BlockStateModel.UnbakedRoot}s for all states of the provided state definition
     *
     * @see BlockModelDefinition#instantiateVanilla(StateDefinition, Supplier)
     */
    Map<BlockState, BlockStateModel.UnbakedRoot> instantiate(StateDefinition<Block, BlockState> states, Supplier<String> sourceSupplier);

    MapCodec<? extends CustomBlockModelDefinition> codec();
}
