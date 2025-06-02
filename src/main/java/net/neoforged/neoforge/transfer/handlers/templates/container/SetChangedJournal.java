package net.neoforged.neoforge.transfer.handlers.templates.container;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import org.jetbrains.annotations.Nullable;

// Boolean is used to prevent allocation. Null values are not allowed by SnapshotParticipant.
public final class SetChangedJournal extends SnapshotJournal<Boolean> {
    @Nullable
    private final Runnable callback;

    public static SetChangedJournal of(@Nullable Runnable callback) {
        return new SetChangedJournal(callback);
    }
    private SetChangedJournal(@Nullable Runnable callback) {
        this.callback = callback;
    }

    @Override
    protected Boolean createSnapshot() {
        return Boolean.TRUE;
    }

    @Override
    protected void revertToSnapshot(Boolean snapshot) {
        //ignored
    }

    @Override
    protected void onCommit(Boolean originalState) {
        runCallback();
    }

    public void runCallback(){
        if (callback != null)
            callback.run();
    }
}
