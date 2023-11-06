/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import net.minecraft.core.IdMapper;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.StartupMessageManager;
import net.neoforged.fml.util.EnhancedRuntimeException;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.advancements.critereon.ICustomItemPredicate;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.util.LogMessageAdapter;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.StructureModifier;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
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

    public static Map<Block, Item> getBlockItemMap()
    {
        return ForgeRegistryCallbacks.ItemCallbacks.BLOCK_TO_ITEM_MAP;
    }

    public static IdMapper<BlockState> getBlockStateIDMap()
    {
        return ForgeRegistryCallbacks.BlockCallbacks.BLOCKSTATE_TO_ID_MAP;
    }

    public static Map<BlockState, PoiType> getBlockStatePointOfInterestTypeMap()
    {
        return ForgeRegistryCallbacks.PoiTypeCallbacks.BLOCKSTATE_TO_POI_TYPE_MAP;
    }

    public static void vanillaSnapshot()
    {
        LOGGER.debug(REGISTRIES, "Creating vanilla freeze snapshot");
        RegistryManager.takeVanillaSnapshot();
        LOGGER.debug(REGISTRIES, "Vanilla freeze snapshot created");
    }

    public static void unfreezeData()
    {
        LOGGER.debug(REGISTRIES, "Unfreezing registries");
        BuiltInRegistries.REGISTRY.stream().filter(r -> r instanceof BaseNeoRegistry).forEach(r -> ((BaseNeoRegistry<?>) r).unfreeze());
    }

    public static void freezeData()
    {
        LOGGER.debug(REGISTRIES, "Freezing registries");
        BuiltInRegistries.REGISTRY.stream().filter(r -> r instanceof MappedRegistry).forEach(r -> ((MappedRegistry<?>)r).freeze());

        RegistryManager.takeFrozenSnapshot();

        // the id mapping is finalized, no ids actually changed but this is a good place to tell everyone to 'bake' their stuff.
        fireRemapEvent(ImmutableMap.of(), true);

        LOGGER.debug(REGISTRIES, "All registries frozen");
    }

    public static void postRegisterEvents()
    {
        Set<ResourceLocation> ordered = new LinkedHashSet<>(MappedRegistry.getKnownRegistries());
        ordered.retainAll(RegistryManager.getVanillaRegistryKeys());
        ordered.addAll(BuiltInRegistries.REGISTRY.keySet().stream().sorted(ResourceLocation::compareNamespaced).toList());

        RuntimeException aggregate = new RuntimeException();
        for (ResourceLocation rootRegistryName : ordered)
        {
            try
            {
                ResourceKey<? extends Registry<?>> registryKey = ResourceKey.createRegistryKey(rootRegistryName);
                Registry<?> registry = Objects.requireNonNull(BuiltInRegistries.REGISTRY.get(rootRegistryName));
                RegisterEvent registerEvent = new RegisterEvent(registryKey, registry);

                StartupMessageManager.modLoaderConsumer().ifPresent(s -> s.accept("REGISTERING " + registryKey.location()));

                ModLoader.get().postEventWrapContainerInModOrder(registerEvent);

                LOGGER.debug(REGISTRIES, "Applying holder lookups: {}", registryKey.location());
                ObjectHolderRegistry.applyObjectHolders(registryKey.location()::equals);
                LOGGER.debug(REGISTRIES, "Holder lookups applied: {}", registryKey.location());
            } catch (Throwable t)
            {
                aggregate.addSuppressed(t);
            }
        }
        if (aggregate.getSuppressed().length > 0)
        {
            LOGGER.error("Failed to register some entries, see suppressed exceptions for details", aggregate);
            LOGGER.error("Rolling back to VANILLA state");
            RegistryManager.revertToVanilla();
            throw aggregate;
        } else
        {
            CommonHooks.modifyAttributes();
            SpawnPlacements.fireSpawnPlacementEvent();
            CreativeModeTabRegistry.sortTabs();
        }
    }

    static void fireRemapEvent(final Map<ResourceLocation, Map<ResourceLocation, IdMappingEvent.IdRemapping>> remaps, final boolean isFreezing) {
        NeoForge.EVENT_BUS.post(new IdMappingEvent(remaps, isFreezing));
    }
}
