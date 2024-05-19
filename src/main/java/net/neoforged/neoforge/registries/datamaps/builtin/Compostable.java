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
 * @param chance the chance that a compost is successful
 */
public record Compostable(float chance) {
    public static final Codec<Compostable> CHANCE_CODEC = Codec.floatRange(0f, 1f)
            .xmap(Compostable::new, Compostable::chance);
    public static final Codec<Compostable> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    Codec.floatRange(0f, 1f).fieldOf("chance").forGetter(Compostable::chance)).apply(in, Compostable::new)),
            CHANCE_CODEC);
}
