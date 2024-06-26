/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenCustomHashSet;
import net.neoforged.neoforge.common.util.strategy.BasicStrategy;
import net.neoforged.neoforge.common.util.strategy.IdentityStrategy;

/**
 * Special linked hash set that allow changing the order of its entries and is strict to throw if attempting to add an entry that already exists.
 * Requires a strategy for the hashing behavior. Use {@link BasicStrategy#BASIC} or {@link IdentityStrategy#IDENTITY} if no special hashing needed.
 */
public class InsertableLinkedOpenCustomHashSet<T> extends ObjectLinkedOpenCustomHashSet<T> {
    /** The number of bits which take up the space for the previous or next position. */
    private static final long LINK_BIT_SPACE = 32L;
    /** The bitmask for the next element's position. */
    private static final long NEXT_LINK = 0x00000000FFFFFFFFL;
    /** The bitmask for the previous element's position. */
    private static final long PREV_LINK = 0xFFFFFFFF00000000L;

    /**
     * Constructs a new {@link InsertableLinkedOpenCustomHashSet} with a {@link BasicStrategy}.
     */
    public InsertableLinkedOpenCustomHashSet() {
        super(BasicStrategy.BASIC);
    }

    /**
     * Constructs a new {@link InsertableLinkedOpenCustomHashSet} with the given {@link Hash.Strategy}.
     * 
     * @param strategy The strategy to use for adding and getting elements from the set.
     */
    public InsertableLinkedOpenCustomHashSet(Hash.Strategy<? super T> strategy) {
        super(strategy);
    }

    /**
     * This method will attempt to add {@code element} after the given element {@code insertAfter} in the set. If an
     * element matching {@code insertAfter} cannot be found with this set's {@link Hash.Strategy}, then {@code element}
     * will be added in insertion order. If {#code element} already exists in the set, then the set is not modified.
     * 
     * @param insertAfter The element to insert {@code element} after.
     * @param element     The element to add into this set.
     * @return {@code true} if the element was added to the set.
     */
    public boolean addAfter(T insertAfter, T element) {
        int afterPos;
        // Use the default return if afterPos == last. Otherwise, special checks would have to be done.
        if (contains(insertAfter) && (last != (afterPos = getPos(insertAfter)))) {
            int pos = HashCommon.mix(strategy.hashCode(element)) & mask;
            T curr = key[pos];
            // If an element exists in this pos, shift index until an empty space is found or return false if it matches.
            if (curr != null) {
                do {
                    if (strategy.equals(curr, element)) {
                        return false;
                    }
                } while (!((curr = key[pos = (pos + 1) & mask]) == null));
            }
            key[pos] = element;

            // This chunk inserts the new pos in-between insertAfter and it's next link.
            long nextPos = link[afterPos] & NEXT_LINK;
            // Fix the previous link for the next element to point back to this pos.
            link[(int) nextPos] ^= (link[(int) nextPos] ^ ((pos & NEXT_LINK) << LINK_BIT_SPACE)) & PREV_LINK;
            // Fix the next link for insertAfter to point forward to this pos.
            link[afterPos] ^= (link[afterPos] ^ (pos & NEXT_LINK)) & NEXT_LINK;
            // Set this pos to point back to insertAfter and forward to the next element.
            link[pos] = ((afterPos & NEXT_LINK) << LINK_BIT_SPACE) | nextPos;

            if (size++ >= maxFill) {
                rehash(HashCommon.arraySize(size + 1, f));
            }
            return true;
        }
        return add(element);
    }

    /**
     * This method will attempt to add {@code element} before the given element {@code insertBefore} in the set. If an
     * element matching {@code insertBefore} cannot be found with this set's {@link Hash.Strategy}, then {@code element}
     * will be added in insertion order. If {#code element} already exists in the set, then the set is not modified.
     * 
     * @param insertBefore The element to insert {@code element} before.
     * @param element      The element to add into this set.
     * @return {@code true} if the element was added to the set.
     */
    public boolean addBefore(T insertBefore, T element) {
        if (contains(insertBefore)) {
            int beforePos = getPos(insertBefore);
            // Use this method instead so special logic isn't needed for handling invalid "previous" links.
            if (beforePos == first) {
                if (contains(element)) {
                    return false;
                }
                return addAndMoveToFirst(element);
            }
            int pos = HashCommon.mix(strategy.hashCode(element)) & mask;
            T curr = key[pos];
            // If an element exists in this pos, shift index until an empty space is found or return false if it matches.
            if (curr != null) {
                do {
                    if (strategy.equals(curr, element)) {
                        return false;
                    }
                } while (!((curr = key[pos = (pos + 1) & mask]) == null));
            }
            key[pos] = element;

            // This chunk inserts the new pos in-between insertBefore and it's previous link.
            long prevPos = ((link[beforePos] & PREV_LINK) >> LINK_BIT_SPACE);
            // Fix the next link for the previous element to point forward to this pos.
            link[(int) prevPos] ^= (link[(int) prevPos] ^ (pos & NEXT_LINK)) & NEXT_LINK;
            // Fix the previous link for insertBefore to point back to this pos.
            link[beforePos] ^= (link[beforePos] ^ ((pos & NEXT_LINK) << LINK_BIT_SPACE)) & PREV_LINK;
            // Set this pos to point back to the previous element and forward to insertBefore.
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
     * Requires that {@code existingElement} exists in the set already.
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
