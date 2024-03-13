/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.attachment;

import net.minecraft.core.HolderLookup;

/**
 * Custom comparator for data attachments, to improve efficiency compared to the default
 * {@code Objects.equals(serializer.write(first), serializer.write(second))} implementation.
 */
public interface IAttachmentComparator<T> {
    /**
     * Checks whether two data attachments are compatible.
     *
     * <p>If the attachments are not compatible,
     * this will prevent item stacks from being stacked together.
     *
     * <p>This function should give the same result as the serialized versions of the attachments
     * with {@code Objects.equals(serializer.write(first), serializer.write(second))},
     * but will often be faster and allocate less.
     */
    boolean areCompatible(HolderLookup.Provider provider, T first, T second);
}
