/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world;

import java.util.Collections;
import java.util.Set;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.MobSpawnSettings;
import org.jspecify.annotations.Nullable;

public class MobSpawnSettingsBuilder extends MobSpawnSettings.Builder {
    private final Set<MobCategory> typesView = Collections.unmodifiableSet(this.spawnsByCategory.keySet());
    private final Set<EntityType<?>> costView = Collections.unmodifiableSet(this.mobSpawnCosts.keySet());

    public MobSpawnSettingsBuilder(MobSpawnSettings orig) {
        orig.definedCategories().forEach(k -> spawnsByCategory.get(k).addAll(orig.getMobsInCategory(k)));
        orig.allSpawnCosts().keySet().forEach(k -> mobSpawnCosts.put(k, orig.getMobSpawnCost(k)));
    }

    public Set<MobCategory> getSpawnerTypes() {
        return this.typesView;
    }

    public WeightedList.Builder<MobSpawnSettings.SpawnerData> getSpawner(MobCategory type) {
        return this.spawnsByCategory.get(type);
    }

    public Set<EntityType<?>> getEntityTypes() {
        return this.costView;
    }

    public MobSpawnSettings.@Nullable MobSpawnCost getCost(EntityType<?> type) {
        return this.mobSpawnCosts.get(type);
    }

    public MobSpawnSettingsBuilder disablePlayerSpawn() {
        return this;
    }

    public MobSpawnSettingsBuilder removeSpawnCost(EntityType<?>... entityTypes) {
        for (EntityType<?> entityType : entityTypes) {
            this.mobSpawnCosts.remove(entityType);
        }
        return this;
    }
}
