/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.references.BlockIds;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
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
        tag(Tags.BlockItems.BARRELS).addTag(Tags.BlockItems.BARRELS_WOODEN);
        tag(Tags.BlockItems.BARRELS_WOODEN).add(Blocks.BARREL);
        tag(Tags.BlockItems.BARS).addTags(Tags.BlockItems.BARS_COPPER, Tags.BlockItems.BARS_IRON).addTag(BlockTags.BARS);
        tag(Tags.BlockItems.BARS_COPPER).addAll(Blocks.COPPER_BARS.asList().stream().map(b -> b.builtInRegistryHolder().key()));
        tag(Tags.BlockItems.BARS_IRON).add(Blocks.IRON_BARS);
        tag(Tags.BlockItems.BOOKSHELVES).add(Blocks.BOOKSHELF);
        tag(Tags.BlockItems.BUDDING_BLOCKS).add(Blocks.BUDDING_AMETHYST);
        tag(Tags.BlockItems.BUDS).add(Blocks.SMALL_AMETHYST_BUD).add(Blocks.MEDIUM_AMETHYST_BUD).add(Blocks.LARGE_AMETHYST_BUD);
        tag(Tags.BlockItems.CHAINS).add(Blocks.IRON_CHAIN).addAll(Blocks.COPPER_CHAIN.asList().stream().map(b -> b.builtInRegistryHolder().key()));
        tag(Tags.BlockItems.CHESTS).addTags(Tags.BlockItems.CHESTS_ENDER, Tags.BlockItems.CHESTS_TRAPPED, Tags.BlockItems.CHESTS_WOODEN).addOptionalTag(BlockTags.COPPER_CHESTS);
        tag(Tags.BlockItems.CHESTS_ENDER).add(Blocks.ENDER_CHEST);
        tag(Tags.BlockItems.CHESTS_TRAPPED).add(Blocks.TRAPPED_CHEST);
        tag(Tags.BlockItems.CHESTS_WOODEN).add(Blocks.CHEST, Blocks.TRAPPED_CHEST);
        tag(Tags.BlockItems.CLUSTERS).add(Blocks.AMETHYST_CLUSTER);
        tag(Tags.BlockItems.COBBLESTONES).addTags(Tags.BlockItems.COBBLESTONES_NORMAL, Tags.BlockItems.COBBLESTONES_INFESTED, Tags.BlockItems.COBBLESTONES_MOSSY, Tags.BlockItems.COBBLESTONES_DEEPSLATE);
        tag(Tags.BlockItems.COBBLESTONES_NORMAL).add(Blocks.COBBLESTONE);
        tag(Tags.BlockItems.COBBLESTONES_INFESTED).add(Blocks.INFESTED_COBBLESTONE);
        tag(Tags.BlockItems.COBBLESTONES_MOSSY).add(Blocks.MOSSY_COBBLESTONE);
        tag(Tags.BlockItems.COBBLESTONES_DEEPSLATE).add(Blocks.COBBLED_DEEPSLATE);
        tag(Tags.BlockItems.CONCRETES).addAll(Blocks.CONCRETE.asList());

        ColorCollection<NeoForgeBlockTagsProvider.Appender> builders = Tags.BlockItems.DYED_COLORS.map(BlockItemTagId::block).map(this::tag);
        for (ColorCollection<BlockItemId> colorCollection : dyeableBlockItems()) {
            ColorCollection.zipApply(builders, colorCollection, NeoForgeBlockTagsProvider.Appender::add);
        }
        for (ColorCollection<ResourceKey<Block>> colorCollection : List.of(BlockIds.WALL_BANNER)) {
            ColorCollection.zipApply(builders, colorCollection, NeoForgeBlockTagsProvider.Appender::add);
        }
        addColoredTags(tag(Tags.BlockItems.DYED)::addTag, Tags.BlockItems.DYED.block());
        tag(Tags.BlockItems.DYEABLE_UNDYED_SIMPLE)
                .add(Blocks.CANDLE)
                .add(Blocks.GLASS)
                .add(Blocks.GLASS_PANE)
                .add(Blocks.SHULKER_BOX)
                .add(Blocks.TERRACOTTA);
        tag(Tags.BlockItems.DYEABLE_REDYEABLE_SIMPLE)
                .addOptionalTag(BlockItemTags.BEDS)
                .addOptionalTag(BlockItemTags.WOOL)
                .addOptionalTag(BlockItemTags.WOOL_CARPETS)
                .addOptionalTag(BlockItemTags.WOOL_SLABS)
                .addOptionalTag(BlockItemTags.WOOL_STAIRS)
                .addAll(BlockItemIds.DYED_SHULKER_BOX.map(BlockItemId::block).asList());
        tag(Tags.BlockItems.DYEABLE_SIMPLE)
                .addTag(Tags.BlockItems.DYEABLE_UNDYED_SIMPLE)
                .addTag(Tags.BlockItems.DYEABLE_REDYEABLE_SIMPLE);
        tag(Tags.BlockItems.DYEABLE_DYNAMIC);
        tag(Tags.BlockItems.DYEABLE)
                .addTag(Tags.BlockItems.DYEABLE_SIMPLE)
                .addTag(Tags.BlockItems.DYEABLE_DYNAMIC);

        tag(Tags.BlockItems.END_STONES).add(Blocks.END_STONE);
        tag(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST);
        tag(Tags.BlockItems.FENCE_GATES).addTags(Tags.BlockItems.FENCE_GATES_WOODEN);
        tag(Tags.BlockItems.FENCE_GATES_WOODEN).add(
                Blocks.OAK_FENCE_GATE, Blocks.ACACIA_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE,
                Blocks.PALE_OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE,
                Blocks.JUNGLE_FENCE_GATE, Blocks.CRIMSON_FENCE_GATE, Blocks.WARPED_FENCE_GATE,
                Blocks.MANGROVE_FENCE_GATE, Blocks.BAMBOO_FENCE_GATE, Blocks.CHERRY_FENCE_GATE,
                Blocks.POPLAR_FENCE_GATE);
        tag(Tags.BlockItems.FENCES).addTags(Tags.BlockItems.FENCES_NETHER_BRICK, Tags.BlockItems.FENCES_WOODEN);
        tag(Tags.BlockItems.FENCES_NETHER_BRICK).add(Blocks.NETHER_BRICK_FENCE);
        tag(Tags.BlockItems.FENCES_WOODEN).addOptionalTag(BlockTags.WOODEN_FENCES);
        tag(Tags.BlockItems.FLOWERS_SMALL).add(Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY, Blocks.WITHER_ROSE, Blocks.TORCHFLOWER, Blocks.OPEN_EYEBLOSSOM, Blocks.CLOSED_EYEBLOSSOM).addOptionalTag(BlockTags.SMALL_FLOWERS);
        tag(Tags.BlockItems.FLOWERS_TALL).add(Blocks.SUNFLOWER, Blocks.LILAC, Blocks.PEONY, Blocks.ROSE_BUSH, Blocks.PITCHER_PLANT).addOptionalTag(BlockTags.create(Identifier.withDefaultNamespace("tall_flowers")));
        tag(Tags.BlockItems.FLOWERS).add(Blocks.CHERRY_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES, Blocks.FLOWERING_AZALEA, Blocks.MANGROVE_PROPAGULE, Blocks.PINK_PETALS, Blocks.WILDFLOWERS, Blocks.CHORUS_FLOWER, Blocks.SPORE_BLOSSOM, Blocks.CACTUS_FLOWER).addTags(Tags.BlockItems.FLOWERS_SMALL, Tags.BlockItems.FLOWERS_TALL).addOptionalTag(BlockTags.FLOWERS);
        tag(Tags.BlockItems.FROGLIGHTS).add(Blocks.OCHRE_FROGLIGHT, Blocks.PEARLESCENT_FROGLIGHT, Blocks.VERDANT_FROGLIGHT);
        tag(Tags.BlockItems.GLASS_BLOCKS).addTags(Tags.BlockItems.GLASS_BLOCKS_COLORLESS, Tags.BlockItems.GLASS_BLOCKS_CHEAP, Tags.BlockItems.GLASS_BLOCKS_TINTED);
        tag(Tags.BlockItems.GLASS_BLOCKS_COLORLESS).add(Blocks.GLASS);
        tag(Tags.BlockItems.GLASS_BLOCKS_CHEAP).add(Blocks.GLASS).addAll(Blocks.STAINED_GLASS.asList());
        tag(Tags.BlockItems.GLASS_BLOCKS_TINTED).add(Blocks.TINTED_GLASS);
        tag(Tags.BlockItems.GLASS_PANES).addAll(Blocks.STAINED_GLASS_PANE.asList()).addTags(Tags.BlockItems.GLASS_PANES_COLORLESS);
        tag(Tags.BlockItems.GLASS_PANES_COLORLESS).add(Blocks.GLASS_PANE);
        tag(Tags.BlockItems.GLAZED_TERRACOTTAS).addAll(Blocks.GLAZED_TERRACOTTA.asList());
        tag(Tags.BlockItems.GRAVELS).add(Blocks.GRAVEL);
        tag(Tags.Blocks.SKULLS).add(
                Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL,
                Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD,
                Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.CREEPER_HEAD,
                Blocks.CREEPER_WALL_HEAD, Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD,
                Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD)
                .addTag(BlockTags.SKULLS);
        tag(Tags.Blocks.HIDDEN_FROM_RECIPE_VIEWERS);
        tag(Tags.BlockItems.ICE).add(Blocks.ICE, Blocks.PACKED_ICE, Blocks.BLUE_ICE, Blocks.FROSTED_ICE);
        tag(Tags.BlockItems.NETHERRACKS).add(Blocks.NETHERRACK);
        tag(Tags.BlockItems.OBSIDIANS_NORMAL).add(Blocks.OBSIDIAN);
        tag(Tags.BlockItems.OBSIDIANS_CRYING).add(Blocks.CRYING_OBSIDIAN);
        tag(Tags.BlockItems.OBSIDIANS).addTags(Tags.BlockItems.OBSIDIANS_NORMAL, Tags.BlockItems.OBSIDIANS_CRYING);
        tag(Tags.BlockItems.ORE_BEARING_GROUND_DEEPSLATE).add(Blocks.DEEPSLATE);
        tag(Tags.BlockItems.ORE_BEARING_GROUND_NETHERRACK).add(Blocks.NETHERRACK);
        tag(Tags.BlockItems.ORE_BEARING_GROUND_STONE).add(Blocks.STONE);
        tag(Tags.BlockItems.ORE_RATES_DENSE).add(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.REDSTONE_ORE);
        tag(Tags.BlockItems.ORE_RATES_SINGULAR).add(Blocks.ANCIENT_DEBRIS, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.GOLD_ORE, Blocks.IRON_ORE, Blocks.NETHER_QUARTZ_ORE);
        tag(Tags.BlockItems.ORE_RATES_SPARSE).add(Blocks.NETHER_GOLD_ORE);
        tag(Tags.BlockItems.ORES).addTags(BlockTags.ORES).addTags(Tags.BlockItems.ORES_COAL, Tags.BlockItems.ORES_COPPER, Tags.BlockItems.ORES_DIAMOND, Tags.BlockItems.ORES_EMERALD, Tags.BlockItems.ORES_GOLD, Tags.BlockItems.ORES_IRON, Tags.BlockItems.ORES_LAPIS, Tags.BlockItems.ORES_NETHERITE_SCRAP, Tags.BlockItems.ORES_REDSTONE, Tags.BlockItems.ORES_QUARTZ);
        tag(Tags.BlockItems.ORES_COAL).addTag(BlockItemTags.COAL_ORES);
        tag(Tags.BlockItems.ORES_COPPER).addTag(BlockTags.COPPER_ORES);
        tag(Tags.BlockItems.ORES_DIAMOND).addTag(BlockItemTags.DIAMOND_ORES);
        tag(Tags.BlockItems.ORES_EMERALD).addTag(BlockItemTags.EMERALD_ORES);
        tag(Tags.BlockItems.ORES_GOLD).addTag(BlockTags.GOLD_ORES);
        tag(Tags.BlockItems.ORES_IRON).addTag(BlockTags.IRON_ORES);
        tag(Tags.BlockItems.ORES_LAPIS).addTag(BlockItemTags.LAPIS_ORES);
        tag(Tags.BlockItems.ORES_QUARTZ).add(Blocks.NETHER_QUARTZ_ORE);
        tag(Tags.BlockItems.ORES_REDSTONE).addTag(BlockItemTags.REDSTONE_ORES);
        tag(Tags.BlockItems.ORES_NETHERITE_SCRAP).add(Blocks.ANCIENT_DEBRIS);
        tag(Tags.BlockItems.ORES_IN_GROUND_DEEPSLATE).add(Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
        tag(Tags.BlockItems.ORES_IN_GROUND_NETHERRACK).add(Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE);
        tag(Tags.BlockItems.ORES_IN_GROUND_STONE).add(Blocks.COAL_ORE, Blocks.COPPER_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.GOLD_ORE, Blocks.IRON_ORE, Blocks.LAPIS_ORE, Blocks.REDSTONE_ORE);
        tag(Tags.BlockItems.PUMPKINS).addTags(Tags.BlockItems.PUMPKINS_NORMAL, Tags.BlockItems.PUMPKINS_CARVED, Tags.BlockItems.PUMPKINS_JACK_O_LANTERNS);
        tag(Tags.BlockItems.PUMPKINS_NORMAL).add(Blocks.PUMPKIN);
        tag(Tags.BlockItems.PUMPKINS_CARVED).add(Blocks.CARVED_PUMPKIN);
        tag(Tags.BlockItems.PUMPKINS_JACK_O_LANTERNS).add(Blocks.JACK_O_LANTERN);
        tag(Tags.BlockItems.PLAYER_WORKSTATIONS_CRAFTING_TABLES).add(Blocks.CRAFTING_TABLE);
        tag(Tags.BlockItems.PLAYER_WORKSTATIONS_FURNACES).add(Blocks.FURNACE);
        tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED);
        tag(Tags.BlockItems.ROPES);
        tag(Tags.BlockItems.SANDS).addTags(Tags.BlockItems.SANDS_COLORLESS, Tags.BlockItems.SANDS_RED);
        tag(Tags.BlockItems.SANDS_COLORLESS).add(Blocks.SAND);
        tag(Tags.BlockItems.SANDS_RED).add(Blocks.RED_SAND);

        tag(Tags.BlockItems.SANDSTONE_RED_BLOCKS).add(Blocks.RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE);
        tag(Tags.BlockItems.SANDSTONE_UNCOLORED_BLOCKS).add(Blocks.SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.SMOOTH_SANDSTONE);
        tag(Tags.BlockItems.SANDSTONE_BLOCKS).addTags(Tags.BlockItems.SANDSTONE_RED_BLOCKS, Tags.BlockItems.SANDSTONE_UNCOLORED_BLOCKS);
        tag(Tags.BlockItems.SANDSTONE_RED_SLABS).add(Blocks.RED_SANDSTONE_SLAB, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE_SLAB);
        tag(Tags.BlockItems.SANDSTONE_UNCOLORED_SLABS).add(Blocks.SANDSTONE_SLAB, Blocks.CUT_SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE_SLAB);
        tag(Tags.BlockItems.SANDSTONE_SLABS).addTags(Tags.BlockItems.SANDSTONE_RED_SLABS, Tags.BlockItems.SANDSTONE_UNCOLORED_SLABS);
        tag(Tags.BlockItems.SANDSTONE_RED_STAIRS).add(Blocks.RED_SANDSTONE_STAIRS, Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
        tag(Tags.BlockItems.SANDSTONE_UNCOLORED_STAIRS).add(Blocks.SANDSTONE_STAIRS, Blocks.SMOOTH_SANDSTONE_STAIRS);
        tag(Tags.BlockItems.SANDSTONE_STAIRS).addTags(Tags.BlockItems.SANDSTONE_RED_STAIRS, Tags.BlockItems.SANDSTONE_UNCOLORED_STAIRS);

        tag(Tags.BlockItems.STONES).add(Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.STONE, Blocks.DEEPSLATE, Blocks.TUFF);
        tag(Tags.BlockItems.STORAGE_BLOCKS).addTags(Tags.BlockItems.STORAGE_BLOCKS_BONE_MEAL, Tags.BlockItems.STORAGE_BLOCKS_COAL,
                Tags.BlockItems.STORAGE_BLOCKS_COPPER, Tags.BlockItems.STORAGE_BLOCKS_DIAMOND, Tags.BlockItems.STORAGE_BLOCKS_DRIED_KELP,
                Tags.BlockItems.STORAGE_BLOCKS_EMERALD, Tags.BlockItems.STORAGE_BLOCKS_GOLD, Tags.BlockItems.STORAGE_BLOCKS_IRON,
                Tags.BlockItems.STORAGE_BLOCKS_LAPIS, Tags.BlockItems.STORAGE_BLOCKS_NETHERITE, Tags.BlockItems.STORAGE_BLOCKS_RAW_COPPER,
                Tags.BlockItems.STORAGE_BLOCKS_RAW_GOLD, Tags.BlockItems.STORAGE_BLOCKS_RAW_IRON, Tags.BlockItems.STORAGE_BLOCKS_REDSTONE,
                Tags.BlockItems.STORAGE_BLOCKS_RESIN, Tags.BlockItems.STORAGE_BLOCKS_SLIME, Tags.BlockItems.STORAGE_BLOCKS_WHEAT);
        tag(Tags.BlockItems.STORAGE_BLOCKS_BONE_MEAL).add(Blocks.BONE_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_COAL).add(Blocks.COAL_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_COPPER).add(Blocks.COPPER_BLOCK.weathering().unaffected());
        tag(Tags.BlockItems.STORAGE_BLOCKS_DIAMOND).add(Blocks.DIAMOND_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_DRIED_KELP).add(Blocks.DRIED_KELP_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_EMERALD).add(Blocks.EMERALD_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_GOLD).add(Blocks.GOLD_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_IRON).add(Blocks.IRON_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_LAPIS).add(Blocks.LAPIS_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_NETHERITE).add(Blocks.NETHERITE_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_RAW_COPPER).add(Blocks.RAW_COPPER_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_RAW_GOLD).add(Blocks.RAW_GOLD_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_RAW_IRON).add(Blocks.RAW_IRON_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_REDSTONE).add(Blocks.REDSTONE_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_RESIN).add(Blocks.RESIN_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_SLIME).add(Blocks.SLIME_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_WHEAT).add(Blocks.HAY_BLOCK);
        tag(Tags.BlockItems.OVERWORLD_NATURAL_LOGS).addOptionalTags(
                BlockTags.OVERWORLD_NATURAL_LOGS);
        tag(Tags.BlockItems.NETHER_NATURAL_LOGS).add(
                Blocks.CRIMSON_STEM, Blocks.WARPED_STEM);
        tag(Tags.BlockItems.NATURAL_LOGS).addOptionalTags(
                Tags.BlockItems.OVERWORLD_NATURAL_LOGS, Tags.BlockItems.NETHER_NATURAL_LOGS);
        tag(Tags.BlockItems.NATURAL_WOODS).add(
                Blocks.ACACIA_WOOD, Blocks.BIRCH_WOOD, Blocks.CHERRY_WOOD,
                Blocks.DARK_OAK_WOOD, Blocks.JUNGLE_WOOD, Blocks.MANGROVE_WOOD,
                Blocks.OAK_WOOD, Blocks.PALE_OAK_WOOD, Blocks.SPRUCE_WOOD,
                Blocks.POPLAR_WOOD, Blocks.CRIMSON_HYPHAE, Blocks.WARPED_HYPHAE);
        tag(Tags.BlockItems.STRIPPED_LOGS).add(
                Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.STRIPPED_BIRCH_LOG,
                Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_JUNGLE_LOG,
                Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_PALE_OAK_LOG,
                Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_POPLAR_LOG, Blocks.STRIPPED_CRIMSON_STEM,
                Blocks.STRIPPED_WARPED_STEM);
        tag(Tags.BlockItems.STRIPPED_WOODS).add(
                Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_CHERRY_WOOD,
                Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD,
                Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD, Blocks.STRIPPED_SPRUCE_WOOD,
                Blocks.STRIPPED_POPLAR_WOOD, Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE);
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

    private void addColored(TagKey<Block> group, String pattern) {
        String prefix = group.location().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color : DyeColor.values()) {
            Identifier key = Identifier.fromNamespaceAndPath("minecraft", pattern.replace("{color}", color.getName()));
            TagKey<Block> tag = getForgeTag(prefix + color.getName());
            Block block = BuiltInRegistries.BLOCK.getValue(key);
            if (block == null || block == Blocks.AIR)
                throw new IllegalStateException("Unknown vanilla block: " + key);
            tag(tag).add(block);
        }
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

    private void addColoredTags(Consumer<TagKey<Block>> consumer, TagKey<Block> group) {
        String prefix = group.location().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color : DyeColor.values()) {
            TagKey<Block> tag = getForgeTag(prefix + color.getName());
            consumer.accept(tag);
        }
    }

    @SuppressWarnings("unchecked")
    private TagKey<Block> getForgeTag(String name) {
        try {
            name = name.toUpperCase(Locale.ENGLISH);
            return (TagKey<Block>) Tags.Blocks.class.getDeclaredField(name).get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(Tags.Blocks.class.getName() + " is missing tag name: " + name);
        }
    }

    static List<ColorCollection<BlockItemId>> dyeableBlockItems() {
        return List.of(
                BlockItemIds.BANNER, BlockItemIds.BED, BlockItemIds.DYED_CANDLE,
                BlockItemIds.CARPET, BlockItemIds.CONCRETE, BlockItemIds.CONCRETE_SLAB,
                BlockItemIds.CONCRETE_STAIRS, BlockItemIds.CONCRETE_POWDER, BlockItemIds.GLAZED_TERRACOTTA,
                BlockItemIds.DYED_SHULKER_BOX, BlockItemIds.STAINED_GLASS, BlockItemIds.STAINED_GLASS_PANE,
                BlockItemIds.DYED_TERRACOTTA, BlockItemIds.WOOL, BlockItemIds.WOOL_SLAB, BlockItemIds.WOOL_STAIRS);
    }
}
