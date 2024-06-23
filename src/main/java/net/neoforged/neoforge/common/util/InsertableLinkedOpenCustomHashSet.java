/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import java.util.Objects;
import net.neoforged.neoforge.common.util.strategy.BasicStrategy;
import net.neoforged.neoforge.common.util.strategy.IdentityStrategy;

/**
 * Special linked hash set that allow changing the order of its entries and is strict to throw if attempting to add an entry that already exists.
 * Requires a strategy for the hashing behavior. Use {@link BasicStrategy#BASIC} or {@link IdentityStrategy#IDENTITY} if no special hashing needed.
 */
public class InsertableLinkedOpenCustomHashSet<T> extends ObjectLinkedOpenCustomHashSet<T> {
    private static final long LINK_BITS = 0xFFFFFFFFL;
    private static final long LINK_BIT_SPACE = 32L;
    private static final long NEXT_LINK = LINK_BITS;
    private static final long PREV_LINK = LINK_BITS << LINK_BIT_SPACE;

    public InsertableLinkedOpenCustomHashSet() {
        super(BasicStrategy.BASIC);
    }

    public InsertableLinkedOpenCustomHashSet(Hash.Strategy<? super T> strategy) {
        super(strategy);
    }

    public boolean addAfter(T insertAfter, T element) {
        int afterPos;
        if (contains(insertAfter) && (last != (afterPos = getPos(insertAfter)))) {
            int pos = HashCommon.mix(strategy.hashCode(element)) & mask;
            T curr = key[pos];
            if (curr != null) {
                do {
                    if (strategy.equals(curr, element)) {
                        return false;
                    }
                } while (!((curr = key[pos = (pos + 1) & mask]) == null));
            }
            key[pos] = element;

            long nextPos = link[afterPos] & NEXT_LINK;
            if (nextPos != LINK_BITS) {
                link[(int) nextPos] ^= (link[(int) nextPos] ^ ((pos & NEXT_LINK) << LINK_BIT_SPACE)) & PREV_LINK;
            }
            link[afterPos] ^= (link[afterPos] ^ (pos & NEXT_LINK)) & NEXT_LINK;
            link[pos] = ((afterPos & NEXT_LINK) << LINK_BIT_SPACE) | nextPos;
            if (size++ >= maxFill) {
                rehash(HashCommon.arraySize(size + 1, f));
            }
            return true;
        }
        return add(element);
    }

    public boolean addBefore(T insertBefore, T element) {
        if (contains(insertBefore)) {
            int beforePos = getPos(insertBefore);
            if (beforePos == first) {
                return addAndMoveToFirst(element);
            }
            int pos = HashCommon.mix(strategy.hashCode(element)) & mask;
            T curr = key[pos];
            if (curr != null) {
                do {
                    if (strategy.equals(curr, element)) {
                        return false;
                    }
                } while (!((curr = key[pos = (pos + 1) & mask]) == null));
            }
            key[pos] = element;

            long prevPos = (link[beforePos] & PREV_LINK) >> LINK_BIT_SPACE;
            if (prevPos != LINK_BITS && prevPos != -1) {
                link[(int) prevPos] ^= (link[(int) prevPos] ^ (pos & NEXT_LINK)) & NEXT_LINK;
            }
            link[beforePos] ^= (link[beforePos] ^ ((pos & NEXT_LINK) << LINK_BIT_SPACE)) & PREV_LINK;
            link[pos] = (prevPos << LINK_BIT_SPACE) | (beforePos & NEXT_LINK);
            if (size++ >= maxFill) {
                rehash(HashCommon.arraySize(size + 1, f));
            }
            return true;
        }
        return add(element);
    }

    @Override
    public void addFirst(T element) {
        addAndMoveToFirst(element);
    }

    @Override
    public void addLast(T element) {
        addAndMoveToLast(element);
    }

    /**
     * Requires that insertAfter exists in the set already.
     */
    private int getPos(T existingElement) {
        int pos = HashCommon.mix(strategy.hashCode(existingElement)) & mask;
        T curr = key[pos];
        do {
            if (strategy.equals(curr, existingElement)) {
                break;
            }
        } while (!((curr = key[pos = (pos + 1) & mask]) == null));
        return pos;
    }
}
