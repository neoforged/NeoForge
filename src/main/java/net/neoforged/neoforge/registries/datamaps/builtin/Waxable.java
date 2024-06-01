/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public record Waxable(Block waxable) {
    public static final Codec<Waxable> WAXABLE_BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec()
            .xmap(Waxable::new, Waxable::waxable);

    public static final Codec<Waxable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("waxable").forGetter(Waxable::waxable)).apply(in, Waxable::new)),
            WAXABLE_BLOCK_CODEC);
}
