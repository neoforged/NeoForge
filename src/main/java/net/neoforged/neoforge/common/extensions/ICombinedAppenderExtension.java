/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.extensions;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;
import net.minecraft.data.tags.BlockItemTagsProvider;
import net.minecraft.references.BlockItemId;
import net.minecraft.tags.BlockItemTagId;

/// Provides extensions to the [BlockItemTagsProvider.CombinedAppender].
public interface ICombinedAppenderExtension {
    private BlockItemTagsProvider.CombinedAppender self() {
        return (BlockItemTagsProvider.CombinedAppender) this;
    }

    /// Adds the block items to the tag whose resolved entries may or may not be present
    /// during data generation.
    ///
    /// @param ids A stream of block item identifiers.
    /// @return The combined appender.
    BlockItemTagsProvider.CombinedAppender addAllOptional(Stream<BlockItemId> ids);

    /// Adds the block item tag entries whose resolved holder set may or may not be present during
    /// data generation.
    ///
    /// @param id The block item tag identifier.
    /// @return The combined appender.
    BlockItemTagsProvider.CombinedAppender addOptionalTag(BlockItemTagId id);

    /// Adds the block items to the tag whose resolved entries may or may not be present
    /// during data generation.
    ///
    /// @param ids A varargs of block item identifiers.
    /// @return The combined appender.
    default BlockItemTagsProvider.CombinedAppender addOptional(BlockItemId... ids) {
        this.addAllOptional(Arrays.stream(ids));
        return self();
    }

    /// Adds the block item tag entries whose resolved holder sets may or may not be present during
    /// data generation.
    ///
    /// @param ids The block item tag identifiers.
    /// @return The combined appender.
    default BlockItemTagsProvider.CombinedAppender addOptionalTag(BlockItemTagId... ids) {
        for (BlockItemTagId id : ids) {
            this.addOptionalTag(id);
        }

        return self();
    }

    /// Adds the block items to the tag whose resolved entries may or may not be present
    /// during data generation.
    ///
    /// @param ids A collection of block item identifiers.
    /// @return The combined appender.
    default BlockItemTagsProvider.CombinedAppender addAllOptional(Collection<BlockItemId> ids) {
        this.addAllOptional(ids.stream());
        return self();
    }

    /// Sets whether this tag replaces any previous entries.
    ///
    /// @param value Whether this tag replaces any previous entries.
    /// @return The combined appender.
    BlockItemTagsProvider.CombinedAppender replace(boolean value);

    /// Marks this tag as replacing any previous entries.
    ///
    /// @return The combined appender.
    default BlockItemTagsProvider.CombinedAppender replace() {
        return this.replace(true);
    }

    /// Removes the block items from the tag.
    ///
    /// @param ids A stream of block item identifiers.
    /// @return The combined appender.
    BlockItemTagsProvider.CombinedAppender removeAll(Stream<BlockItemId> ids);

    /// Removes the block item tag entries from the tag.
    ///
    /// @param id The block item tag identifier.
    /// @return The combined appender.
    BlockItemTagsProvider.CombinedAppender removeTag(BlockItemTagId id);

    /// Removes the block items from the tag.
    ///
    /// @param ids A varargs of block item identifiers.
    /// @return The combined appender.
    default BlockItemTagsProvider.CombinedAppender remove(BlockItemId... ids) {
        this.removeAll(Arrays.stream(ids));
        return self();
    }

    /// Removes the block item tag entries from the tag.
    ///
    /// @param ids The block item tag identifiers.
    /// @return The combined appender.
    default BlockItemTagsProvider.CombinedAppender removeTag(BlockItemTagId... ids) {
        for (BlockItemTagId id : ids) {
            this.removeTag(id);
        }

        return self();
    }

    /// Removes the block items from the tag.
    ///
    /// @param ids A collection of block item identifiers.
    /// @return The combined appender.
    default BlockItemTagsProvider.CombinedAppender removeAll(Collection<BlockItemId> ids) {
        this.removeAll(ids.stream());
        return self();
    }
}
