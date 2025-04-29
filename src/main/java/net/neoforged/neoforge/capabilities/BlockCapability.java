/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.TriState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.EventPriority;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * A {@code BlockCapability} gives flexible access to objects of type {@code T} located in the world.
 *
 * <h3>Querying a block capability</h3>
 * <p>To get an object of type {@code T}, use {@link Level#getCapability(BlockCapability, BlockPos, Object)}.
 * For example, to query an item handler in the world, from a specific side:
 * 
 * <pre>{@code
 * Level level = ...;
 * BlockPos pos = ...;
 * Direction side = ...;
 *
 * IItemHandler maybeHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, side);
 * if (maybeHandler != null) {
 *     // Use maybeHandler
 * }
 * }</pre>
 * 
 * <p>For repeated queries at a specific position, use {@link BlockCapabilityCache} to improve performance.
 *
 * <h3>Providing a capability for a block entity</h3>
 * <p>To provide objects of type {@code T}, register providers to {@link RegisterCapabilitiesEvent}. For example:
 * 
 * <pre>{@code
 * modBus.addListener(RegisterCapabilitiesEvent.class, event -> {
 *     event.registerBlockEntity(
 *         Capabilities.ItemHandler.BLOCK, // capability to register for
 *         MY_BLOCK_ENTITY_TYPE,
 *         (myBlockEntity, side) -> <return the IItemHandler for myBlockEntity and side>);
 * });
 * }</pre>
 * 
 * <p><b>If a previously returned capability is not valid anymore, or if a new capability is available,
 * {@link Level#invalidateCapabilities(BlockPos)} MUST be called to notify the caches (see below).</b>
 *
 * <h3>Providing a capability for a plain block</h3>
 * For blocks without a block entity,
 * we use {@link RegisterCapabilitiesEvent#registerBlock registerBlock} instead:
 *
 * <pre>{@code
 * modBus.addListener(RegisterCapabilitiesEvent.class, event -> {
 *     event.registerBlock(
 *         Capabilities.ItemHandler.BLOCK, // capability to register for
 *         (level, pos, state, be, side) -> <return the IItemHandler>,
 *         // blocks to register for
 *         MY_ITEM_HANDLER_BLOCK, MY_OTHER_ITEM_HANDLER_BLOCK);
 * });
 * }</pre>
 *
 * <p>Plain blocks must invalidate their capabilities whenever they change, <b>including on placement and removal</b>. For example:
 *
 * <pre>{@code
 * public class MyBlock extends Block {
 *     ＠Override
 *     public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
 *         // Invalidate capabilities on block placement or block state change
 *         level.invalidateCapabilities(pos);
 *     }
 *
 *     ＠Override
 *     public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
 *         super.onRemove(state, level, pos, newState, movedByPiston);
 *         // Invalidate capabilities on block removal or block state change
 *         level.invalidateCapabilities(pos);
 *     }
 * }
 * }</pre>
 *
 * @param <T> type of queried objects
 * @param <C> type of the additional context
 */
public final class BlockCapability<T, C extends @Nullable Object> extends BaseCapability<T, C> {
    /**
     * Creates a new block capability, or gets it if it already exists.
     *
     * @param name         name of the capability
     * @param typeClass    type of the queried API
     * @param contextClass type of the additional context
     */
    public static <T, C extends @Nullable Object> BlockCapability<T, C> create(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        return (BlockCapability<T, C>) registry.create(name, typeClass, contextClass);
    }

    /**
     * Creates a new block capability with {@code Void} context, or gets it if it already exists.
     * This should be used for capabilities that do not require any additional context.
     *
     * @see #create(ResourceLocation, Class, Class)
     */
    public static <T> BlockCapability<T, @Nullable Void> createVoid(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, void.class);
    }

    /**
     * Creates a new block capability with nullable {@code Direction} context, or gets it if it already exists.
     */
    public static <T> BlockCapability<T, @Nullable Direction> createSided(ResourceLocation name, Class<T> typeClass) {
        return create(name, typeClass, Direction.class);
    }

    /**
     * {@return a new immutable copy of all the currently known block capabilities}
     *
     * <p>Mods that want to forward "all" capability requests should likely use {@link #getAllProxyable()} instead.
     */
    public static synchronized List<BlockCapability<?, ?>> getAll() {
        return registry.getAll();
    }

    /**
     * {@return a new immutable copy of all the currently known proxyable block capabilities}
     *
     * <p>This method should typically only be used in the {@link EventPriority#LOW} or
     * {@link EventPriority#LOWEST} phase of {@link RegisterCapabilitiesEvent}, or later,
     * to ensure that all proxyable capabilities have been {@link RegisterCapabilitiesEvent#setProxyable marked as such}.
     *
     * @see #isProxyable()
     */
    public static synchronized List<BlockCapability<?, ?>> getAllProxyable() {
        return registry.getAll().stream().filter(BlockCapability::isProxyable).toList();
    }

    /**
     * Returns whether this capability is proxyable.
     * This information is metadata: it does not change how the capability works internally,
     * but it tells mods whether they should or should not register proxying capability providers.
     *
     * <p>If the capability is proxyable, requests for this capability are safe to forward unilaterally to other blocks.
     *
     * <p>If the capability is not proxyable, requests for this capability should not be forwarded to other blocks without further information.
     * In that case, refer to documentation of the capability to understand under which circumstances it is safe to forward, if at all.
     * For this reason, mods that forward "all" capabilities should not forward non-proxyable capabilities.
     *
     * <p>Block capabilities are not proxyable by default.
     * Any call to {@link RegisterCapabilitiesEvent#setProxyable(BlockCapability)} will mark the capability as proxyable.
     * Any call to {@link RegisterCapabilitiesEvent#setNonProxyable(BlockCapability)} will mark the capability as non-proxyable,
     * and prevent it from being marked as proxyable.
     */
    public boolean isProxyable() {
        return proxyable.isTrue();
    }

    // INTERNAL

    // Requires explicitly-typed constructor due to ECJ inference failure.
    private static final CapabilityRegistry<BlockCapability<?, ?>> registry = new CapabilityRegistry<BlockCapability<?, ?>>(BlockCapability::new);

    private BlockCapability(ResourceLocation name, Class<T> typeClass, Class<C> contextClass) {
        super(name, typeClass, contextClass);
    }

    final Map<Block, List<IBlockCapabilityProvider<T, C>>> providers = new IdentityHashMap<>();
    private TriState proxyable = TriState.DEFAULT; // default is false

    void setProxyable(boolean proxyable) {
        if (CapabilityHooks.initFinished) {
            throw new IllegalStateException("Cannot call setProxyable after the RegisterCapabilitiesEvent has been fired.");
        }
        switch (this.proxyable) {
            case DEFAULT -> this.proxyable = proxyable ? TriState.TRUE : TriState.FALSE;
            case TRUE -> {
                if (!proxyable) {
                    throw new IllegalStateException("Cannot make capability %s non-proxyable because it was already set to be proxyable.".formatted(name()));
                }
            }
            case FALSE -> {
                if (proxyable) {
                    throw new IllegalStateException("Cannot make capability %s proxyable because it was already set to be non-proxyable.".formatted(name()));
                }
            }
        }
    }

    @ApiStatus.Internal
    @Nullable
    public T getCapability(Level level, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context) {
        // Convert pos to immutable, it's easy to forget otherwise
        pos = pos.immutable();

        // Get block state and block entity if they were not provided
        if (blockEntity == null) {
            if (state == null)
                state = level.getBlockState(pos);

            if (state.hasBlockEntity())
                blockEntity = level.getBlockEntity(pos);
        } else {
            if (state == null)
                state = blockEntity.getBlockState();
        }

        for (var provider : providers.getOrDefault(state.getBlock(), List.of())) {
            var ret = provider.getCapability(level, pos, state, blockEntity, context);
            if (ret != null)
                return ret;
        }
        return null;
    }
}
