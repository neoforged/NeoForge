/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.wrappers.items;

import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.function.BooleanSupplier;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;

public final class HopperJournal extends SnapshotJournal<Boolean> {
    private final BooleanConsumer setter;
    private final BooleanSupplier getter;

    public HopperJournal(BooleanConsumer setter, BooleanSupplier getter) {
        this.setter = setter;
        this.getter = getter;
    }

    @Override
    protected Boolean createSnapshot() {
        return getter.getAsBoolean();
    }

    @Override
    protected void revertToSnapshot(Boolean snapshot) {
        setter.accept(snapshot.booleanValue());
    }
}
