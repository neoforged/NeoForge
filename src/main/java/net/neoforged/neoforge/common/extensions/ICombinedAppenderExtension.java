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

public interface ICombinedAppenderExtension {
    private BlockItemTagsProvider.CombinedAppender self() {
        return (BlockItemTagsProvider.CombinedAppender) this;
    }

    BlockItemTagsProvider.CombinedAppender addAllOptional(Stream<BlockItemId> ids);

    BlockItemTagsProvider.CombinedAppender addOptionalTag(BlockItemTagId id);

    default BlockItemTagsProvider.CombinedAppender addOptional(BlockItemId... ids) {
        this.addAllOptional(Arrays.stream(ids));
        return self();
    }

    default BlockItemTagsProvider.CombinedAppender addOptionalTag(BlockItemTagId... ids) {
        for (BlockItemTagId id : ids) {
            this.addOptionalTag(id);
        }

        return self();
    }

    default BlockItemTagsProvider.CombinedAppender addAllOptional(Collection<BlockItemId> ids) {
        this.addAllOptional(ids.stream());
        return self();
    }

    BlockItemTagsProvider.CombinedAppender replace(boolean value);

    default BlockItemTagsProvider.CombinedAppender replace() {
        return this.replace(true);
    }

    BlockItemTagsProvider.CombinedAppender removeAll(Stream<BlockItemId> ids);

    BlockItemTagsProvider.CombinedAppender removeTag(BlockItemTagId id);

    default BlockItemTagsProvider.CombinedAppender remove(BlockItemId... ids) {
        this.removeAll(Arrays.stream(ids));
        return self();
    }

    default BlockItemTagsProvider.CombinedAppender removeTag(BlockItemTagId... ids) {
        for (BlockItemTagId id : ids) {
            this.removeTag(id);
        }

        return self();
    }

    default BlockItemTagsProvider.CombinedAppender removeAll(Collection<BlockItemId> ids) {
        this.removeAll(ids.stream());
        return self();
    }
}
