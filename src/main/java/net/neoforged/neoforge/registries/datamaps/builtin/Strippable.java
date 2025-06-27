/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public record Strippable(Block stripped) {
    public static final Codec<Strippable> BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec()
            .xmap(Strippable::new, Strippable::stripped);

    public static final Codec<Strippable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(inst -> inst.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("stripped").forGetter(Strippable::stripped)).apply(inst, Strippable::new)),
            BLOCK_CODEC);
}
