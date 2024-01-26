/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsReplyPayload;
import net.neoforged.neoforge.network.payload.RegistryDataMapSyncPayload;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

@ApiStatus.Internal
public class ClientRegistryManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static <R> void handleDataMapSync(final RegistryDataMapSyncPayload<R> payload, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            final BaseMappedRegistry<R> registry = (BaseMappedRegistry<R>) Minecraft.getInstance().level.registryAccess()
                    .registryOrThrow(payload.registryKey());
            registry.dataMaps.clear();
            payload.dataMaps().forEach((attachKey, maps) -> registry.dataMaps.put(RegistryManager.getDataMap(payload.registryKey(), attachKey), Collections.unmodifiableMap(maps)));
        }).exceptionally(ex -> {
            context.packetHandler().disconnect(Component.translatable("neoforge.network.data_maps.failed", payload.registryKey().location(), ex.getMessage()));
            LOGGER.error("Failed to handle registry data map sync: ", ex);
            return null;
        });
    }

    public static void handleKnownDataMaps(final KnownRegistryDataMapsPayload payload, final ConfigurationPayloadContext context) {
        record MandatoryEntry(ResourceKey<Registry<?>> registry, ResourceLocation id) {}
        final Set<MandatoryEntry> ourMandatory = new HashSet<>();
        RegistryManager.getDataMaps().forEach((reg, values) -> values.values().forEach(attach -> {
            if (attach.mandatorySync()) {
                ourMandatory.add(new MandatoryEntry(reg, attach.id()));
            }
        }));

        final Set<MandatoryEntry> theirMandatory = new HashSet<>();
        payload.dataMaps().forEach((reg, values) -> values.forEach(attach -> {
            if (attach.mandatory()) {
                theirMandatory.add(new MandatoryEntry(reg, attach.id()));
            }
        }));

        final var missingOur = Sets.difference(ourMandatory, theirMandatory);
        final Set<MandatoryEntry> missing;
        final String errorKey;
        if (!missingOur.isEmpty()) {
            missing = missingOur;
            errorKey = "neoforge.network.data_maps.missing_our";
        } else {
            missing = Sets.difference(theirMandatory, ourMandatory);
            errorKey = "neoforge.network.data_maps.missing_their";
        }

        if (!missing.isEmpty()) {
            context.packetHandler().disconnect(Component.translatable(errorKey, Component.literal(missing.stream()
                    .map(e -> e.id() + " (" + e.registry().location() + ")")
                    .collect(Collectors.joining(", "))).withStyle(ChatFormatting.GOLD)));

            return;
        }

        final var known = new HashMap<ResourceKey<Registry<?>>, Collection<ResourceLocation>>();
        RegistryManager.getDataMaps().forEach((key, vals) -> known.put(key, vals.keySet()));
        context.replyHandler().send(new KnownRegistryDataMapsReplyPayload(known));
    }
}
