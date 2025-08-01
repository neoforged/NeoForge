/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.transaction;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Manager for handling opening new {@link Transaction Transactions} or querying status of a transaction chain in a given thread.
 *
 * @see Transaction
 */
final class TransactionManager {
    private static final ThreadLocal<TransactionManager> MANAGERS = ThreadLocal.withInitial(TransactionManager::new);
    final Thread thread = Thread.currentThread();
    final List<Transaction> stack = new ArrayList<>();
    final List<SnapshotJournal<?>> closeableJournals = new ArrayList<>();
    int currentDepth = -1;
    final Int2ObjectMap<Class<?>> debugMap = new Int2ObjectOpenHashMap<>();

    boolean isOpen() {
        return currentDepth > -1;
    }

    /**
     * @return The manager for the current thread.
     */
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

    Transaction open(@Nullable TransactionContext parent, Class<?> callerClass) {
        if (parent != null) {
            Transaction parentImpl = (Transaction) parent;
            parentImpl.validateCurrentTransaction();
            parentImpl.validateOpen();
        } else if (isOpen()) {
            String currentRoot = debugMap.get(0).toString();
            throw new IllegalStateException("A root transaction of `" + currentRoot + "` is already active on this thread " + thread + " when `" + debugNameFrom(callerClass) + "` tried to open.");
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

    private TransactionManager() {}
}
