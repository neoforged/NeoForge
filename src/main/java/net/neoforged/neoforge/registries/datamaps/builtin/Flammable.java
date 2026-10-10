/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.FireBlock;

/// @param igniteOdds being the likelihood of a block to catch fire, typical values used by vanilla are [FireBlock#IGNITE_INSTANT],[FireBlock#IGNITE_EASY],[FireBlock#IGNITE_MEDIUM],[FireBlock#IGNITE_HARD]
/// @param burnOdds being the likelihood of a block to be destroyed by fire, typical values used by vanilla are [FireBlock#BURN_INSTANT],[FireBlock#BURN_EASY],[FireBlock#BURN_MEDIUM],[FireBlock#BURN_HARD]
public record Flammable(int igniteOdds, int burnOdds) {
    public static final Codec<Flammable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("ignite_odds").forGetter(Flammable::igniteOdds),
            Codec.INT.fieldOf("burn_odds").forGetter(Flammable::burnOdds))
            .apply(instance, Flammable::new));
}
