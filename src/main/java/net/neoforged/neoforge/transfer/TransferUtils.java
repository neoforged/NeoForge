package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.storage.IResourceHandler;

public class TransferUtils {

    public static boolean isEmpty(IResourceHandler<? extends IResource> handler, int index) {
        return handler.getResource(index).isBlank() || handler.getAmount(index) <= 0;
    }

    public static <T extends IResource> int insertRange(IResourceHandler<T> handler, int start, int end, T resource, int amount, TransferAction action) {
        int inserted = 0;
        for (int index = start; index < end; index++) {
            if (TransferUtils.isEmpty(handler, index)) continue;
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }

        for (int index = start; index < end; index++) {
            if (!TransferUtils.isEmpty(handler, index)) continue;
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }

        return inserted;
    }

    public static <T extends IResource> int insertStacking(IResourceHandler<T> handler, T resource, int amount, TransferAction action) {
        return insertRange(handler, 0, handler.size(), resource, amount, action);
    }

    public static <T extends IResource> int extractRange(IResourceHandler<T> handler, int start, int end, T resource, int amount, TransferAction action) {
        int extracted = 0;
        for (int index = start; index < end; index++) {
            extracted += handler.extract(index, resource, amount - extracted, action);
            if (extracted >= amount) {
                return extracted;
            }
        }
        return extracted;
    }

    public static <T extends IResource> int extractStacking(IResourceHandler<T> handler, T resource, int amount, TransferAction action) {
        return extractRange(handler, 0, handler.size(), resource, amount, action);
    }
}
