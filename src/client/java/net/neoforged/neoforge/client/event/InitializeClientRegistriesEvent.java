/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.event.IModBusEvent;
import org.jetbrains.annotations.ApiStatus;

/**
 * Fired during {@link Minecraft} initialization and datagen startup to allow initializing
 * custom client-only "registries".
 * <p>
 * This event is fired on the mod-specific event bus, only on the {@linkplain LogicalSide#CLIENT logical client}.
 */
public final class InitializeClientRegistriesEvent extends Event implements IModBusEvent {
    @ApiStatus.Internal
    public InitializeClientRegistriesEvent() {}
}
