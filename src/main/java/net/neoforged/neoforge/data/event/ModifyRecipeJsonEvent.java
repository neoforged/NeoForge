/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.data.event;

import com.google.gson.JsonElement;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

public class ModifyRecipeJsonEvent extends Event implements IModBusEvent {
    private final RegistryOps.RegistryInfoLookup registryInfoLookup;
    private final Map<Identifier, JsonElement> recipeJsons;

    public ModifyRecipeJsonEvent(final RegistryOps.RegistryInfoLookup registryInfoLookup, final Map<Identifier, JsonElement> recipeJsons) {
        this.registryInfoLookup = registryInfoLookup;
        this.recipeJsons = recipeJsons;
    }

    public Map<Identifier, JsonElement> getRecipeJsons() {
        return recipeJsons;
    }

    public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryKey) {
        return registryInfoLookup.lookup(registryKey);
    }

    public <T> RegistryOps.RegistryInfo<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> registryKey) {
        return registryInfoLookup.lookup(registryKey).orElseThrow();
    }
}
