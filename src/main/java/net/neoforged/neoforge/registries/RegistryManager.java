/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.google.common.collect.Table;
import com.mojang.logging.LogUtils;
import io.netty.handler.codec.CodecException;
import io.netty.util.AttributeKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import net.neoforged.neoforge.common.crafting.RecipePropertySetManager;
import net.neoforged.neoforge.common.tooltip.ItemTooltipHandler;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.network.configuration.RegistryDataMapNegotiation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.payload.FrozenRegistryPayload;
import net.neoforged.neoforge.network.payload.KnownRegistryDataMapsReplyPayload;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@ApiStatus.Internal
public class RegistryManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Marker REGISTRIES = MarkerFactory.getMarker("REGISTRIES");
    public static final RegistryManager INSTANCE = new RegistryManager();
    @Nullable
    private static Set<Identifier> pendingModdedRegistries = new HashSet<>();
    private static Set<Identifier> vanillaRegistryKeys = Set.of();
    private static Map<Identifier, RegistrySnapshot> vanillaSnapshot = null;
    private static Map<Identifier, RegistrySnapshot> frozenSnapshot = null;
    private static Map<ResourceKey<Registry<?>>, Map<Identifier, DataMapType<?, ?>>> dataMaps = Map.of();

    protected RegistryManager() {}

    public static Map<Block, Item> getBlockItemMap() {
        return NeoForgeRegistryCallbacks.ItemCallbacks.BLOCK_TO_ITEM_MAP;
    }

    public static IdMapper<BlockState> getBlockStateIDMap() {
        return NeoForgeRegistryCallbacks.BlockCallbacks.BLOCKSTATE_TO_ID_MAP;
    }

    public static Map<BlockState, Holder<PoiType>> getBlockStatePointOfInterestTypeMap() {
        return NeoForgeRegistryCallbacks.PoiTypeCallbacks.BLOCKSTATE_TO_POI_TYPE_MAP;
    }

    public static Table<Block, Block, Block> getFlowerPotBlockTable() {
        return NeoForgeRegistryCallbacks.BlockCallbacks.EMPTY_POT_AND_FLOWER_TO_FULL_POT_TABLE;
    }

    /**
     * Called by {@link RegistryBuilder} to make sure that modders don't forget to register their registries.
     */
    static synchronized void trackModdedRegistry(Identifier registry) {
        Objects.requireNonNull(registry);

        if (pendingModdedRegistries == null) {
            throw new IllegalStateException("Attempting to instantiate registry with name " + registry + " after NewRegistryEvent was fired!");
        }

        if (!pendingModdedRegistries.add(registry)) {
            throw new IllegalStateException("Registry with name " + registry + " was already instantiated once, cannot instantiate it again!");
        }
    }

    protected static void registerRegistries(Registry<? extends Registry<?>> rootRegistry, List<Registry<?>> registries) {
        ((BaseMappedRegistry<?>) rootRegistry).unfreeze(true);
        for (Registry<?> registry : registries) {
            registerToRootRegistry(rootRegistry, registry);
        }
        ((WritableRegistry<?>) rootRegistry).freeze();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void registerToRootRegistry(Registry<? extends Registry<?>> rootRegistry, Registry<?> registry) {
        Identifier registryName = registry.key().identifier();
        if (rootRegistry.containsKey(registryName)) {
            throw new IllegalStateException("Attempted duplicate registration of registry " + registryName);
        }

        ((WritableRegistry) rootRegistry).register(registry.key(), registry, RegistrationInfo.BUILT_IN);
    }

    protected static void unfreezeRegistries(Registry<? extends Registry<?>> rootRegistry) {
        rootRegistry.stream().filter(r -> r instanceof BaseMappedRegistry).forEach(r -> ((BaseMappedRegistry<?>) r).unfreeze(true));
    }

    protected static void freezeRegistries(Registry<? extends Registry<?>> rootRegistry) {
        rootRegistry.stream().filter(r -> r instanceof MappedRegistry).forEach(r -> {
            // HolderSet.Named may be used for registry objects, vanilla binds these tags so freeze doesn't throw for unbound tags
            ((MappedRegistry<?>) r).bindAllTagsToEmpty();
            ((MappedRegistry<?>) r).freeze();
        });
    }

    protected static void postRegisterEvent(Registry<? extends Registry<?>> rootRegistry, Identifier registryName) {
        Registry<?> registry = Objects.requireNonNull(rootRegistry.getValue(registryName));
        RegisterEvent registerEvent = new RegisterEvent(registry);
        StartupNotificationManager.modLoaderMessage("REGISTERING " + registryName);
        ModLoader.postEventWrapContainerInModOrder(registerEvent);
    }

    @Nullable
    public static <R> DataMapType<R, ?> getDataMap(ResourceKey<? extends Registry<R>> registry, Identifier key) {
        final var map = dataMaps.get(registry);
        return map == null ? null : (DataMapType<R, ?>) map.get(key);
    }

    /**
     * {@return a view of all registered data maps}
     */
    public static Map<ResourceKey<Registry<?>>, Map<Identifier, DataMapType<?, ?>>> getDataMaps() {
        return dataMaps;
    }

    public void postNewRegistryEvent() {
        vanillaRegistryKeys = Set.copyOf(BuiltInRegistries.REGISTRY.keySet());

        NewRegistryEvent staticEvent = new NewRegistryEvent();
        ModLoader.postEventWrapContainerInModOrder(staticEvent);
        registerRegistries(BuiltInRegistries.REGISTRY, staticEvent.registries);

        NewDatapackRegistryEvent datapackEvent = new NewDatapackRegistryEvent();
        ModLoader.postEventWrapContainerInModOrder(datapackEvent);
        datapackEvent.process();

        ModLoader.postEvent(new ModifyRegistriesEvent());
    }

    public final void validateRegistries() {
        assert pendingModdedRegistries != null;

        pendingModdedRegistries.removeIf(this::isKnownRegistry);
        if (!pendingModdedRegistries.isEmpty()) {
            throw new IllegalStateException("The following registries were created but not registered to " + getNewRegistryEventNames() + ":"
                    + pendingModdedRegistries.stream().map(Identifier::toString).collect(Collectors.joining("\n\t - ", "\n\t - ", "")));
        }
        pendingModdedRegistries = null;
    }

    protected boolean isKnownRegistry(Identifier registry) {
        return BuiltInRegistries.REGISTRY.containsKey(registry);
    }

    protected String getNewRegistryEventNames() {
        return "NewRegistryEvent";
    }

    public void postRegisterEvents() {
        unfreezeRegistries(BuiltInRegistries.REGISTRY);
        for (Identifier registryName : getRegistrationOrder()) {
            try {
                postRegisterEvent(BuiltInRegistries.REGISTRY, registryName);
            } catch (Throwable t) {
                LOGGER.error("Failed to register some entries to {}, rolling back to VANILLA state", registryName, t);
                revertToVanilla();
                throw t;
            }
        }
        freezeRegistries(BuiltInRegistries.REGISTRY);
        takeFrozenSnapshot();
    }

    public static void runPostRegistrationTasks() {
        CommonHooks.modifyAttributes();
        SpawnPlacements.fireSpawnPlacementEvent();
        ModLoader.postEvent(new BlockEntityTypeAddBlocksEvent());
        CreativeModeTabRegistry.sortTabs();
        GameRuleCategory.registerModdedCategories();
        ItemTooltipHandler.init();
        RecipePropertySetManager.init();
    }

    public static void initDataMaps() {
        final Map<ResourceKey<Registry<?>>, Map<Identifier, DataMapType<?, ?>>> dataMapTypes = new HashMap<>();
        ModLoader.postEvent(new RegisterDataMapTypesEvent(dataMapTypes));
        dataMaps = new IdentityHashMap<>();
        dataMapTypes.forEach((key, values) -> dataMaps.put(key, Collections.unmodifiableMap(values)));
        dataMaps = Collections.unmodifiableMap(dataMapTypes);
    }

    public static void takeVanillaSnapshot() {
        vanillaSnapshot = takeSnapshot(SnapshotType.FULL);
    }

    private static void takeFrozenSnapshot() {
        frozenSnapshot = takeSnapshot(SnapshotType.SYNC_TO_CLIENT);
    }

    private static void revertToVanilla() {
        applySnapshot(vanillaSnapshot, true);
    }

    public static void revertToFrozen() {
        applySnapshot(frozenSnapshot, true);
    }

    /**
     * Applies the snapshot to the current state of the {@link BuiltInRegistries}.
     *
     * @param snapshots    the map of registry name to snapshot
     * @param isLocalWorld changes the logging depending on if the snapshot is coming from a local save or a remote connection
     * @return the set of unhandled missing registry entries after firing remapping events for mods
     */
    public static Set<ResourceKey<?>> applySnapshot(Map<Identifier, RegistrySnapshot> snapshots, boolean isLocalWorld) {
        Set<ResourceKey<?>> missingEntries = new HashSet<>();

        snapshots.forEach((registryName, snapshot) -> {
            if (!BuiltInRegistries.REGISTRY.containsKey(registryName)) {
                if (snapshot.getIds().isEmpty()) {
                    // Ignore registries that the client is not aware of as long as they are empty.
                    return;
                } else {
                    throw new IllegalStateException("Tried to applied snapshot with registry name " + registryName + " but was not found");
                }
            }

            MappedRegistry<?> registry = (MappedRegistry<?>) BuiltInRegistries.REGISTRY.getValue(registryName);
            applySnapshot(registry, snapshot, missingEntries);
        });

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

        forgeRegistry.unfreeze(false);

        if (backup == null) {
            forgeRegistry.clear(false);
            boolean foundMissing = false;
            for (var entry : snapshot.getIds().int2ObjectEntrySet()) {
                ResourceKey<T> key = ResourceKey.create(registryKey, entry.getValue());
                if (!registry.containsKey(key)) {
                    missing.add(key);
                    foundMissing = true;
                } else if (!foundMissing) {
                    // ID mappings must only be added if this registry didn't encounter missing entries before, otherwise certain operations such
                    // as iterating the registry will crash due to the ID->value list being filled up with nulls to add the next known entry.
                    // Encountering entries unknown to the client in the snapshot sent by the server guarantees that the player will be disconnected
                    // and the registry reverted to the frozen state, so the incomplete ID mapping registration cannot cause issues later.
                    forgeRegistry.registerIdMapping(key, entry.getIntKey());
                }
            }
        } else {
            forgeRegistry.clear(true);
            for (var entry : backup.entrySet()) {
                ResourceKey<T> key = entry.getKey();
                T value = entry.getValue();
                registry.register(backup.getId(key), key, value, backup.registrationInfo(key).orElse(RegistrationInfo.BUILT_IN));
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
    public static Map<Identifier, RegistrySnapshot> takeSnapshot(SnapshotType snapshotType) {
        Map<Identifier, RegistrySnapshot> map = new HashMap<>();
        boolean full = snapshotType == SnapshotType.FULL;

        for (Registry<?> registry : BuiltInRegistries.REGISTRY) {
            if (snapshotType == SnapshotType.SYNC_TO_CLIENT) {
                if (!registry.doesSync())
                    continue;
            }
            map.put(registry.key().identifier(), new RegistrySnapshot(registry, full));
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

    public static List<Identifier> getRegistryNamesForSyncToClient() {
        List<Identifier> list = new ArrayList<>();

        BuiltInRegistries.REGISTRY.entrySet().forEach(e -> {
            if (e.getValue().doesSync())
                list.add(e.getKey().identifier());
        });

        return list;
    }

    public static Set<Identifier> getVanillaRegistryKeys() {
        return vanillaRegistryKeys;
    }

    /// Creates a set containing the ordered list of registry names in the registration order.
    ///
    /// The order is as follows:
    /// - Attributes, Data Component Types, Particle Types
    /// - The remaining vanilla registries in vanilla order
    /// - Modded registries in alphabetical order
    ///
    /// Due to static init issues, this is not necessarily the order that vanilla objects are bootstrapped in.
    ///
    /// @return A [LinkedHashSet] containing the registration order.
    public static SequencedSet<Identifier> getRegistrationOrder() {
        SequencedSet<Identifier> ordered = new LinkedHashSet<>();
        ordered.add(Registries.ATTRIBUTE.identifier()); // Vanilla order is incorrect, both Item and MobEffect depend on Attribute at construction time.
        ordered.add(Registries.DATA_COMPONENT_TYPE.identifier()); // Vanilla order is incorrect, Item depends on data components at construction time.
        ordered.add(Registries.PARTICLE_TYPE.identifier()); // Vanilla order is incorrect, both Block and MobEffect depend on ParticleType at construction time.
        ordered.addAll(BuiltInRegistries.getVanillaRegistrationOrder());
        BuiltInRegistries.REGISTRY.keySet().stream().sorted(Identifier::compareNamespaced).forEach(ordered::add);
        return ordered;
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

    public static final AttributeKey<Map<ResourceKey<? extends Registry<?>>, Collection<Identifier>>> ATTRIBUTE_KNOWN_DATA_MAPS = AttributeKey.valueOf("neoforge:known_data_maps");

    @ApiStatus.Internal
    public static void handleKnownDataMapsReply(final KnownRegistryDataMapsReplyPayload payload, final IPayloadContext context) {
        context.channelHandlerContext().attr(ATTRIBUTE_KNOWN_DATA_MAPS).set(payload.dataMaps());
        context.finishCurrentTask(RegistryDataMapNegotiation.TYPE);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void assertIsSyncedBuiltInRegistry(ResourceKey<? extends Registry<?>> registryKey) {
        Registry<?> value = BuiltInRegistries.REGISTRY.getValue((ResourceKey) registryKey);
        if (value != null && !value.doesSync()) {
            throw new CodecException("Cannot use ID syncing for non-synced built-in registry: " + registryKey.identifier());
        }
    }
}
