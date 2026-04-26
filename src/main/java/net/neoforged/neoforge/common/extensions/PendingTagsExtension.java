/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry.PendingTags;
import net.minecraft.tags.TagKey;

/**
 * Extension for {@link PendingTags} so that we can inspect tag contents before they are bound.
 * The {@link HolderSet.Named} returned by {@link PendingTags#lookup()} is not safe to read from until {@link PendingTags#apply()}
 * has been called. So to counteract that, we have to forward the underlying pending contents.
 */
public interface PendingTagsExtension<T> {
    /**
     * {@return the tag contents collected during load, keyed by tag}
     * This is the raw data before {@link PendingTags#apply()} binds it to the registry's holders.
     */
    Map<TagKey<T>, List<Holder<T>>> contents();
}
