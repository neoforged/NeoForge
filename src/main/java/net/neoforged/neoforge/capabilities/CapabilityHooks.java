/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;
import net.neoforged.neoforge.transfer.item.ComposterWrapper;
import net.neoforged.neoforge.transfer.item.ContainerStorage;
import net.neoforged.neoforge.transfer.item.ItemVariant;
import net.neoforged.neoforge.transfer.item.WorldlyContainerStorage;
import net.neoforged.neoforge.transfer.item.base.ComponentItemStorage;
import net.neoforged.neoforge.transfer.storage.Storage;
import net.neoforged.neoforge.transfer.storage.base.CombinedStorage;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CapabilityHooks {
    private static boolean initialized = false;
    static boolean initFinished = false;

    public static void init() {
        if (initialized)
            throw new IllegalArgumentException("CapabilityHooks.init() called twice");
        initialized = true;

        var event = new RegisterCapabilitiesEvent();
        ModLoader.postEventWrapContainerInModOrder(event);

        initFinished = true;
    }

    public static void markProxyableCapabilities(RegisterCapabilitiesEvent event) {
        event.setProxyable(Capabilities.EnergyStorage.BLOCK);
        event.setProxyable(Capabilities.FluidStorage.BLOCK);
        event.setProxyable(Capabilities.ItemStorage.BLOCK);
    }

    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Storage<ItemVariant>>> CHEST_STORAGE_COMBINER = new DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<Storage<ItemVariant>>>() {
        @Override
        public Optional<Storage<ItemVariant>> acceptDouble(ChestBlockEntity chest1, ChestBlockEntity chest2) {
            return Optional.of(new CombinedStorage<>(ContainerStorage.of(chest1), ContainerStorage.of(chest2)));
        }

        @Override
        public Optional<Storage<ItemVariant>> acceptSingle(ChestBlockEntity chest) {
            return Optional.of(ContainerStorage.of(chest));
        }

        @Override
        public Optional<Storage<ItemVariant>> acceptNone() {
            return Optional.empty();
        }
    };

    public static void registerVanillaProviders(RegisterCapabilitiesEvent event) {
        // TODO: port missing stuff to Storage API
        // Blocks
        event.registerBlock(Capabilities.ItemStorage.BLOCK, (level, pos, state, blockEntity, side) -> {
            // Invalidation is taken care of by the patches to ComposterBlock
            return ComposterWrapper.get(level, pos, side);
        }, Blocks.COMPOSTER);

        event.registerBlock(Capabilities.ItemStorage.BLOCK, (level, pos, state, blockEntity, side) -> {
            return ((ChestBlock) state.getBlock()).combine(state, level, pos, true).apply(CHEST_STORAGE_COMBINER).orElse(null);
        }, Blocks.CHEST, Blocks.TRAPPED_CHEST);

        var sidedVanillaContainers = List.of(
                BlockEntityType.BLAST_FURNACE,
                BlockEntityType.BREWING_STAND,
                BlockEntityType.FURNACE,
                BlockEntityType.SMOKER,
                BlockEntityType.SHULKER_BOX);
        for (var type : sidedVanillaContainers) {
            event.registerBlockEntity(Capabilities.ItemStorage.BLOCK, type, WorldlyContainerStorage::new);
        }

        var nonSidedVanillaContainers = List.of(
                BlockEntityType.BARREL,
                BlockEntityType.CHISELED_BOOKSHELF,
                BlockEntityType.DISPENSER,
                BlockEntityType.DROPPER,
                BlockEntityType.HOPPER,
                BlockEntityType.JUKEBOX,
                BlockEntityType.CRAFTER,
                BlockEntityType.DECORATED_POT);
        for (var type : nonSidedVanillaContainers) {
            event.registerBlockEntity(Capabilities.ItemStorage.BLOCK, type, (container, side) -> ContainerStorage.of(container));
        }

        // Entities
        var containerEntities = List.of(
                EntityType.CHEST_BOAT,
                EntityType.CHEST_MINECART,
                EntityType.HOPPER_MINECART);
        for (var entityType : containerEntities) {
            event.registerEntity(Capabilities.ItemStorage.ENTITY, entityType, (entity, ctx) -> ContainerStorage.of(entity));
            event.registerEntity(Capabilities.ItemStorage.ENTITY_AUTOMATION, entityType, (entity, ctx) -> ContainerStorage.of(entity));
        }
        // TODO: be very careful with the armor slots! InventoryStorage is not up to the task yet I think
//        event.registerEntity(Capabilities.ItemHandler.ENTITY, EntityType.PLAYER, (player, ctx) -> new PlayerInvWrapper(player.getInventory()));

        // Items
        event.registerItem(Capabilities.ItemStorage.ITEM, (stack, ctx) -> new ComponentItemStorage(ctx, DataComponents.CONTAINER, 27),
                Items.SHULKER_BOX,
                Items.BLACK_SHULKER_BOX,
                Items.BLUE_SHULKER_BOX,
                Items.BROWN_SHULKER_BOX,
                Items.CYAN_SHULKER_BOX,
                Items.GRAY_SHULKER_BOX,
                Items.GREEN_SHULKER_BOX,
                Items.LIGHT_BLUE_SHULKER_BOX,
                Items.LIGHT_GRAY_SHULKER_BOX,
                Items.LIME_SHULKER_BOX,
                Items.MAGENTA_SHULKER_BOX,
                Items.ORANGE_SHULKER_BOX,
                Items.PINK_SHULKER_BOX,
                Items.PURPLE_SHULKER_BOX,
                Items.RED_SHULKER_BOX,
                Items.WHITE_SHULKER_BOX,
                Items.YELLOW_SHULKER_BOX);
    }

    public static void registerFallbackVanillaProviders(RegisterCapabilitiesEvent event) {
        // Entities
        // Register to all entity types to make sure we support all living entity subclasses.
        // TODO: requires a transactional implementation of the EntityEquipmentInvWrapper
//        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
//            event.registerEntity(Capabilities.ItemHandler.ENTITY, entityType, (entity, ctx) -> {
//                if (entity instanceof AbstractHorse horse)
//                    return new InvWrapper(horse.getInventory());
//                else if (entity instanceof LivingEntity livingEntity)
//                    return new CombinedInvWrapper(new EntityHandsInvWrapper(livingEntity), new EntityArmorInvWrapper(livingEntity));
//
//                return null;
//            });
//        }

        // Items
        for (Item item : BuiltInRegistries.ITEM) {
            if (item.getClass() == BucketItem.class)
                event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), item);
        }

        // We want mods to be able to override our milk cap by default
        if (NeoForgeMod.MILK.isBound()) {
            event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack), Items.MILK_BUCKET);
        }
    }

    public static void invalidateCapsOnChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel sl) {
            sl.invalidateCapabilities(event.getChunk().getPos());
        }
    }

    public static void invalidateCapsOnChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel sl) {
            sl.invalidateCapabilities(event.getChunk().getPos());
        }
    }

    public static void cleanCapabilityListenerReferencesOnTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel sl) {
            sl.cleanCapabilityListenerReferences();
        }
    }
}
