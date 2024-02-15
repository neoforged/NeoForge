/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

/**
 * Data map value for {@linkplain NeoForgeDataMaps#VIBRATION_FREQUENCIES vibration frequencies}.
 *
 * @param frequency the vibration frequency of the game event
 */
public record VibrationFrequency(int frequency) {
    public static final Codec<VibrationFrequency> FREQUENCY_CODEC = Codec.intRange(1, 15)
            .xmap(VibrationFrequency::new, VibrationFrequency::frequency);
    public static final Codec<VibrationFrequency> CODEC = ExtraCodecs.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    Codec.intRange(1, 15).fieldOf("frequency").forGetter(VibrationFrequency::frequency)).apply(in, VibrationFrequency::new)),
            FREQUENCY_CODEC);
}
