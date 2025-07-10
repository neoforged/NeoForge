/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Data map value for {@linkplain NeoForgeDataMaps#ACCEPTABLE_VILLAGER_DISTANCES acceptable villager distances}.
 *
 * @param distance the acceptable distance between the hostile mob and a villager
 */
public record AcceptableVillagerDistance(float distance) {
    public static final Codec<AcceptableVillagerDistance> DISTANCE_CODEC = Codec.FLOAT
            .xmap(AcceptableVillagerDistance::new, AcceptableVillagerDistance::distance);

    public static final Codec<AcceptableVillagerDistance> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    Codec.FLOAT.fieldOf("acceptable_villager_distance").forGetter(AcceptableVillagerDistance::distance)).apply(in, AcceptableVillagerDistance::new)),
            DISTANCE_CODEC);
}
