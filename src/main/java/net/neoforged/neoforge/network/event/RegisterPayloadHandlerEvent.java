/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.event;

import java.util.function.Function;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import org.jetbrains.annotations.ApiStatus;

/**
 * Event fired when the {@link NetworkRegistry} is being set up.
 * <p>
 * This event is used to collect all the payload types and their handlers that should be used on the network.
 * </p>
 */
public class RegisterPayloadHandlerEvent extends Event implements IModBusEvent {
    private final Function<String, IPayloadRegistrar> registrarFactory;

    @ApiStatus.Internal
    public RegisterPayloadHandlerEvent(Function<String, IPayloadRegistrar> registrarFactory) {
        this.registrarFactory = registrarFactory;
    }

    /**
     * {@return A {@link IPayloadRegistrar} for the given namespace, creating one if it doesn't exist.}
     */
    public IPayloadRegistrar registrar(String namespace) {
        return registrarFactory.apply(namespace);
    }
}
