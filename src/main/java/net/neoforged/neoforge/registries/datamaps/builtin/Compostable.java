/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Data map value for {@linkplain NeoForgeDataMaps#COMPOSTABLES compostables}.
 *
 * @param chance             the chance that a compost is successful
 * @param canVillagerCompost whether farmer villagers can compost the item
 */
public record Compostable(float chance, boolean canVillagerCompost) {
    public static final Codec<Compostable> CHANCE_CODEC = Codec.floatRange(0f, 1f)
            .xmap(val -> new Compostable(val, false), Compostable::chance);
    public static final Codec<Compostable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    Codec.floatRange(0f, 1f).fieldOf("chance").forGetter(Compostable::chance),
                    Codec.BOOL.optionalFieldOf("can_villager_compost", false).forGetter(Compostable::canVillagerCompost)).apply(in, Compostable::new)),
            CHANCE_CODEC);
}
