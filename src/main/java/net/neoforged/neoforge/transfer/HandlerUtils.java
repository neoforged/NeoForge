/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer;

import net.neoforged.neoforge.transfer.handlers.IResourceHandler;

public class HandlerUtils {

    public static boolean isEmpty(IResourceHandler<? extends IResource> handler) {
        for (int i = 0; i < handler.size(); i++) {
            if (!isIndexEmpty(handler, i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isFull(IResourceHandler<? extends IResource> handler) {
        for (int i = 0; i < handler.size(); i++) {
            if (!isIndexFull(handler, i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isIndexEmpty(IResourceHandler<? extends IResource> handler, int index) {
        return handler.getResource(index).isBlank() || handler.getAmount(index) <= 0;
    }

    public static <T extends IResource> boolean isIndexFull(IResourceHandler<T> handler, int index) {
        return handler.getAmount(index) >= handler.getLimit(index, handler.getResource(index));
    }

    public static <T extends IResource> int insertRange(IResourceHandler<T> handler, int start, int end, T resource, int amount, TransferAction action) {
        int inserted = 0;
        for (int index = start; index < end; index++) {
            if (HandlerUtils.isIndexEmpty(handler, index)) continue;
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }

        for (int index = start; index < end; index++) {
            if (!HandlerUtils.isIndexEmpty(handler, index)) continue;
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }

        return inserted;
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

    public static <T extends IResource> int insertStacking(IResourceHandler<T> handler, T resource, int amount, TransferAction action) {
        return insertRange(handler, 0, handler.size(), resource, amount, action);
    }

    public static <T extends IResource> int extractStacking(IResourceHandler<T> handler, T resource, int amount, TransferAction action) {
        return extractRange(handler, 0, handler.size(), resource, amount, action);
    }

    public static <T extends IResource> int insertIndices(IResourceHandler<T> handler, int[] indices, T resource, int amount, TransferAction action) {
        int inserted = 0;
        for (int index : indices) {
            if (HandlerUtils.isIndexEmpty(handler, index)) continue;
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }

        for (int index : indices) {
            if (!HandlerUtils.isIndexEmpty(handler, index)) continue;
            inserted += handler.insert(index, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }
        return inserted;
    }

    public static <T extends IResource> int extractIndices(IResourceHandler<T> handler, int[] indices, T resource, int amount, TransferAction action) {
        int extracted = 0;
        for (int index : indices) {
            extracted += handler.extract(index, resource, amount - extracted, action);
            if (extracted >= amount) {
                return extracted;
            }
        }
        return extracted;
    }

    public static <T extends IResource> int extractExcludingIndex(IResourceHandler<T> handler, int index, T resource, int amount, TransferAction action) {
        int extracted = 0;
        for (int i = 0; i < handler.size(); i++) {
            if (i == index) continue;
            extracted += handler.extract(i, resource, amount - extracted, action);
            if (extracted >= amount) {
                return extracted;
            }
        }
        return extracted;
    }

    public static <T extends IResource> int insertExcludingIndex(IResourceHandler<T> handler, int index, T resource, int amount, TransferAction action) {
        int inserted = 0;
        // First try to insert into existing stacks
        for (int i = 0; i < handler.size(); i++) {
            if (i == index) continue;
            if (HandlerUtils.isIndexEmpty(handler, i)) continue;
            inserted += handler.insert(i, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }
        // Then try to insert into empty slots
        for (int i = 0; i < handler.size(); i++) {
            if (i == index) continue;
            if (!HandlerUtils.isIndexEmpty(handler, i)) continue;
            inserted += handler.insert(i, resource, amount - inserted, action);
            if (inserted >= amount) {
                return inserted;
            }
        }
        return inserted;
    }
}
