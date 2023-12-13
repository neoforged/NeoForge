/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;

/**
 * Allows users to register custom {@link net.minecraft.client.KeyMapping key mappings}.
 *
 * <p>This event is not {@linkplain ICancellableEvent cancellable}, and does not {@linkplain HasResult have a result}.
 *
 * <p>This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RegisterKeyMappingsEvent extends Event implements IModBusEvent {
    private final Options options;

    @ApiStatus.Internal
    public RegisterKeyMappingsEvent(Options options) {
        this.options = options;
    }

    /**
     * Registers a new key mapping.
     */
    public void register(KeyMapping key) {
        options.keyMappings = ArrayUtils.add(options.keyMappings, key);
    }
}
