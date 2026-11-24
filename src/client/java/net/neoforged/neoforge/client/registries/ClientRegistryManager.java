/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.registries;

import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.core.registries.BuiltInClientRegistries;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsReplyPayload;
import net.neoforged.neoforge.network.payload.RegistryDataMapSyncPayload;
import net.neoforged.neoforge.registries.BaseMappedRegistry;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

@ApiStatus.Internal
public final class ClientRegistryManager extends RegistryManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ClientRegistryManager INSTANCE = new ClientRegistryManager();

    private ClientRegistryManager() {}

    @Override
    public void postNewRegistryEvent() {
        super.postNewRegistryEvent();

        NewClientRegistryEvent event = new NewClientRegistryEvent();
        ModLoader.postEventWrapContainerInModOrder(event);
        RegistryManager.registerRegistries(BuiltInClientRegistries.REGISTRY, event.registries);

        ModLoader.postEvent(new ModifyClientRegistriesEvent());
    }

    @Override
    public void postRegisterEvents() {
        super.postRegisterEvents();

        RegistryManager.unfreezeRegistries(BuiltInClientRegistries.REGISTRY);
        for (Identifier registryName : getRegistrationOrder()) {
            try {
                RegistryManager.postRegisterEvent(BuiltInClientRegistries.REGISTRY, registryName);
            } catch (Throwable t) {
                LOGGER.error("Failed to register some entries to {}", registryName, t);
                throw t;
            }
        }
        RegistryManager.freezeRegistries(BuiltInClientRegistries.REGISTRY);
    }

    @Override
    protected boolean isKnownRegistry(Identifier registry) {
        return super.isKnownRegistry(registry) || BuiltInClientRegistries.REGISTRY.containsKey(registry);
    }

    @Override
    protected String getNewRegistryEventNames() {
        return "NewRegistryEvent or NewClientRegistryEvent";
    }

    /// Creates a set containing the ordered list of registry names in the registration order.
    ///
    /// The order is as follows:
    /// - The vanilla registries in vanilla order
    /// - Modded registries in alphabetical order
    ///
    /// Due to static init issues, this is not necessarily the order that vanilla objects are bootstrapped in.
    ///
    /// @return A set containing the registration order
    public static SequencedSet<Identifier> getRegistrationOrder() {
        SequencedSet<Identifier> ordered = new LinkedHashSet<>();
        ordered.addAll(BuiltInClientRegistries.getVanillaRegistrationOrder());
        BuiltInClientRegistries.REGISTRY.keySet().stream().sorted(Identifier::compareNamespaced).forEach(ordered::add);
        return ordered;
    }

    public static <R> void handleDataMapSync(final RegistryDataMapSyncPayload<R> payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            try {
                var regAccess = Minecraft.getInstance().level.registryAccess();
                final BaseMappedRegistry<R> registry = (BaseMappedRegistry<R>) regAccess
                        .lookupOrThrow(payload.registryKey());
                registry.getDataMaps().clear();
                payload.dataMaps().forEach((attachKey, maps) -> registry.getDataMaps().put(RegistryManager.getDataMap(payload.registryKey(), attachKey), Collections.unmodifiableMap(maps)));
                NeoForge.EVENT_BUS.post(new DataMapsUpdatedEvent(regAccess, registry, DataMapsUpdatedEvent.UpdateCause.CLIENT_SYNC));
            } catch (Throwable t) {
                LOGGER.error("Failed to handle registry data map sync: ", t);
                context.disconnect(Component.translatable("neoforge.network.data_maps.failed", payload.registryKey().identifier().toString(), t.toString()));
            }
        });
    }

    public static void handleKnownDataMaps(final KnownRegistryDataMapsPayload payload, final IPayloadContext context) {
        record MandatoryEntry(ResourceKey<? extends Registry<?>> registry, Identifier id) {}
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

        final List<Component> messages = new ArrayList<>();
        final var missingOur = Sets.difference(ourMandatory, theirMandatory);
        if (!missingOur.isEmpty()) {
            messages.add(Component.translatable("neoforge.network.data_maps.missing_our", Component.literal(missingOur.stream()
                    .map(e -> e.id() + " (" + e.registry().identifier() + ")")
                    .collect(Collectors.joining(", "))).withStyle(ChatFormatting.GOLD)));
        }

        final var missingTheir = Sets.difference(theirMandatory, ourMandatory);
        if (!missingTheir.isEmpty()) {
            messages.add(Component.translatable("neoforge.network.data_maps.missing_their", Component.literal(missingTheir.stream()
                    .map(e -> e.id() + " (" + e.registry().identifier() + ")")
                    .collect(Collectors.joining(", "))).withStyle(ChatFormatting.GOLD)));
        }

        if (!messages.isEmpty()) {
            MutableComponent message = Component.empty();
            final var itr = messages.iterator();
            while (itr.hasNext()) {
                message = message.append(itr.next());
                if (itr.hasNext()) {
                    message = message.append("\n");
                }
            }

            context.disconnect(message);
            return;
        }

        final var known = new HashMap<ResourceKey<? extends Registry<?>>, Collection<Identifier>>();
        RegistryManager.getDataMaps().forEach((key, vals) -> known.put(key, vals.keySet()));
        context.reply(new KnownRegistryDataMapsReplyPayload(known));
    }
}
