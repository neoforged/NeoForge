/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.model.BlockStateModel;
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
}
