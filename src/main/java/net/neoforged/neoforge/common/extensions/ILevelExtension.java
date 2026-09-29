/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.model.data.ModelDataManager;
import org.jspecify.annotations.Nullable;

public interface ILevelExtension {
    /**
     * Prefix used for all dimension based translations
     * <p>
     * All dimension translations must start with this prefix,
     * followed by the registry namespace and path.
     * <p>
     * {@code dimension.<namespace>.<path>}
     */
    String TRANSLATION_PREFIX = "dimension";

    private Level self() {
        return (Level) this;
    }

    /**
     * The maximum radius to scan for entities when trying to check bounding boxes. Vanilla's default is
     * 2.0D But mods that add larger entities may increase this.
     */
    public double getMaxEntityRadius();

    /**
     * Increases the max entity radius, this is safe to call with any value.
     * The setter will verify the input value is larger then the current setting.
     *
     * @param value New max radius to set.
     * @return The new max radius
     */
    public double increaseMaxEntityRadius(double value);

    /**
     * Retrieves the model data manager for the given level. May be null on a server level.
     *
     * <p>For model data retrieval, prefer calling {@link IBlockGetterExtension#getModelData(BlockPos)} rather than this method,
     * as it works on more than just a level.
     */
    @Nullable
    default ModelDataManager getModelDataManager() {
        return null;
    }

    /**
     * Retrieve a block capability.
     *
     * <p>If the block state and/or the block entity is known,
     * pass them via {@link #getCapability(BlockCapability, BlockPos, BlockState, BlockEntity, Object)} instead.
     */
    @Nullable
    default <T, C extends @Nullable Object> T getCapability(BlockCapability<T, C> cap, BlockPos pos, C context) {
        return cap.getCapability(self(), pos, null, null, context);
    }

    /**
     * Retrieve a block capability.
     *
     * <p>Use this override if the block state and/or the block entity is known,
     * otherwise prefer the shorter {@link #getCapability(BlockCapability, BlockPos, Object)}.
     *
     * <p>If either the block state or the block entity is unknown, simply pass {@code null}.
     * This function will fetch {@code null} parameters from the level,
     * with some extra checks to attempt to skip unnecessary fetches.
     *
     * @param state       the block state, if known, or {@code null} if unknown
     * @param blockEntity the block entity, if known, or {@code null} if unknown
     */
    @Nullable
    default <T, C extends @Nullable Object> T getCapability(BlockCapability<T, C> cap, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
        return cap.getCapability(self(), pos, state, blockEntity, context);
    }

    /**
     * Retrieve a block capability with no context.
     *
     * <p>If the block state and/or the block entity is known,
     * pass them via {@link #getCapability(BlockCapability, BlockPos, BlockState, BlockEntity)} instead.
     */
    @Nullable
    default <T> T getCapability(BlockCapability<T, @Nullable Void> cap, BlockPos pos) {
        return cap.getCapability(self(), pos, null, null, null);
    }

    /**
     * Retrieve a block capability with no context.
     *
     * <p>Use this override if the block state and/or the block entity is known,
     * otherwise prefer the shorter {@link #getCapability(BlockCapability, BlockPos)}.
     *
     * <p>If either the block state or the block entity is unknown, simply pass {@code null}.
     * This function will fetch {@code null} parameters from the level,
     * with some extra checks to attempt to skip unnecessary fetches.
     *
     * @param state       the block state, if known, or {@code null} if unknown
     * @param blockEntity the block entity, if known, or {@code null} if unknown
     */
    @Nullable
    default <T> T getCapability(BlockCapability<T, @Nullable Void> cap, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity) {
        return cap.getCapability(self(), pos, state, blockEntity, null);
    }

    /**
     * Notify all listeners that the capabilities at a specific position might have changed.
     * This includes new capabilities becoming available.
     *
     * <p>This method will only do something on {@link ServerLevel}s,
     * but it is safe to call on any {@link Level}, without the need for an {@code instanceof} check.
     *
     * <p>If you already have a block entity at that position, you can call {@link BlockEntity#invalidateCapabilities()} instead.
     */
    default void invalidateCapabilities(BlockPos pos) {}

    /**
     * Notify all listeners that the capabilities at all the positions in a chunk might have changed.
     * This includes new capabilities becoming available.
     *
     * <p>This method will only do something on {@link ServerLevel}s,
     * but it is safe to call on any {@link Level}, without the need for an {@code instanceof} check.
     */
    default void invalidateCapabilities(ChunkPos pos) {}

    /// {@return the translation key for this dimension}.
    /// Used when looking up the matching translation.
    ///
    /// @see #TRANSLATION_PREFIX
    /// @see CommonHooks#getDimensionDescriptionKey(ResourceKey)
    default String getDescriptionKey() {
        return CommonHooks.getDimensionDescriptionKey(self().dimension());
    }

    /// {@return the translated description of this dimension, with a fallback to the registry name}
    ///
    /// @see #getDescriptionKey()
    /// @see CommonHooks#getDimensionDescription(ResourceKey)
    default Component getDescription() {
        return CommonHooks.getDimensionDescription(self().dimension());
    }
}
