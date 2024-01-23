/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Map;
import java.util.Objects;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.client.IConfigScreenFactory;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event to register {@link IConfigScreenFactory} instances for mods.
 */
public class RegisterConfigScreenEvent extends Event implements IModBusEvent {
    private final Map<String, IConfigScreenFactory> factories;

    @ApiStatus.Internal
    public RegisterConfigScreenEvent(Map<String, IConfigScreenFactory> factories) {
        this.factories = factories;
    }

    /**
     * Registers a config screen factory for the given mod.
     *
     * @param modid   the mod id
     * @param factory the factory
     */
    public void register(String modid, IConfigScreenFactory factory) {
        Objects.requireNonNull(modid, "modid may not be null");
        Objects.requireNonNull(factory, "factory may not be null");

        if (factories.putIfAbsent(modid, factory) != null) {
            throw new IllegalArgumentException("A config screen factory for mod " + modid + " is already registered");
        }
    }

    /**
     * Registers a config screen factory for the given mod.
     *
     * @param container the mod container
     * @param factory   the factory
     */
    public void register(ModContainer container, IConfigScreenFactory factory) {
        register(container.getModId(), factory);
    }
}
