/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

public interface ITagAppenderExtension<T> {
    private TagAppender<T> self() {
        return (TagAppender<T>) this;
    }

    /**
     * @see TagAppender#addTag(TagKey)
     */
    @SuppressWarnings("unchecked")
    default TagAppender<T> addTags(TagKey<T>... values) {
        var appender = self();
        for (var value : values) {
            appender.addTag(value);
        }
        return appender;
    }

    /**
     * @see TagAppender#addOptionalTag(TagKey)
     */
    @SuppressWarnings("unchecked")
    default TagAppender<T> addOptionalTags(TagKey<T>... values) {
        var appender = self();
        for (var value : values) {
            appender.addOptionalTag(value);
        }
        return appender;
    }

    /**
     * Add the given {@code entry} to the tag.
     *
     * @param entry the entry to add
     * @return The appender for chaining
     */
    TagAppender<T> add(TagEntry entry);

    /**
     * Marks this tag as replacing previous entries.
     *
     * @return The appender for chaining
     */
    default TagAppender<T> replace() {
        return replace(true);
    }

    /**
     * Set whether this tag replaces previous entries.
     *
     * @param value whether the tag replaces previous entries
     * @return The appender for chaining
     */
    TagAppender<T> replace(boolean value);

    /**
     * Adds a resource key to the tag json's remove list. Callable during datageneration.
     *
     * @param element the resource key of the element to remove
     * @return The appender for chaining
     */
    TagAppender<T> remove(final ResourceKey<T> element);

    /**
     * Adds multiple resource keys to the tag json's remove list. Callable during datageneration.
     *
     * @return The appender for chaining
     */
    @SuppressWarnings("unchecked")
    default TagAppender<T> remove(final ResourceKey<T> firstE, final ResourceKey<T>... es) {
        this.remove(firstE);
        for (var e : es) {
            this.remove(e);
        }
        return self();
    }

    /**
     * Adds a tag to the tag json's remove list. Callable during datageneration.
     *
     * @param tag The ID of the tag to remove
     * @return The builder for chaining
     */
    TagAppender<T> remove(TagKey<T> tag);

    /**
     * Adds multiple tags to the tag json's remove list. Callable during datageneration.
     *
     * @param tags The IDs of the tags to remove
     * @return The builder for chaining
     */
    @SuppressWarnings("unchecked")
    default TagAppender<T> remove(TagKey<T> first, TagKey<T>... tags) {
        this.remove(first);
        for (TagKey<T> tag : tags) {
            this.remove(tag);
        }
        return self();
    }
}
