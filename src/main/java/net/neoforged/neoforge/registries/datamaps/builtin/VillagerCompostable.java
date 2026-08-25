/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Data map value for {@linkplain NeoForgeDataMaps#VILLAGER_COMPOSTABLES compostables}.
 *
 * @param canVillagerCompost whether farmer villagers can compost the item
 */
public record VillagerCompostable(boolean canVillagerCompost) {
    public static final Codec<VillagerCompostable> BOOLEAN_CODEC = Codec.BOOL
            .xmap(VillagerCompostable::new, VillagerCompostable::canVillagerCompost);
    public static final Codec<VillagerCompostable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    Codec.BOOL.optionalFieldOf("can_villager_compost", false).forGetter(VillagerCompostable::canVillagerCompost)).apply(in, VillagerCompostable::new)),
            BOOLEAN_CODEC);
}
