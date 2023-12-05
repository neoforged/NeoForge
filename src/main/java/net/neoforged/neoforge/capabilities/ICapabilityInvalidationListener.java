/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.capabilities;

import net.minecraft.server.level.ServerLevel;

/**
 * A listener for block capability invalidation.
 *
 * <p>Register with {@link ServerLevel#registerCapabilityListener}.
 *
 * <p>The listener will be held by a weak reference, so it is important to keep a strong reference to it
 * as long as you need it.
 */
@FunctionalInterface
public interface ICapabilityInvalidationListener {
    /**
     * Called when capabilities are invalidated.
     *
     * <p>The listener should check that it is valid before reacting to this notification,
     * and if it is not valid anymore, it should return {@code false}.
     *
     * @return {@code true} if the listener is still valid, {@code false} if it should be removed from the list of listeners.
     */
    boolean onInvalidate();
}
