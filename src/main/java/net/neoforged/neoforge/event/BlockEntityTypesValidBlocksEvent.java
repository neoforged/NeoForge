/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
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
    private final Supplier<? extends Class<?>> baseClass = Suppliers.memoize(this::setCommonSuperClassForExistingValidBlocks);

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
        if (this.baseClass.get() == null || this.baseClass.get().isAssignableFrom(block.getClass())) {
            this.currentValidBlocks.add(block);
        } else {
            throw new IllegalArgumentException("Given block " + block + " does not derive from existing valid block's common superclass of " + this.baseClass);
        }
    }

    @Nullable
    private Class<?> setCommonSuperClassForExistingValidBlocks() {
        Class<?> calculatedBaseClass = null;

        for (Block existingBlock : this.currentValidBlocks) {
            if (calculatedBaseClass != null) {
                calculatedBaseClass = findClosestCommonSuper(calculatedBaseClass, existingBlock.getClass());
            } else {
                calculatedBaseClass = existingBlock.getClass();
            }
        }

        return calculatedBaseClass;
    }

    private static Class<?> findClosestCommonSuper(Class<?> a, Class<?> b) {
        while (!a.isAssignableFrom(b)) {
            a = a.getSuperclass();
        }
        return a;
    }

    @EventBusSubscriber(modid = "neoforge", bus = EventBusSubscriber.Bus.MOD)
    private static class CommonHandler {
        private static final MethodHandle METHOD_HANDLE;

        static {
            try {
                METHOD_HANDLE = MethodHandles.privateLookupIn(BlockEntityType.class, MethodHandles.lookup()).findSetter(BlockEntityType.class, "validBlocks", Set.class);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @SubscribeEvent
        private static void onCommonSetup(FMLCommonSetupEvent event) throws Throwable {
            for (Map.Entry<ResourceKey<BlockEntityType<?>>, BlockEntityType<?>> blockEntityTypeEntry : BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()) {
                BlockEntityTypesValidBlocksEvent blockEntityTypesValidBlocksEvent = new BlockEntityTypesValidBlocksEvent(blockEntityTypeEntry.getKey(), blockEntityTypeEntry.getValue());
                ModLoader.postEventWrapContainerInModOrder(blockEntityTypesValidBlocksEvent); // Allow modders to add to the list in the events.
                METHOD_HANDLE.invoke(blockEntityTypeEntry.getValue(), blockEntityTypesValidBlocksEvent.getCurrentValidBlocks()); // Set the validBlocks field without exposing a setter publicly.
            }
        }
    }
}
