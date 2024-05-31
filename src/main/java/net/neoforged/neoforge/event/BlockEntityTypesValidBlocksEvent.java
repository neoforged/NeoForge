/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.Nullable;

public class BlockEntityTypesValidBlocksEvent extends Event implements IModBusEvent {
    private final ResourceKey<BlockEntityType<?>> blockEntityTypeResourceKey;
    private final BlockEntityType<?> blockEntityType;
    private final Set<Block> currentValidBlocks;
    @Nullable
    private Class<?> baseClass = null;

    protected BlockEntityTypesValidBlocksEvent(ResourceKey<BlockEntityType<?>> blockEntityTypeResourceKey, BlockEntityType<?> blockEntityType) {
        this.blockEntityTypeResourceKey = blockEntityTypeResourceKey;
        this.blockEntityType = blockEntityType;
        this.currentValidBlocks = new HashSet<>(blockEntityType.getValidBlocks());
    }

    /**
     * The {@link ResourceKey} that this current {@link BlockEntityType} was registered under.
     */
    public ResourceKey<BlockEntityType<?>> getBlockEntityTypeResourceKey() {
        return blockEntityTypeResourceKey;
    }

    /**
     * The current {@link BlockEntityType}.
     */
    public BlockEntityType<?> getBlockEntityType() {
        return blockEntityType;
    }

    /**
     * An immutable list of the list of valid blocks that will be assigned to replace {@link BlockEntityType#validBlocks}'s value.
     */
    public Set<Block> getCurrentValidBlocks() {
        return Collections.unmodifiableSet(this.currentValidBlocks);
    }

    /**
     * Will add the given block to the list of valid blocks we will assign to the current {@link BlockEntityType}.
     * Make sure the given block is the same or derived from the highest common class of all the entries in valid blocks.
     * <p></p>
     * Example: If the valid blocks list has {@link StandingSignBlock} entry and {@link WallSignBlock} entry, the common class is SignBlock
     * and the given block must be a {@link SignBlock} or has {@link SignBlock} as a child class in its hierarchy.
     * 
     * @param block The block to add as a valid block for the current {@link BlockEntityType}
     */
    public void addValidBlock(Block block) {
        setCommonSuperClassForExistingValidBlocks();

        if (this.baseClass == null || this.baseClass.isAssignableFrom(block.getClass())) {
            this.currentValidBlocks.add(block);
        } else {
            throw new IllegalArgumentException("Given block " + block + " does not derive from existing valid block's common superclass of " + this.baseClass);
        }
    }

    private void setCommonSuperClassForExistingValidBlocks() {
        if (this.baseClass == null) {
            for (Block existingBlock : this.currentValidBlocks) {
                if (this.baseClass != null) {
                    if (!existingBlock.getClass().isAssignableFrom(this.baseClass)) {
                        this.baseClass = findClosestCommonSuper(existingBlock.getClass(), this.baseClass);
                    }
                } else {
                    this.baseClass = existingBlock.getClass();
                }
            }
        }
    }

    private static Class<?> findClosestCommonSuper(Class<?> a, Class<?> b) {
        while (!a.isAssignableFrom(b)) {
            a = a.getSuperclass();
        }
        return a;
    }

    @EventBusSubscriber(modid = "neoforge", bus = EventBusSubscriber.Bus.MOD)
    private static class CommonHandler {
        @SubscribeEvent
        private static void onCommonSetup(FMLCommonSetupEvent event) {
            for (Map.Entry<ResourceKey<BlockEntityType<?>>, BlockEntityType<?>> blockEntityTypeEntry : BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()) {
                BlockEntityTypesValidBlocksEvent blockEntityTypesValidBlocksEvent = new BlockEntityTypesValidBlocksEvent(blockEntityTypeEntry.getKey(), blockEntityTypeEntry.getValue());
                ModLoader.postEventWrapContainerInModOrder(blockEntityTypesValidBlocksEvent); // Allow modders to add to the list in the events.
                blockEntityTypeEntry.getValue().setValidBlocks(blockEntityTypesValidBlocksEvent.getCurrentValidBlocks()); // Update the block entity type's valid blocks to the new modified list.
            }
        }
    }
}
