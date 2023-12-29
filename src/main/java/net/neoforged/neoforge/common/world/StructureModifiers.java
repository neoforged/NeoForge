/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

import java.util.List;
import java.util.Set;

public final class StructureModifiers {
    private StructureModifiers() {
    } //Utility class

    /**
     * <p>Stock structure modifier that adds a mob spawn override to a structure.
     * If a structure has mob spawn overrides, random mob spawning will use the structure's spawns instead of the local biome's spawns.
     * Has the following json format:</p>
     * <pre>
     * {
     *   "type": "forge:add_spawns", // Required
     *   "structures": "#namespace:structure_tag", // Accepts a structure id, [list of structure ids], or #namespace:structure_tag
     *   "spawners":
     *   {
     *     "type": "namespace:entity_type", // Type of mob to spawn
     *     "weight": 100, // int, spawn weighting
     *     "minCount": 1, // int, minimum pack size
     *     "maxCount": 4, // int, maximum pack size
     *   }
     * }
     * </pre>
     * <p>Optionally accepts a list of spawner objects instead of a single spawner:</p>
     * <pre>
     * {
     *   "type": "forge:add_spawns", // Required
     *   "structure": "#namespace:structure_tag", // Accepts a structure id, [list of structure ids], or #namespace:structure_tag
     *   "spawners":
     *   [
     *     {
     *       "type": "namespace:entity_type", // Type of mob to spawn
     *       "weight": 100, // int, spawn weighting
     *       "minCount": 1, // int, minimum pack size
     *       "maxCount": 4, // int, maximum pack size
     *     },
     *     {
     *       // additional spawner object
     *     }
     *   ]
     * }
     * </pre>
     *
     * @param structures Structures to add mob spawns to.
     * @param spawners   List of {@linkplain SpawnerData}'s specifying EntityType, weight, and pack size.
     */

    public record AddSpawnsStructureModifier(HolderSet<Structure> structures,
                                             List<SpawnerData> spawners) implements StructureModifier {

        /**
         * Convenience method for using a single spawn data.
         *
         * @param structures Structures to add mob spawns to.
         * @param spawner    SpawnerData specifying EntityTYpe, weight, and pack size.
         * @return AddSpawnsStructureModifier that adds a single spawn entry to the specified biomes.
         */
        public static AddSpawnsStructureModifier singleSpawn(HolderSet<Structure> structures, SpawnerData spawner) {
            return new AddSpawnsStructureModifier(structures, List.of(spawner));
        }

        @Override
        public void modify(Holder<Structure> structure, Phase phase, ModifiableStructureInfo.StructureInfo.Builder builder) {
            if (phase == Phase.ADD && this.structures.contains(structure)) {
                StructureSettingsBuilder settingsBuilder = builder.getStructureSettings();
                spawners.forEach(spawner -> {
                    var category = spawner.type.getCategory();
                    settingsBuilder.getOrAddSpawnOverrides(category).addSpawn(spawner);
                });
            }
        }

        @Override
        public Codec<? extends StructureModifier> codec() {
            return NeoForgeMod.ADD_SPAWNS_STRUCTURE_MODIFIER_TYPE.get();
        }
    }

    /**
     * <p>Stock structure modifier that removes mob spawns from a structure.
     * Does not remove the override list itself;
     * see {@link ClearSpawnsStructureModifier} to remove override lists completely.
     * </p>
     * Has the following json format:</p>
     * <pre>
     * {
     *   "type": "forge:remove_spawns", // Required
     *   "structures": "#namespace:structure_tag", // Accepts a structure id, [list of structure ids], or #namespace:structure_tag
     *   "entity_types": #namespace:entitytype_tag // Accepts an entity type, [list of entity types], or #namespace:entitytype_tag
     * }
     * </pre>
     *
     * @param structures Biomes to remove mob spawns from.
     * @param entityTypes EntityTypes to remove from spawn lists.
     */
    public record RemoveSpawnsStructureModifier(HolderSet<Structure> structures, HolderSet<EntityType<?>> entityTypes) implements StructureModifier
    {
        @Override
        public void modify(Holder<Structure> structure, Phase phase, ModifiableStructureInfo.StructureInfo.Builder builder)
        {
            if (phase == Phase.REMOVE && this.structures.contains(structure))
            {
                StructureSettingsBuilder settingsBuilder = builder.getStructureSettings();
                for (MobCategory category : MobCategory.values())
                {
                    var overrides = settingsBuilder.getSpawnOverrides(category);
                    if (overrides == null || overrides.getSpawns().isEmpty())
                        continue;

                    overrides.getSpawns().forEach(spawnerData -> {
                        var key = ResourceKey.create(Registries.ENTITY_TYPE, new ResourceLocation(NeoForgeVersion.MOD_ID, BuiltInRegistries.ENTITY_TYPE.getKey(spawnerData.type).getPath()));
                        this.entityTypes.contains(BuiltInRegistries.ENTITY_TYPE.getHolder(key).get());
                    });
                }
            }
        }

        @Override
        public Codec<? extends StructureModifier> codec()
        {
            return NeoForgeMod.REMOVE_SPAWNS_STRUCTURE_MODIFIER_TYPE.get();
        }
    }

    /**
     * <p>Stock biome modifier that removes mob spawns from a structure modifier.
     * Does not remove the override list itself;
     * see {@link ClearSpawnsStructureModifier} to remove override lists completely.
     * </p>
     * Has the following json format:</p>
     * <pre>
     * {
     *   "type": "forge:clear_spawns", // Required
     *   "structures": "#namespace:structure_tag", // Accepts a structure id, [list of structure ids], or #namespace:structure_tag
     *   "categories": "monster" OR ["monster", "creature"] // Optional, one or more {@link MobCategory}s; defaults to all categories if not specified.
     * }
     * </pre>
     *
     * @param structures Structures to remove mob spawn overrides from.
     * @param categories Set of mob categories to remove spawn overrides for.
     */
    public record ClearSpawnsStructureModifier(HolderSet<Structure> structures, Set<MobCategory> categories) implements StructureModifier
    {
        @Override
        public void modify(Holder<Structure> structure, Phase phase, ModifiableStructureInfo.StructureInfo.Builder builder)
        {
            if (phase == Phase.REMOVE && this.structures.contains(structure))
            {
                StructureSettingsBuilder settingsBuilder = builder.getStructureSettings();
                for (MobCategory category : this.categories)
                {
                    settingsBuilder.removeSpawnOverrides(category);
                }
            }
        }

        @Override
        public Codec<? extends StructureModifier> codec()
        {
            return NeoForgeMod.CLEAR_SPAWNS_STRUCTURE_MODIFIER_TYPE.get();
        }

    }
}
