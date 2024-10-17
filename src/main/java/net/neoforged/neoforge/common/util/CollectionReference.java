/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An {@link AtomicReference} of {@link Collection} implementing {@link Collection}, providing a modifiable view of the
 * referenced {@link Collection}.
 * 
 * @param <E> the element type of the {@link Collection}
 * @param <C> the type of the {@link Collection}
 */
public class CollectionReference<E, C extends Collection<E>> extends AtomicReference<C> implements Collection<E> {
    public CollectionReference(C initialValue) {
        super(initialValue);
    }

    @Override
    public boolean isEmpty() {
        return this.get().isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.get().contains(o);
    }

    @Override
    public boolean add(E e) {
        return this.get().add(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.get().remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.get().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.get().addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.get().removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.get().retainAll(c);
    }

    @Override
    public void clear() {
        this.get().clear();
    }

    @Override
    public Iterator<E> iterator() {
        return this.get().iterator();
    }

    @Override
    public Object[] toArray() {
        return this.get().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.get().toArray(a);
    }

    @Override
    public int size() {
        return this.get().size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == this.get())
            return true;
        if (!(obj instanceof Collection<?>))
            return false;
        if (obj instanceof CollectionReference<?, ?> collection)
            return this.get().equals(collection.get());
        return this.get().equals(obj);
    }

    @Override
    public int hashCode() {
        return this.get().hashCode();
    }
}
