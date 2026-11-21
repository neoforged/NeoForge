/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.neoforged.neoforge.client.registries.NeoForgeClientRegistries;

/// Interface for modded [BlockStateModel.Unbaked] implementations.
///
/// The codecs must be registered via [NeoForgeClientRegistries#BLOCK_STATE_MODEL_TYPE].
public interface CustomUnbakedBlockStateModel extends BlockStateModel.Unbaked {
    /**
     * Returns the codec for this type of unbaked block state model.
     */
    MapCodec<? extends CustomUnbakedBlockStateModel> codec();
}
