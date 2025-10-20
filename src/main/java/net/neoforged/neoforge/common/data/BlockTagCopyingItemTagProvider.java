/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * This item tag provider waits for block tags to be available and allows to declaratively copy
 * block tags over to item tags using the {@link #copy(TagKey, TagKey)} method.
 * <p>
 * The alternative to this is extracting all block tags that are simultaneously used as item tags
 * into a separate class and reuse that from your block and item tag provider. See {@link net.minecraft.data.tags.BlockItemTagsProvider}
 * for vanillas implementation of this concept.
 */
public abstract class BlockTagCopyingItemTagProvider extends IntrinsicHolderTagsProvider<Item> {
    private final CompletableFuture<TagsProvider.TagLookup<Block>> blockTags;
    private final Map<TagKey<Block>, TagKey<Item>> tagsToCopy = new HashMap<>();

    public BlockTagCopyingItemTagProvider(PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagLookup<Block>> blockTags,
            String modId) {
        super(output, Registries.ITEM, lookupProvider, item -> item.builtInRegistryHolder().key(), modId);
        this.blockTags = blockTags;
    }

    protected void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        this.tagsToCopy.put(blockTag, itemTag);
    }

    @Override
    protected CompletableFuture<HolderLookup.Provider> createContentsProvider() {
        return super.createContentsProvider().thenCombine(this.blockTags, (provider, blockTags) -> {
            this.tagsToCopy.forEach((fromBlockTag, toItemTag) -> {
                var toBuilder = this.getOrCreateRawBuilder(toItemTag);
                var fromBuilder = blockTags.apply(fromBlockTag).orElseThrow(() -> new IllegalStateException("Missing block tag " + toItemTag.location()));
                var fromTags = fromBuilder.build();
                fromTags.forEach(toBuilder::add);
                fromBuilder.getRemoveEntries().forEach(toBuilder::remove);
            });
            return provider;
        });
    }
}
