/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/**
 * Data map value for {@linkplain NeoForgeDataMaps#OXIDIZABLES oxidizable blocks} allowing mods to easily register basic
 * oxidizing interactions for their blocks.
 *
 * @param nextOxidationStage the block that the key value will transform into when its oxidation stage changes
 */
public record Oxidizable(Block nextOxidationStage) {
    public static final Codec<Oxidizable> OXIDIZABLE_CODEC = BuiltInRegistries.BLOCK.byNameCodec()
            .xmap(Oxidizable::new, Oxidizable::nextOxidationStage);
    public static final Codec<Oxidizable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("next_oxidation_stage").forGetter(Oxidizable::nextOxidationStage)).apply(in, Oxidizable::new)),
            OXIDIZABLE_CODEC);
}
