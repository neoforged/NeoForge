/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.npc.VillagerType;

/**
 * Data map value for {@linkplain NeoForgeDataMaps#BIOME_VILLAGERS biome villagers}.
 *
 * @param type the type of the villagers present in this biome
 */
public record BiomeVillager(VillagerType type) {
    public static final Codec<BiomeVillager> TYPE_CODEC = BuiltInRegistries.VILLAGER_TYPE.byNameCodec()
            .xmap(BiomeVillager::new, BiomeVillager::type);
    public static final Codec<BiomeVillager> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    BuiltInRegistries.VILLAGER_TYPE.byNameCodec().fieldOf("villager_type").forGetter(BiomeVillager::type)).apply(in, BiomeVillager::new)),
            TYPE_CODEC);
}
