/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

/**
 * Data map value for {@linkplain NeoForgeDataMaps#FURNACE_FUELS furnace fuels}.
 *
 * @param burnTime how long (in ticks) the item will burn for
 */
public record FurnaceFuel(int burnTime) {
    public static final Codec<FurnaceFuel> BURN_TIME_CODEC = ExtraCodecs.POSITIVE_INT
            .xmap(FurnaceFuel::new, FurnaceFuel::burnTime);
    public static final Codec<FurnaceFuel> CODEC = ExtraCodecs.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    ExtraCodecs.POSITIVE_INT.fieldOf("burn_time").forGetter(FurnaceFuel::burnTime)).apply(in, FurnaceFuel::new)),
            BURN_TIME_CODEC);
}
