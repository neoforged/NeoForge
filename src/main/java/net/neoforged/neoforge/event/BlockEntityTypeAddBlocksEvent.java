/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import com.google.common.base.Suppliers;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.mixins.BlockEntityTypeAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * Allows injecting new blocks into a block entity's {@link BlockEntityType#validBlocks} field in a safe manner.
 * The class of the newly injected block should share the highest common class that all existing blocks in the targeted validBlocks has.
 * Please use this event instead of manipulating {@link BlockEntityType} directly.
 */
public class BlockEntityTypeAddBlocksEvent extends Event implements IModBusEvent {
    public BlockEntityTypeAddBlocksEvent() {}

    /**
     * Will add the given blocks to the provided {@link BlockEntityType}'s set of valid blocks.
     * Make sure the given block is the same or derived from the highest common class of all the entries in valid blocks.
     * <p>
     * Example: If the valid blocks list has {@linkplain StandingSignBlock} entry and {@linkplain WallSignBlock} entry, the common class is {@linkplain SignBlock},
     * the given block must be a {@linkplain SignBlock} or have {@link SignBlock} as a parent class in its hierarchy.
     */
    public void modify(BlockEntityType<?> blockEntityType, Block... blocksToAdd) {
        Supplier<? extends @Nullable Class<?>> baseClass = Suppliers.memoize(() -> getCommonSuperClassForExistingValidBlocks(blockEntityType.getValidBlocks()));
        Set<Block> currentValidBlocks = new HashSet<>(blockEntityType.getValidBlocks());

        for (Block block : blocksToAdd) {
            addValidBlock(block, baseClass, currentValidBlocks);
        }

        // Set the validBlocks field without exposing a setter publicly.
        ((BlockEntityTypeAccessor) blockEntityType).neoforge$setValidBlocks(currentValidBlocks);
    }

    /**
     * Will add the given blocks to the {@link BlockEntityType}'s set of valid blocks.
     * Make sure the given block is the same or derived from the highest common class of all the entries in valid blocks.
     * <p>
     * Example: If the valid blocks list has {@linkplain StandingSignBlock} entry and {@linkplain WallSignBlock} entry, the common class is {@linkplain SignBlock},
     * the given block must be a {@linkplain SignBlock} or have {@link SignBlock} as a parent class in its hierarchy.
     */
    public void modify(ResourceKey<BlockEntityType<?>> blockEntityTypeKey, Block... blocksToAdd) {
        BuiltInRegistries.BLOCK_ENTITY_TYPE.getOptional(blockEntityTypeKey)
                .ifPresent(blockEntityType -> modify(blockEntityType, blocksToAdd));
    }

    /**
     * Will add the given blocks to the matching {@link BlockEntityType}'s set of valid blocks.
     * Make sure the given block is the same or derived from the highest common class of all the entries in valid blocks.
     * <p>
     * Example: If the valid blocks list has {@linkplain StandingSignBlock} entry and {@linkplain WallSignBlock} entry, the common class is {@linkplain SignBlock},
     * the given block must be a {@linkplain SignBlock} or have {@link SignBlock} as a parent class in its hierarchy.
     */
    public void modify(BiPredicate<ResourceKey<BlockEntityType<?>>, BlockEntityType<?>> blockEntityTypeToMatch, Block... blocksToAdd) {
        for (Map.Entry<ResourceKey<BlockEntityType<?>>, BlockEntityType<?>> blockEntityTypeEntry : BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet()) {
            if (blockEntityTypeToMatch.test(blockEntityTypeEntry.getKey(), blockEntityTypeEntry.getValue())) {
                modify(blockEntityTypeEntry.getValue(), blocksToAdd);
            }
        }
    }

    private void addValidBlock(Block block, Supplier<? extends @Nullable Class<?>> baseClass, Set<Block> currentValidBlocks) {
        if (baseClass.get() == null || baseClass.get().isAssignableFrom(block.getClass())) {
            currentValidBlocks.add(block);
        } else {
            throw new IllegalArgumentException("Given block " + block + " does not derive from existing valid block's common superclass of " + baseClass);
        }
    }

    @Nullable
    private Class<?> getCommonSuperClassForExistingValidBlocks(Set<Block> validBlocks) {
        Class<?> calculatedBaseClass = null;

        for (Block existingBlock : validBlocks) {
            if (calculatedBaseClass != null) {
                calculatedBaseClass = findClosestCommonSuper(calculatedBaseClass, existingBlock.getClass());
            } else {
                calculatedBaseClass = existingBlock.getClass();
            }
        }

        return calculatedBaseClass;
    }

    private static Class<?> findClosestCommonSuper(Class<?> superClass, Class<?> childClass) {
        while (!superClass.isAssignableFrom(childClass)) {
            superClass = superClass.getSuperclass();
        }
        return superClass;
    }
}
