/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelReader;

public interface ILevelReaderExtension {
    default LevelReader self() {
        return (LevelReader) this;
    }

    /**
     * Returns a holder for the key using this levels registry access.
     *
     * @throws IllegalStateException if the registry is missing or the key is not present in the registry
     */
    default <T> Holder.Reference<T> holderOrThrow(ResourceKey<T> key) {
        return self().registryAccess().registryOrThrow(key.registryKey()).getHolderOrThrow(key);
    }

    /**
     * Returns a optional holder for the key using this levels registry access.
     */
    default <T> Optional<Holder.Reference<T>> holder(ResourceKey<T> key) {
        Optional<Registry<T>> registry = self().registryAccess().registry(key.registryKey());
        return registry.flatMap(ts -> ts.getHolder(key));
    }
}
