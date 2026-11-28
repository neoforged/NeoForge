/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.data.tags.BlockItemTagsProvider;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ColorCollection;
import net.neoforged.neoforge.common.Tags;

public class NeoForgeBlockItemTagsProvider extends BlockItemTagsProvider {
    protected NeoForgeBlockItemTagsProvider(Function<BlockItemTagId, CombinedAppender> tagSupplier) {
        super(tagSupplier);
    }

    @Override
    protected void run() {
        tag(Tags.BlockItems.BARRELS).addTag(Tags.BlockItems.BARRELS_WOODEN);
        tag(Tags.BlockItems.BARRELS_WOODEN).add(BlockItemIds.BARREL);
        tag(Tags.BlockItems.BARS).addTag(Tags.BlockItems.BARS_COPPER, Tags.BlockItems.BARS_IRON, BlockItemTags.BARS);
        tag(Tags.BlockItems.BARS_COPPER).addAll(BlockItemIds.COPPER_BARS.asList());
        tag(Tags.BlockItems.BARS_IRON).add(BlockItemIds.IRON_BARS);
        tag(Tags.BlockItems.BOOKSHELVES).add(BlockItemIds.BOOKSHELF);
        tag(Tags.BlockItems.BUDDING_BLOCKS).add(BlockItemIds.BUDDING_AMETHYST);
        tag(Tags.BlockItems.BUDS).add(BlockItemIds.SMALL_AMETHYST_BUD).add(BlockItemIds.MEDIUM_AMETHYST_BUD).add(BlockItemIds.LARGE_AMETHYST_BUD);
        tag(Tags.BlockItems.CHAINS).add(BlockItemIds.IRON_CHAIN).addAll(BlockItemIds.COPPER_CHAIN.asList());
        tag(Tags.BlockItems.CHESTS).addTag(Tags.BlockItems.CHESTS_ENDER, Tags.BlockItems.CHESTS_TRAPPED, Tags.BlockItems.CHESTS_WOODEN).addOptionalTag(BlockItemTags.COPPER_CHESTS);
        tag(Tags.BlockItems.CHESTS_ENDER).add(BlockItemIds.ENDER_CHEST);
        tag(Tags.BlockItems.CHESTS_TRAPPED).add(BlockItemIds.TRAPPED_CHEST);
        tag(Tags.BlockItems.CHESTS_WOODEN).add(BlockItemIds.CHEST, BlockItemIds.TRAPPED_CHEST);
        tag(Tags.BlockItems.CLUSTERS).add(BlockItemIds.AMETHYST_CLUSTER);
        tag(Tags.BlockItems.COBBLESTONES).addTag(Tags.BlockItems.COBBLESTONES_NORMAL, Tags.BlockItems.COBBLESTONES_INFESTED, Tags.BlockItems.COBBLESTONES_MOSSY, Tags.BlockItems.COBBLESTONES_DEEPSLATE);
        tag(Tags.BlockItems.COBBLESTONES_NORMAL).add(BlockItemIds.COBBLESTONE);
        tag(Tags.BlockItems.COBBLESTONES_INFESTED).add(BlockItemIds.INFESTED_COBBLESTONE);
        tag(Tags.BlockItems.COBBLESTONES_MOSSY).add(BlockItemIds.MOSSY_COBBLESTONE);
        tag(Tags.BlockItems.COBBLESTONES_DEEPSLATE).add(BlockItemIds.COBBLED_DEEPSLATE);
        tag(Tags.BlockItems.CONCRETES).addAll(BlockItemIds.CONCRETE.asList());

        ColorCollection<CombinedAppender> builders = Tags.BlockItems.DYED_COLORS.map(this::tag);
        for (ColorCollection<BlockItemId> colorCollection : dyeableBlockItems()) {
            ColorCollection.zipApply(builders, colorCollection, CombinedAppender::add);
        }
        addColoredTags(Tags.BlockItems.DYED);

        tag(Tags.BlockItems.DYEABLE_UNDYED_SIMPLE)
                .add(BlockItemIds.CANDLE)
                .add(BlockItemIds.GLASS)
                .add(BlockItemIds.GLASS_PANE)
                .add(BlockItemIds.SHULKER_BOX)
                .add(BlockItemIds.TERRACOTTA);
        tag(Tags.BlockItems.DYEABLE_REDYEABLE_SIMPLE)
                .addOptionalTag(BlockItemTags.BEDS)
                .addOptionalTag(BlockItemTags.WOOL)
                .addOptionalTag(BlockItemTags.WOOL_CARPETS)
                .addOptionalTag(BlockItemTags.WOOL_SLABS)
                .addOptionalTag(BlockItemTags.WOOL_STAIRS)
                .addAll(BlockItemIds.DYED_SHULKER_BOX.asList());
        tag(Tags.BlockItems.DYEABLE_SIMPLE)
                .addTag(Tags.BlockItems.DYEABLE_UNDYED_SIMPLE)
                .addTag(Tags.BlockItems.DYEABLE_REDYEABLE_SIMPLE);
        tag(Tags.BlockItems.DYEABLE_DYNAMIC);
        tag(Tags.BlockItems.DYEABLE)
                .addTag(Tags.BlockItems.DYEABLE_SIMPLE)
                .addTag(Tags.BlockItems.DYEABLE_DYNAMIC);

        tag(Tags.BlockItems.END_STONES).add(BlockItemIds.END_STONE);
        tag(Tags.BlockItems.FENCE_GATES).addTag(Tags.BlockItems.FENCE_GATES_WOODEN);
        tag(Tags.BlockItems.FENCE_GATES_WOODEN).add(
                BlockItemIds.OAK_FENCE_GATE, BlockItemIds.ACACIA_FENCE_GATE, BlockItemIds.DARK_OAK_FENCE_GATE,
                BlockItemIds.PALE_OAK_FENCE_GATE, BlockItemIds.SPRUCE_FENCE_GATE, BlockItemIds.BIRCH_FENCE_GATE,
                BlockItemIds.JUNGLE_FENCE_GATE, BlockItemIds.CRIMSON_FENCE_GATE, BlockItemIds.WARPED_FENCE_GATE,
                BlockItemIds.MANGROVE_FENCE_GATE, BlockItemIds.BAMBOO_FENCE_GATE, BlockItemIds.CHERRY_FENCE_GATE,
                BlockItemIds.POPLAR_FENCE_GATE);
        tag(Tags.BlockItems.FENCES).addTag(Tags.BlockItems.FENCES_NETHER_BRICK, Tags.BlockItems.FENCES_WOODEN);
        tag(Tags.BlockItems.FENCES_NETHER_BRICK).add(BlockItemIds.NETHER_BRICK_FENCE);
        tag(Tags.BlockItems.FENCES_WOODEN).addOptionalTag(BlockItemTags.WOODEN_FENCES);
        tag(Tags.BlockItems.FLOWERS_SMALL).add(BlockItemIds.DANDELION, BlockItemIds.POPPY, BlockItemIds.BLUE_ORCHID, BlockItemIds.ALLIUM, BlockItemIds.AZURE_BLUET, BlockItemIds.RED_TULIP, BlockItemIds.ORANGE_TULIP, BlockItemIds.WHITE_TULIP, BlockItemIds.PINK_TULIP, BlockItemIds.OXEYE_DAISY, BlockItemIds.CORNFLOWER, BlockItemIds.LILY_OF_THE_VALLEY, BlockItemIds.WITHER_ROSE, BlockItemIds.TORCHFLOWER, BlockItemIds.OPEN_EYEBLOSSOM, BlockItemIds.CLOSED_EYEBLOSSOM).addOptionalTag(BlockItemTags.SMALL_FLOWERS);
        tag(Tags.BlockItems.FLOWERS_TALL).add(BlockItemIds.SUNFLOWER, BlockItemIds.LILAC, BlockItemIds.PEONY, BlockItemIds.ROSE_BUSH, BlockItemIds.PITCHER_PLANT).addOptionalTag(BlockItemTagId.create("tall_flowers"));
        tag(Tags.BlockItems.FLOWERS).add(BlockItemIds.CHERRY_LEAVES, BlockItemIds.FLOWERING_AZALEA_LEAVES, BlockItemIds.FLOWERING_AZALEA, BlockItemIds.MANGROVE_PROPAGULE, BlockItemIds.PINK_PETALS, BlockItemIds.WILDFLOWERS, BlockItemIds.CHORUS_FLOWER, BlockItemIds.SPORE_BLOSSOM, BlockItemIds.CACTUS_FLOWER).addTag(Tags.BlockItems.FLOWERS_SMALL, Tags.BlockItems.FLOWERS_TALL).addOptionalTag(BlockItemTags.FLOWERS);
        tag(Tags.BlockItems.FROGLIGHTS).add(BlockItemIds.OCHRE_FROGLIGHT, BlockItemIds.PEARLESCENT_FROGLIGHT, BlockItemIds.VERDANT_FROGLIGHT);
        tag(Tags.BlockItems.GLASS_BLOCKS).addTag(Tags.BlockItems.GLASS_BLOCKS_COLORLESS, Tags.BlockItems.GLASS_BLOCKS_CHEAP, Tags.BlockItems.GLASS_BLOCKS_TINTED);
        tag(Tags.BlockItems.GLASS_BLOCKS_COLORLESS).add(BlockItemIds.GLASS);
        tag(Tags.BlockItems.GLASS_BLOCKS_CHEAP).add(BlockItemIds.GLASS).addAll(BlockItemIds.STAINED_GLASS.asList());
        tag(Tags.BlockItems.GLASS_BLOCKS_TINTED).add(BlockItemIds.TINTED_GLASS);
        tag(Tags.BlockItems.GLASS_PANES).addAll(BlockItemIds.STAINED_GLASS_PANE.asList()).addTag(Tags.BlockItems.GLASS_PANES_COLORLESS);
        tag(Tags.BlockItems.GLASS_PANES_COLORLESS).add(BlockItemIds.GLASS_PANE);
        tag(Tags.BlockItems.GLAZED_TERRACOTTAS).addAll(BlockItemIds.GLAZED_TERRACOTTA.asList());
        tag(Tags.BlockItems.GRAVELS).add(BlockItemIds.GRAVEL);
        tag(Tags.BlockItems.ICE).add(BlockItemIds.ICE, BlockItemIds.PACKED_ICE, BlockItemIds.BLUE_ICE);
        tag(Tags.BlockItems.NETHERRACKS).add(BlockItemIds.NETHERRACK);
        tag(Tags.BlockItems.OBSIDIANS_NORMAL).add(BlockItemIds.OBSIDIAN);
        tag(Tags.BlockItems.OBSIDIANS_CRYING).add(BlockItemIds.CRYING_OBSIDIAN);
        tag(Tags.BlockItems.OBSIDIANS).addTag(Tags.BlockItems.OBSIDIANS_NORMAL, Tags.BlockItems.OBSIDIANS_CRYING);
        tag(Tags.BlockItems.ORE_BEARING_GROUND_DEEPSLATE).add(BlockItemIds.DEEPSLATE);
        tag(Tags.BlockItems.ORE_BEARING_GROUND_NETHERRACK).add(BlockItemIds.NETHERRACK);
        tag(Tags.BlockItems.ORE_BEARING_GROUND_STONE).add(BlockItemIds.STONE);
        tag(Tags.BlockItems.ORE_RATES_DENSE).add(BlockItemIds.COPPER_ORE, BlockItemIds.DEEPSLATE_COPPER_ORE, BlockItemIds.DEEPSLATE_LAPIS_ORE, BlockItemIds.DEEPSLATE_REDSTONE_ORE, BlockItemIds.LAPIS_ORE, BlockItemIds.REDSTONE_ORE);
        tag(Tags.BlockItems.ORE_RATES_SINGULAR).add(BlockItemIds.ANCIENT_DEBRIS, BlockItemIds.COAL_ORE, BlockItemIds.DEEPSLATE_COAL_ORE, BlockItemIds.DEEPSLATE_DIAMOND_ORE, BlockItemIds.DEEPSLATE_EMERALD_ORE, BlockItemIds.DEEPSLATE_GOLD_ORE, BlockItemIds.DEEPSLATE_IRON_ORE, BlockItemIds.DIAMOND_ORE, BlockItemIds.EMERALD_ORE, BlockItemIds.GOLD_ORE, BlockItemIds.IRON_ORE, BlockItemIds.NETHER_QUARTZ_ORE);
        tag(Tags.BlockItems.ORE_RATES_SPARSE).add(BlockItemIds.NETHER_GOLD_ORE);
        tag(Tags.BlockItems.ORES).addTag(BlockItemTags.ORES, Tags.BlockItems.ORES_COAL, Tags.BlockItems.ORES_COPPER, Tags.BlockItems.ORES_DIAMOND, Tags.BlockItems.ORES_EMERALD, Tags.BlockItems.ORES_GOLD, Tags.BlockItems.ORES_IRON, Tags.BlockItems.ORES_LAPIS, Tags.BlockItems.ORES_NETHERITE_SCRAP, Tags.BlockItems.ORES_REDSTONE, Tags.BlockItems.ORES_QUARTZ);
        tag(Tags.BlockItems.ORES_COAL).addTag(BlockItemTags.COAL_ORES);
        tag(Tags.BlockItems.ORES_COPPER).addTag(BlockItemTags.COPPER_ORES);
        tag(Tags.BlockItems.ORES_DIAMOND).addTag(BlockItemTags.DIAMOND_ORES);
        tag(Tags.BlockItems.ORES_EMERALD).addTag(BlockItemTags.EMERALD_ORES);
        tag(Tags.BlockItems.ORES_GOLD).addTag(BlockItemTags.GOLD_ORES);
        tag(Tags.BlockItems.ORES_IRON).addTag(BlockItemTags.IRON_ORES);
        tag(Tags.BlockItems.ORES_LAPIS).addTag(BlockItemTags.LAPIS_ORES);
        tag(Tags.BlockItems.ORES_QUARTZ).add(BlockItemIds.NETHER_QUARTZ_ORE);
        tag(Tags.BlockItems.ORES_REDSTONE).addTag(BlockItemTags.REDSTONE_ORES);
        tag(Tags.BlockItems.ORES_NETHERITE_SCRAP).add(BlockItemIds.ANCIENT_DEBRIS);
        tag(Tags.BlockItems.ORES_IN_GROUND_DEEPSLATE).add(BlockItemIds.DEEPSLATE_COAL_ORE, BlockItemIds.DEEPSLATE_COPPER_ORE, BlockItemIds.DEEPSLATE_DIAMOND_ORE, BlockItemIds.DEEPSLATE_EMERALD_ORE, BlockItemIds.DEEPSLATE_GOLD_ORE, BlockItemIds.DEEPSLATE_IRON_ORE, BlockItemIds.DEEPSLATE_LAPIS_ORE, BlockItemIds.DEEPSLATE_REDSTONE_ORE);
        tag(Tags.BlockItems.ORES_IN_GROUND_NETHERRACK).add(BlockItemIds.NETHER_GOLD_ORE, BlockItemIds.NETHER_QUARTZ_ORE);
        tag(Tags.BlockItems.ORES_IN_GROUND_STONE).add(BlockItemIds.COAL_ORE, BlockItemIds.COPPER_ORE, BlockItemIds.DIAMOND_ORE, BlockItemIds.EMERALD_ORE, BlockItemIds.GOLD_ORE, BlockItemIds.IRON_ORE, BlockItemIds.LAPIS_ORE, BlockItemIds.REDSTONE_ORE);
        tag(Tags.BlockItems.PUMPKINS).addTag(Tags.BlockItems.PUMPKINS_NORMAL, Tags.BlockItems.PUMPKINS_CARVED, Tags.BlockItems.PUMPKINS_JACK_O_LANTERNS);
        tag(Tags.BlockItems.PUMPKINS_NORMAL).add(BlockItemIds.PUMPKIN);
        tag(Tags.BlockItems.PUMPKINS_CARVED).add(BlockItemIds.CARVED_PUMPKIN);
        tag(Tags.BlockItems.PUMPKINS_JACK_O_LANTERNS).add(BlockItemIds.JACK_O_LANTERN);
        tag(Tags.BlockItems.PLAYER_WORKSTATIONS_CRAFTING_TABLES).add(BlockItemIds.CRAFTING_TABLE);
        tag(Tags.BlockItems.PLAYER_WORKSTATIONS_FURNACES).add(BlockItemIds.FURNACE);
        tag(Tags.BlockItems.ROPES);
        tag(Tags.BlockItems.SANDS).addTag(Tags.BlockItems.SANDS_COLORLESS, Tags.BlockItems.SANDS_RED);
        tag(Tags.BlockItems.SANDS_COLORLESS).add(BlockItemIds.SAND);
        tag(Tags.BlockItems.SANDS_RED).add(BlockItemIds.RED_SAND);

        tag(Tags.BlockItems.SANDSTONE_RED_BLOCKS).add(BlockItemIds.RED_SANDSTONE, BlockItemIds.CUT_RED_SANDSTONE, BlockItemIds.CHISELED_RED_SANDSTONE, BlockItemIds.SMOOTH_RED_SANDSTONE);
        tag(Tags.BlockItems.SANDSTONE_UNCOLORED_BLOCKS).add(BlockItemIds.SANDSTONE, BlockItemIds.CUT_SANDSTONE, BlockItemIds.CHISELED_SANDSTONE, BlockItemIds.SMOOTH_SANDSTONE);
        tag(Tags.BlockItems.SANDSTONE_BLOCKS).addTag(Tags.BlockItems.SANDSTONE_RED_BLOCKS, Tags.BlockItems.SANDSTONE_UNCOLORED_BLOCKS);
        tag(Tags.BlockItems.SANDSTONE_RED_SLABS).add(BlockItemIds.RED_SANDSTONE_SLAB, BlockItemIds.CUT_RED_SANDSTONE_SLAB, BlockItemIds.SMOOTH_RED_SANDSTONE_SLAB);
        tag(Tags.BlockItems.SANDSTONE_UNCOLORED_SLABS).add(BlockItemIds.SANDSTONE_SLAB, BlockItemIds.CUT_SANDSTONE_SLAB, BlockItemIds.SMOOTH_SANDSTONE_SLAB);
        tag(Tags.BlockItems.SANDSTONE_SLABS).addTag(Tags.BlockItems.SANDSTONE_RED_SLABS, Tags.BlockItems.SANDSTONE_UNCOLORED_SLABS);
        tag(Tags.BlockItems.SANDSTONE_RED_STAIRS).add(BlockItemIds.RED_SANDSTONE_STAIRS, BlockItemIds.SMOOTH_RED_SANDSTONE_STAIRS);
        tag(Tags.BlockItems.SANDSTONE_UNCOLORED_STAIRS).add(BlockItemIds.SANDSTONE_STAIRS, BlockItemIds.SMOOTH_SANDSTONE_STAIRS);
        tag(Tags.BlockItems.SANDSTONE_STAIRS).addTag(Tags.BlockItems.SANDSTONE_RED_STAIRS, Tags.BlockItems.SANDSTONE_UNCOLORED_STAIRS);

        tag(Tags.BlockItems.STONES).add(BlockItemIds.ANDESITE, BlockItemIds.DIORITE, BlockItemIds.GRANITE, BlockItemIds.STONE, BlockItemIds.DEEPSLATE, BlockItemIds.TUFF);
        tag(Tags.BlockItems.STORAGE_BLOCKS).addTag(Tags.BlockItems.STORAGE_BLOCKS_BONE_MEAL, Tags.BlockItems.STORAGE_BLOCKS_COAL,
                Tags.BlockItems.STORAGE_BLOCKS_COPPER, Tags.BlockItems.STORAGE_BLOCKS_DIAMOND, Tags.BlockItems.STORAGE_BLOCKS_DRIED_KELP,
                Tags.BlockItems.STORAGE_BLOCKS_EMERALD, Tags.BlockItems.STORAGE_BLOCKS_GOLD, Tags.BlockItems.STORAGE_BLOCKS_IRON,
                Tags.BlockItems.STORAGE_BLOCKS_LAPIS, Tags.BlockItems.STORAGE_BLOCKS_NETHERITE, Tags.BlockItems.STORAGE_BLOCKS_RAW_COPPER,
                Tags.BlockItems.STORAGE_BLOCKS_RAW_GOLD, Tags.BlockItems.STORAGE_BLOCKS_RAW_IRON, Tags.BlockItems.STORAGE_BLOCKS_REDSTONE,
                Tags.BlockItems.STORAGE_BLOCKS_RESIN, Tags.BlockItems.STORAGE_BLOCKS_SLIME, Tags.BlockItems.STORAGE_BLOCKS_WHEAT);
        tag(Tags.BlockItems.STORAGE_BLOCKS_BONE_MEAL).add(BlockItemIds.BONE_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_COAL).add(BlockItemIds.COAL_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_COPPER).add(BlockItemIds.COPPER_BLOCK.weathering().unaffected());
        tag(Tags.BlockItems.STORAGE_BLOCKS_DIAMOND).add(BlockItemIds.DIAMOND_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_DRIED_KELP).add(BlockItemIds.DRIED_KELP_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_EMERALD).add(BlockItemIds.EMERALD_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_GOLD).add(BlockItemIds.GOLD_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_IRON).add(BlockItemIds.IRON_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_LAPIS).add(BlockItemIds.LAPIS_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_NETHERITE).add(BlockItemIds.NETHERITE_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_RAW_COPPER).add(BlockItemIds.RAW_COPPER_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_RAW_GOLD).add(BlockItemIds.RAW_GOLD_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_RAW_IRON).add(BlockItemIds.RAW_IRON_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_REDSTONE).add(BlockItemIds.REDSTONE_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_RESIN).add(BlockItemIds.RESIN_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_SLIME).add(BlockItemIds.SLIME_BLOCK);
        tag(Tags.BlockItems.STORAGE_BLOCKS_WHEAT).add(BlockItemIds.HAY_BLOCK);
        tag(Tags.BlockItems.OVERWORLD_NATURAL_LOGS);
        tag(Tags.BlockItems.NETHER_NATURAL_LOGS).add(BlockItemIds.CRIMSON_STEM, BlockItemIds.WARPED_STEM);
        tag(Tags.BlockItems.NATURAL_LOGS).addOptionalTag(
                Tags.BlockItems.OVERWORLD_NATURAL_LOGS, Tags.BlockItems.NETHER_NATURAL_LOGS);
        tag(Tags.BlockItems.NATURAL_WOODS).add(
                BlockItemIds.ACACIA_WOOD, BlockItemIds.BIRCH_WOOD, BlockItemIds.CHERRY_WOOD,
                BlockItemIds.DARK_OAK_WOOD, BlockItemIds.JUNGLE_WOOD, BlockItemIds.MANGROVE_WOOD,
                BlockItemIds.OAK_WOOD, BlockItemIds.PALE_OAK_WOOD, BlockItemIds.SPRUCE_WOOD,
                BlockItemIds.POPLAR_WOOD, BlockItemIds.CRIMSON_HYPHAE, BlockItemIds.WARPED_HYPHAE);
        tag(Tags.BlockItems.STRIPPED_LOGS).add(
                BlockItemIds.STRIPPED_ACACIA_LOG, BlockItemIds.STRIPPED_BAMBOO_BLOCK, BlockItemIds.STRIPPED_BIRCH_LOG,
                BlockItemIds.STRIPPED_CHERRY_LOG, BlockItemIds.STRIPPED_DARK_OAK_LOG, BlockItemIds.STRIPPED_JUNGLE_LOG,
                BlockItemIds.STRIPPED_MANGROVE_LOG, BlockItemIds.STRIPPED_OAK_LOG, BlockItemIds.STRIPPED_PALE_OAK_LOG,
                BlockItemIds.STRIPPED_SPRUCE_LOG, BlockItemIds.STRIPPED_POPLAR_LOG, BlockItemIds.STRIPPED_CRIMSON_STEM,
                BlockItemIds.STRIPPED_WARPED_STEM);
        tag(Tags.BlockItems.STRIPPED_WOODS).add(
                BlockItemIds.STRIPPED_ACACIA_WOOD, BlockItemIds.STRIPPED_BIRCH_WOOD, BlockItemIds.STRIPPED_CHERRY_WOOD,
                BlockItemIds.STRIPPED_DARK_OAK_WOOD, BlockItemIds.STRIPPED_JUNGLE_WOOD, BlockItemIds.STRIPPED_MANGROVE_WOOD,
                BlockItemIds.STRIPPED_OAK_WOOD, BlockItemIds.STRIPPED_PALE_OAK_WOOD, BlockItemIds.STRIPPED_SPRUCE_WOOD,
                BlockItemIds.STRIPPED_POPLAR_WOOD, BlockItemIds.STRIPPED_CRIMSON_HYPHAE, BlockItemIds.STRIPPED_WARPED_HYPHAE);
    }

    private static List<ColorCollection<BlockItemId>> dyeableBlockItems() {
        return List.of(
                BlockItemIds.BANNER, BlockItemIds.BED, BlockItemIds.DYED_CANDLE,
                BlockItemIds.CARPET, BlockItemIds.CONCRETE, BlockItemIds.CONCRETE_SLAB,
                BlockItemIds.CONCRETE_STAIRS, BlockItemIds.CONCRETE_POWDER, BlockItemIds.GLAZED_TERRACOTTA,
                BlockItemIds.DYED_SHULKER_BOX, BlockItemIds.STAINED_GLASS, BlockItemIds.STAINED_GLASS_PANE,
                BlockItemIds.DYED_TERRACOTTA, BlockItemIds.WOOL, BlockItemIds.WOOL_SLAB, BlockItemIds.WOOL_STAIRS);
    }

    private void addColoredTags(BlockItemTagId group) {
        CombinedAppender appender = tag(group);
        // Since the colored tags are within block items, they should have the name
        // names for block and item tags
        String prefix = group.block().location().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color : DyeColor.values()) {
            BlockItemTagId tag = getForgeTag(prefix + color.getName());
            appender.addTag(tag);
        }
    }

    private BlockItemTagId getForgeTag(String name) {
        try {
            name = name.toUpperCase(Locale.ENGLISH);
            return (BlockItemTagId) Tags.BlockItems.class.getDeclaredField(name).get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(Tags.BlockItems.class.getName() + " is missing tag name: " + name);
        }
    }
}
