package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.TransferAction;

public interface IStorage<T> {
    int getSlots();

    T getResource(int slot);

    int getAmount(int slot);

    int getSlotLimit(int slot);

    boolean isResourceValid(int slot, T resource);

    boolean isEmpty(int slot);

    boolean allowsInsertion();

    boolean allowsExtraction();

    int insert(int slot, T resource, int amount, TransferAction action);

    int insert(T resource, int amount, TransferAction action);

    int extract(int slot, T resource, int amount, TransferAction action);

    int extract(T resource, int amount, TransferAction action);

    static <T> Class<IStorage<T>> asClass() {
        return (Class<IStorage<T>>) (Object) IStorage.class;
    }
}
