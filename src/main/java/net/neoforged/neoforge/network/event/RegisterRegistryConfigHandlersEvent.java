/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.event;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.network.syncreg.RegistryConfigHandler;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fired to register registry config handlers. At this point all registry-affecting config should be loaded already.
 */
public class RegisterRegistryConfigHandlersEvent extends Event implements IModBusEvent {

	private final Map<ResourceLocation, RegistryConfigHandler> handlers = new ConcurrentHashMap<>();

	@ApiStatus.Internal
	public RegisterRegistryConfigHandlersEvent() {
	}

	/**
	 * Register a registry config handler.
	 *
	 * @param id      The identifier of the handler
	 * @param handler The registry config handler
	 */
	public void register(ResourceLocation id, RegistryConfigHandler handler) {
		handlers.put(id, handler);
	}

	/**
	 * Get the registry config handlers that have been registered.
	 *
	 * @return The registry config handlers.
	 */
	@ApiStatus.Internal
	public Map<ResourceLocation, RegistryConfigHandler> getHandlers() {
		return new TreeMap<>(handlers);
	}
}
