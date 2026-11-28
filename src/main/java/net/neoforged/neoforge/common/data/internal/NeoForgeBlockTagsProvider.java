/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BlockItemTagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.references.BlockIds;
import net.minecraft.references.BlockItemId;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColorCollection;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public final class NeoForgeBlockTagsProvider extends BlockTagsProvider {
    public NeoForgeBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, "neoforge");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTags(HolderLookup.Provider registries) {
        new NeoForgeBlockItemTagsProvider(tagId -> BlockItemTagsProvider.wrapForBlocks(this.tag(tagId.block()))).run();

        ColorCollection<NeoForgeBlockTagsProvider.Appender> builders = Tags.BlockItems.DYED_COLORS.map(BlockItemTagId::block).map(this::tag);
        for (ColorCollection<ResourceKey<Block>> colorCollection : List.of(BlockIds.WALL_BANNER)) {
            ColorCollection.zipApply(builders, colorCollection, NeoForgeBlockTagsProvider.Appender::add);
        }

        tag(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST);
        tag(Tags.Blocks.SKULLS).add(
                Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL,
                Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD,
                Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.CREEPER_HEAD,
                Blocks.CREEPER_WALL_HEAD, Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD,
                Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD)
                .addTag(BlockTags.SKULLS);
        tag(Tags.Blocks.HIDDEN_FROM_RECIPE_VIEWERS);
        tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED);
        tag(Tags.BlockItems.ICE).add(BlockIds.FROSTED_ICE); // Frosted ice is not an item

        tag(Tags.BlockItems.OVERWORLD_NATURAL_LOGS).addTag(BlockTags.OVERWORLD_NATURAL_LOGS); // Overworld natural logs does not have an item tag

        tag(Tags.Blocks.VILLAGER_JOB_SITES).add(
                Blocks.BARREL, Blocks.BLAST_FURNACE, Blocks.BREWING_STAND, Blocks.CARTOGRAPHY_TABLE,
                Blocks.CAULDRON, Blocks.WATER_CAULDRON, Blocks.LAVA_CAULDRON, Blocks.POWDER_SNOW_CAULDRON,
                Blocks.COMPOSTER, Blocks.FLETCHING_TABLE, Blocks.GRINDSTONE, Blocks.LECTERN,
                Blocks.LOOM, Blocks.SMITHING_TABLE, Blocks.SMOKER, Blocks.STONECUTTER);

        tag(Tags.Blocks.VILLAGER_FARMLANDS).add(Blocks.FARMLAND);

        // Make our Needs Netherite Tool tag be functional.
        tag(Tags.Blocks.NEEDS_NETHERITE_TOOL);
        tag(BlockTags.INCORRECT_FOR_WOODEN_TOOL).addTag(Tags.Blocks.NEEDS_NETHERITE_TOOL);
        tag(BlockTags.INCORRECT_FOR_STONE_TOOL).addTag(Tags.Blocks.NEEDS_NETHERITE_TOOL);
        tag(BlockTags.INCORRECT_FOR_COPPER_TOOL).addTag(Tags.Blocks.NEEDS_NETHERITE_TOOL);
        tag(BlockTags.INCORRECT_FOR_IRON_TOOL).addTag(Tags.Blocks.NEEDS_NETHERITE_TOOL);
        tag(BlockTags.INCORRECT_FOR_GOLD_TOOL).addTag(Tags.Blocks.NEEDS_NETHERITE_TOOL);
        tag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL).addTag(Tags.Blocks.NEEDS_NETHERITE_TOOL);
    }

    protected record Appender(TagAppender<Block> app) implements TagAppender<Block> {
        @Override
        public Appender add(ResourceKey<Block> element) {
            app.add(element);
            return this;
        }

        @Override
        public Appender addOptional(ResourceKey<Block> element) {
            app.addOptional(element);
            return this;
        }

        @Override
        public Appender addTag(TagKey<Block> tag) {
            app.addTag(tag);
            return this;
        }

        @Override
        public Appender addOptionalTag(TagKey<Block> tag) {
            app.addOptionalTag(tag);
            return this;
        }

        @Override
        public Appender add(TagEntry entry) {
            app.add(entry);
            return this;
        }

        @Override
        public Appender replace(boolean value) {
            app.replace(value);
            return this;
        }

        @Override
        public Appender remove(ResourceKey<Block> element) {
            app.remove(element);
            return this;
        }

        @Override
        public Appender remove(TagKey<Block> tag) {
            app.remove(tag);
            return this;
        }

        @Override
        public Appender addAll(Stream<ResourceKey<Block>> elements) {
            app.addAll(elements);
            return this;
        }

        @Override
        public Appender addAll(Collection<ResourceKey<Block>> elements) {
            app.addAll(elements);
            return this;
        }

        @SafeVarargs
        @Override
        public final Appender addTags(TagKey<Block>... values) {
            app.addTags(values);
            return this;
        }

        @SafeVarargs
        @Override
        public final Appender addOptionalTags(TagKey<Block>... values) {
            app.addOptionalTags(values);
            return this;
        }

        public Appender add(Block... blocks) {
            for (Block block : blocks) {
                add(BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow(() -> new IllegalStateException("Unregistered block")));
            }
            return this;
        }

        public Appender add(BlockItemId... ids) {
            for (ResourceKey<Block> blockResourceKey : Arrays.stream(ids).map(BlockItemId::block).toList()) {
                add(blockResourceKey);
            }
            return this;
        }

        public Appender addAll(Iterable<Block> blocks) {
            for (Block block : blocks) {
                add(block);
            }
            return this;
        }

        public Appender addTag(BlockItemTagId tag) {
            return this.addTag(tag.block());
        }

        public Appender addOptionalTag(BlockItemTagId tag) {
            return this.addOptionalTag(tag.block());
        }

        public Appender addTags(BlockItemTagId... tags) {
            for (BlockItemTagId tag : tags) {
                this.addTag(tag);
            }
            return this;
        }

        public Appender addOptionalTags(BlockItemTagId... tags) {
            for (BlockItemTagId tag : tags) {
                this.addOptionalTag(tag);
            }
            return this;
        }
    }

    @Override
    protected Appender tag(TagKey<Block> tag) {
        return new Appender(super.tag(tag));
    }

    private Appender tag(BlockItemTagId tag) {
        return tag(tag.block());
    }
}
