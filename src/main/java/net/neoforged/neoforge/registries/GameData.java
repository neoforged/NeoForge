/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.IdMapper;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.StartupMessageManager;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * INTERNAL ONLY
 * MODDERS SHOULD HAVE NO REASON TO USE THIS CLASS
 */
@ApiStatus.Internal
public class GameData {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Marker REGISTRIES = MarkerFactory.getMarker("REGISTRIES");

    public static Map<Block, Item> getBlockItemMap() {
        return NeoForgeRegistryCallbacks.ItemCallbacks.BLOCK_TO_ITEM_MAP;
    }

    public static IdMapper<BlockState> getBlockStateIDMap() {
        return NeoForgeRegistryCallbacks.BlockCallbacks.BLOCKSTATE_TO_ID_MAP;
    }

    public static Map<BlockState, PoiType> getBlockStatePointOfInterestTypeMap() {
        return NeoForgeRegistryCallbacks.PoiTypeCallbacks.BLOCKSTATE_TO_POI_TYPE_MAP;
    }

    public static void vanillaSnapshot() {
        LOGGER.debug(REGISTRIES, "Creating vanilla freeze snapshot");
        RegistryManager.takeVanillaSnapshot();
        LOGGER.debug(REGISTRIES, "Vanilla freeze snapshot created");
    }

    public static void unfreezeData() {
        LOGGER.debug(REGISTRIES, "Unfreezing registries");
        BuiltInRegistries.REGISTRY.stream().filter(r -> r instanceof BaseNeoRegistry).forEach(r -> ((BaseNeoRegistry<?>) r).unfreeze());
    }

    public static void freezeData() {
        LOGGER.debug(REGISTRIES, "Freezing registries");
        BuiltInRegistries.REGISTRY.stream().filter(r -> r instanceof MappedRegistry).forEach(r -> ((MappedRegistry<?>) r).freeze());

        RegistryManager.takeFrozenSnapshot();

        // the id mapping is finalized, no ids actually changed but this is a good place to tell everyone to 'bake' their stuff.
        fireRemapEvent(ImmutableMap.of(), true);

        LOGGER.debug(REGISTRIES, "All registries frozen");
    }

    public static void postRegisterEvents() {
        Set<ResourceLocation> ordered = new LinkedHashSet<>(MappedRegistry.getKnownRegistries());
        ordered.retainAll(RegistryManager.getVanillaRegistryKeys());
        ordered.addAll(BuiltInRegistries.REGISTRY.keySet().stream().sorted(ResourceLocation::compareNamespaced).toList());

        RuntimeException aggregate = new RuntimeException();
        for (ResourceLocation rootRegistryName : ordered) {
            try {
                ResourceKey<? extends Registry<?>> registryKey = ResourceKey.createRegistryKey(rootRegistryName);
                Registry<?> registry = Objects.requireNonNull(BuiltInRegistries.REGISTRY.get(rootRegistryName));
                RegisterEvent registerEvent = new RegisterEvent(registryKey, registry);

                StartupMessageManager.modLoaderConsumer().ifPresent(s -> s.accept("REGISTERING " + registryKey.location()));

                ModLoader.get().postEventWrapContainerInModOrder(registerEvent);
            } catch (Throwable t) {
                aggregate.addSuppressed(t);
            }
        }
        if (aggregate.getSuppressed().length > 0) {
            LOGGER.error("Failed to register some entries, see suppressed exceptions for details", aggregate);
            LOGGER.error("Rolling back to VANILLA state");
            RegistryManager.revertToVanilla();
            throw aggregate;
        } else {
            CommonHooks.modifyAttributes();
            SpawnPlacements.fireSpawnPlacementEvent();
            CreativeModeTabRegistry.sortTabs();
        }
    }

    static void fireRemapEvent(final Map<ResourceLocation, Map<ResourceLocation, IdMappingEvent.IdRemapping>> remaps, final boolean isFreezing) {
        NeoForge.EVENT_BUS.post(new IdMappingEvent(remaps, isFreezing));
    }
}
