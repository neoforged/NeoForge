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
 * Data map value for {@link NeoForgeDataMaps#STRIPPABLES strippable blocks}.
 *
 * @param strippedVariant the loot table that the villager will hand out after a raid
 */
public record Strippable(Block strippedVariant) {
    public static final Codec<Strippable> STRIPPED_VARIANT_CODEC = BuiltInRegistries.BLOCK.byNameCodec()
            .xmap(Strippable::new, Strippable::strippedVariant);

    public static final Codec<Strippable> CODEC = ExtraCodecs.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("stripped_variant").forGetter(Strippable::strippedVariant))
                    .apply(in, Strippable::new)),
            STRIPPED_VARIANT_CODEC);
}
