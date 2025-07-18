/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.world.poi;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class PoiStateSet implements Set<BlockState> {
    private final Set<BlockState> backingSet = new ReferenceOpenHashSet<>();

    public PoiStateSet(Set<BlockState> states) {
        this.backingSet.addAll(states);
    }

    @Override
    public int size() {
        return this.backingSet.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backingSet.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.backingSet.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.backingSet.containsAll(c);
    }

    @Override
    public Iterator<BlockState> iterator() {
        return Iterators.unmodifiableIterator(this.backingSet.iterator());
    }

    @Override
    public Object[] toArray() {
        return this.backingSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.backingSet.toArray(a);
    }

    @Override
    public boolean add(BlockState state) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends BlockState> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super BlockState> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    void addCustomStates(Set<BlockState> states) {
        this.backingSet.addAll(states);
    }
}
