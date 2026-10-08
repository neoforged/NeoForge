/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Flammable(int flammability, int fireSpreadSpeed) {
    public static final Codec<Flammable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("flammability").forGetter(Flammable::flammability),
            Codec.INT.fieldOf("fireSpreadSpeed").forGetter(Flammable::fireSpreadSpeed)).apply(instance, Flammable::new));
}
