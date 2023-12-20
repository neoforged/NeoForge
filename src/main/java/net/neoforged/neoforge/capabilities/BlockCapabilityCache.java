/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * A cache for block capabilities, to be used to track capabilities at a specific position, with a specific context.
 *
 * <p>The cache is invalidated when the level is notified of a change via {@link Level#invalidateCapabilities(BlockPos)}.
 *
 * <p>Instances are automatically cleared by the garbage collector when they are no longer in use.
 */
public final class BlockCapabilityCache<T, C> {
    /**
     * Creates a new cache instance and registers it to the level.
     *
     * @param capability the capability
     * @param level      the level
     * @param pos        the position
     * @param context    extra context for the query
     */
    public static <T, C> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context) {
        return create(capability, level, pos, context, () -> true, () -> {});
    }

    /**
     * Creates a new cache instance with an invalidation listener, and registers it to the level.
     *
     * <p>A few details regarding the system:
     * <ul>
     * <li>Calling {@link #getCapability()} from the invalidation listener is not supported,
     * as the block being invalidated might not be ready to be queried again yet.
     * If you receive an invalidation notification, you should wait for some time
     * (e.g. until your own tick) before checking {@link #getCapability()} again.</li>
     * <li>In general, do not perform any level access for the listener.
     * The listener itself might be in a chunk that is being unloaded, for example.</li>
     * <li>The listener does not receive notifications before {@link #getCapability()} is called.
     * After each invalidation, {@link #getCapability()} must be called again to enable further notifications.</li>
     * </ul>
     *
     * @param capability           the capability
     * @param level                the level
     * @param pos                  the position
     * @param context              extra context for the query
     * @param isValid              a function to check if the listener still wants to receive notifications.
     *                             A typical example would be {@code () -> !this.isRemoved()} for a block entity
     *                             that should not receive invalidation notifications anymore once it is removed.
     * @param invalidationListener the invalidation listener. Will be called whenever the capability of the cache might have changed.
     */
    public static <T, C> BlockCapabilityCache<T, C> create(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context, BooleanSupplier isValid, Runnable invalidationListener) {
        Objects.requireNonNull(capability);
        Objects.requireNonNull(isValid);
        Objects.requireNonNull(invalidationListener);
        pos = pos.immutable();

        var cache = new BlockCapabilityCache<>(capability, level, pos, context, isValid, invalidationListener);
        level.registerCapabilityListener(pos, cache.listener);
        return cache;
    }

    private final BlockCapability<T, C> capability;
    private final ServerLevel level;
    private final BlockPos pos;
    private final C context;

    /**
     * {@code true} if notifications received by the cache will be forwarded to {@link #listener}.
     * By default and after each invalidation, this is set to {@code false}.
     * Calling {@link #getCapability()} sets it to {@code true}.
     */
    private boolean cacheValid = false;
    @Nullable
    private T cachedCap = null;

    private boolean canQuery = true;
    private final ICapabilityInvalidationListener listener;

    private BlockCapabilityCache(BlockCapability<T, C> capability, ServerLevel level, BlockPos pos, C context, BooleanSupplier isValid, Runnable invalidationListener) {
        this.capability = capability;
        this.level = level;
        this.pos = pos;
        this.context = context;

        this.listener = () -> {
            if (!cacheValid) {
                // already invalidated, just check if the cache should be removed
                return isValid.getAsBoolean();
            }

            // disable queries for now
            canQuery = false;
            // mark cached cap as invalid
            cacheValid = false;

            if (isValid.getAsBoolean()) {
                // notify
                invalidationListener.run();
                // re-enable queries
                canQuery = true;
                return true;
            } else {
                // not valid anymore: keep queries disabled and return false
                return false;
            }
        };
    }

    public ServerLevel level() {
        return level;
    }

    public BlockPos pos() {
        return pos;
    }

    public C context() {
        return context;
    }

    /**
     * Gets the capability instance, or {@code null} if the capability is not present.
     *
     * <p>If {@linkplain #pos() the target position} is not loaded, this method will return {@code null}.
     */
    @Nullable
    public T getCapability() {
        if (!canQuery)
            throw new IllegalStateException("Do not call getCapability on an invalid cache or from the invalidation listener!");

        if (!cacheValid) {
            if (!level.isLoaded(pos)) {
                // If the position is not loaded, return no capability for now.
                // The cache will be invalidated when the chunk is loaded.
                cachedCap = null;
            } else {
                cachedCap = level.getCapability(capability, pos, context);
            }
            cacheValid = true;
        }

        return cachedCap;
    }
}
