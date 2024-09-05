/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.registries.deferred;

import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Special {@link DeferredHolder} for {@link BlockEntityType BlockEntityTypes}.
 *
 * @param <TBlockEntity> The specific {@link BlockEntityType}.
 */
public class DeferredBlockEntityType<TBlockEntity extends BlockEntity> extends DeferredHolder<BlockEntityType<?>, BlockEntityType<TBlockEntity>> {
    protected DeferredBlockEntityType(ResourceKey<BlockEntityType<?>> key) {
        super(key);
    }

    /**
     * Returns true if the given {@link BlockState} is valid for this {@link BlockEntityType}.
     *
     * @param blockState {@link BlockState} to validate.
     * @return {@code true} if {@link BlockState} is valid.
     */
    public boolean isValid(BlockState blockState) {
        return value().isValid(blockState);
    }

    /**
     * Returns true if the given {@link Block} is valid for this {@link BlockEntityType}.
     *
     * @param block {@link Block} to validate.
     * @return {@code true} if {@link Block} is valid.
     */
    public boolean isValid(Block block) {
        return value().getValidBlocks().contains(block);
    }

    /**
     * Looks up {@link BlockEntity} in world at given position returning if it matches this {@link BlockEntityType}.
     *
     * @param level The level to lookup the {@link BlockEntity} from.
     * @param pos   The position to lookup the {@link BlockEntity} at.
     * @return {@link BlockEntity} matching this type or null if none exists.
     */
    @Nullable
    public TBlockEntity get(BlockGetter level, BlockPos pos) {
        return value().getBlockEntity(level, pos);
    }

    /**
     * Looks up {@link BlockEntity} in world at given position returning if it matches this {@link BlockEntityType}.
     *
     * @param level The level to lookup the {@link BlockEntity} from.
     * @param pos   The position to lookup the {@link BlockEntity} at.
     * @return {@link BlockEntity} matching this type.
     * @throws NullPointerException if no matching {@link BlockEntity} could be found.
     */
    public TBlockEntity getOrThrow(BlockGetter level, BlockPos pos) {
        return Objects.requireNonNull(get(level, pos), () -> "Invalid or no BlockEntity as position: " + pos.toShortString());
    }

    /**
     * Looks up {@link BlockEntity} in world at given position returning if it matches this {@link BlockEntityType}.
     *
     * @param level The level to lookup the {@link BlockEntity} from.
     * @param pos   The position to lookup the {@link BlockEntity} at.
     * @return {@link BlockEntity} matching this type or {@link Optional#empty()} if none exists.
     */
    public Optional<TBlockEntity> find(BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos, value());
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the specified {@link BlockEntityType}.
     *
     * @param <TBlockEntity> The type of the target {@link BlockEntityType}.
     * @param registryKey    The resource key of the target {@link BlockEntityType}.
     */
    public static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> createBlockEntityType(ResourceKey<BlockEntityType<?>> registryKey) {
        return new DeferredBlockEntityType<>(registryKey);
    }

    /**
     * Creates a new {@link DeferredHolder} targeting the {@link BlockEntityType} with the specified name.
     *
     * @param <TBlockEntity> The type of the target {@link BlockEntityType}.
     * @param registryName   The name of the target {@link BlockEntityType}.
     */
    public static <TBlockEntity extends BlockEntity> DeferredBlockEntityType<TBlockEntity> createBlockEntityType(ResourceLocation registryName) {
        return createBlockEntityType(ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, registryName));
    }
}
