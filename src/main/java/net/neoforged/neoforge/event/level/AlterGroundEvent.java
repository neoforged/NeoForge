/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;

import net.neoforged.neoforge.event.ForgeEventFactory;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.neoforged.neoforge.common.MinecraftForge;
import net.neoforged.bus.api.Event;

/**
 * This event is fired when {@link AlterGroundDecorator#placeBlockAt(TreeDecorator.Context, BlockPos)} attempts to alter a ground block when generating a feature.<br>
 * An example of this would be large spruce trees converting grass blocks into podzol.
 * <p>
 * This event is not {@linkplain ICancellableEvent cancellable}.
 * <p>
 * This event is fired on the {@linkplain MinecraftForge#EVENT_BUS main Forge event bus} only on the {@linkplain net.neoforged.fml.LogicalSide#SERVER logical server}.
 * <p>
 * This event is fired on worker threads, meaning it is unsafe to access external global state.<br>
 * Doing so may induce {@link ConcurrentModificationException} or deadlocks.
 */
public class AlterGroundEvent extends Event
{
    private final TreeDecorator.Context ctx;
    private final List<BlockPos> positions;

    private StateProvider provider;

    /**
     * @see {@link ForgeEventFactory#alterGround} as the API endpoint for firing this event.
     */
    @ApiStatus.Internal
    public AlterGroundEvent(TreeDecorator.Context ctx, List<BlockPos> positions, StateProvider provider)
    {
        this.ctx = ctx;
        this.positions = Collections.unmodifiableList(positions);
        this.provider = provider;
    }

    /**
     * Gets the tree decoration context for the current alteration.
     */
    public TreeDecorator.Context getContext()
    {
        return this.ctx;
    }

    /**
     * The list of positions that are considered roots is different depending on if the context has roots, logs, or both.
     * <p>
     * If {@linkplain TreeDecorator.Context#roots() roots} are not present, this list is equivalent to the {@linkplain TreeDecorator.Context#logs() logs}.<br>
     * If there are roots, and the roots have the same y-level as the lowest log, both this list is the union of both lists.<br>
     * Otherwise, this list is equal to only the roots.
     * <p>
     * In either case, only positions which match the y-level of the zeroth element will be used during placement.
     * <p>
     * This list is immutable.
     * 
     * @return The list of positions that will be used for alteration placement.
     */
    public List<BlockPos> getPositions()
    {
        return positions;
    }

    /**
     * Gets the current {@link BlockStateProvider} that will be used by the {@link AlterGroundDecorator}.
     * 
     * @return The (possibly event-modified) state provider.
     */
    public StateProvider getStateProvider()
    {
        return this.provider;
    }

    /**
     * Sets the {@link BlockStateProvider} that will be used by the {@link AlterGroundDecorator}.<br>
     * Because this may be modified by multiple mods, it is advisable to wrap {@linkplain #getStateProvider() the current provider}.
     * <p>
     * An example of wrapping the current provider is shown below: <code><pre>
     * StateProvider old = event.getStateProvider();
     * event.setStateProvider((rand, pos) -> {
     *     BlockState state = old.getState(rand, pos);
     *     return state.is(Blocks.PODZOL) ? Blocks.REDSTONE_BLOCK.defaultBlockState() : state;
     * });
     * </pre></code>
     * 
     * @param provider The new state provider.
     */
    public void setStateProvider(StateProvider provider)
    {
        this.provider = Objects.requireNonNull(provider);
    }

    @FunctionalInterface
    public interface StateProvider
    {
        /**
         * Gets the BlockState that will be placed at the passed position.
         */
        BlockState getState(RandomSource random, BlockPos state);
    }
}