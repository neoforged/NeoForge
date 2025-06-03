/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.container.adapters;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

public class EmptySnapshot extends SnapshotJournal<Object> {
    public static final EmptySnapshot INSTANCE = new EmptySnapshot();

    @Override
    protected Object createSnapshot() {
        return this;
    }

    @Override
    protected void revertToSnapshot(Object snapshot) {}

    private EmptySnapshot() {}
}
