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

public record Flattenable(Block flattened) {
    public static final Codec<Flattenable> FLATTENABLE_CODEC = BuiltInRegistries.BLOCK.byNameCodec().xmap(Flattenable::new, Flattenable::flattened);

    public static final Codec<Flattenable> CODEC = ExtraCodecs.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("flattened").forGetter(flattenable -> flattenable.flattened)).apply(in, Flattenable::new)),
            FLATTENABLE_CODEC);
}
