/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

public record Oxidizable(Block oxidizable) {
    public static final Codec<Oxidizable> OXIDIZABLE_BLOCK_CODEC = BuiltInRegistries.BLOCK.byNameCodec()
            .xmap(Oxidizable::new, Oxidizable::oxidizable);

    public static final Codec<Oxidizable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("oxidizable").forGetter(Oxidizable::oxidizable)).apply(in, Oxidizable::new)),
            OXIDIZABLE_BLOCK_CODEC);
}
