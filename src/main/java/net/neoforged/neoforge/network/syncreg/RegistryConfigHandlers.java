/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.syncreg;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.network.event.RegisterRegistryConfigHandlersEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RegistryConfigHandlers {

	private static Map<ResourceLocation, RegistryConfigHandler> MAP;

	public static void init() {
		MAP = ModLoader.postEventWithReturn(new RegisterRegistryConfigHandlersEvent()).getHandlers();
	}

	public static Map<ResourceLocation, RegistryConfigHandler> getAllHandlers() {
		return MAP;
	}

	@Nullable
	public static RegistryConfigHandler getHandler(ResourceLocation key) {
		return MAP.get(key);
	}

}
