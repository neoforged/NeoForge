package net.neoforged.neoforge.transfer;

public enum TransferResult {
    SUCCESS,
    FAIL,
    PASS;

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFail() {
        return this == FAIL;
    }

    public boolean isPass() {
        return this == PASS;
    }
}
