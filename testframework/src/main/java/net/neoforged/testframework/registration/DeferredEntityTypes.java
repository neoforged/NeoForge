/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.registration;

import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class DeferredEntityTypes extends net.neoforged.neoforge.registries.deferred.DeferredEntityTypes {
    private final RegistrationHelper helper;

    public DeferredEntityTypes(String namespace, RegistrationHelper helper) {
        super(namespace);
        this.helper = helper;
    }

    @Override
    protected <I extends EntityType<?>> DeferredEntityTypeBuilder createHolder(ResourceKey<? extends Registry<EntityType<?>>> registryType, ResourceLocation registryName) {
        return new DeferredEntityTypeBuilder(ResourceKey.create(registryType, registryName), helper);
    }

    public <E extends Entity> DeferredEntityTypeBuilder<E, EntityType<E>> registerType(String name, Supplier<EntityType.Builder<E>> sup) {
        return (DeferredEntityTypeBuilder<E, EntityType<E>>) super.register(name, () -> sup.get().build(name));
    }
}
