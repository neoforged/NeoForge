/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.chunk;

import java.util.function.Consumer;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired in order to register chunk {@link TicketController ticket controllers}.
 * This event is fired on the mod-specific event bus.
 */
public class RegisterTicketControllersEvent extends Event implements IModBusEvent {
    private final Consumer<TicketController> registrar;

    RegisterTicketControllersEvent(Consumer<TicketController> registrar) {
        this.registrar = registrar;
    }

    /**
     * Registers a controller.
     *
     * @param controller the controller to register
     * @throws IllegalArgumentException if a controller with the same {@link TicketController#id() id} is already registered
     */
    public void register(TicketController controller) {
        registrar.accept(controller);
    }
}
