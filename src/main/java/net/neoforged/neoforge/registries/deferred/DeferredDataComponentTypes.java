/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import java.util.function.UnaryOperator;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Specialized DeferredRegister for {@link DataComponentType DataComponentTypes} that uses the specialized {@link DeferredDataComponentType} as the return type for {@link #register}.
 */
public class DeferredDataComponentTypes extends DeferredRegister<DataComponentType<?>> {
    protected DeferredDataComponentTypes(String namespace) {
        super(Registries.DATA_COMPONENT_TYPE, namespace);
    }

    @Override
    protected <TDataComponentType extends DataComponentType<?>> DeferredHolder<DataComponentType<?>, TDataComponentType> createHolder(ResourceKey<? extends Registry<DataComponentType<?>>> registryType, ResourceLocation registryName) {
        return (DeferredHolder<DataComponentType<?>, TDataComponentType>) DeferredDataComponentType.createDataComponentType(ResourceKey.create(registryType, registryName));
    }

    /**
     * Convenience method that constructs a builder for use in the operator. Use this to avoid inference issues.
     *
     * @param identifier    The identifier for this data component type. It will automatically have the {@linkplain #getNamespace() namespace} prefixed.
     * @param builderAction The unary operator, which is passed a new builder for user operations, then builds it upon registration.
     * @return A {@link DeferredDataComponentType} which reflects the data that will be registered.
     */
    public <TComponent> DeferredDataComponentType<TComponent> registerComponentType(String identifier, UnaryOperator<DataComponentType.Builder<TComponent>> builderAction) {
        return (DeferredDataComponentType<TComponent>) register(identifier, () -> builderAction.apply(DataComponentType.builder()).build());
    }

    /**
     * Factory for a specialized DeferredRegister for {@link DataComponentType DataComponentTypes}.
     *
     * @param namespace The namespace for all objects registered to this DeferredRegister
     */
    public static DeferredDataComponentTypes createDataComponentTypes(String namespace) {
        return new DeferredDataComponentTypes(namespace);
    }
}
