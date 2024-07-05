/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.entity.living;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.NaturalSpawner;

/**
 * This event is fired from {@link NaturalSpawner#spawnCategoryForPosition} when the spawning
 * system determines the maximum amount of the selected entity that can spawn at the same time.
 */
public class SpawnClusterSizeEvent extends LivingEvent {
    private int size;

    public SpawnClusterSizeEvent(Mob entity) {
        super(entity);
        this.size = entity.getMaxSpawnClusterSize();
    }

    /**
     * Gets the possibly event-modified max spawn cluster size for the entity.
     * <p>
     * To see the default size, use {@link Mob#getMaxSpawnClusterSize()}
     * 
     * @return The max spawn cluster size
     */
    public int getSize() {
        return size;
    }

    /**
     * Changes the max cluster size for the entity.
     * 
     * @param size The new size
     */
    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public Mob getEntity() {
        return (Mob) super.getEntity();
    }
}
