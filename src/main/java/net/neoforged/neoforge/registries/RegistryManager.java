/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.mojang.logging.LogUtils;
import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.network.configuration.RegistryDataMapNegotiation;
import net.neoforged.neoforge.network.handling.ConfigurationPayloadContext;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsReplyPayload;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class RegistryManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Marker REGISTRIES = MarkerFactory.getMarker("REGISTRIES");
    private static Set<ResourceLocation> pendingModdedRegistries = new HashSet<>();
    private static Set<ResourceLocation> vanillaRegistryKeys = Set.of();
    private static Map<ResourceLocation, RegistrySnapshot> vanillaSnapshot = null;
    private static Map<ResourceLocation, RegistrySnapshot> frozenSnapshot = null;
    private static Map<ResourceKey<Registry<?>>, Map<ResourceLocation, DataMapType<?, ?>>> dataMaps = Map.of();

    /**
     * Called by {@link RegistryBuilder} to make sure that modders don't forget to register their registries.
     */
    static synchronized void trackModdedRegistry(ResourceLocation registry) {
        Objects.requireNonNull(registry);

        if (pendingModdedRegistries == null) {
            throw new IllegalStateException("Attempting to instantiate registry with name " + registry + " after NewRegistryEvent was fired!");
        }

        if (!pendingModdedRegistries.add(registry)) {
            throw new IllegalStateException("Registry with name " + registry + " was already instantiated once, cannot instantiate it again!");
        }
    }

    @Nullable
    public static <R> DataMapType<R, ?> getDataMap(ResourceKey<? extends Registry<R>> registry, ResourceLocation key) {
        final var map = dataMaps.get(registry);
        return map == null ? null : (DataMapType<R, ?>) map.get(key);
    }

    /**
     * {@return a view of all registered data maps}
     */
    public static Map<ResourceKey<Registry<?>>, Map<ResourceLocation, DataMapType<?, ?>>> getDataMaps() {
        return dataMaps;
    }

    public static void postNewRegistryEvent() {
        NewRegistryEvent event = new NewRegistryEvent();
        DataPackRegistryEvent.NewRegistry dataPackEvent = new DataPackRegistryEvent.NewRegistry();
        vanillaRegistryKeys = Set.copyOf(BuiltInRegistries.REGISTRY.keySet());

        ModLoader.get().postEventWrapContainerInModOrder(event);
        ModLoader.get().postEventWrapContainerInModOrder(dataPackEvent);

        event.fill();
        dataPackEvent.process();

        ModLoader.get().postEvent(new ModifyRegistriesEvent());

        pendingModdedRegistries.removeIf(BuiltInRegistries.REGISTRY::containsKey);
        if (!pendingModdedRegistries.isEmpty()) {
            throw new IllegalStateException("The following registries were created but not registered to NewRegistryEvent:"
                    + pendingModdedRegistries.stream().map(ResourceLocation::toString).collect(Collectors.joining("\n\t - ", "\n\t - ", "")));
        }
        pendingModdedRegistries = null;
    }

    @ApiStatus.Internal
    public static void initDataMaps() {
        final Map<ResourceKey<Registry<?>>, Map<ResourceLocation, DataMapType<?, ?>>> dataMapTypes = new HashMap<>();
        ModLoader.get().postEvent(new RegisterDataMapTypesEvent(dataMapTypes));
        dataMaps = new IdentityHashMap<>();
        dataMapTypes.forEach((key, values) -> dataMaps.put(key, Collections.unmodifiableMap(values)));
        dataMaps = Collections.unmodifiableMap(dataMapTypes);
    }

    static void takeVanillaSnapshot() {
        vanillaSnapshot = takeSnapshot(SnapshotType.FULL);
    }

    static void takeFrozenSnapshot() {
        frozenSnapshot = takeSnapshot(SnapshotType.SYNC_TO_CLIENT);
    }

    public static void revertToVanilla() {
        applySnapshot(vanillaSnapshot, false, true);
    }

    public static void revertToFrozen() {
        applySnapshot(frozenSnapshot, false, true);
    }

    /**
     * Applies the snapshot to the current state of the {@link BuiltInRegistries}.
     *
     * @param snapshots    the map of registry name to snapshot
     * @param allowMissing if {@code true}, missing registries will be skipped but will log a warning.
     *                     Otherwise, an exception will be thrown if a registry name in the snapshot map is missing.
     * @param isLocalWorld changes the logging depending on if the snapshot is coming from a local save or a remote connection
     * @return the set of unhandled missing registry entries after firing remapping events for mods
     */
    public static Set<ResourceKey<?>> applySnapshot(Map<ResourceLocation, RegistrySnapshot> snapshots, boolean allowMissing, boolean isLocalWorld) {
        List<ResourceLocation> missingRegistries = allowMissing ? new ArrayList<>() : null;
        Set<ResourceKey<?>> missingEntries = new HashSet<>();

        snapshots.forEach((registryName, snapshot) -> {
            if (!BuiltInRegistries.REGISTRY.containsKey(registryName)) {
                if (!allowMissing)
                    throw new IllegalStateException("Tried to applied snapshot with registry name " + registryName + " but was not found");

                missingRegistries.add(registryName);
                return;
            }

            MappedRegistry<?> registry = (MappedRegistry<?>) BuiltInRegistries.REGISTRY.get(registryName);
            applySnapshot(registry, snapshot, missingEntries);
        });

        if (missingRegistries != null && !missingRegistries.isEmpty() && LOGGER.isWarnEnabled(REGISTRIES)) {
            StringBuilder builder = new StringBuilder("NeoForge detected missing/unknown registries.\n\n")
                    .append("There are ").append(missingRegistries.size()).append(" missing registries.\n");
            if (isLocalWorld)
                builder.append("These missing registries will be deleted from the save file on next save.\n");

            builder.append("Missing Registries:\n");

            for (ResourceLocation registryName : missingRegistries)
                builder.append(registryName).append("\n");

            LOGGER.warn(REGISTRIES, builder.toString());
        }

        if (missingEntries.isEmpty()) {
            return Set.of();
        }

        LOGGER.debug(REGISTRIES, "There are {} mappings missing", missingEntries.size());// Only log if the world save is something we control
        if (isLocalWorld && LOGGER.isWarnEnabled(REGISTRIES)) {
            StringBuilder builder = new StringBuilder("NeoForge detected missing registry entries.\n\n")
                    .append("There are ").append(missingEntries.size()).append(" missing entries in this save.\n")
                    .append("These missing entries will be deleted from the save file on next save.");

            missingEntries.forEach(key -> builder.append("Missing ").append(key).append('\n'));

            LOGGER.warn(REGISTRIES, builder.toString());
        }

        return Set.copyOf(missingEntries);
    }

    private static <T> void applySnapshot(MappedRegistry<T> registry, RegistrySnapshot snapshot, Set<ResourceKey<?>> missing) {
        // Needed for package-private operations
        // noinspection UnnecessaryLocalVariable
        BaseMappedRegistry<T> forgeRegistry = registry;
        ResourceKey<? extends Registry<T>> registryKey = registry.key();
        Registry<T> backup = snapshot.getFullBackup();

        forgeRegistry.unfreeze();

        if (backup == null) {
            forgeRegistry.clear(false);
            for (var entry : snapshot.getIds().int2ObjectEntrySet()) {
                ResourceKey<T> key = ResourceKey.create(registryKey, entry.getValue());
                if (!registry.containsKey(key)) {
                    missing.add(key);
                } else {
                    forgeRegistry.registerIdMapping(key, entry.getIntKey());
                }
            }
        } else {
            forgeRegistry.clear(true);
            for (var entry : backup.entrySet()) {
                ResourceKey<T> key = entry.getKey();
                T value = entry.getValue();
                registry.registerMapping(backup.getId(key), key, value, backup.lifecycle(value));
            }
        }

        snapshot.getAliases().forEach(registry::addAlias);

        forgeRegistry.freeze();
    }

    /**
     * Takes a snapshot of the current registries registered to {@link BuiltInRegistries#REGISTRY}.
     *
     * @param snapshotType If {@link SnapshotType#SYNC_TO_CLIENT}, only takes a snapshot of registries set to {@linkplain IRegistryExtension#doesSync() sync to the client}.
     *                     If {@link SnapshotType#FULL}, takes a snapshot of all registries including entries.
     * @return the snapshot map of registry name to snapshot data
     */
    public static Map<ResourceLocation, RegistrySnapshot> takeSnapshot(SnapshotType snapshotType) {
        Map<ResourceLocation, RegistrySnapshot> map = new HashMap<>();
        boolean full = snapshotType == SnapshotType.FULL;

        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            if (snapshotType == SnapshotType.SYNC_TO_CLIENT) {
                if (!registry.doesSync())
                    continue;
            }
            map.put(registry.key().location(), new RegistrySnapshot(registry, full));
        }

        return map;
    }

    public static List<FrozenRegistryPayload> generateRegistryPackets(boolean isLocal) {
        if (isLocal)
            return List.of();

        return takeSnapshot(SnapshotType.SYNC_TO_CLIENT).entrySet().stream()
                .map(e -> new FrozenRegistryPayload(e.getKey(), e.getValue()))
                .toList();
    }

    public static List<ResourceLocation> getRegistryNamesForSyncToClient() {
        List<ResourceLocation> list = new ArrayList<>();

        BuiltInRegistries.REGISTRY.entrySet().forEach(e -> {
            if (e.getValue().doesSync())
                list.add(e.getKey().location());
        });

        return list;
    }

    public static Set<ResourceLocation> getVanillaRegistryKeys() {
        return vanillaRegistryKeys;
    }

    public enum SnapshotType {
        /**
         * The snapshot can be synced to clients.
         */
        SYNC_TO_CLIENT,
        /**
         * A full snapshot is being taken of all registries including entries,
         * never sent to the client or saved to disk.
         */
        FULL
    }

    public static final AttributeKey<Map<ResourceKey<Registry<?>>, Collection<ResourceLocation>>> ATTRIBUTE_KNOWN_DATA_MAPS = AttributeKey.valueOf("neoforge:known_data_maps");

    @ApiStatus.Internal
    public static void handleKnownDataMapsReply(final KnownRegistryDataMapsReplyPayload payload, final ConfigurationPayloadContext context) {
        context.channelHandlerContext().attr(ATTRIBUTE_KNOWN_DATA_MAPS).set(payload.dataMaps());
        context.taskCompletedHandler().onTaskCompleted(RegistryDataMapNegotiation.TYPE);
    }
}
