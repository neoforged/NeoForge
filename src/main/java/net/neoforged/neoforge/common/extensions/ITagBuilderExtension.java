/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;

public interface ITagBuilderExtension {
    // TODO: In 1.21.2, rename this to self() and mark as private
    default TagBuilder getRawBuilder() {
        return (TagBuilder) this;
    }

    /**
     * Adds a tag entry to the remove list.
     * 
     * @param tagEntry The tag entry to add to the remove list
     * @param source   The source of the caller for logging purposes (generally a modid)
     * @return The builder for chaining purposes
     * @deprecated Use {@link TagBuilder#remove(TagEntry)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.21.1")
    default TagBuilder remove(final TagEntry tagEntry, final String source) {
        return this.getRawBuilder().remove(tagEntry);
    }

    /**
     * Adds a single-element entry to the remove list.
     * 
     * @param elementID The ID of the element to add to the remove list
     * @param source    The source of the caller for logging purposes (generally a modid)
     * @return The builder for chaining purposes
     * @deprecated Use {@link #removeElement(ResourceLocation)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.21.1")
    default TagBuilder removeElement(final ResourceLocation elementID, final String source) {
        return this.remove(TagEntry.element(elementID), source);
    }

    /**
     * Adds a single-element entry to the remove list.
     *
     * @param elementID The ID of the element to add to the remove list
     * @return The builder for chaining purposes
     */
    default TagBuilder removeElement(final ResourceLocation elementID) {
        return this.getRawBuilder().remove(TagEntry.element(elementID));
    }

    /**
     * Adds a tag to the remove list.
     * 
     * @param tagID  The ID of the tag to add to the remove list
     * @param source The source of the caller for logging purposes (generally a modid)
     * @return The builder for chaining purposes
     * @deprecated Use {@link #removeElement(ResourceLocation)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.21.1")
    default TagBuilder removeTag(final ResourceLocation tagID, final String source) {
        return this.remove(TagEntry.tag(tagID), source);
    }

    /**
     * Adds a tag to the remove list.
     *
     * @param tagID The ID of the tag to add to the remove list
     * @return The builder for chaining purposes
     */
    default TagBuilder removeTag(final ResourceLocation tagID) {
        return this.getRawBuilder().remove(TagEntry.tag(tagID));
    }
}
