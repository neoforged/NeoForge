/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.network.HandshakeMessages;
import net.neoforged.neoforge.network.simple.MessageFunctions;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class RegistryManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Marker REGISTRIES = MarkerFactory.getMarker("REGISTRIES");
    private static Set<ResourceLocation> vanillaRegistryKeys = Set.of();
    private static Map<ResourceLocation, RegistrySnapshot> vanillaSnapshot = null;
    private static Map<ResourceLocation, RegistrySnapshot> frozenSnapshot = null;
    private static List<Registry<?>> pendingRegistries = new ArrayList<>();

    /**
     * Schedule a registry to be registered to {@link BuiltInRegistries#REGISTRY}.
     * Called from {@link RegistryBuilder#create()}. Do not try to call this directly.
     */
    synchronized static void addRegistry(Registry<?> registry) {
        if (pendingRegistries == null) {
            throw new IllegalStateException("Too late to add registries, register them in your mod constructor!");
        }

        ResourceLocation registryName = registry.key().location();

        if (BuiltInRegistries.REGISTRY.containsKey(registryName) || pendingRegistries.stream().anyMatch(reg -> reg.key() == registry.key()))
            throw new IllegalStateException("Cannot create a registry that already exists - " + registryName);

        pendingRegistries.add(registry);
    }

    public synchronized static void createRegistries() {
        List<Registry<?>> moddedStaticRegistries = pendingRegistries;
        pendingRegistries = null; // Prevent further registration

        DataPackRegistryEvent.NewRegistry dataPackEvent = new DataPackRegistryEvent.NewRegistry();
        vanillaRegistryKeys = Set.copyOf(BuiltInRegistries.REGISTRY.keySet());

        ModLoader.get().postEventWrapContainerInModOrder(dataPackEvent);

        registerModdedRegistries(moddedStaticRegistries);
        dataPackEvent.process();

        BuiltInRegistries.REGISTRY.forEach(RegistryManager::postModifyRegistryEvent);
    }

    private static void registerModdedRegistries(List<Registry<?>> registries) {
        RuntimeException aggregate = new RuntimeException();

        ((BaseMappedRegistry<?>) BuiltInRegistries.REGISTRY).unfreeze();

        for (final var registry : registries) {
            try {
                registerToRootRegistry(registry);
            } catch (Throwable t) {
                aggregate.addSuppressed(t);
                return;
            }
        }

        ((WritableRegistry<?>) BuiltInRegistries.REGISTRY).freeze();

        if (aggregate.getSuppressed().length > 0)
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to create some forge registries, see suppressed exceptions for details", aggregate);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void registerToRootRegistry(Registry<?> registry) {
        ResourceLocation registryName = registry.key().location();
        if (BuiltInRegistries.REGISTRY.containsKey(registryName))
            throw new IllegalStateException("Tried to register new registry for " + registryName);

        ((WritableRegistry) BuiltInRegistries.REGISTRY).register(registry.key(), registry, Lifecycle.stable());
    }

    public static void postModifyRegistryEvent(Registry<?> registry) {
        ModLoader.get().postEventWrapContainerInModOrder(new ModifyRegistryEvent(registry));
    }

    static void takeVanillaSnapshot() {
        vanillaSnapshot = takeSnapshot(SnapshotType.FULL);
    }

    static void takeFrozenSnapshot() {
        frozenSnapshot = takeSnapshot(SnapshotType.FULL);
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
            for (var entry : snapshot.getIds().object2IntEntrySet()) {
                ResourceKey<T> key = ResourceKey.create(registryKey, entry.getKey());
                if (!registry.containsKey(key)) {
                    missing.add(key);
                } else {
                    forgeRegistry.registerIdMapping(key, entry.getIntValue());
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

    public static List<MessageFunctions.LoginPacket<HandshakeMessages.S2CRegistry>> generateRegistryPackets(boolean isLocal) {
        if (isLocal)
            return List.of();

        return takeSnapshot(SnapshotType.SYNC_TO_CLIENT).entrySet().stream()
                .map(e -> new MessageFunctions.LoginPacket<>("Registry " + e.getKey(), new HandshakeMessages.S2CRegistry(e.getKey(), e.getValue())))
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
}
