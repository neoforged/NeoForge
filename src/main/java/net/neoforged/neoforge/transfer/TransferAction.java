/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

public enum TransferAction {
    EXECUTE,
    SIMULATE;

    public boolean isSimulating() {
        return this == SIMULATE;
    }

    public boolean isExecuting() {
        return this == EXECUTE;
    }
}
