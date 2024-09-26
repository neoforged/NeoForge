/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.client.gui.RegistryConfigMismatchScreen;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.RegistryConfigAckPayload;
import net.neoforged.neoforge.network.payload.RegistryConfigDataPayload;
import net.neoforged.neoforge.network.syncreg.RegistryConfigHandler;
import net.neoforged.neoforge.network.syncreg.RegistryConfigHandlers;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public record SyncRegistryConfigTask(ServerConfigurationPacketListener listener) implements ConfigurationTask {
	public static final Type TYPE = new Type("synchronize_registry_config");
	public static final Logger LOGGER = LogUtils.getLogger();

	public static synchronized void handleData(RegistryConfigDataPayload payload, IPayloadContext ctx) {
		ClientHandler.handleData(payload, ctx);
	}

	public static void handleAck(RegistryConfigAckPayload payload, IPayloadContext ctx) {
		ctx.finishCurrentTask(TYPE);
	}

	@Override
	public synchronized void start(Consumer<Packet<?>> packetSender) {
		if (listener.getConnection().isMemoryConnection()) {
			listener.finishCurrentTask(TYPE);
			return;
		}
		Map<ResourceLocation, JsonElement> configs = new TreeMap<>();
		for (var ent : RegistryConfigHandlers.getAllHandlers().entrySet()) {
			try {
				JsonElement data = ent.getValue().serializeConfig();
				configs.put(ent.getKey(), data);
			} catch (Exception e) {
				LOGGER.error("Failed to serialize " + ent.getKey(), e);
				listener.disconnect(Component.literal("Failed to serialize " + ent.getKey()));//TODO
				return;
			}
		}
		packetSender.accept(new RegistryConfigDataPayload(configs).toVanillaClientbound());
	}

	@Override
	public Type type() {
		return TYPE;
	}

	private static class ClientHandler {
		static synchronized void handleData(RegistryConfigDataPayload payload, IPayloadContext context) {
			Set<ResourceLocation> clientMissing = new TreeSet<>();
			Set<ResourceLocation> serverMissing = new TreeSet<>();
			for (var key : RegistryConfigHandlers.getAllHandlers().keySet()) {
				if (!payload.map().containsKey(key)) {
					serverMissing.add(key);
				}
			}
			for (var key : payload.map().keySet()) {
				if (RegistryConfigHandlers.getHandler(key) == null) {
					clientMissing.add(key);
				}
			}
			if (!clientMissing.isEmpty() || !serverMissing.isEmpty()) {
				context.disconnect(Component.literal("Mismatch in registry config handler definition"));//TODO info
				return;
			}
			Map<RegistryConfigHandler, JsonElement> mismatched = new LinkedHashMap<>();
			Map<ResourceLocation, Component> reasons = new TreeMap<>();
			for (var ent : payload.map().entrySet()) {
				try {
					var handler = RegistryConfigHandlers.getHandler(ent.getKey());
					if (handler == null) continue;
					var reason = handler.verifyConfig(ent.getValue());
					if (reason != null) {
						mismatched.put(handler, ent.getValue());
						reasons.put(ent.getKey(), reason);
					}
				} catch (Exception e) {
					context.disconnect(Component.literal("Exception in decoding " + ent.getKey()));//TODO info
					return;
				}
			}
			if (!mismatched.isEmpty()) {
				context.disconnect(Component.literal("Mismatched registry config"));//TODO info
				Minecraft.getInstance().setScreen(new RegistryConfigMismatchScreen(
						new JoinMultiplayerScreen(new TitleScreen()),
						Component.literal("Mismatched Registry config"),//TODO
						() -> applyAndRestart(mismatched), reasons));
				return;
			}
			context.reply(RegistryConfigAckPayload.INSTANCE);
		}

		static synchronized void applyAndRestart(Map<RegistryConfigHandler, JsonElement> mismatched) {
			for (var ent : mismatched.entrySet()) {
				ent.getKey().applyConfig(ent.getValue());
			}
			Minecraft.getInstance().stop();//TODO try if we can restart?
		}
	}
}
