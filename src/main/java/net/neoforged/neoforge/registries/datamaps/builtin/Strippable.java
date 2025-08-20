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
 * Data map value for {@link NeoForgeDataMaps#STRIPPABLES strippable blocks}.
 * Note that the stripped block will inherit all properties of the unstripped block, given that they're present on the strippable block as well.
 *
 * @param strippedBlock the stripped block, as a result of being right-clicked by an axe
 */
public record Strippable(Block strippedBlock) {
    public static final Codec<Strippable> STRIPPED_BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec()
            .xmap(Strippable::new, Strippable::strippedBlock);

    public static final Codec<Strippable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(inst -> inst.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("stripped_block")
                            .forGetter(Strippable::strippedBlock))
                    .apply(inst, Strippable::new)),
            STRIPPED_BLOCK_CODEC);
}
