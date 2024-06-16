/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

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
}
