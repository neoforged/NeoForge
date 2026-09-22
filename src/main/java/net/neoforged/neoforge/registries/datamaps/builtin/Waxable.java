/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.BlockTransformer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/// Data map value for [waxable blocks][NeoForgeDataMaps#WAXABLES] allowing mods to easily register basic
/// waxing interactions for their blocks.
///
/// @param waxed                  The block that the key value will transform into when waxed with a honeycomb
/// @param generateBlockTransform Whether a [BlockTransformer.BlockTransformData] for removing the wax should be generated automatically
public record Waxable(Block waxed, boolean generateBlockTransform) {
    public static final Codec<Waxable> CODEC = RecordCodecBuilder.create(in -> in.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("waxed").forGetter(Waxable::waxed),
            Codec.BOOL.optionalFieldOf("generate_block_transform", true).forGetter(Waxable::generateBlockTransform)).apply(in, Waxable::new));

    public Waxable(Block waxed) {
        this(waxed, true);
    }
}
