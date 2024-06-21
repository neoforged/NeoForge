/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.Pair;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import net.neoforged.neoforge.common.util.strategy.BasicStrategy;
import net.neoforged.neoforge.common.util.strategy.IdentityStrategy;

/**
 * Special linked hash set that allow changing the order of its entries and is strict to throw if attempting to add an entry that already exists.
 * Requires a strategy for the hashing behavior. Use {@link InsertableStrictLinkedHashSet#BASIC} or {@link InsertableStrictLinkedHashSet#IDENTITY} if no special hashing needed.
 */
public class InsertableStrictLinkedHashSet<T> extends AbstractSet<T> {
    /**
     * A strategy that uses {@link Objects#hashCode(Object)} and {@link Object#equals(Object)}.
     */
    public static final Hash.Strategy<? super Object> BASIC = new BasicStrategy();

    /**
     * A strategy that uses {@link System#identityHashCode(Object)} and {@code a == b} comparisons.
     */
    public static final Hash.Strategy<? super Object> IDENTITY = new IdentityStrategy();

    private final Hash.Strategy<? super T> strategy;
    private final LinkedList<T> backingList = new LinkedList<>();
    transient HashMap<Integer, Pair<T, Integer>> map = new HashMap<>();

    public InsertableStrictLinkedHashSet(Hash.Strategy<? super T> strategy) {
        super();
        this.strategy = strategy;
    }

    @Override
    public Iterator<T> iterator() {
        return backingList.iterator();
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    public boolean add(T entry) {
        int hash = strategy.hashCode(entry);
        if (map.containsKey(hash)) {
            throw new IllegalArgumentException(entry + " object with hash " + hash + " already exists in set");
        }

        map.put(hash, Pair.of(entry, backingList.size()));
        backingList.add(entry);
        return true;
    }

    public void add(int index, T entry) {
        int hash = strategy.hashCode(entry);
        if (map.containsKey(hash)) {
            throw new IllegalArgumentException(entry + " object with hash " + hash + " already exists in set");
        }

        backingList.add(index, entry);

        // Need to update all existing indices stored in map.
        map.clear();
        for (int i = 0; i < backingList.size(); i++) {
            T curr = backingList.get(i);
            map.put(strategy.hashCode(curr), Pair.of(curr, i));
        }
    }

    public void addFirst(T entry) {
        int hash = strategy.hashCode(entry);
        if (map.containsKey(hash)) {
            throw new IllegalArgumentException(entry + " object with hash " + hash + " already exists in set");
        }

        map.put(hash, Pair.of(entry, backingList.size()));
        backingList.addFirst(entry);

        // Need to update all existing indices stored in map.
        map.clear();
        for (int i = 0; i < backingList.size(); i++) {
            T curr = backingList.get(i);
            map.put(strategy.hashCode(curr), Pair.of(curr, i));
        }
    }

    @Override
    public boolean remove(Object o) {
        int hash = strategy.hashCode((T) o);
        if (!map.containsKey(hash)) {
            return false;
        }

        Pair<T, Integer> removed = map.remove(hash);
        backingList.remove((int) removed.right());
        return true;
    }

    public int indexOf(T entry) {
        int hash = strategy.hashCode(entry);
        if (!map.containsKey(hash)) {
            return -1;
        }

        return map.get(hash).right();
    }

    public void clear() {
        map.clear();
        backingList.clear();
    }
}
