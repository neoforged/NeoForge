/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.FeatureFlagAcknowledgePayload;
import net.neoforged.neoforge.network.payload.FeatureFlagDataPayload;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

@ApiStatus.Internal
public record CheckFeatureFlags(ServerConfigurationPacketListener listener) implements ConfigurationTask {
    public static final Type TYPE = new Type(ResourceLocation.fromNamespaceAndPath("neoforge", "check_feature_flags"));
    private static final Logger LOGGER = LogUtils.getLogger();
    private static Set<ResourceLocation> moddedFlags = null;

    @Override
    public void start(Consumer<Packet<?>> packetSender) {
        if (listener.getConnection().isMemoryConnection()) {
            listener.finishCurrentTask(TYPE);
            return;
        }

        Set<ResourceLocation> moddedFlags = getModdedFeatureFlags();
        if (listener.getConnectionType().isOther() || !listener.hasChannel(FeatureFlagDataPayload.TYPE)) {
            if (!moddedFlags.isEmpty()) {
                // Use plain components as vanilla connections will be missing our translation keys
                listener.disconnect(Component.literal("This server does not support vanilla clients as it has custom FeatureFlags"));
            } else {
                listener.finishCurrentTask(TYPE);
            }
            return;
        }
        packetSender.accept(new FeatureFlagDataPayload(moddedFlags).toVanillaClientbound());
    }

    public static void handleClientboundPayload(FeatureFlagDataPayload payload, IPayloadContext context) {
        Set<ResourceLocation> localFlags = getModdedFeatureFlags();
        Set<ResourceLocation> remoteFlags = payload.moddedFlags();
        if (localFlags.equals(remoteFlags)) {
            context.reply(FeatureFlagAcknowledgePayload.INSTANCE);
        } else {
            context.disconnect(Component.translatable("neoforge.network.feature_flags.entry_mismatch"));

            StringBuilder message = new StringBuilder("The server and client have different sets of custom FeatureFlags");
            Set<ResourceLocation> missingLocal = Sets.difference(remoteFlags, localFlags);
            if (!missingLocal.isEmpty()) {
                message.append("\n\tFlags missing on the client, but present on the server:");
                for (ResourceLocation flag : missingLocal) {
                    message.append("\n\t\t- ").append(flag);
                }
            }
            Set<ResourceLocation> missingRemote = Sets.difference(localFlags, remoteFlags);
            if (!missingRemote.isEmpty()) {
                message.append("\n\tFlags missing on the server, but present on the client:");
                for (ResourceLocation flag : missingRemote) {
                    message.append("\n\t\t- ").append(flag);
                }
            }
            LOGGER.warn(message.toString());
        }
    }

    public static void handleServerboundPayload(@SuppressWarnings("unused") FeatureFlagAcknowledgePayload payload, IPayloadContext context) {
        context.finishCurrentTask(TYPE);
    }

    public static boolean handleVanillaServerConnection(ClientConfigurationPacketListener listener) {
        if (!getModdedFeatureFlags().isEmpty()) {
            listener.disconnect(Component.translatable("neoforge.network.feature_flags.no_vanilla_server"));
            return false;
        }
        return true;
    }

    private static Set<ResourceLocation> getModdedFeatureFlags() {
        if (moddedFlags == null) {
            moddedFlags = FeatureFlags.REGISTRY.getAllFlags()
                    .entrySet()
                    .stream()
                    .filter(e -> e.getValue().isModded())
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
        }
        return moddedFlags;
    }

    @Override
    public Type type() {
        return TYPE;
    }
}
