/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Represents an action that can be taken when transferring resources.
 * <br>
 * {@link #SIMULATE} will simulate the action, allowing the caller to determine the outcome without actually performing the action.
 * <br>
 * {@link #EXECUTE} will actually perform the action.
 */
public enum TransferAction {
    EXECUTE,
    SIMULATE;

    /**
     * @return {@code true} if this action is simulating, {@code false} if it is executing.
     */
    public boolean isSimulating() {
        return this == SIMULATE;
    }

    /**
     * @return {@code true} if this action is executing, {@code false} if it is simulating.
     */
    public boolean isExecuting() {
        return this == EXECUTE;
    }

    /**
     * Helper to combines this action with another {@link TransferAction}. This allows easily compounding actions.
     *
     * @return Compounded action.
     */
    public TransferAction combine(TransferAction other) {
        return get(other.isExecuting() && isExecuting());
    }

    public boolean commit(Transaction context) {
        if (isExecuting())
            context.commit();
        return true;
    }

    /**
     * Helper to get an action based on a boolean representing execution.
     *
     * @param execute {@code true} for {@link #EXECUTE}.
     * @return Action.
     */
    public static TransferAction get(boolean execute) {
        return execute ? EXECUTE : SIMULATE;
    }
}
