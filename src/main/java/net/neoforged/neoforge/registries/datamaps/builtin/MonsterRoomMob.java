/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.random.Weight;

/**
 * Data map value for {@linkplain NeoForgeDataMaps#MONSTER_ROOM_MOBS monster room spawner mobs}.
 *
 * @param weight The weight that will be used for this type when selecting a type for the spawner
 */
public record MonsterRoomMob(Weight weight) {
    public static final Codec<MonsterRoomMob> WEIGHT_CODEC = Weight.CODEC
            .xmap(MonsterRoomMob::new, MonsterRoomMob::weight);
    public static final Codec<MonsterRoomMob> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    Weight.CODEC.fieldOf("weight").forGetter(MonsterRoomMob::weight)).apply(in, MonsterRoomMob::new)),
            WEIGHT_CODEC);
}
