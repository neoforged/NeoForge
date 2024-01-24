/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.network.handling;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Defines a replyHandler which can accept work that needs to be run synchronously, on the main thread of the game.
 */
public interface ISynchronizedWorkHandler {
    /**
     * Executes a task on the main thread of the game.
     * <p>
     * The runnable task is protected against exceptions, and any exceptions thrown will be logged.
     * </p>
     *
     * @param task The task to run.
     */
    void execute(Runnable task);

    /**
     * Submits the given work to be run synchronously on the main thread of the game.
     * <p>
     * This method will <bold>not</bold> be guarded against exceptions.
     * <br>
     * If you need to guard against exceptions, call {@link CompletableFuture#exceptionally(Function)},
     * {@link CompletableFuture#exceptionallyAsync(Function)}}, or derivatives on the returned future.
     * </p>
     * 
     * @param task The task to run.
     */
    CompletableFuture<Void> submitAsync(Runnable task);

    /**
     * Submits the given work to be run synchronously on the main thread of the game.
     * <p>
     * This method will <bold>not</bold> be guarded against exceptions.
     * <br>
     * If you need to guard against exceptions, call {@link CompletableFuture#exceptionally(Function)},
     * {@link CompletableFuture#exceptionallyAsync(Function)}}, or derivatives on the returned future.
     * </p>
     *
     * @param task The task to run.
     * @return A future which will complete when the task has been run.
     */
    <T> CompletableFuture<T> submitAsync(Supplier<T> task);
}
