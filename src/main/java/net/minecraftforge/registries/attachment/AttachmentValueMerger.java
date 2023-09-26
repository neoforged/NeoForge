/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.registries.attachment;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * A functional interface used to merge two attachment values.
 *
 * @param <A> the type of the attachment object
 */
@FunctionalInterface
public interface AttachmentValueMerger<A>
{
    /**
     * A merger that picks the newest entry and forgets about the old one.
     */
    AttachmentValueMerger<?> DEFAULT = (o, n) -> n;

    /**
     * Merges the old value with the new one. <br>
     *
     * @param oldValue the old attachment
     * @param newValue the new attachment. This value is generally higher than the previous one in datapack order
     * @return the merged value
     */
    A merge(A oldValue, A newValue);

    /**
     * {@return a merger that merges lists into one immutable one}
     * @param <A> the type of the list content
     */
    static <A> AttachmentValueMerger<List<A>> mergeLists()
    {
        return (oldList, newList) ->
        {
            final var listBuilder = ImmutableList.<A>builderWithExpectedSize(oldList.size() + newList.size());
            listBuilder.addAll(oldList);
            listBuilder.addAll(newList);
            return listBuilder.build();
        };
    }
}
