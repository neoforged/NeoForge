/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.component.BlockTransformer;

/// Data map value for appending additional entries to a [BlockTransformer].
///
/// @param entries List of transformer entries to add
public record BlockTransformAppender(List<BlockTransformer.BlockTransformData> entries) {
    public static final Codec<BlockTransformAppender> CODEC = BlockTransformer.BlockTransformData.CODEC.listOf()
            .xmap(BlockTransformAppender::new, BlockTransformAppender::entries);
}
