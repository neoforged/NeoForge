/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.registration;

import net.neoforged.neoforge.network.handling.IPayloadHandler;

/**
 * Used by {@link PayloadRegistrar} to declare the default handling thread for registered {@link IPayloadHandler}s.
 */
public enum HandlerThread {
    /**
     * The main thread of the receiving side.
     * <p>
     * On the logical client, this is the Render Thread.
     * <p>
     * On the logical server, this is the Server Thread.
     */
    MAIN,

    /**
     * The network thread, which executes concurrently to the main thread.
     */
    NETWORK;
}
