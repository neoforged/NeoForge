/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.Nullable;

/**
 * This event is fired whenever a block (like a sapling) grows into a feature (like a tree).
 * <p>
 * In vanilla, this fires for saplings, fungi, mushrooms and azaleas. Mods may fire it for similar blocks.
 * <p>
 * This event is only fired on the logical server.
 */
public class BlockGrowFeatureEvent extends LevelEvent implements ICancellableEvent {
    private final RandomSource rand;
    private final BlockPos pos;
    @Nullable
    private Holder<ConfiguredFeature<?, ?>> feature;

    public BlockGrowFeatureEvent(LevelAccessor level, RandomSource rand, BlockPos pos, @Nullable Holder<ConfiguredFeature<?, ?>> feature) {
        super(level);
        this.rand = rand;
        this.pos = pos;
        this.feature = feature;
    }

    /**
     * {@return the random source which initiated the sapling growth}
     */
    public RandomSource getRandom() {
        return this.rand;
    }

    /**
     * {@return the coordinates of the sapling attempting to grow}
     */
    public BlockPos getPos() {
        return pos;
    }

    /**
     * {@return the holder of the feature which will be placed, possibly null}
     */
    @Nullable
    public Holder<ConfiguredFeature<?, ?>> getFeature() {
        return feature;
    }

    /**
     * Changes the feature that will be grown. May not set
     * 
     * @param feature a {@linkplain Holder} referencing a tree feature to be placed instead of the current feature.
     */
    public void setFeature(Holder<ConfiguredFeature<?, ?>> feature) {
        this.feature = feature;
    }

    /**
     * Changes the feature that will be grown.
     * 
     * @param featureKey a {@linkplain ResourceKey} referencing a tree feature to be placed instead of the current feature.
     */
    public void setFeature(ResourceKey<ConfiguredFeature<?, ?>> featureKey) {
        this.feature = this.getLevel().registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE).getHolderOrThrow(featureKey);
    }

    /**
     * Canceling this event will prevent the feature from growing.
     * <p>
     * The original block will remain in place.
     */
    @Override
    public void setCanceled(boolean canceled) {
        ICancellableEvent.super.setCanceled(canceled);
    }
}
