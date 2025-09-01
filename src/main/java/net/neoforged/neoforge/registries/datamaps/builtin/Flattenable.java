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
 * Data map value for {@link NeoForgeDataMaps#FLATTENABLES flattenable blocks}.
 *
 * @param flattenedBlock the flattened block, as a result of being right-clicked by a shovel.
 */
public record Flattenable(Block flattenedBlock) {
    public static final Codec<Flattenable> FLATTENED_BLOCK_CODEC =
            BuiltInRegistries.BLOCK.byNameCodec().xmap(Flattenable::new, Flattenable::flattenedBlock);

    public static final Codec<Flattenable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(
                    instance -> instance.group(
                            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("flattened_block")
                                    .forGetter(Flattenable::flattenedBlock)
                    ).apply(instance, Flattenable::new)
            ), FLATTENED_BLOCK_CODEC
    );
}
