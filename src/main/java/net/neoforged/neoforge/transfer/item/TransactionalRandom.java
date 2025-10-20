/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.item;

import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.ApiStatus;

/**
 * A deterministic random source whose internal state is transactional,
 * ensuring determinism across transactions until one is committed.
 */
@ApiStatus.Internal
public class TransactionalRandom extends SnapshotJournal<Long> {
    private long seed = RandomSupport.generateUniqueSeed();

    @Override
    protected Long createSnapshot() {
        return seed;
    }

    @Override
    protected void revertToSnapshot(Long snapshot) {
        seed = snapshot;
    }

    public double nextDouble(TransactionContext transaction) {
        updateSnapshots(transaction);
        var random = new SingleThreadedRandomSource(seed);
        double rand = random.nextDouble();
        seed = random.nextLong();
        return rand;
    }
}
