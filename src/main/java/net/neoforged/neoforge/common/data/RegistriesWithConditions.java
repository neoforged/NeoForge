/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.conditions.ICondition;

public record RegistriesWithConditions(HolderLookup.Provider registries, Map<ResourceKey<? extends Registry<?>>, Map<ResourceKey<?>, List<ICondition>>> conditionsByRegistry) implements HolderLookup.Provider {
    public RegistriesWithConditions(HolderLookup.Provider registries) {
        this(registries, Map.of());
    }

    @Override
    public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
        return this.registries.listRegistryKeys();
    }

    @Override
    public <T> Optional<? extends HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> key) {
        return this.registries.lookup(key);
    }
}
