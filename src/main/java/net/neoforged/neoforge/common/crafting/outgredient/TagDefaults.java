/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting.outgredient;

import java.util.Map;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a tag default value from a tag_defaults datapack file.
 * Basically just a wrapper around a map, to avoid generics headaches in {@link TagDefaultsManager}.
 * 
 * @param <T> The type of the tag key and the associated value.
 */
public class TagDefaults<T> {
    protected final Map<TagKey<T>, T> values;

    /**
     * @param values The {@code TagKey<T>} -> {@code T} associations.
     */
    public TagDefaults(Map<TagKey<T>, T> values) {
        this.values = values;
    }

    /**
     * @param tagKey The tag key to query for.
     * @return The contents associated with the given tag key, or null if no value was found.
     */
    @Nullable
    public T resolve(TagKey<T> tagKey) {
        return values.get(tagKey);
    }
}
