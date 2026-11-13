/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.invoke.MethodHandles;
import java.util.concurrent.atomic.AtomicReference;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.junit.jupiter.api.Test;

class ExplicitTransactionCallerTests {
    private static final MethodHandles.Lookup CALLER = MethodHandles.lookup();

    static class Value extends SnapshotJournal<Integer> {
        int value;

        void set(int value, TransactionContext tx) {
            updateSnapshots(tx);
            this.value = value;
        }

        @Override
        protected Integer createSnapshot() {
            return value;
        }

        @Override
        protected void revertToSnapshot(Integer value) {
            this.value = value;
        }
    }

    @Test
    void committedChildrenStillRollBackWithTheirParent() {
        var value = new Value();
        try (var root = Transaction.open(null, CALLER)) {
            value.set(1, root);
            try (var child = Transaction.open(root, CALLER)) {
                value.set(2, child);
                child.commit();
            }
            assertThat(value.value).isEqualTo(2);
        }
        assertThat(value.value).isZero();
        try (var root = Transaction.open(null, CALLER)) {
            value.set(3, root);
            root.commit();
        }
        assertThat(value.value).isEqualTo(3);
        assertThat(Transaction.getLifecycle()).isEqualTo(Transaction.Lifecycle.NONE);
    }

    @Test
    void callerNamesAndParentValidationArePreserved() {
        try (var root = Transaction.open(null, CALLER)) {
            var duplicate = assertThrows(IllegalStateException.class, () -> Transaction.open(null, CALLER));
            assertThat(duplicate).hasMessageContaining(ExplicitTransactionCallerTests.class.getName());
            try (var child = Transaction.open(root, CALLER)) {
                assertThrows(IllegalStateException.class, () -> Transaction.open(root, CALLER));
            }
        }
        try (var root = Transaction.openRoot()) {
            try (var child = Transaction.open(root, CALLER)) {
                assertThat(child.depth()).isEqualTo(1);
            }
        }
    }

    @Test
    void crossThreadUseIsStillRejected() throws InterruptedException {
        var error = new AtomicReference<Throwable>();
        try (var root = Transaction.open(null, CALLER)) {
            var thread = new Thread(() -> {
                try (var ignored = Transaction.open(root, CALLER)) {
                    error.set(new AssertionError("Cross-thread parent accepted"));
                } catch (Throwable ex) {
                    error.set(ex);
                }
            });
            thread.start();
            thread.join();
            assertThat(error.get()).isInstanceOf(IllegalStateException.class);
        }
    }
}
