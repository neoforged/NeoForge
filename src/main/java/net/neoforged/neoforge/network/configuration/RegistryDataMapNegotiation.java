/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.configuration.ServerConfigurationPacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record RegistryDataMapNegotiation(ServerConfigurationPacketListener listener) implements ConfigurationTask {
    public static final ResourceLocation ID = new ResourceLocation("neoforge:registry_data_map_negotiation");
    public static final Type TYPE = new Type(ID);

    @Override
    public Type type() {
        return TYPE;
    }

    @Override
    public void start(Consumer<Packet<?>> sender) {
        if (!listener.hasChannel(KnownRegistryDataMapsPayload.ID)) {
            final var mandatory = RegistryManager.getDataMaps().values()
                    .stream()
                    .flatMap(map -> map.values().stream())
                    .filter(DataMapType::mandatorySync)
                    .map(type -> type.id() + " (" + type.registryKey().location() + ")")
                    .toList();
            if (!mandatory.isEmpty()) {
                // Use plain components as vanilla connections will be missing our translation keys
                listener.disconnect(Component.literal("This server does not support vanilla clients as it has mandatory registry data maps: ")
                        .append(Component.literal(String.join(", ", mandatory)).withStyle(ChatFormatting.GOLD)));
            } else {
                listener.finishCurrentTask(TYPE);
            }

            return;
        }

        final Map<ResourceKey<Registry<?>>, List<KnownRegistryDataMapsPayload.KnownDataMap>> dataMaps = new HashMap<>();
        RegistryManager.getDataMaps().forEach((key, attach) -> {
            final List<KnownRegistryDataMapsPayload.KnownDataMap> list = new ArrayList<>();
            attach.forEach((id, val) -> {
                if (val.networkCodec() != null) {
                    list.add(new KnownRegistryDataMapsPayload.KnownDataMap(id, val.mandatorySync()));
                }
            });
            dataMaps.put(key, list);
        });
        sender.accept(new ClientboundCustomPayloadPacket(new KnownRegistryDataMapsPayload(dataMaps)));
    }
}
