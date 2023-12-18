/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Defines a handler which can accept work that needs to be run synchronously, on the main thread of the game.
 */
public interface ISynchronizedWorkHandler {

    /**
     * Submits the given work to be run synchronously on the main thread of the game.
     *
     * @param task The task to run.
     * @return A future which will complete when the task has been run.
     */
    CompletableFuture<Void> submitAsync(Runnable task);

    /**
     * Submits the given work to be run synchronously on the main thread of the game.
     *
     * @param task The task to run.
     * @return A future which will complete when the task has been run.
     */
    <T> CompletableFuture<T> submitAsync(Supplier<T> task);
}
