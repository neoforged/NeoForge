/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.event;

import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.network.registration.registrar.ConfigurationRegistration;
import net.neoforged.neoforge.network.registration.registrar.IPayloadRegistrar;
import net.neoforged.neoforge.network.registration.registrar.ModdedPacketRegistrar;
import net.neoforged.neoforge.network.registration.registrar.PlayRegistration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegisterPacketHandlerEvent extends Event implements IModBusEvent {
    
    private final Map<String, ModdedPacketRegistrar> registrarsByNamespace = Collections.synchronizedMap(new HashMap<>());
    
    public IPayloadRegistrar registrar(String namespace) {
        return registrarsByNamespace.computeIfAbsent(namespace, ModdedPacketRegistrar::new);
    }
    
    public Map<ResourceLocation, ConfigurationRegistration<?>> getConfigurationRegistrations() {
        final ImmutableMap.Builder<ResourceLocation, ConfigurationRegistration<?>> builder = ImmutableMap.builder();
        registrarsByNamespace.values().forEach(registrar -> registrar.getConfigurationRegistrations().forEach(builder::put));
        return builder.build();
    }
    
    public Map<ResourceLocation, PlayRegistration<?>> getPlayRegistrations() {
        final ImmutableMap.Builder<ResourceLocation, PlayRegistration<?>> builder = ImmutableMap.builder();
        registrarsByNamespace.values().forEach(registrar -> registrar.getPlayRegistrations().forEach(builder::put));
        return builder.build();
    }
}
