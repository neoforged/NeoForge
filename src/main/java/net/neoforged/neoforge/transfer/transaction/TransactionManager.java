/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Manager for handling opening new {@link Transaction Transactions} or querying status of a transaction chain in a given thread.
 *
 * @see Transaction
 */
public final class TransactionManager {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     * Opens a new transaction with a specified parent. The example below, we open the outermost layer or the `root`.
     * <p>
     * {@link TransactionContext#ROOT} is {@code null} so they are interchangeable
     *
     * <pre>
     * {@code
     * try (var transaction = TransactionManager.open(TransactionContext.ROOT)) {
     *     // do exchanges
     * }
     * }</pre>
     *
     * @param parent the parent transaction, or null if this is the root transaction
     * @throws IllegalStateException If no parent is passed, but a transaction is already active on the current thread.
     * @throws IllegalStateException If a parent is passed, but it's not the current transaction.
     * @throws IllegalStateException If a parent is passed, but it was already closed.
     */
    public static Transaction open(@Nullable TransactionContext parent) {
        return getManagerForThread().internalOpen(parent, STACK_WALKER.getCallerClass());
    }

    /**
     * @return The current lifecycle of the transaction stack on this thread.
     */
    public static Transaction.Lifecycle getLifecycle() {
        return getManagerForThread().internalGetLifecycle();
    }

    /**
     * @return True if a transaction is open or closing on the current thread, and false otherwise.
     */
    public static boolean isActive() {
        return getLifecycle() != TransactionContext.Lifecycle.NONE;
    }

    // Internal calls //

    private static final ThreadLocal<TransactionManager> MANAGERS = ThreadLocal.withInitial(TransactionManager::new);
    @ApiStatus.Internal
    final Thread thread = Thread.currentThread();
    @ApiStatus.Internal
    final List<Transaction> stack = new ArrayList<>();
    @ApiStatus.Internal
    final List<TransactionContext.RootCloseCallback> rootCloseCallbacks = new ArrayList<>();
    @ApiStatus.Internal
    int currentDepth = -1;
    @ApiStatus.Internal
    final Int2ObjectMap<Class<?>> debugMap = new Int2ObjectOpenHashMap<>();

    @ApiStatus.Internal
    boolean isOpen() {
        return currentDepth > -1;
    }

    /**
     * @return The manager for the current thread.
     */
    @ApiStatus.Internal
    static TransactionManager getManagerForThread() {
        return MANAGERS.get();
    }

    /**
     * Changing what is printed here will affect all places we debug
     */
    static String debugNameFrom(@Nullable Class<?> callerClass) {
        if (callerClass == null) return "null";
        return callerClass.toString();
    }

    @ApiStatus.Internal
    Transaction internalOpen(@Nullable TransactionContext parent, Class<?> callerClass) {
        if (parent != TransactionContext.ROOT) {
            Transaction parentImpl = (Transaction) parent;
            parentImpl.validateCurrentTransaction();
            parentImpl.validateOpen();
        } else if (isOpen()) {
            throw new IllegalStateException("A root transaction is already active on this thread " + thread + " when `" + debugNameFrom(callerClass) + "` tried to open.");
        }

        Transaction current;
        if (stack.size() == ++currentDepth) {
            current = new Transaction(this, currentDepth);
            stack.add(current);
        } else {
            current = stack.get(currentDepth);
        }
        debugMap.put(currentDepth, callerClass);
        current.lifecycle = TransactionContext.Lifecycle.OPEN;
        return current;
    }

    @ApiStatus.Internal
    Transaction.Lifecycle internalGetLifecycle() {
        return currentDepth == -1 ? TransactionContext.Lifecycle.NONE : stack.get(currentDepth).lifecycle;
    }

    private TransactionManager() {}
}
