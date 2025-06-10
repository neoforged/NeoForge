/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record AcceptableHostileDistance(float distance) {
    public static final Codec<AcceptableHostileDistance> DISTANCE_CODEC = Codec.FLOAT
            .xmap(AcceptableHostileDistance::new, AcceptableHostileDistance::distance);

    public static final Codec<AcceptableHostileDistance> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                Codec.FLOAT.fieldOf("acceptable_hostile_distance").forGetter(AcceptableHostileDistance::distance)
            ).apply(in, AcceptableHostileDistance::new)), DISTANCE_CODEC
    );
}