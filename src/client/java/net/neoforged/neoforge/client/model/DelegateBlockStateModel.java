/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.model;

import java.util.List;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
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
    public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
        this.delegate.collectParts(random, parts);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        this.delegate.collectParts(level, pos, state, random, parts);
    }

    @Override
    @Deprecated
    public Material.Baked particleMaterial() {
        return this.delegate.particleMaterial();
    }

    @Override
    public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.delegate.particleMaterial(level, pos, state);
    }

    @Override
    @Deprecated
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return this.delegate.materialFlags();
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.delegate.materialFlags(level, pos, state);
    }
}
