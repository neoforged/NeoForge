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
