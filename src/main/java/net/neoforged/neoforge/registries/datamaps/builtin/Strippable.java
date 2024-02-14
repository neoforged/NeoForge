/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.Block;

/**
 * Data map value for {@linkplain NeoForgeDataMaps#STRIPPABLES strippable blocks}.
 *
 * @param strippedBlock the stripped variant of a block
 */
public record Strippable(Block strippedBlock) {
    public static final Codec<Strippable> STRIPPED_BLOCK_CODEC = BuiltInRegistries.BLOCK
            .byNameCodec().xmap(Strippable::new, Strippable::strippedBlock);

    public static final Codec<Strippable> CODEC = ExtraCodecs.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("stripped_variant")
                            .forGetter(Strippable::strippedBlock))
                    .apply(in, Strippable::new)),
            STRIPPED_BLOCK_CODEC);
}
