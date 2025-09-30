/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public final class NeoForgeBlockTagsProvider extends BlockTagsProvider {
    public NeoForgeBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, "neoforge");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTags(HolderLookup.Provider p_256380_) {
        tag(Tags.Blocks.BARRELS).addTag(Tags.Blocks.BARRELS_WOODEN);
        tag(Tags.Blocks.BARRELS_WOODEN).add(Blocks.BARREL);
        tag(Tags.Blocks.BOOKSHELVES).add(Blocks.BOOKSHELF);
        tag(Tags.Blocks.BUDDING_BLOCKS).add(Blocks.BUDDING_AMETHYST);
        tag(Tags.Blocks.BUDS).add(Blocks.SMALL_AMETHYST_BUD).add(Blocks.MEDIUM_AMETHYST_BUD).add(Blocks.LARGE_AMETHYST_BUD);
        tag(Tags.Blocks.CHAINS).add(Blocks.IRON_CHAIN).addAll(Blocks.COPPER_CHAIN.asList());
        tag(Tags.Blocks.CHESTS).add(Blocks.COPPER_CHEST).addTags(Tags.Blocks.CHESTS_ENDER, Tags.Blocks.CHESTS_TRAPPED, Tags.Blocks.CHESTS_WOODEN);
        tag(Tags.Blocks.CHESTS_ENDER).add(Blocks.ENDER_CHEST);
        tag(Tags.Blocks.CHESTS_TRAPPED).add(Blocks.TRAPPED_CHEST);
        tag(Tags.Blocks.CHESTS_WOODEN).add(Blocks.CHEST, Blocks.TRAPPED_CHEST);
        tag(Tags.Blocks.CLUSTERS).add(Blocks.AMETHYST_CLUSTER);
        tag(Tags.Blocks.COBBLESTONES).addTags(Tags.Blocks.COBBLESTONES_NORMAL, Tags.Blocks.COBBLESTONES_INFESTED, Tags.Blocks.COBBLESTONES_MOSSY, Tags.Blocks.COBBLESTONES_DEEPSLATE);
        tag(Tags.Blocks.COBBLESTONES_NORMAL).add(Blocks.COBBLESTONE);
        tag(Tags.Blocks.COBBLESTONES_INFESTED).add(Blocks.INFESTED_COBBLESTONE);
        tag(Tags.Blocks.COBBLESTONES_MOSSY).add(Blocks.MOSSY_COBBLESTONE);
        tag(Tags.Blocks.COBBLESTONES_DEEPSLATE).add(Blocks.COBBLED_DEEPSLATE);
        tag(Tags.Blocks.CONCRETES).add(Blocks.WHITE_CONCRETE, Blocks.ORANGE_CONCRETE, Blocks.MAGENTA_CONCRETE, Blocks.LIGHT_BLUE_CONCRETE, Blocks.YELLOW_CONCRETE, Blocks.LIME_CONCRETE, Blocks.PINK_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.LIGHT_GRAY_CONCRETE, Blocks.CYAN_CONCRETE, Blocks.PURPLE_CONCRETE, Blocks.BLUE_CONCRETE, Blocks.BROWN_CONCRETE, Blocks.GREEN_CONCRETE, Blocks.RED_CONCRETE, Blocks.BLACK_CONCRETE);
        addColored(Tags.Blocks.DYED, "{color}_banner");
        addColored(Tags.Blocks.DYED, "{color}_bed");
        addColored(Tags.Blocks.DYED, "{color}_candle");
        addColored(Tags.Blocks.DYED, "{color}_carpet");
        addColored(Tags.Blocks.DYED, "{color}_concrete");
        addColored(Tags.Blocks.DYED, "{color}_concrete_powder");
        addColored(Tags.Blocks.DYED, "{color}_glazed_terracotta");
        addColored(Tags.Blocks.DYED, "{color}_shulker_box");
        addColored(Tags.Blocks.DYED, "{color}_stained_glass");
        addColored(Tags.Blocks.DYED, "{color}_stained_glass_pane");
        addColored(Tags.Blocks.DYED, "{color}_terracotta");
        addColored(Tags.Blocks.DYED, "{color}_wall_banner");
        addColored(Tags.Blocks.DYED, "{color}_wool");
        addColoredTags(tag(Tags.Blocks.DYED)::addTag, Tags.Blocks.DYED);
        tag(Tags.Blocks.END_STONES).add(Blocks.END_STONE);
        tag(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST);
        tag(Tags.Blocks.FENCE_GATES).addTags(Tags.Blocks.FENCE_GATES_WOODEN);
        tag(Tags.Blocks.FENCE_GATES_WOODEN).add(Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.ACACIA_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.CRIMSON_FENCE_GATE, Blocks.WARPED_FENCE_GATE, Blocks.MANGROVE_FENCE_GATE, Blocks.BAMBOO_FENCE_GATE, Blocks.CHERRY_FENCE_GATE);
        tag(Tags.Blocks.FENCES).addTags(Tags.Blocks.FENCES_NETHER_BRICK, Tags.Blocks.FENCES_WOODEN);
        tag(Tags.Blocks.FENCES_NETHER_BRICK).add(Blocks.NETHER_BRICK_FENCE);
        tag(Tags.Blocks.FENCES_WOODEN).addTag(BlockTags.WOODEN_FENCES);
        tag(Tags.Blocks.FLOWERS_SMALL).add(Blocks.DANDELION, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET, Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER, Blocks.LILY_OF_THE_VALLEY, Blocks.WITHER_ROSE, Blocks.TORCHFLOWER, Blocks.OPEN_EYEBLOSSOM, Blocks.CLOSED_EYEBLOSSOM).addOptionalTag(BlockTags.SMALL_FLOWERS);
        tag(Tags.Blocks.FLOWERS_TALL).add(Blocks.SUNFLOWER, Blocks.LILAC, Blocks.PEONY, Blocks.ROSE_BUSH, Blocks.PITCHER_PLANT).addOptionalTag(BlockTags.create(ResourceLocation.withDefaultNamespace("tall_flowers")));
        tag(Tags.Blocks.FLOWERS).add(Blocks.CHERRY_LEAVES, Blocks.FLOWERING_AZALEA_LEAVES, Blocks.FLOWERING_AZALEA, Blocks.MANGROVE_PROPAGULE, Blocks.PINK_PETALS, Blocks.WILDFLOWERS, Blocks.CHORUS_FLOWER, Blocks.SPORE_BLOSSOM, Blocks.CACTUS_FLOWER).addTags(Tags.Blocks.FLOWERS_SMALL, Tags.Blocks.FLOWERS_TALL).addOptionalTag(BlockTags.FLOWERS);
        tag(Tags.Blocks.GLASS_BLOCKS).addTags(Tags.Blocks.GLASS_BLOCKS_COLORLESS, Tags.Blocks.GLASS_BLOCKS_CHEAP, Tags.Blocks.GLASS_BLOCKS_TINTED);
        tag(Tags.Blocks.GLASS_BLOCKS_COLORLESS).add(Blocks.GLASS);
        tag(Tags.Blocks.GLASS_BLOCKS_CHEAP).add(Blocks.GLASS, Blocks.WHITE_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.PINK_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.RED_STAINED_GLASS, Blocks.BLACK_STAINED_GLASS);
        tag(Tags.Blocks.GLASS_BLOCKS_TINTED).add(Blocks.TINTED_GLASS);
        tag(Tags.Blocks.GLASS_PANES).addTags(Tags.Blocks.GLASS_PANES_COLORLESS).add(Blocks.WHITE_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS_PANE, Blocks.PINK_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS_PANE, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.RED_STAINED_GLASS_PANE, Blocks.BLACK_STAINED_GLASS_PANE);
        tag(Tags.Blocks.GLASS_PANES_COLORLESS).add(Blocks.GLASS_PANE);
        tag(Tags.Blocks.GLAZED_TERRACOTTAS).add(Blocks.WHITE_GLAZED_TERRACOTTA, Blocks.ORANGE_GLAZED_TERRACOTTA, Blocks.MAGENTA_GLAZED_TERRACOTTA, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA, Blocks.YELLOW_GLAZED_TERRACOTTA, Blocks.LIME_GLAZED_TERRACOTTA, Blocks.PINK_GLAZED_TERRACOTTA, Blocks.GRAY_GLAZED_TERRACOTTA, Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, Blocks.CYAN_GLAZED_TERRACOTTA, Blocks.PURPLE_GLAZED_TERRACOTTA, Blocks.BLUE_GLAZED_TERRACOTTA, Blocks.BROWN_GLAZED_TERRACOTTA, Blocks.GREEN_GLAZED_TERRACOTTA, Blocks.RED_GLAZED_TERRACOTTA, Blocks.BLACK_GLAZED_TERRACOTTA);
        tag(Tags.Blocks.GRAVELS).add(Blocks.GRAVEL);
        tag(Tags.Blocks.SKULLS).add(Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL, Blocks.PLAYER_HEAD, Blocks.PLAYER_WALL_HEAD, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD, Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD, Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD);
        tag(Tags.Blocks.HIDDEN_FROM_RECIPE_VIEWERS);
        tag(Tags.Blocks.NETHERRACKS).add(Blocks.NETHERRACK);
        tag(Tags.Blocks.OBSIDIANS_NORMAL).add(Blocks.OBSIDIAN);
        tag(Tags.Blocks.OBSIDIANS_CRYING).add(Blocks.CRYING_OBSIDIAN);
        tag(Tags.Blocks.OBSIDIANS).addTags(Tags.Blocks.OBSIDIANS_NORMAL, Tags.Blocks.OBSIDIANS_CRYING);
        tag(Tags.Blocks.ORE_BEARING_GROUND_DEEPSLATE).add(Blocks.DEEPSLATE);
        tag(Tags.Blocks.ORE_BEARING_GROUND_NETHERRACK).add(Blocks.NETHERRACK);
        tag(Tags.Blocks.ORE_BEARING_GROUND_STONE).add(Blocks.STONE);
        tag(Tags.Blocks.ORE_RATES_DENSE).add(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.REDSTONE_ORE);
        tag(Tags.Blocks.ORE_RATES_SINGULAR).add(Blocks.ANCIENT_DEBRIS, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.GOLD_ORE, Blocks.IRON_ORE, Blocks.NETHER_QUARTZ_ORE);
        tag(Tags.Blocks.ORE_RATES_SPARSE).add(Blocks.NETHER_GOLD_ORE);
        tag(Tags.Blocks.ORES).addTags(Tags.Blocks.ORES_COAL, Tags.Blocks.ORES_COPPER, Tags.Blocks.ORES_DIAMOND, Tags.Blocks.ORES_EMERALD, Tags.Blocks.ORES_GOLD, Tags.Blocks.ORES_IRON, Tags.Blocks.ORES_LAPIS, Tags.Blocks.ORES_NETHERITE_SCRAP, Tags.Blocks.ORES_REDSTONE, Tags.Blocks.ORES_QUARTZ);
        tag(Tags.Blocks.ORES_COAL).addTag(BlockTags.COAL_ORES);
        tag(Tags.Blocks.ORES_COPPER).addTag(BlockTags.COPPER_ORES);
        tag(Tags.Blocks.ORES_DIAMOND).addTag(BlockTags.DIAMOND_ORES);
        tag(Tags.Blocks.ORES_EMERALD).addTag(BlockTags.EMERALD_ORES);
        tag(Tags.Blocks.ORES_GOLD).addTag(BlockTags.GOLD_ORES);
        tag(Tags.Blocks.ORES_IRON).addTag(BlockTags.IRON_ORES);
        tag(Tags.Blocks.ORES_LAPIS).addTag(BlockTags.LAPIS_ORES);
        tag(Tags.Blocks.ORES_QUARTZ).add(Blocks.NETHER_QUARTZ_ORE);
        tag(Tags.Blocks.ORES_REDSTONE).addTag(BlockTags.REDSTONE_ORES);
        tag(Tags.Blocks.ORES_NETHERITE_SCRAP).add(Blocks.ANCIENT_DEBRIS);
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
        tag(Tags.Blocks.ORES_IN_GROUND_NETHERRACK).add(Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE);
        tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(Blocks.COAL_ORE, Blocks.COPPER_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.GOLD_ORE, Blocks.IRON_ORE, Blocks.LAPIS_ORE, Blocks.REDSTONE_ORE);
        tag(Tags.Blocks.PUMPKINS).addTags(Tags.Blocks.PUMPKINS_NORMAL, Tags.Blocks.PUMPKINS_CARVED, Tags.Blocks.PUMPKINS_JACK_O_LANTERNS);
        tag(Tags.Blocks.PUMPKINS_NORMAL).add(Blocks.PUMPKIN);
        tag(Tags.Blocks.PUMPKINS_CARVED).add(Blocks.CARVED_PUMPKIN);
        tag(Tags.Blocks.PUMPKINS_JACK_O_LANTERNS).add(Blocks.JACK_O_LANTERN);
        tag(Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES).add(Blocks.CRAFTING_TABLE);
        tag(Tags.Blocks.PLAYER_WORKSTATIONS_FURNACES).add(Blocks.FURNACE);
        tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED);
        tag(Tags.Blocks.ROPES);
        tag(Tags.Blocks.SANDS).addTags(Tags.Blocks.SANDS_COLORLESS, Tags.Blocks.SANDS_RED);
        tag(Tags.Blocks.SANDS_COLORLESS).add(Blocks.SAND);
        tag(Tags.Blocks.SANDS_RED).add(Blocks.RED_SAND);

        tag(Tags.Blocks.SANDSTONE_RED_BLOCKS).add(Blocks.RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE);
        tag(Tags.Blocks.SANDSTONE_UNCOLORED_BLOCKS).add(Blocks.SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.SMOOTH_SANDSTONE);
        tag(Tags.Blocks.SANDSTONE_BLOCKS).addTags(Tags.Blocks.SANDSTONE_RED_BLOCKS, Tags.Blocks.SANDSTONE_UNCOLORED_BLOCKS);
        tag(Tags.Blocks.SANDSTONE_RED_SLABS).add(Blocks.RED_SANDSTONE_SLAB, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE_SLAB);
        tag(Tags.Blocks.SANDSTONE_UNCOLORED_SLABS).add(Blocks.SANDSTONE_SLAB, Blocks.CUT_SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE_SLAB);
        tag(Tags.Blocks.SANDSTONE_SLABS).addTags(Tags.Blocks.SANDSTONE_RED_SLABS, Tags.Blocks.SANDSTONE_UNCOLORED_SLABS);
        tag(Tags.Blocks.SANDSTONE_RED_STAIRS).add(Blocks.RED_SANDSTONE_STAIRS, Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
        tag(Tags.Blocks.SANDSTONE_UNCOLORED_STAIRS).add(Blocks.SANDSTONE_STAIRS, Blocks.SMOOTH_SANDSTONE_STAIRS);
        tag(Tags.Blocks.SANDSTONE_STAIRS).addTags(Tags.Blocks.SANDSTONE_RED_STAIRS, Tags.Blocks.SANDSTONE_UNCOLORED_STAIRS);

        tag(Tags.Blocks.STONES).add(Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.STONE, Blocks.DEEPSLATE, Blocks.TUFF);
        tag(Tags.Blocks.STORAGE_BLOCKS).addTags(Tags.Blocks.STORAGE_BLOCKS_BONE_MEAL, Tags.Blocks.STORAGE_BLOCKS_COAL,
                Tags.Blocks.STORAGE_BLOCKS_COPPER, Tags.Blocks.STORAGE_BLOCKS_DIAMOND, Tags.Blocks.STORAGE_BLOCKS_DRIED_KELP,
                Tags.Blocks.STORAGE_BLOCKS_EMERALD, Tags.Blocks.STORAGE_BLOCKS_GOLD, Tags.Blocks.STORAGE_BLOCKS_IRON,
                Tags.Blocks.STORAGE_BLOCKS_LAPIS, Tags.Blocks.STORAGE_BLOCKS_NETHERITE, Tags.Blocks.STORAGE_BLOCKS_RAW_COPPER,
                Tags.Blocks.STORAGE_BLOCKS_RAW_GOLD, Tags.Blocks.STORAGE_BLOCKS_RAW_IRON, Tags.Blocks.STORAGE_BLOCKS_REDSTONE,
                Tags.Blocks.STORAGE_BLOCKS_RESIN, Tags.Blocks.STORAGE_BLOCKS_SLIME, Tags.Blocks.STORAGE_BLOCKS_WHEAT);
        tag(Tags.Blocks.STORAGE_BLOCKS_BONE_MEAL).add(Blocks.BONE_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_COAL).add(Blocks.COAL_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_COPPER).add(Blocks.COPPER_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_DIAMOND).add(Blocks.DIAMOND_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_DRIED_KELP).add(Blocks.DRIED_KELP_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_EMERALD).add(Blocks.EMERALD_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_GOLD).add(Blocks.GOLD_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_IRON).add(Blocks.IRON_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_LAPIS).add(Blocks.LAPIS_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_NETHERITE).add(Blocks.NETHERITE_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_RAW_COPPER).add(Blocks.RAW_COPPER_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_RAW_GOLD).add(Blocks.RAW_GOLD_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_RAW_IRON).add(Blocks.RAW_IRON_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_REDSTONE).add(Blocks.REDSTONE_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_RESIN).add(Blocks.RESIN_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_SLIME).add(Blocks.SLIME_BLOCK);
        tag(Tags.Blocks.STORAGE_BLOCKS_WHEAT).add(Blocks.HAY_BLOCK);
        tag(Tags.Blocks.STRIPPED_LOGS).add(
                Blocks.STRIPPED_ACACIA_LOG, Blocks.STRIPPED_BAMBOO_BLOCK, Blocks.STRIPPED_BIRCH_LOG,
                Blocks.STRIPPED_CHERRY_LOG, Blocks.STRIPPED_DARK_OAK_LOG, Blocks.STRIPPED_JUNGLE_LOG,
                Blocks.STRIPPED_MANGROVE_LOG, Blocks.STRIPPED_OAK_LOG, Blocks.STRIPPED_PALE_OAK_LOG,
                Blocks.STRIPPED_SPRUCE_LOG, Blocks.STRIPPED_CRIMSON_STEM, Blocks.STRIPPED_WARPED_STEM);
        tag(Tags.Blocks.STRIPPED_WOODS).add(
                Blocks.STRIPPED_ACACIA_WOOD, Blocks.STRIPPED_BIRCH_WOOD, Blocks.STRIPPED_CHERRY_WOOD,
                Blocks.STRIPPED_DARK_OAK_WOOD, Blocks.STRIPPED_JUNGLE_WOOD, Blocks.STRIPPED_MANGROVE_WOOD,
                Blocks.STRIPPED_OAK_WOOD, Blocks.STRIPPED_PALE_OAK_WOOD, Blocks.STRIPPED_SPRUCE_WOOD,
                Blocks.STRIPPED_CRIMSON_HYPHAE, Blocks.STRIPPED_WARPED_HYPHAE);
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
        tag(BlockTags.INCORRECT_FOR_IRON_TOOL).addTag(Tags.Blocks.NEEDS_NETHERITE_TOOL);
        tag(BlockTags.INCORRECT_FOR_GOLD_TOOL).addTag(Tags.Blocks.NEEDS_NETHERITE_TOOL);
        tag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL).addTag(Tags.Blocks.NEEDS_NETHERITE_TOOL);

        // Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
        // TODO: Remove backwards compat tag entries in 1.22
        tagWithOptionalLegacy(Tags.Blocks.BARRELS);
        tagWithOptionalLegacy(Tags.Blocks.BARRELS_WOODEN);
        tagWithOptionalLegacy(Tags.Blocks.BOOKSHELVES);
        tagWithOptionalLegacy(Tags.Blocks.CHESTS);
        tagWithOptionalLegacy(Tags.Blocks.CHESTS_ENDER);
        tagWithOptionalLegacy(Tags.Blocks.CHESTS_TRAPPED);
        tagWithOptionalLegacy(Tags.Blocks.CHESTS_WOODEN);
        tag(Tags.Blocks.COBBLESTONES).addOptionalTag(forge("cobblestone"));
        tag(Tags.Blocks.COBBLESTONES_NORMAL).addOptionalTag(forge("cobblestone/normal"));
        tag(Tags.Blocks.COBBLESTONES_INFESTED).addOptionalTag(forge("cobblestone/infested"));
        tag(Tags.Blocks.COBBLESTONES_MOSSY).addOptionalTag(forge("cobblestone/mossy"));
        tag(Tags.Blocks.COBBLESTONES_DEEPSLATE).addOptionalTag(forge("cobblestone/deepslate"));
        tag(Tags.Blocks.DYED_BLACK)
                .addOptionalTag(forge("glass/black"))
                .addOptionalTag(forge("stained_glass/black"));
        tag(Tags.Blocks.DYED_BLUE)
                .addOptionalTag(forge("glass/blue"))
                .addOptionalTag(forge("stained_glass/blue"));
        tag(Tags.Blocks.DYED_BROWN)
                .addOptionalTag(forge("glass/brown"))
                .addOptionalTag(forge("stained_glass/brown"));
        tag(Tags.Blocks.DYED_CYAN)
                .addOptionalTag(forge("glass/cyan"))
                .addOptionalTag(forge("stained_glass/cyan"));
        tag(Tags.Blocks.DYED_GRAY)
                .addOptionalTag(forge("glass/gray"))
                .addOptionalTag(forge("stained_glass/gray"));
        tag(Tags.Blocks.DYED_GREEN)
                .addOptionalTag(forge("glass/green"))
                .addOptionalTag(forge("stained_glass/green"));
        tag(Tags.Blocks.DYED_LIGHT_BLUE)
                .addOptionalTag(forge("glass/light_blue"))
                .addOptionalTag(forge("stained_glass/light_blue"));
        tag(Tags.Blocks.DYED_LIGHT_GRAY)
                .addOptionalTag(forge("glass/light_gray"))
                .addOptionalTag(forge("stained_glass/light_gray"));
        tag(Tags.Blocks.DYED_LIME)
                .addOptionalTag(forge("glass/lime"))
                .addOptionalTag(forge("stained_glass/lime"));
        tag(Tags.Blocks.DYED_MAGENTA)
                .addOptionalTag(forge("glass/magenta"))
                .addOptionalTag(forge("stained_glass/magenta"));
        tag(Tags.Blocks.DYED_MAGENTA)
                .addOptionalTag(forge("glass/magenta"))
                .addOptionalTag(forge("stained_glass/magenta"));
        tag(Tags.Blocks.DYED_ORANGE)
                .addOptionalTag(forge("glass/orange"))
                .addOptionalTag(forge("stained_glass/orange"));
        tag(Tags.Blocks.DYED_PINK)
                .addOptionalTag(forge("glass/pink"))
                .addOptionalTag(forge("stained_glass/pink"));
        tag(Tags.Blocks.DYED_PURPLE)
                .addOptionalTag(forge("glass/purple"))
                .addOptionalTag(forge("stained_glass/purple"));
        tag(Tags.Blocks.DYED_RED)
                .addOptionalTag(forge("glass/red"))
                .addOptionalTag(forge("stained_glass/red"));
        tag(Tags.Blocks.DYED_WHITE)
                .addOptionalTag(forge("glass/white"))
                .addOptionalTag(forge("stained_glass/white"));
        tag(Tags.Blocks.DYED_YELLOW)
                .addOptionalTag(forge("glass/yellow"))
                .addOptionalTag(forge("stained_glass/yellow"));
        tagWithOptionalLegacy(Tags.Blocks.END_STONES);
        tagWithOptionalLegacy(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST);
        tagWithOptionalLegacy(Tags.Blocks.FENCE_GATES);
        tagWithOptionalLegacy(Tags.Blocks.FENCE_GATES_WOODEN);
        tagWithOptionalLegacy(Tags.Blocks.FENCES);
        tagWithOptionalLegacy(Tags.Blocks.FENCES_NETHER_BRICK);
        tagWithOptionalLegacy(Tags.Blocks.FENCES_WOODEN);
        tag(Tags.Blocks.GRAVELS).addOptionalTag(forge("gravel"));
        tag(Tags.Blocks.GLASS_BLOCKS).addOptionalTag(forge("glass"));
        tag(Tags.Blocks.GLASS_BLOCKS_COLORLESS).addOptionalTag(forge("glass_colorless"));
        tag(Tags.Blocks.GLASS_BLOCKS_CHEAP).addOptionalTag(forge("glass_silica"));
        tag(Tags.Blocks.GLASS_BLOCKS_TINTED).addOptionalTag(forge("glass_tinted"));
        tag(Tags.Blocks.GLASS_PANES_COLORLESS).addOptionalTag(forge("glass_panes_colorless"));
        tag(Tags.Blocks.NETHERRACKS).addOptionalTag(forge("netherrack"));
        tag(Tags.Blocks.OBSIDIANS).addOptionalTag(forge("obsidian"));
        tagWithOptionalLegacy(Tags.Blocks.ORE_BEARING_GROUND_DEEPSLATE);
        tagWithOptionalLegacy(Tags.Blocks.ORE_BEARING_GROUND_NETHERRACK);
        tagWithOptionalLegacy(Tags.Blocks.ORE_BEARING_GROUND_STONE);
        tagWithOptionalLegacy(Tags.Blocks.ORE_RATES_DENSE);
        tagWithOptionalLegacy(Tags.Blocks.ORE_RATES_SINGULAR);
        tagWithOptionalLegacy(Tags.Blocks.ORE_RATES_SPARSE);
        tagWithOptionalLegacy(Tags.Blocks.ORES);
        tagWithOptionalLegacy(Tags.Blocks.ORES_COAL);
        tagWithOptionalLegacy(Tags.Blocks.ORES_COPPER);
        tagWithOptionalLegacy(Tags.Blocks.ORES_DIAMOND);
        tagWithOptionalLegacy(Tags.Blocks.ORES_EMERALD);
        tagWithOptionalLegacy(Tags.Blocks.ORES_GOLD);
        tagWithOptionalLegacy(Tags.Blocks.ORES_IRON);
        tagWithOptionalLegacy(Tags.Blocks.ORES_LAPIS);
        tagWithOptionalLegacy(Tags.Blocks.ORES_QUARTZ);
        tagWithOptionalLegacy(Tags.Blocks.ORES_REDSTONE);
        tagWithOptionalLegacy(Tags.Blocks.ORES_NETHERITE_SCRAP);
        tagWithOptionalLegacy(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE);
        tagWithOptionalLegacy(Tags.Blocks.ORES_IN_GROUND_NETHERRACK);
        tagWithOptionalLegacy(Tags.Blocks.ORES_IN_GROUND_STONE);
        tag(Tags.Blocks.STONES).addOptionalTag(forge("stone"));
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_COAL);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_COPPER);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_DIAMOND);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_EMERALD);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_GOLD);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_IRON);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_LAPIS);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_RAW_COPPER);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_RAW_GOLD);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_RAW_IRON);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_REDSTONE);
        tagWithOptionalLegacy(Tags.Blocks.STORAGE_BLOCKS_NETHERITE);
        tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED)
                .addOptionalTag(forge("relocation_not_supported"))
                .addOptionalTag(forge("immovable"));
        tag(Tags.Blocks.SANDSTONE_BLOCKS).addOptionalTag(forge("sandstone"));
        tag(Tags.Blocks.SANDS).addOptionalTag(forge("sand"));
        tag(Tags.Blocks.SANDS_COLORLESS).addOptionalTag(forge("sand/colorless"));
        tag(Tags.Blocks.SANDS_RED).addOptionalTag(forge("sand/red"));
    }

    private TagAppender<Block, Block> tagWithOptionalLegacy(TagKey<Block> tag) {
        TagAppender<Block, Block> tagAppender = tag(tag);
        tagAppender.addOptionalTag(forge(tag.location().getPath()));
        return tagAppender;
    }

    private void addColored(TagKey<Block> group, String pattern) {
        String prefix = group.location().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color : DyeColor.values()) {
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath("minecraft", pattern.replace("{color}", color.getName()));
            TagKey<Block> tag = getForgeTag(prefix + color.getName());
            Block block = BuiltInRegistries.BLOCK.getValue(key);
            if (block == null || block == Blocks.AIR)
                throw new IllegalStateException("Unknown vanilla block: " + key);
            tag(tag).add(block);
        }
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

    private TagKey<Block> forge(String id) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", id));
    }
}
