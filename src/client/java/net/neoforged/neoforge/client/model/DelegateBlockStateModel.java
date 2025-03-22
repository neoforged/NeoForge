/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * A {@link BlockStateModel} that delegates all calls to another {@link BlockStateModel}.
 *
 * <p>There is one exception: the {@link #createGeometryKey} method, which is not delegated.
 * The default implementation of this method returns {@code null},
 * meaning that the geometry of the delegate model cannot be cached using the key.
 */
public abstract class DelegateBlockStateModel implements BlockStateModel {
    protected final BlockStateModel delegate;

    protected DelegateBlockStateModel(BlockStateModel delegate) {
        this.delegate = delegate;
    }

    @Override
    @Deprecated
    public void collectParts(RandomSource random, List<BlockModelPart> parts) {
        this.delegate.collectParts(random, parts);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        this.delegate.collectParts(level, pos, state, random, parts);
    }

    @Override
    @Deprecated
    public TextureAtlasSprite particleIcon() {
        return this.delegate.particleIcon();
    }

    @Override
    public TextureAtlasSprite particleIcon(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.delegate.particleIcon(level, pos, state);
    }
}
