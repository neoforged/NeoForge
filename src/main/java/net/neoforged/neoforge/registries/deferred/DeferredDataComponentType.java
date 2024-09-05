/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Special {@link DeferredHolder} for {@link DataComponentType DataComponentTypes}.
 *
 * @param <TComponent> The specific data type.
 */
public class DeferredDataComponentType<TComponent> extends DeferredHolder<DataComponentType<?>, DataComponentType<TComponent>> {
    protected DeferredDataComponentType(ResourceKey<DataComponentType<?>> key) {
        super(key);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the specified {@link DataComponentType}.
     *
     * @param <TComponent> The type of the target {@link DataComponentType}.
     * @param registryKey  The resource key of the target {@link DataComponentType}.
     */
    public static <TComponent extends BlockEntity> DeferredDataComponentType<TComponent> createDataComponentType(ResourceKey<DataComponentType<?>> registryKey) {
        return new DeferredDataComponentType<>(registryKey);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the {@link DataComponentType} with the specified name.
     *
     * @param <TComponent> The type of the target {@link DataComponentType}.
     * @param registryName The name of the target {@link DataComponentType}.
     */
    public static <TComponent extends BlockEntity> DeferredDataComponentType<TComponent> createDataComponentType(ResourceLocation registryName) {
        return createDataComponentType(ResourceKey.create(Registries.DATA_COMPONENT_TYPE, registryName));
    }
}
