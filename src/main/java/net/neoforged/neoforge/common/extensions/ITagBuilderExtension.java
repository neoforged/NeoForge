/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;

public interface ITagBuilderExtension {
    private TagBuilder self() {
        return (TagBuilder) this;
    }

    /**
     * Adds a single-element entry to the remove list.
     *
     * @param elementID The ID of the element to add to the remove list
     * @return The builder for chaining purposes
     */
    default TagBuilder removeElement(final ResourceLocation elementID) {
        return this.self().remove(TagEntry.element(elementID));
    }

    /**
     * Adds a tag to the remove list.
     *
     * @param tagID The ID of the tag to add to the remove list
     * @return The builder for chaining purposes
     */
    default TagBuilder removeTag(final ResourceLocation tagID) {
        return this.self().remove(TagEntry.tag(tagID));
    }
}
