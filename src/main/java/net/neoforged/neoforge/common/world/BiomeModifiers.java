/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import com.mojang.serialization.Codec;

import java.util.*;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.NeoForgeMod;

public final class BiomeModifiers {
    private BiomeModifiers() {
    } // Utility class.

    /**
     * <p>Stock biome modifier that adds features to biomes. Has the following json format:</p>
     *
     * <pre>
     * {
     *   "type": "neoforge:add_features", // required
     *   "biomes": "#namespace:your_biome_tag" // accepts a biome id, [list of biome ids], or #namespace:biome_tag
     *   "features": "namespace:your_feature", // accepts a placed feature id, [list of placed feature ids], or #namespace:feature_tag
     *   "step": "underground_ores" // accepts a Decoration enum name
     * }
     * </pre>
     *
     * <p>Be wary of using this to add vanilla PlacedFeatures to biomes, as doing so may cause a feature cycle violation.</p>
     *
     * @param biomes   Biomes to add features to.
     * @param features PlacedFeatures to add to biomes.
     * @param step     Decoration step to run features in.
     */
    public record AddFeaturesBiomeModifier(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features,
                                           Decoration step) implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD && this.biomes.contains(biome)) {
                BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
                this.features.forEach(holder -> generationSettings.addFeature(this.step, holder));
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return NeoForgeMod.ADD_FEATURES_BIOME_MODIFIER_TYPE.get();
        }
    }

    /**
     * <p>Stock biome modifier that removes features from biomes. Has the following json format:</p>
     *
     * <pre>
     * {
     *   "type": "neoforge:remove_features", // required
     *   "biomes": "#namespace:your_biome_tag", // accepts a biome id, [list of biome ids], or #namespace:biome_tag
     *   "features": "namespace:your_feature", // accepts a placed feature id, [list of placed feature ids], or #namespace:feature_tag
     *   "steps": "underground_ores" OR ["underground_ores", "vegetal_decoration"] // one or more decoration steps; optional field, defaults to all steps if not specified
     * }
     * </pre>
     *
     * @param biomes   Biomes to remove features from.
     * @param features PlacedFeatures to remove from biomes.
     * @param steps    Decoration steps to remove features from.
     */
    public record RemoveFeaturesBiomeModifier(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features,
                                              Set<Decoration> steps) implements BiomeModifier {
        /**
         * Creates a modifier that removes the given features from all decoration steps in the given biomes.
         *
         * @param biomes   Biomes to remove features from.
         * @param features PlacedFeatures to remove from biomes.
         */
        public static RemoveFeaturesBiomeModifier allSteps(HolderSet<Biome> biomes, HolderSet<PlacedFeature> features) {
            return new RemoveFeaturesBiomeModifier(biomes, features, EnumSet.allOf(Decoration.class));
        }

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.REMOVE && this.biomes.contains(biome)) {
                BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
                for (Decoration step : this.steps) {
                    generationSettings.getFeatures(step).removeIf(this.features::contains);
                }
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return NeoForgeMod.REMOVE_FEATURES_BIOME_MODIFIER_TYPE.get();
        }
    }

    /**
     * <p>Stock biome modifier that adds a mob spawn to a biome. Has the following json format:</p>
     *
     * <pre>
     * {
     *   "type": "neoforge:add_spawns", // Required
     *   "biomes": "#namespace:biome_tag", // Accepts a biome id, [list of biome ids], or #namespace:biome_tag
     *   "spawners":
     *   {
     *     "type": "namespace:entity_type", // Type of mob to spawn
     *     "weight": 100, // int, spawn weighting
     *     "minCount": 1, // int, minimum pack size
     *     "maxCount": 4, // int, maximum pack size
     *   }
     * }
     * </pre>
     *
     * <p>Optionally accepts a list of spawner objects instead of a single spawner:</p>
     *
     * <pre>
     * {
     *   "type": "neoforge:add_spawns", // Required
     *   "biomes": "#namespace:biome_tag", // Accepts a biome id, [list of biome ids], or #namespace:biome_tag
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
     * @param biomes   Biomes to add mob spawns to.
     * @param spawners List of SpawnerDatas specifying EntityType, weight, and pack size.
     */
    public record AddSpawnsBiomeModifier(HolderSet<Biome> biomes, List<SpawnerData> spawners) implements BiomeModifier {
        /**
         * Convenience method for using single spawn data.
         *
         * @param biomes  Biomes to add mob spawns to.
         * @param spawner SpawnerData specifying EntityTYpe, weight, and pack size.
         * @return AddSpawnsBiomeModifier that adds a single spawn entry to the specified biomes.
         */
        public static AddSpawnsBiomeModifier singleSpawn(HolderSet<Biome> biomes, SpawnerData spawner) {
            return new AddSpawnsBiomeModifier(biomes, List.of(spawner));
        }

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD && this.biomes.contains(biome)) {
                MobSpawnSettingsBuilder spawns = builder.getMobSpawnSettings();
                for (SpawnerData spawner : this.spawners) {
                    EntityType<?> type = spawner.type;
                    spawns.addSpawn(type.getCategory(), spawner);
                }
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return NeoForgeMod.ADD_SPAWNS_BIOME_MODIFIER_TYPE.get();
        }
    }

    /**
     * <p>Stock biome modifier that removes mob spawns from a biome. Has the following json format:</p>
     *
     * <pre>
     * {
     *   "type": "neoforge:add_spawns", // Required
     *   "biomes": "#namespace:biome_tag", // Accepts a biome id, [list of biome ids], or #namespace:biome_tag
     *   "entity_types": #namespace:entitytype_tag // Accepts an entity type, [list of entity types], or #namespace:entitytype_tag
     * }
     * </pre>
     *
     * @param biomes      Biomes to remove mob spawns from.
     * @param entityTypes EntityTypes to remove from spawn lists.
     */
    public record RemoveSpawnsBiomeModifier(HolderSet<Biome> biomes,
                                            HolderSet<EntityType<?>> entityTypes) implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.REMOVE && this.biomes.contains(biome)) {
                MobSpawnSettingsBuilder spawnBuilder = builder.getMobSpawnSettings();
                Arrays.stream(MobCategory.values()).forEach(category -> {
                    List<SpawnerData> spawns = spawnBuilder.getSpawner(category);
                    spawns.removeIf(spawnerData -> this.entityTypes.contains(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(spawnerData.type)));
                });
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return NeoForgeMod.REMOVE_SPAWNS_BIOME_MODIFIER_TYPE.get();
        }
    }

    /**
     * <p>Stock biome modifier at adds spawn costs to a biome. Has the following json format:</p>
     * <pre>
     * {
     *   "type": "neoforge:add_spawn_costs", // Required
     *   "biomes": "#namespace:biome_tag", // Accepts a biome id, [list of biome ids], or #namespace:biome_tag
     *   "entity_types": #namespace:entitytype_tag, // Accepts an entity type, [list of entity types], or #namespace:entitytype_tag
     *   "spawn_cost": {
     *     "energy_budget": 1.0, // double
     *     "charge": 1.0 // double
     *   }
     * }
     * </pre>
     *
     * @param biomes      Biomes to add spawn costs to.
     * @param entityTypes EntityTypes to add spawn costs for.
     * @param spawnCost   MobSpawnCost to add for those entity types.
     */
    public record AddSpawnCostsBiomeModifier(HolderSet<Biome> biomes, HolderSet<EntityType<?>> entityTypes,
                                             MobSpawnSettings.MobSpawnCost spawnCost) implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD) {
                MobSpawnSettingsBuilder spawnBuilder = builder.getMobSpawnSettings();
                entityTypes.forEach(entityType -> spawnBuilder.addMobCharge(entityType.value(), spawnCost.charge(), spawnCost.energyBudget()));
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return NeoForgeMod.ADD_SPAWN_COSTS_BIOME_MODIFIER.get();
        }
    }

    /**
     * <p>Stock biome modifier that removes mob spawn costs from a biome. Has the following json format:</p>
     * <pre>
     * {
     *   "type": "neoforge:remove_spawn_costs", // Required
     *   "biomes": "#namespace:biome_tag", // Accepts a biome id, [list of biome ids], or #namespace:biome_tag
     *   "entity_types": #namespace:entitytype_tag // Accepts an entity type, [list of entity types], or #namespace:entitytype_tag
     * }
     * </pre>
     *
     * @param biomes      Biomes to remove mob spawns from.
     * @param entityTypes EntityTypes to remove from spawn lists.
     */
    public record RemoveSpawnCostsBiomeModifier(HolderSet<Biome> biomes,
                                                HolderSet<EntityType<?>> entityTypes) implements BiomeModifier {
        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.REMOVE && this.biomes.contains(biome)) {
                MobSpawnSettingsBuilder spawnBuilder = builder.getMobSpawnSettings();
                entityTypes.forEach(entityType -> spawnBuilder.removeCost(entityType.value()));
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return NeoForgeMod.REMOVE_SPAWN_COSTS_BIOME_MODIFIER_TYPE.get();
        }
    }

    /**
     * <p>Stock biome modifier that adds carvers to biomes (from the configured_carver json registry). Has the following json format:</p>
     * <pre>
     * {
     *   "type": "neoforge:add_carvers", // required
     *   "biomes": "#namespace:your_biome_tag" // accepts a biome id, [list of biome ids], or #namespace:biome_tag
     *   "carvers": "namespace:your_carver", // accepts a configured carver id, [list of configured carver ids], or #namespace:carver_tag
     *   "step": "air" // Carving step, can be "air" or "liquid"
     * }
     * </pre>
     *
     * @param biomes  Biomes to add features to.
     * @param carvers ConfiguredWorldCarvers to add to biomes.
     * @param steps   Carving step to run features in.
     */
    public record AddCarversBiomeModifier(HolderSet<Biome> biomes, HolderSet<ConfiguredWorldCarver<?>> carvers,
                                          GenerationStep.Carving steps) implements BiomeModifier {

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD && this.biomes.contains(biome)) {
                var generationSettings = builder.getGenerationSettings();
                carvers.forEach(carver -> generationSettings.addCarver(steps, carver));
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return NeoForgeMod.ADD_CARVERS_BIOME_MODIFIER_TYPE.get();
        }
    }

    /**
     * <p>Stock biome modifier that removes carvers from biomes. Has the following json format:</p>
     * <pre>
     * {
     *   "type": "neoforge:remove_carvers", // required
     *   "biomes": "#namespace:your_biome_tag" // accepts a biome id, [list of biome ids], or #namespace:biome_tag
     *   "carvers": "namespace:your_carver", // accepts a configured carver id, [list of configured carver ids], or #namespace:carver_tag
     *   "step": "air" // Carving step, can be "air" or "liquid"
     * }
     * </pre>
     *
     * @param biomes  Biomes to add features to.
     * @param carvers ConfiguredWorldCarvers to add to biomes.
     * @param steps   Carving step to run features in.
     */
    public record RemoveCarversBiomeModifier(HolderSet<Biome> biomes, HolderSet<ConfiguredWorldCarver<?>> carvers,
                                             Set<GenerationStep.Carving> steps) implements BiomeModifier {

        public static RemoveCarversBiomeModifier allSteps(HolderSet<Biome> biomes, HolderSet<ConfiguredWorldCarver<?>> features) {
            return new RemoveCarversBiomeModifier(biomes, features, EnumSet.allOf(GenerationStep.Carving.class));
        }

        @Override
        public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
            if (phase == Phase.ADD && this.biomes.contains(biome)) {
                var generationSettings = builder.getGenerationSettings();
                steps.forEach(step -> generationSettings.getCarvers(step).removeIf(this.carvers::contains));
            }
        }

        @Override
        public Codec<? extends BiomeModifier> codec() {
            return NeoForgeMod.ADD_CARVERS_BIOME_MODIFIER_TYPE.get();
        }
    }
}
