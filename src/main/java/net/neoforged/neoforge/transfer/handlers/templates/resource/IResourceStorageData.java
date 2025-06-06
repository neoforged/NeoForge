/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.resource;

import java.util.Iterator;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import org.jetbrains.annotations.Contract;

public interface IResourceStorageData<R extends IResource> extends Iterable<IResourceStack<R>> {
    int size();

    IResourceStack<R> get(int index);

    IResourceStorageData<R> modify(int index, R resource, int amount);

    ResourceStorageComponent<R> component();

    ResourceStorageAttachment<R> attachment();

    static boolean equals(IResourceStorageData<?> data1, Object data2) {
        if (data1 == data2)
            return true;

        if (!(data2 instanceof IResourceStorageData<?> otherData) || otherData.size() != data1.size())
            return false;

        for (var i = 0; i < otherData.size(); i++) {
            var current = data1.get(i);
            var other = otherData.get(i);
            if (!current.resource().equals(other.resource())) return false;
            if (current.amount() != other.amount()) return false;
        }

        return true;
    }

    /**
     * Creates an iterator over the indices in this component.
     *
     * @return A new iterator.
     */
    @Contract(value = "-> new", pure = true)
    @Override
    default Iterator<IResourceStack<R>> iterator() {
        return new Iterator<>() {
            final int size = size();
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public IResourceStack<R> next() {
                return get(i++);
            }
        };
    }
}
