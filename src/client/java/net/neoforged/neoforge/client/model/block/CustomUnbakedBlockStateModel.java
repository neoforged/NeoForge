/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;

/**
 * Interface for modded {@link BlockStateModel.Unbaked} implementations.
 *
 * <p>The codecs must be registered in {@link RegisterBlockStateModels}.
 */
public interface CustomUnbakedBlockStateModel extends BlockStateModel.Unbaked {
    /**
     * Returns the codec for this type of unbaked block state model.
     */
    MapCodec<? extends CustomUnbakedBlockStateModel> codec();

    /**
     * Tries to return a copy of this unbaked model with the given mutator applied.
     *
     * @throws UnsupportedOperationException If this custom unbaked model does not have anything it can apply the variant mutator to.
     */
    default CustomUnbakedBlockStateModel with(VariantMutator mutator) {
        throw new UnsupportedOperationException("This unbaked block state model (" + getClass() + ") does not support applying variant mutators.");
    }
}
