/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.transfer.handlers.templates.storage;

import java.util.Iterator;
import net.neoforged.neoforge.transfer.resources.IResource;
import net.neoforged.neoforge.transfer.resources.IResourceStack;
import org.jetbrains.annotations.Contract;

public interface IResourceData<TResource extends IResource> extends Iterable<IResourceStack<TResource>> {
    int size();

    IResourceStack<TResource> get(int index);

    IResourceData<TResource> modify(int index, TResource resource, int amount);

    ResourceStorageComponent<TResource> component();

    ResourceStorageAttachment<TResource> attachment();

    static boolean equals(IResourceData<?> data1, Object data2) {
        if (data1 == data2)
            return true;

        if (!(data2 instanceof IResourceData<?> otherData) || otherData.size() != data1.size())
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
    default Iterator<IResourceStack<TResource>> iterator() {
        return new Iterator<>() {
            final int size = size();
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public IResourceStack<TResource> next() {
                return get(i++);
            }
        };
    }
}
