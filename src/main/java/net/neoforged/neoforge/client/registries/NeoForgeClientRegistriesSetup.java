/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.registries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NeoForgeClientRegistriesSetup {
    private static boolean setup = false;

    public static synchronized void setup(IEventBus modEventBus) {
        if (setup)
            throw new IllegalStateException("Setup has already been called!");

        setup = true;

        modEventBus.addListener(NeoForgeClientRegistriesSetup::registerRegistries);
    }

    private static void registerRegistries(NewRegistryEvent event) {
        event.register(NeoForgeClientRegistries.UNBAKED_MODEL_SERIALIZERS);
    }
}
