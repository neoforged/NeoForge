package net.neoforged.neoforge.transfer.storage;

import net.neoforged.neoforge.transfer.IResource;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.TransferUtils;

public interface IResourceHandler<T extends IResource> {
    int size();

    T getResource(int index);

    int getAmount(int index);

    int getLimit(int index, T resource);

    boolean isValid(int index, T resource);

    boolean canInsert();

    boolean canExtract();

    int insert(int index, T resource, int amount, TransferAction action);

    int extract(int index, T resource, int amount, TransferAction action);

    int insert(T resource, int amount, TransferAction action);

    int extract(T resource, int amount, TransferAction action);

    static <T extends IResource> Class<IResourceHandler<T>> asClass() {
        return (Class<IResourceHandler<T>>) (Object) IResourceHandler.class;
    }
}
