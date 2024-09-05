/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.Nullable;

/**
 * Special {@link DeferredHolder} for {@link EntityType EntityTypes} that implements {@link EntityTypeTest}.
 *
 * @param <TEntity> The specific {@link EntityType}.
 */
public class DeferredEntityType<TEntity extends Entity> extends DeferredHolder<EntityType<?>, EntityType<TEntity>> implements EntityTypeTest<Entity, TEntity> {
    protected DeferredEntityType(ResourceKey<EntityType<?>> key) {
        super(key);
    }

    @Nullable
    @Override
    public TEntity tryCast(Entity entity) {
        return value().tryCast(entity);
    }

    @Override
    public Class<? extends Entity> getBaseClass() {
        return value().getBaseClass();
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the specified {@link EntityType}.
     *
     * @param <TEntity>   The type of the target {@link EntityType}.
     * @param registryKey The resource key of the target {@link EntityType}.
     */
    public static <TEntity extends Entity> DeferredEntityType<TEntity> createEntityType(ResourceKey<EntityType<?>> registryKey) {
        return new DeferredEntityType<>(registryKey);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the {@link EntityType} with the specified name.
     *
     * @param <TEntity>    The type of the target {@link EntityType}.
     * @param registryName The name of the target {@link EntityType}.
     */
    public static <TEntity extends Entity> DeferredEntityType<TEntity> createEntityType(ResourceLocation registryName) {
        return createEntityType(ResourceKey.create(Registries.ENTITY_TYPE, registryName));
    }
}
