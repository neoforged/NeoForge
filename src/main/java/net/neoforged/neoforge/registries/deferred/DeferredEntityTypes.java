/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Specialized DeferredRegister for {@link EntityType EntityTypes} that uses the specialized {@link DeferredEntityType} as the return type for {@link #register}.
 */
public class DeferredEntityTypes extends DeferredRegister<EntityType<?>> {
    protected DeferredEntityTypes(String namespace) {
        super(Registries.ENTITY_TYPE, namespace);
    }

    @Override
    protected <TEntityType extends EntityType<?>> DeferredHolder<EntityType<?>, TEntityType> createHolder(ResourceKey<? extends Registry<EntityType<?>>> registryType, ResourceLocation registryName) {
        return (DeferredHolder<EntityType<?>, TEntityType>) DeferredEntityType.createEntityType(ResourceKey.create(registryType, registryName));
    }

    /**
     * Adds a new entity type to the list of entries to be registered and returns a {@link DeferredEntityType} that will be populated with the created entry automatically.
     *
     * @param identifier    The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory       A factory for the new entry. The factory should not cache the created entry.
     * @param category      A {@link MobCategory category} to be associated with this entry.
     * @param builderAction Action to be invoked with the builder during registration.
     * @return A {@link DeferredEntityType} that will track updates from the registry for this entry.
     */
    public <TEntity extends Entity> DeferredEntityType<TEntity> registerEntity(String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category, UnaryOperator<EntityType.Builder<TEntity>> builderAction) {
        return (DeferredEntityType<TEntity>) register(identifier, registryName -> builderAction.apply(EntityType.Builder.of(factory, category)).build(registryName.toString()));
    }

    /**
     * Adds a new entity type to the list of entries to be registered and returns a {@link DeferredEntityType} that will be populated with the created entry automatically.
     *
     * @param identifier The new entry's identifier. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param factory    A factory for the new entry. The factory should not cache the created entry.
     * @param category   A {@link MobCategory category} to be associated with this entry.
     * @return A {@link DeferredEntityType} that will track updates from the registry for this entry.
     */
    public <TEntity extends Entity> DeferredEntityType<TEntity> registerEntity(String identifier, EntityType.EntityFactory<TEntity> factory, MobCategory category) {
        return registerEntity(identifier, factory, category, UnaryOperator.identity());
    }

    /**
     * Factory for a specialized DeferredRegister for {@link EntityType EntityTypes}.
     *
     * @param namespace The namespace for all objects registered to this DeferredRegister
     */
    public static DeferredEntityTypes createEntityTypes(String namespace) {
        return new DeferredEntityTypes(namespace);
    }
}
