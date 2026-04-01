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

public class ModifyJsonDataEvent extends Event implements IModBusEvent {
    private final String prefix;
    private final String extension;
    private final RegistryOps.RegistryInfoLookup registryInfoLookup;
    private final Map<Identifier, JsonElement> jsonData;

    public ModifyJsonDataEvent(final String prefix, final String extension, final RegistryOps.RegistryInfoLookup registryInfoLookup, final Map<Identifier, JsonElement> jsons) {
        this.prefix = prefix;
        this.extension = extension;
        this.registryInfoLookup = registryInfoLookup;
        this.jsonData = jsons;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getExtension() {
        return extension;
    }

    public Map<Identifier, JsonElement> getJsonData() {
        return jsonData;
    }

    public <T> Optional<RegistryOps.RegistryInfo<T>> lookup(ResourceKey<? extends Registry<? extends T>> registryKey) {
        return registryInfoLookup.lookup(registryKey);
    }

    public <T> RegistryOps.RegistryInfo<T> lookupOrThrow(ResourceKey<? extends Registry<? extends T>> registryKey) {
        return registryInfoLookup.lookup(registryKey).orElseThrow();
    }
}
