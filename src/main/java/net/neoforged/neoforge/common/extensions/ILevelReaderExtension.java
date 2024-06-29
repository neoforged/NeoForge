/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;

public interface ILevelReaderExtension extends RegistryAccess {
    default LevelReader self() {
        return (LevelReader) this;
    }

    default boolean isAreaLoaded(BlockPos center, int range) {
        return self().hasChunksAt(center.offset(-range, -range, -range), center.offset(range, range, range));
    }

    /**
     * Delegates the implementation of RegistryAccess to enable modders to access HolderLookup.Provider methods
     * directly without traversing through level.registryAccess().
     */

    @Override
    default <E> Optional<Registry<E>> registry(ResourceKey<? extends Registry<? extends E>> p_123085_) {
        return self().registryAccess().registry(p_123085_);
    }

    @Override
    default Stream<RegistryEntry<?>> registries() {
        return self().registryAccess().registries();
    }
}
