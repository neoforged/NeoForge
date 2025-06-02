package net.neoforged.neoforge.transfer.transaction;


import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@ApiStatus.Internal
public class TransactionManagerImpl {
    public static final ThreadLocal<TransactionManagerImpl> MANAGERS = ThreadLocal.withInitial(TransactionManagerImpl::new);

    private final Thread thread = Thread.currentThread();
    private final List<TransactionImpl> stack = new ArrayList<>();
    private final List<Transaction.OuterCloseCallback> outerCloseCallbacks = new ArrayList<>();
    private int currentDepth = -1;

    public boolean isOpen() {
        return currentDepth > -1;
    }

    public Transaction open(@Nullable TransactionContext parent) {
        if (parent == null) {
            if (isOpen()) {
                throw new IllegalStateException("An outer transaction is already active on this thread " + thread);
            }
        } else {
            TransactionImpl parentImpl = (TransactionImpl) parent;
            parentImpl.validateCurrentTransaction();
            parentImpl.validateOpen();
        }

        return open();
    }

    //    We should be careful adding methods that are intentionally "playing with fire". Concrete use cases are needed vs "Generally nice to have"
    //    @Nullable
    //    public TransactionContext getCurrentUnsafe() {
    //        if (currentDepth == -1) {
    //            return null;
    //        }
    //
    //        var current = stack.get(currentDepth);
    //        if (current.lifecycle == Transaction.Lifecycle.OPEN) {
    //            return current;
    //        } else {
    //            throw new IllegalStateException("May not call getCurrentUnsafe() from a close callback.");
    //        }
    //    }

    /**
     * Open a new transaction, outer or nested, without performing any state check.
     */
    private Transaction open() {
        TransactionImpl current;
        if (stack.size() == ++currentDepth) {
            current = new TransactionImpl(currentDepth);
            stack.add(current);
        } else {
            current = stack.get(currentDepth);
        }
        current.lifecycle = Transaction.Lifecycle.OPEN;
        return current;
    }

    private void validateCurrentThread() {
        if (Thread.currentThread() != thread) {
            String errorMessage = String.format(
                    "Attempted to access transaction state from thread %s, but this transaction is only valid on thread %s.",
                    Thread.currentThread().getName(),
                    thread.getName());
            throw new IllegalStateException(errorMessage);
        }
    }

    public Transaction.Lifecycle getLifecycle() {
        return currentDepth == -1 ? Transaction.Lifecycle.NONE : stack.get(currentDepth).lifecycle;
    }

    private class TransactionImpl implements Transaction {
        final int nestingDepth;
        final List<CloseCallback> closeCallbacks = new ArrayList<>();
        Lifecycle lifecycle = Lifecycle.NONE;

        TransactionImpl(int nestingDepth) {
            this.nestingDepth = nestingDepth;
        }

        void validateCurrentTransaction() {
            validateCurrentThread();

            if (currentDepth != -1 && stack.get(currentDepth) == this) return;

            throw new IllegalStateException("Transaction function was called on a transaction with depth %d, but the current transaction has depth %d."
                    .formatted(nestingDepth, currentDepth));
        }

        // Validate that this transaction is open.
        private void validateOpen() {
            if (lifecycle != Lifecycle.OPEN) {
                throw new IllegalStateException("Transaction operation cannot be applied to a closed transaction.");
            }
        }

        private void close(Result result) {
            validateCurrentTransaction();
            validateOpen();
            // Block transaction operations
            lifecycle = Lifecycle.CLOSING;

            // Note: it is important that we don't let exceptions corrupt the global state of the transaction manager.
            // That is why any callback has to run inside a try block.
            RuntimeException closeException = null;

            // Invoke callbacks in reverse order
            for (int i = closeCallbacks.size() - 1; i >= 0; i--) {
                try {
                    closeCallbacks.get(i).onClose(this, result);
                } catch (Exception exception) {
                    if (closeException == null) {
                        closeException = new RuntimeException("Encountered an exception while invoking a transaction close callback.", exception);
                    } else {
                        closeException.addSuppressed(exception);
                    }
                }
            }

            closeCallbacks.clear();

            if (currentDepth == 0) {
                lifecycle = Lifecycle.OUTER_CLOSING;

                // Invoke outer close callbacks in reverse order
                for (int i = outerCloseCallbacks.size() - 1; i >= 0; i--) {
                    try {
                        outerCloseCallbacks.get(i).afterOuterClose(result);
                    } catch (Exception exception) {
                        if (closeException == null) {
                            closeException = new RuntimeException("Encountered an exception while invoking a transaction outer close callback.", exception);
                        } else {
                            closeException.addSuppressed(exception);
                        }
                    }
                }

                outerCloseCallbacks.clear();
            }

            // Only this check will allow openOuter operations.
            currentDepth--;
            lifecycle = Lifecycle.NONE;

            // Throw exception if necessary
            if (closeException != null) {
                throw closeException;
            }
        }

        @Override
        public void commit() {
            close(Result.COMMITTED);
        }

        @Override
        public void close() {
            if (isOpen() && lifecycle == Lifecycle.OPEN) { // check that a transaction is open on this thread and that this transaction is open.
                close(Result.ABORTED);
            }
        }

        @Override
        public int nestingDepth() {
            validateCurrentThread();
            return nestingDepth;
        }

        @Override
        public Transaction getOpenTransaction(int nestingDepth) {
            validateCurrentThread();

            if (nestingDepth < 0) {
                throw new IndexOutOfBoundsException("Nesting depth may not be negative.");
            }

            if (nestingDepth > currentDepth) {
                throw new IndexOutOfBoundsException("There is no open transaction for nesting depth " + nestingDepth);
            }

            TransactionImpl transaction = stack.get(nestingDepth);
            transaction.validateOpen();
            return transaction;
        }

        @Override
        public void addCloseCallback(CloseCallback closeCallback) {
            validateCurrentThread();
            validateOpen();
            closeCallbacks.add(closeCallback);
        }

        @Override
        public void addOuterCloseCallback(OuterCloseCallback outerCloseCallback) {
            validateCurrentThread();
            // Note: we don't call validateOpen() because this transaction may not be open if this is called during a CloseCallback.
            // We rely on a currentDepth check instead, as the depth is only set to -1 at the very end of close(Result).

            if (currentDepth == -1) {
                throw new IllegalStateException("There is no open transaction on this thread.");
            }

            outerCloseCallbacks.add(outerCloseCallback);
        }

        @Override
        public String toString() {
            return "Transaction[depth=%d, lifecycle=%s, thread=%s]".formatted(nestingDepth, lifecycle.name(), thread.getName());
        }
    }
}