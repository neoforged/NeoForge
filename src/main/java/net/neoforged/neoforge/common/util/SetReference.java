/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.util.Set;

/**
 * The {@link Set} implementation of {@link CollectionReference}.
 * 
 * @param <E> the element type of the {@link Set}
 * @param <S> the type of the {@link Set}
 */
public class SetReference<E, S extends Set<E>> extends CollectionReference<E, S> implements Set<E> {
    public SetReference(S initialValue) {
        super(initialValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == this.get())
            return true;
        if (!(obj instanceof Set<?>))
            return false;
        if (obj instanceof SetReference<?, ?> set)
            return this.get().equals(set.get());
        return this.get().equals(obj);
    }
}
