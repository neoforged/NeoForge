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
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

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

    public static Map<BlockState, Holder<PoiType>> getBlockStatePointOfInterestTypeMap() {
        return NeoForgeRegistryCallbacks.PoiTypeCallbacks.BLOCKSTATE_TO_POI_TYPE_MAP;
    }

    public static void vanillaSnapshot() {
        LOGGER.debug(REGISTRIES, "Creating vanilla freeze snapshot");
        RegistryManager.takeVanillaSnapshot();
        LOGGER.debug(REGISTRIES, "Vanilla freeze snapshot created");
    }

    public static void unfreezeData() {
        LOGGER.debug(REGISTRIES, "Unfreezing registries");
        BuiltInRegistries.REGISTRY.stream().filter(r -> r instanceof BaseMappedRegistry).forEach(r -> ((BaseMappedRegistry<?>) r).unfreeze());
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
        Set<ResourceLocation> ordered = GameData.getRegistrationOrder();

        RuntimeException aggregate = new RuntimeException();
        for (ResourceLocation rootRegistryName : ordered) {
            try {
                ResourceKey<? extends Registry<?>> registryKey = ResourceKey.createRegistryKey(rootRegistryName);
                Registry<?> registry = Objects.requireNonNull(BuiltInRegistries.REGISTRY.get(rootRegistryName));
                RegisterEvent registerEvent = new RegisterEvent(registryKey, registry);

                StartupNotificationManager.modLoaderMessage("REGISTERING " + registryKey.location());

                ModLoader.postEventWrapContainerInModOrder(registerEvent);
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

    /**
     * Creates a {@link LinkedHashSet} containing the ordered list of registry names in the registration order.
     * <p>
     * The order is Attributes, then the remaining vanilla registries in vanilla order, then modded registries in alphabetical order.
     * <p>
     * Due to static init issues, this is not necessarily the order that vanilla objects are bootstrapped in.
     * 
     * @return A {@link LinkedHashSet} containing the registration order.
     */
    public static Set<ResourceLocation> getRegistrationOrder() {
        Set<ResourceLocation> ordered = new LinkedHashSet<>();
        ordered.add(Registries.ATTRIBUTE.location()); // Vanilla order is incorrect, both Item and MobEffect depend on Attribute at construction time.
        ordered.add(Registries.DATA_COMPONENT_TYPE.location()); // Vanilla order is incorrect, Item depends on data components at construction time.
        ordered.addAll(BuiltInRegistries.getVanillaRegistrationOrder());
        ordered.addAll(BuiltInRegistries.REGISTRY.keySet().stream().sorted(ResourceLocation::compareNamespaced).toList());
        return ordered;
    }
}
