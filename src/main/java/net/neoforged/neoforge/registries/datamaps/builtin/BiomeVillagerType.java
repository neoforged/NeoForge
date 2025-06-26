/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.datamaps.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerType;

/**
 * Data map value for {@linkplain NeoForgeDataMaps#VILLAGER_TYPES biome villager types}.
 *
 * @param type the type of the villagers present in this biome
 */
public record BiomeVillagerType(ResourceKey<VillagerType> type) {
    public static final Codec<BiomeVillagerType> TYPE_CODEC = ResourceKey.codec(Registries.VILLAGER_TYPE)
            .xmap(BiomeVillagerType::new, BiomeVillagerType::type);
    public static final Codec<BiomeVillagerType> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(in -> in.group(
                    ResourceKey.codec(Registries.VILLAGER_TYPE).fieldOf("villager_type").forGetter(BiomeVillagerType::type)).apply(in, BiomeVillagerType::new)),
            TYPE_CODEC);
}
