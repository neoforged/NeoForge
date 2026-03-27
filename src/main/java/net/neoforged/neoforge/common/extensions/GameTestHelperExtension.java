/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jspecify.annotations.Nullable;

/**
 * Additional functionality for {@link net.minecraft.gametest.framework.GameTestHelper}
 */
public interface GameTestHelperExtension {
    private GameTestHelper self() {
        return (GameTestHelper) this;
    }

    /**
     * Same as {@link GameTestHelper#fail(Component, BlockPos)}, but for untranslated messages.
     */
    default void fail(String message, BlockPos pos) {
        self().fail(Component.literal(message), pos);
    }

    /**
     * Same as {@link GameTestHelper#fail(Component, Entity)}, but for untranslated messages.
     */
    default void fail(String message, Entity entity) {
        self().fail(Component.literal(message), entity);
    }

    /**
     * Gets a capability from the given relative position with the given context.
     */
    @Nullable
    default <T, C extends @Nullable Object> T getCapability(BlockCapability<T, C> cap, BlockPos pos, C context) {
        return self().getLevel().getCapability(cap, self().absolutePos(pos), context);
    }

    /**
     * Gets a capability from the given relative position and fails the test, if it does not exist.
     */
    default <T, C extends @Nullable Object> T requireCapability(BlockCapability<T, C> cap, BlockPos pos, C context) {
        var capability = getCapability(cap, pos, context);
        if (capability == null) {
            throw self().assertionException(pos, "Expected capability %s but there was none", cap);
        }
        return capability;
    }
}
