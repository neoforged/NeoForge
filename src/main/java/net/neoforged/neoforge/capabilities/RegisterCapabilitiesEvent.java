/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import java.util.ArrayList;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

/**
 * Fired to register capability providers at an appropriate time.
 */
public class RegisterCapabilitiesEvent extends Event implements IModBusEvent {
    RegisterCapabilitiesEvent() {}

    // BLOCKS

    /**
     * Register a capability provider for some blocks.
     *
     * <p><b>If a previously returned capability is not valid anymore, or if a new capability is available,
     * {@link Level#invalidateCapabilities(BlockPos)} MUST be called to notify the caches.</b>
     * See {@link IBlockCapabilityProvider} for details.
     */
    public <T, C> void registerBlock(BlockCapability<T, C> capability, IBlockCapabilityProvider<T, C> provider, Block... blocks) {
        Objects.requireNonNull(provider);
        // Probably a programmer error
        if (blocks.length == 0)
            throw new IllegalArgumentException("Must register at least one block");

        for (Block block : blocks) {
            Objects.requireNonNull(block);
            capability.providers.computeIfAbsent(block, b -> new ArrayList<>()).add(provider);
        }
    }

    /**
     * Register a capability provider for a block entity type.
     *
     * <p><b>If a previously returned capability is not valid anymore, or if a new capability is available,
     * {@link Level#invalidateCapabilities(BlockPos)} MUST be called to notify the caches.</b>
     * See {@link IBlockCapabilityProvider} for details.
     */
    public <T, C, BE extends BlockEntity> void registerBlockEntity(BlockCapability<T, C> capability, BlockEntityType<BE> blockEntityType, ICapabilityProvider<? super BE, C, T> provider) {
        Objects.requireNonNull(provider);

        IBlockCapabilityProvider<T, C> adaptedProvider = (level, pos, state, blockEntity, context) -> {
            // The block entity type can change, so we also check for that.
            if (blockEntity == null || blockEntity.getType() != blockEntityType)
                return null;
            return provider.getCapability((BE) blockEntity, context);
        };

        for (Block block : blockEntityType.getValidBlocks()) {
            Objects.requireNonNull(block);
            capability.providers.computeIfAbsent(block, b -> new ArrayList<>()).add(adaptedProvider);
        }
    }

    /**
     * Return {@code true} if a provider is registered for the given block and capability.
     */
    public boolean isBlockRegistered(BlockCapability<?, ?> capability, Block block) {
        Objects.requireNonNull(block);
        return capability.providers.containsKey(block);
    }

    // ENTITIES

    /**
     * Register a capability provider for some entity type.
     */
    public <T, C, E extends Entity> void registerEntity(EntityCapability<T, C> capability, EntityType<E> entityType, ICapabilityProvider<? super E, C, T> provider) {
        Objects.requireNonNull(provider);
        capability.providers.computeIfAbsent(entityType, et -> new ArrayList<>()).add((ICapabilityProvider<Entity, C, T>) provider);
    }

    /**
     * Return {@code true} if a provider is registered for the given entity type and capability.
     */
    public boolean isEntityRegistered(EntityCapability<?, ?> capability, EntityType<?> entityType) {
        Objects.requireNonNull(entityType);
        return capability.providers.containsKey(entityType);
    }

    // ITEMS

    /**
     * Register a capability provider for some items.
     */
    public <T, C> void registerItem(ItemCapability<T, C> capability, ICapabilityProvider<ItemStack, C, T> provider, ItemLike... items) {
        Objects.requireNonNull(provider);
        // Probably a programmer error
        if (items.length == 0)
            throw new IllegalArgumentException("Must register at least one item");

        for (ItemLike itemLike : items) {
            Item item = Objects.requireNonNull(itemLike.asItem());
            capability.providers.computeIfAbsent(item, i -> new ArrayList<>()).add(provider);
        }
    }

    /**
     * Return {@code true} if a provider is registered for the given item and capability.
     */
    public boolean isItemRegistered(ItemCapability<?, ?> capability, Item item) {
        Objects.requireNonNull(item);
        return capability.providers.containsKey(item);
    }
}
