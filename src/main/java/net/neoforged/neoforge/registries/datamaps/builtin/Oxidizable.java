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
 * @param nextOxidizedStage The block that will the key value will transform into when it's oxidization stage changes
 */
public record Oxidizable(Block nextOxidizedStage) {
    public static final Codec<Oxidizable> OXIDIZABLE_CODEC = BuiltInRegistries.BLOCK.byNameCodec()
            .xmap(Oxidizable::new, Oxidizable::nextOxidizedStage);
    public static final Codec<Oxidizable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    BuiltInRegistries.BLOCK.byNameCodec().fieldOf("nextOxidizedStage").forGetter(Oxidizable::nextOxidizedStage)).apply(in, Oxidizable::new)),
            OXIDIZABLE_CODEC);
}
