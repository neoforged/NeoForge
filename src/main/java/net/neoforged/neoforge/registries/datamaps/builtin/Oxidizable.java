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

/// Data map value for [oxidizable blocks][NeoForgeDataMaps#OXIDIZABLES] allowing mods to easily register basic
/// oxidizing interactions for their blocks.
///
/// @param nextOxidationStage     The block that the key value will transform into when its oxidation stage changes
/// @param generateBlockTransform Whether a [BlockTransformer.BlockTransformData] for scraping off the oxidization should be generated automatically
public record Oxidizable(Block nextOxidationStage, boolean generateBlockTransform) {
    public static final Codec<Oxidizable> CODEC = RecordCodecBuilder.create(in -> in.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("next_oxidation_stage").forGetter(Oxidizable::nextOxidationStage),
            Codec.BOOL.optionalFieldOf("generate_block_transform", true).forGetter(Oxidizable::generateBlockTransform)).apply(in, Oxidizable::new));

    public Oxidizable(Block nextOxidationStage) {
        this(nextOxidationStage, true);
    }
}
