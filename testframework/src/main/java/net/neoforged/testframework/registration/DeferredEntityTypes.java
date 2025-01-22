/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.registration;

import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DeferredEntityTypes extends DeferredRegister.Entities {
    private final RegistrationHelper helper;

    public DeferredEntityTypes(String namespace, RegistrationHelper helper) {
        super(namespace);
        this.helper = helper;
    }

    @Override
    protected <I extends EntityType<?>> DeferredEntityTypeBuilder createHolder(ResourceKey<? extends Registry<EntityType<?>>> registryKey, ResourceLocation key) {
        return new DeferredEntityTypeBuilder(ResourceKey.create(registryKey, key), helper);
    }

    @Override
    public <E extends Entity> DeferredEntityTypeBuilder<E, EntityType<E>> registerEntityType(String name, EntityType.EntityFactory<E> factory, MobCategory category, UnaryOperator<EntityType.Builder<E>> builder) {
        return (DeferredEntityTypeBuilder<E, EntityType<E>>) super.registerEntityType(name, factory, category, builder);
    }
}
