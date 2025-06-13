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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
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
import net.neoforged.neoforge.transfer.handlers.resources.IResourceHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.CombinedResourceHandlerWrapper;
import net.neoforged.neoforge.transfer.handlers.wrappers.fluids.BucketResourceHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.ComposterWrapper;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.EntityEquipmentItemHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.ItemContainerContentsResourceHandler;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.PlayerInventoryWrapper;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.handlers.wrappers.items.WorldlyContainerWrapper;
import net.neoforged.neoforge.transfer.resources.ItemResource;
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
        event.setProxyable(Capabilities.EnergyHandler.BLOCK);
        event.setProxyable(Capabilities.FluidHandler.BLOCK);
        event.setProxyable(Capabilities.ItemHandler.BLOCK);
    }

    private static final DoubleBlockCombiner.Combiner<ChestBlockEntity, Optional<IResourceHandler<ItemResource>>> CHEST_COMBINED_HANDLER = new DoubleBlockCombiner.Combiner<>() {
        @Override
        public Optional<IResourceHandler<ItemResource>> acceptDouble(ChestBlockEntity chest1, ChestBlockEntity chest2) {
            return Optional.of(new CombinedResourceHandlerWrapper<>(VanillaContainerWrapper.of(chest1), VanillaContainerWrapper.of(chest2)));
        }

        @Override
        public Optional<IResourceHandler<ItemResource>> acceptSingle(ChestBlockEntity chest) {
            return Optional.of(VanillaContainerWrapper.of(chest));
        }

        @Override
        public Optional<IResourceHandler<ItemResource>> acceptNone() {
            return Optional.empty();
        }
    };

    public static void registerVanillaProviders(RegisterCapabilitiesEvent event) {
        // Blocks
        event.registerBlock(Capabilities.ItemHandler.BLOCK, (level, pos, state, blockEntity, side) -> {
            // Invalidation is taken care of by the patches to ComposterBlock
            return ComposterWrapper.get(level, pos, side);
        }, Blocks.COMPOSTER);

        event.registerBlock(
                Capabilities.ItemHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> ((ChestBlock) state.getBlock()).combine(state, level, pos, true).apply(CHEST_COMBINED_HANDLER).orElse(null),
                Blocks.CHEST, Blocks.TRAPPED_CHEST);

        var sidedVanillaContainers = List.of(
                BlockEntityType.BLAST_FURNACE,
                BlockEntityType.BREWING_STAND,
                BlockEntityType.FURNACE,
                BlockEntityType.SMOKER,
                BlockEntityType.SHULKER_BOX);
        for (var type : sidedVanillaContainers) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, WorldlyContainerWrapper::new);
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
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (container, side) -> VanillaContainerWrapper.of(container));
        }

        // Entities
        var containerEntities = List.of(
                EntityType.ACACIA_CHEST_BOAT,
                EntityType.BIRCH_CHEST_BOAT,
                EntityType.CHERRY_CHEST_BOAT,
                EntityType.DARK_OAK_CHEST_BOAT,
                EntityType.JUNGLE_CHEST_BOAT,
                EntityType.MANGROVE_CHEST_BOAT,
                EntityType.OAK_CHEST_BOAT,
                EntityType.SPRUCE_CHEST_BOAT,
                EntityType.BAMBOO_CHEST_RAFT,
                EntityType.PALE_OAK_CHEST_BOAT,
                EntityType.CHEST_MINECART,
                EntityType.HOPPER_MINECART);

        for (var entityType : containerEntities) {
            event.registerEntity(Capabilities.ItemHandler.ENTITY, entityType, (entity, ctx) -> VanillaContainerWrapper.of(entity));
            event.registerEntity(Capabilities.ItemHandler.ENTITY_AUTOMATION, entityType, (entity, ctx) -> VanillaContainerWrapper.of(entity));
        }
        event.registerEntity(Capabilities.ItemHandler.ENTITY, EntityType.PLAYER, (player, ctx) -> PlayerInventoryWrapper.of(player));

        // Items
        event.registerItem(Capabilities.ItemHandler.ITEM, (stack, ctx) -> new ItemContainerContentsResourceHandler(ctx, DataComponents.CONTAINER, 27),
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
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            //todo possibly skip players?
            event.registerEntity(Capabilities.ItemHandler.ENTITY, entityType, (entity, ctx) -> {
                if (entity instanceof AbstractHorse horse)
                    return VanillaContainerWrapper.of(horse.getInventory());
                else if (entity instanceof LivingEntity livingEntity)
                    return EntityEquipmentItemHandler.of(livingEntity, EntityEquipmentItemHandler::isHands, EquipmentSlot::isArmor);
                return null;
            });
        }

        // Items
        for (Item item : BuiltInRegistries.ITEM) {
            if (item.getClass() == BucketItem.class)
                event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new BucketResourceHandler(ctx), item);
        }

        // We want mods to be able to override our milk cap by default
        if (NeoForgeMod.MILK.isBound()) {
            event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new BucketResourceHandler(ctx), Items.MILK_BUCKET);
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
