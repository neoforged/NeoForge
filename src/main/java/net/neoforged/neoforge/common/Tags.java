/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.commands.TimeCommand;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColorCollection;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;

public class Tags {
    public static class Blocks {
        // `neoforge` tags for functional behavior provided by NeoForge
        /**
         * Controls what blocks Endermen cannot place blocks onto.
         * <p>
         * This is patched into the following method: {@link net.minecraft.world.entity.monster.EnderMan.EndermanLeaveBlockGoal#canPlaceBlock(Level, BlockPos, BlockState, BlockState, BlockState, BlockPos)}
         */
        public static final TagKey<Block> ENDERMAN_PLACE_ON_BLACKLIST = neoforgeTag("enderman_place_on_blacklist");

        /**
         * For denoting blocks that need tools that are Wood or higher to mine.
         * By default, this is not added to any Minecraft tag since Wood is in the lowest "tier".
         */
        public static final TagKey<Block> NEEDS_WOOD_TOOL = neoforgeTag("needs_wood_tool");

        /**
         * For denoting blocks that need tools that are Gold or higher to mine.
         * By default, this is not added to any Minecraft tag since Gold is in the lowest "tier".
         */
        public static final TagKey<Block> NEEDS_GOLD_TOOL = neoforgeTag("needs_gold_tool");

        /**
         * For denoting blocks that need tools that are Netherite or higher to mine.
         * Blocks in this tag gets added to the following Minecraft tags:
         * {@link BlockTags#INCORRECT_FOR_WOODEN_TOOL}
         * {@link BlockTags#INCORRECT_FOR_STONE_TOOL}
         * {@link BlockTags#INCORRECT_FOR_IRON_TOOL}
         * {@link BlockTags#INCORRECT_FOR_GOLD_TOOL}
         * {@link BlockTags#INCORRECT_FOR_DIAMOND_TOOL}
         */
        public static final TagKey<Block> NEEDS_NETHERITE_TOOL = neoforgeTag("needs_netherite_tool");

        // `c` tags for common conventions
        /**
         * Tag that holds all blocks that recipe viewers should not show to users.
         * Recipe viewers may use this to automatically find the corresponding BlockItem to hide.
         */
        public static final TagKey<Block> HIDDEN_FROM_RECIPE_VIEWERS = tag("hidden_from_recipe_viewers");
        /**
         * Blocks should be included in this tag if their movement/relocation can cause serious issues such
         * as world corruption upon being moved or for balance reason where the block should not be able to be relocated.
         * Example: Chunk loaders or pipes where other mods that move blocks do not respect
         * {@link BlockBehaviour.BlockStateBase#getPistonPushReaction}.
         */
        public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = tag("relocation_not_supported");
        /**
         * Tag that holds all head based blocks such as Skeleton Skull or Player Head. (Named skulls to match minecraft:skulls item tag)
         */
        public static final TagKey<Block> SKULLS = tag("skulls");
        public static final TagKey<Block> VILLAGER_JOB_SITES = tag("villager_job_sites");

        /**
         * Blocks tagged here will be tracked by Farmer Villagers who will attempt to plant crops on top.
         */
        public static final TagKey<Block> VILLAGER_FARMLANDS = neoforgeTag("villager_farmlands");

        // These fields are deprecated for removal in 26.4.
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BARRELS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> BARRELS = BlockItems.BARRELS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BARRELS_WOODEN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> BARRELS_WOODEN = BlockItems.BARRELS_WOODEN.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BARS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> BARS = BlockItems.BARS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BARS_COPPER] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> BARS_COPPER = BlockItems.BARS_COPPER.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BARS_IRON] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> BARS_IRON = BlockItems.BARS_IRON.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BOOKSHELVES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> BOOKSHELVES = BlockItems.BOOKSHELVES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BUDDING_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> BUDDING_BLOCKS = BlockItems.BUDDING_BLOCKS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BUDS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> BUDS = BlockItems.BUDS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CHAINS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> CHAINS = BlockItems.CHAINS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CHESTS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> CHESTS = BlockItems.CHESTS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CHESTS_ENDER] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> CHESTS_ENDER = BlockItems.CHESTS_ENDER.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CHESTS_TRAPPED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> CHESTS_TRAPPED = BlockItems.CHESTS_TRAPPED.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CHESTS_WOODEN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> CHESTS_WOODEN = BlockItems.CHESTS_WOODEN.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CLUSTERS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> CLUSTERS = BlockItems.CLUSTERS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#COBBLESTONES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> COBBLESTONES = BlockItems.COBBLESTONES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#COBBLESTONES_NORMAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> COBBLESTONES_NORMAL = BlockItems.COBBLESTONES_NORMAL.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#COBBLESTONES_INFESTED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> COBBLESTONES_INFESTED = BlockItems.COBBLESTONES_INFESTED.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#COBBLESTONES_MOSSY] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> COBBLESTONES_MOSSY = BlockItems.COBBLESTONES_MOSSY.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#COBBLESTONES_DEEPSLATE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> COBBLESTONES_DEEPSLATE = BlockItems.COBBLESTONES_DEEPSLATE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CONCRETES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> CONCRETES = BlockItems.CONCRETES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED = BlockItems.DYED.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_BLACK] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_BLACK = BlockItems.DYED_BLACK.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_BLUE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_BLUE = BlockItems.DYED_BLUE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_BROWN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_BROWN = BlockItems.DYED_BROWN.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_CYAN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_CYAN = BlockItems.DYED_CYAN.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_GRAY] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_GRAY = BlockItems.DYED_GRAY.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_GREEN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_GREEN = BlockItems.DYED_GREEN.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_LIGHT_BLUE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_LIGHT_BLUE = BlockItems.DYED_LIGHT_BLUE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_LIGHT_GRAY] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_LIGHT_GRAY = BlockItems.DYED_LIGHT_GRAY.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_LIME] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_LIME = BlockItems.DYED_LIME.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_MAGENTA] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_MAGENTA = BlockItems.DYED_MAGENTA.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_ORANGE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_ORANGE = BlockItems.DYED_ORANGE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_PINK] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_PINK = BlockItems.DYED_PINK.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_PURPLE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_PURPLE = BlockItems.DYED_PURPLE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_RED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_RED = BlockItems.DYED_RED.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_WHITE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_WHITE = BlockItems.DYED_WHITE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_YELLOW] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYED_YELLOW = BlockItems.DYED_YELLOW.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_COLORS] mapped to the block using [BlockItemTagId#block()] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final ColorCollection<TagKey<Block>> DYED_COLORS = BlockItems.DYED_COLORS.map(BlockItemTagId::block);
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYEABLE_UNDYED_SIMPLE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYEABLE_UNDYED_SIMPLE = BlockItems.DYEABLE_UNDYED_SIMPLE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYEABLE_REDYEABLE_SIMPLE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYEABLE_REDYEABLE_SIMPLE = BlockItems.DYEABLE_REDYEABLE_SIMPLE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYEABLE_SIMPLE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYEABLE_SIMPLE = BlockItems.DYEABLE_SIMPLE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYEABLE_DYNAMIC] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYEABLE_DYNAMIC = BlockItems.DYEABLE_DYNAMIC.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYEABLE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> DYEABLE = BlockItems.DYEABLE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#END_STONES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> END_STONES = BlockItems.END_STONES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FENCE_GATES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> FENCE_GATES = BlockItems.FENCE_GATES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FENCE_GATES_WOODEN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> FENCE_GATES_WOODEN = BlockItems.FENCE_GATES_WOODEN.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FENCES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> FENCES = BlockItems.FENCES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FENCES_NETHER_BRICK] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> FENCES_NETHER_BRICK = BlockItems.FENCES_NETHER_BRICK.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FENCES_WOODEN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> FENCES_WOODEN = BlockItems.FENCES_WOODEN.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FLOWERS_SMALL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> FLOWERS_SMALL = BlockItems.FLOWERS_SMALL.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FLOWERS_TALL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> FLOWERS_TALL = BlockItems.FLOWERS_TALL.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FLOWERS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> FLOWERS = BlockItems.FLOWERS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FROGLIGHTS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> FROGLIGHTS = BlockItems.FROGLIGHTS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> GLASS_BLOCKS = BlockItems.GLASS_BLOCKS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_BLOCKS_COLORLESS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> GLASS_BLOCKS_COLORLESS = BlockItems.GLASS_BLOCKS_COLORLESS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_BLOCKS_CHEAP] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> GLASS_BLOCKS_CHEAP = BlockItems.GLASS_BLOCKS_CHEAP.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_BLOCKS_TINTED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> GLASS_BLOCKS_TINTED = BlockItems.GLASS_BLOCKS_TINTED.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_PANES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> GLASS_PANES = BlockItems.GLASS_PANES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_PANES_COLORLESS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> GLASS_PANES_COLORLESS = BlockItems.GLASS_PANES_COLORLESS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLAZED_TERRACOTTAS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> GLAZED_TERRACOTTAS = BlockItems.GLAZED_TERRACOTTAS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GRAVELS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> GRAVELS = BlockItems.GRAVELS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#NETHERRACKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> NETHERRACKS = BlockItems.NETHERRACKS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#OBSIDIANS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> OBSIDIANS = BlockItems.OBSIDIANS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#OBSIDIANS_NORMAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> OBSIDIANS_NORMAL = BlockItems.OBSIDIANS_NORMAL.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#OBSIDIANS_CRYING] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> OBSIDIANS_CRYING = BlockItems.OBSIDIANS_CRYING.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_BEARING_GROUND_DEEPSLATE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORE_BEARING_GROUND_DEEPSLATE = BlockItems.ORE_BEARING_GROUND_DEEPSLATE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_BEARING_GROUND_NETHERRACK] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORE_BEARING_GROUND_NETHERRACK = BlockItems.ORE_BEARING_GROUND_NETHERRACK.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_BEARING_GROUND_STONE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORE_BEARING_GROUND_STONE = BlockItems.ORE_BEARING_GROUND_STONE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_RATES_DENSE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORE_RATES_DENSE = BlockItems.ORE_RATES_DENSE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_RATES_SINGULAR] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORE_RATES_SINGULAR = BlockItems.ORE_RATES_SINGULAR.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_RATES_SPARSE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORE_RATES_SPARSE = BlockItems.ORE_RATES_SPARSE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES = BlockItems.ORES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_COAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_COAL = BlockItems.ORES_COAL.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_COPPER] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_COPPER = BlockItems.ORES_COPPER.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_DIAMOND] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_DIAMOND = BlockItems.ORES_DIAMOND.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_EMERALD] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_EMERALD = BlockItems.ORES_EMERALD.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_GOLD] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_GOLD = BlockItems.ORES_GOLD.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_IRON] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_IRON = BlockItems.ORES_IRON.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_LAPIS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_LAPIS = BlockItems.ORES_LAPIS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_NETHERITE_SCRAP] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_NETHERITE_SCRAP = BlockItems.ORES_NETHERITE_SCRAP.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_QUARTZ] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_QUARTZ = BlockItems.ORES_QUARTZ.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_REDSTONE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_REDSTONE = BlockItems.ORES_REDSTONE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_IN_GROUND_DEEPSLATE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_IN_GROUND_DEEPSLATE = BlockItems.ORES_IN_GROUND_DEEPSLATE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_IN_GROUND_NETHERRACK] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_IN_GROUND_NETHERRACK = BlockItems.ORES_IN_GROUND_NETHERRACK.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_IN_GROUND_STONE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ORES_IN_GROUND_STONE = BlockItems.ORES_IN_GROUND_STONE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PUMPKINS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> PUMPKINS = BlockItems.PUMPKINS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PUMPKINS_NORMAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> PUMPKINS_NORMAL = BlockItems.PUMPKINS_NORMAL.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PUMPKINS_CARVED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> PUMPKINS_CARVED = BlockItems.PUMPKINS_CARVED.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PUMPKINS_JACK_O_LANTERNS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> PUMPKINS_JACK_O_LANTERNS = BlockItems.PUMPKINS_JACK_O_LANTERNS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PLAYER_WORKSTATIONS_CRAFTING_TABLES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> PLAYER_WORKSTATIONS_CRAFTING_TABLES = BlockItems.PLAYER_WORKSTATIONS_CRAFTING_TABLES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PLAYER_WORKSTATIONS_FURNACES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> PLAYER_WORKSTATIONS_FURNACES = BlockItems.PLAYER_WORKSTATIONS_FURNACES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ROPES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> ROPES = BlockItems.ROPES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDS = BlockItems.SANDS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDS_COLORLESS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDS_COLORLESS = BlockItems.SANDS_COLORLESS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDS_RED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDS_RED = BlockItems.SANDS_RED.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDSTONE_BLOCKS = BlockItems.SANDSTONE_BLOCKS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_SLABS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDSTONE_SLABS = BlockItems.SANDSTONE_SLABS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_STAIRS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDSTONE_STAIRS = BlockItems.SANDSTONE_STAIRS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_RED_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDSTONE_RED_BLOCKS = BlockItems.SANDSTONE_RED_BLOCKS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_RED_SLABS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDSTONE_RED_SLABS = BlockItems.SANDSTONE_RED_SLABS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_RED_STAIRS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDSTONE_RED_STAIRS = BlockItems.SANDSTONE_RED_STAIRS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_UNCOLORED_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDSTONE_UNCOLORED_BLOCKS = BlockItems.SANDSTONE_UNCOLORED_BLOCKS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_UNCOLORED_SLABS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDSTONE_UNCOLORED_SLABS = BlockItems.SANDSTONE_UNCOLORED_SLABS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_UNCOLORED_STAIRS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> SANDSTONE_UNCOLORED_STAIRS = BlockItems.SANDSTONE_UNCOLORED_STAIRS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STONES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STONES = BlockItems.STONES.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS = BlockItems.STORAGE_BLOCKS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_BONE_MEAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_BONE_MEAL = BlockItems.STORAGE_BLOCKS_BONE_MEAL.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_COAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_COAL = BlockItems.STORAGE_BLOCKS_COAL.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_COPPER] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_COPPER = BlockItems.STORAGE_BLOCKS_COPPER.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_DIAMOND] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_DIAMOND = BlockItems.STORAGE_BLOCKS_DIAMOND.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_DRIED_KELP] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_DRIED_KELP = BlockItems.STORAGE_BLOCKS_DRIED_KELP.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_EMERALD] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_EMERALD = BlockItems.STORAGE_BLOCKS_EMERALD.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_GOLD] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_GOLD = BlockItems.STORAGE_BLOCKS_GOLD.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_IRON] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_IRON = BlockItems.STORAGE_BLOCKS_IRON.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_LAPIS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_LAPIS = BlockItems.STORAGE_BLOCKS_LAPIS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_NETHERITE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_NETHERITE = BlockItems.STORAGE_BLOCKS_NETHERITE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_RAW_COPPER] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_COPPER = BlockItems.STORAGE_BLOCKS_RAW_COPPER.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_RAW_GOLD] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_GOLD = BlockItems.STORAGE_BLOCKS_RAW_GOLD.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_RAW_IRON] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_IRON = BlockItems.STORAGE_BLOCKS_RAW_IRON.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_REDSTONE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_REDSTONE = BlockItems.STORAGE_BLOCKS_REDSTONE.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_RESIN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_RESIN = BlockItems.STORAGE_BLOCKS_RESIN.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_SLIME] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_SLIME = BlockItems.STORAGE_BLOCKS_SLIME.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_WHEAT] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STORAGE_BLOCKS_WHEAT = BlockItems.STORAGE_BLOCKS_WHEAT.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#OVERWORLD_NATURAL_LOGS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> OVERWORLD_NATURAL_LOGS = BlockItems.OVERWORLD_NATURAL_LOGS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#NETHER_NATURAL_LOGS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> NETHER_NATURAL_LOGS = BlockItems.NETHER_NATURAL_LOGS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#NATURAL_LOGS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> NATURAL_LOGS = BlockItems.NATURAL_LOGS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#NATURAL_WOODS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> NATURAL_WOODS = BlockItems.NATURAL_WOODS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STRIPPED_LOGS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STRIPPED_LOGS = BlockItems.STRIPPED_LOGS.block();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STRIPPED_WOODS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Block> STRIPPED_WOODS = BlockItems.STRIPPED_WOODS.block();

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(Identifier.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Block> neoforgeTag(String name) {
            return BlockTags.create(Identifier.fromNamespaceAndPath("neoforge", name));
        }
    }

    /// Tags which exist for both [Block]s and [Item]s.
    public static class BlockItems {
        public static final BlockItemTagId BARRELS = tag("barrels");
        public static final BlockItemTagId BARRELS_WOODEN = tag("barrels/wooden");
        /// Equivalent to the [`minecraft:bars` tag][BlockItemTags#BARS].
        public static final BlockItemTagId BARS = tag("bars");
        public static final BlockItemTagId BARS_COPPER = tag("bars/copper");
        public static final BlockItemTagId BARS_IRON = tag("bars/iron");
        public static final BlockItemTagId BOOKSHELVES = tag("bookshelves");
        /// Budding blocks where buds and clusters are produced from (similar to amethyst).
        /// 
        /// @see #BUDS
        /// @see #CLUSTERS
        public static final BlockItemTagId BUDDING_BLOCKS = tag("budding_blocks");
        /// Bud blocks formed from budding blocks (similar to amethyst).
        /// 
        /// @see #BUDDING_BLOCKS
        public static final BlockItemTagId BUDS = tag("buds");
        public static final BlockItemTagId CHAINS = tag("chains");
        public static final BlockItemTagId CHESTS = tag("chests");
        public static final BlockItemTagId CHESTS_ENDER = tag("chests/ender");
        public static final BlockItemTagId CHESTS_TRAPPED = tag("chests/trapped");
        public static final BlockItemTagId CHESTS_WOODEN = tag("chests/wooden");
        /// Cluster blocks forming from budding blocks (similar to amethyst).
        /// 
        /// @see #BUDDING_BLOCKS
        public static final BlockItemTagId CLUSTERS = tag("clusters");
        public static final BlockItemTagId COBBLESTONES = tag("cobblestones");
        public static final BlockItemTagId COBBLESTONES_NORMAL = tag("cobblestones/normal");
        public static final BlockItemTagId COBBLESTONES_INFESTED = tag("cobblestones/infested");
        public static final BlockItemTagId COBBLESTONES_MOSSY = tag("cobblestones/mossy");
        public static final BlockItemTagId COBBLESTONES_DEEPSLATE = tag("cobblestones/deepslate");
        public static final BlockItemTagId CONCRETES = tag("concretes");

        /// Holds all blocks and items which are dyed a specific color.
        ///
        /// This does not include color blending blocks and items, such as leather armor; use [DYEABLE] instead.
        ///
        /// Note: Use custom ingredients in recipes to do tag intersections and/or tag exclusions to make more powerful
        /// recipes utilizing multiple tags such as dyed tags for an ingredient, such as [DifferenceIngredient] and [CompoundIngredient].
        public static final BlockItemTagId DYED = tag("dyed");
        public static final BlockItemTagId DYED_BLACK = tag("dyed/black");
        public static final BlockItemTagId DYED_BLUE = tag("dyed/blue");
        public static final BlockItemTagId DYED_BROWN = tag("dyed/brown");
        public static final BlockItemTagId DYED_CYAN = tag("dyed/cyan");
        public static final BlockItemTagId DYED_GRAY = tag("dyed/gray");
        public static final BlockItemTagId DYED_GREEN = tag("dyed/green");
        public static final BlockItemTagId DYED_LIGHT_BLUE = tag("dyed/light_blue");
        public static final BlockItemTagId DYED_LIGHT_GRAY = tag("dyed/light_gray");
        public static final BlockItemTagId DYED_LIME = tag("dyed/lime");
        public static final BlockItemTagId DYED_MAGENTA = tag("dyed/magenta");
        public static final BlockItemTagId DYED_ORANGE = tag("dyed/orange");
        public static final BlockItemTagId DYED_PINK = tag("dyed/pink");
        public static final BlockItemTagId DYED_PURPLE = tag("dyed/purple");
        public static final BlockItemTagId DYED_RED = tag("dyed/red");
        public static final BlockItemTagId DYED_WHITE = tag("dyed/white");
        public static final BlockItemTagId DYED_YELLOW = tag("dyed/yellow");

        /// A color collection of the dyed color tags.
        public static final ColorCollection<BlockItemTagId> DYED_COLORS = new ColorCollection<>(
                DYED_WHITE,
                DYED_ORANGE,
                DYED_MAGENTA,
                DYED_LIGHT_BLUE,
                DYED_YELLOW,
                DYED_LIME,
                DYED_PINK,
                DYED_GRAY,
                DYED_LIGHT_GRAY,
                DYED_CYAN,
                DYED_PURPLE,
                DYED_BLUE,
                DYED_BROWN,
                DYED_GREEN,
                DYED_RED,
                DYED_BLACK);

        /// Holds blocks and items which can be dyed but do not have their own color already, such as glass.
        /// This does not include color blending blocks and items, such as leather armor.
        public static final BlockItemTagId DYEABLE_UNDYED_SIMPLE = tag("dyeable/simple/undyed");

        /// Holds blocks and items which can be dyed despite already having a color, such as wool.
        /// This does not include color blending blocks and items, such as leather armor.
        public static final BlockItemTagId DYEABLE_REDYEABLE_SIMPLE = tag("dyeable/simple/redyeable");

        /// Holds blocks and items which can be dyed in a simple fashion without color blending, typically in the standard
        /// [16 colors][DyeColor], whether they have a color already or not.
        public static final BlockItemTagId DYEABLE_SIMPLE = tag("dyeable/simple");
        /// Holds blocks and items which can be dyed in a dynamic color blending fashion, such as leather armor.
        public static final BlockItemTagId DYEABLE_DYNAMIC = tag("dyeable/dynamic");

        /// Holds blocks and items which can have dye applied to them, whether they have a color already or not.
        public static final BlockItemTagId DYEABLE = tag("dyeable");

        public static final BlockItemTagId END_STONES = tag("end_stones");
        public static final BlockItemTagId FENCE_GATES = tag("fence_gates");
        public static final BlockItemTagId FENCE_GATES_WOODEN = tag("fence_gates/wooden");
        public static final BlockItemTagId FENCES = tag("fences");
        public static final BlockItemTagId FENCES_NETHER_BRICK = tag("fences/nether_brick");
        public static final BlockItemTagId FENCES_WOODEN = tag("fences/wooden");
        /// Living ground-based flowers that are one block tall, such as dandelions or poppies.
        /// Equivalent to the [`minecraft:small_flowers` tag][BlockItemTags#SMALL_FLOWERS].
        public static final BlockItemTagId FLOWERS_SMALL = tag("flowers/small");
        /// Living ground-based flowers that are two blocks tall, such as rose bushes or peonies.
        /// Equivalent to the `minecraft:tall_flowers` tag that used to exist in previous Minecraft versions.
        public static final BlockItemTagId FLOWERS_TALL = tag("flowers/tall");
        /// Living plants that are or contains flowers.
        /// Equivalent to the [`minecraft:flowers` tag][BlockItemTags#FLOWERS].
        public static final BlockItemTagId FLOWERS = tag("flowers");
        /// Light-emitting blocks created when a Frog eats a Magma Cube.
        public static final BlockItemTagId FROGLIGHTS = tag("froglights");

        public static final BlockItemTagId GLASS_BLOCKS = tag("glass_blocks");
        public static final BlockItemTagId GLASS_BLOCKS_COLORLESS = tag("glass_blocks/colorless");
        /// Glass made from cheap resources like sand and only minor additional ingredients like dyes.
        public static final BlockItemTagId GLASS_BLOCKS_CHEAP = tag("glass_blocks/cheap");
        public static final BlockItemTagId GLASS_BLOCKS_TINTED = tag("glass_blocks/tinted");

        public static final BlockItemTagId GLASS_PANES = tag("glass_panes");
        public static final BlockItemTagId GLASS_PANES_COLORLESS = tag("glass_panes/colorless");
        public static final BlockItemTagId GLAZED_TERRACOTTAS = tag("glazed_terracottas");

        public static final BlockItemTagId GRAVELS = tag("gravels");
        /// Solid/full blocks of ice.
        public static final BlockItemTagId ICE = tag("ice");
        public static final BlockItemTagId NETHERRACKS = tag("netherracks");
        public static final BlockItemTagId OBSIDIANS = tag("obsidians");
        /// Common obsidian that has no special quirks or behaviors, which is ideal for recipe use.
        ///
        /// Crying Obsidian, for example, is a light block and harder to obtain, so it is under [#OBSIDIANS_CRYING] rather than this tag.
        public static final BlockItemTagId OBSIDIANS_NORMAL = tag("obsidians/normal");
        public static final BlockItemTagId OBSIDIANS_CRYING = tag("obsidians/crying");
        /// Blocks often replaced by [deepslate ores][#ORES_IN_GROUND_DEEPSLATE] during world generation.
        public static final BlockItemTagId ORE_BEARING_GROUND_DEEPSLATE = tag("ore_bearing_ground/deepslate");
        /// Blocks often replaced by [netherrack ores][#ORES_IN_GROUND_NETHERRACK] during world generation.
        public static final BlockItemTagId ORE_BEARING_GROUND_NETHERRACK = tag("ore_bearing_ground/netherrack");
        /// Blocks often replaced by [stone ores][#ORES_IN_GROUND_STONE] during world generation.
        public static final BlockItemTagId ORE_BEARING_GROUND_STONE = tag("ore_bearing_ground/stone");
        /// Ores which on average result in more than one resource's worth of materials (ignoring fortune and other modifiers).
        /// For example, copper ore may drop multiple raw copper items, each convertible to one copper ingot.
        public static final BlockItemTagId ORE_RATES_DENSE = tag("ore_rates/dense");
        /// Ores which on average result in one resource's worth of materials (ignoring fortune and other modifiers), such as iron ore.
        /// For example, iron ore drops one raw iron item, which is convertible to one iron ingot.
        public static final BlockItemTagId ORE_RATES_SINGULAR = tag("ore_rates/singular");
        /// Ores which on average result in less than one resource's worth of materials (ignoring fortune and other modifiers).
        /// For example, nether gold ore drops 2-6 gold nuggets, but it takes 9 gold nuggets to convert to one gold ingot (equivalent to one raw gold).
        public static final BlockItemTagId ORE_RATES_SPARSE = tag("ore_rates/sparse");
        public static final BlockItemTagId ORES = tag("ores");
        public static final BlockItemTagId ORES_COAL = tag("ores/coal");
        public static final BlockItemTagId ORES_COPPER = tag("ores/copper");
        public static final BlockItemTagId ORES_DIAMOND = tag("ores/diamond");
        public static final BlockItemTagId ORES_EMERALD = tag("ores/emerald");
        public static final BlockItemTagId ORES_GOLD = tag("ores/gold");
        public static final BlockItemTagId ORES_IRON = tag("ores/iron");
        public static final BlockItemTagId ORES_LAPIS = tag("ores/lapis");
        public static final BlockItemTagId ORES_NETHERITE_SCRAP = tag("ores/netherite_scrap");
        public static final BlockItemTagId ORES_QUARTZ = tag("ores/quartz");
        public static final BlockItemTagId ORES_REDSTONE = tag("ores/redstone");
        /// Ores in deepslate (or equivalent blocks in [#ORE_BEARING_GROUND_DEEPSLATE]) which could logically use that as recipe input or output.
        public static final BlockItemTagId ORES_IN_GROUND_DEEPSLATE = tag("ores_in_ground/deepslate");
        /// Ores in netherrack (or equivalent blocks in [#ORE_BEARING_GROUND_NETHERRACK]) which could logically use that as recipe input or output.
        public static final BlockItemTagId ORES_IN_GROUND_NETHERRACK = tag("ores_in_ground/netherrack");
        /// Ores in stone (or equivalent blocks in [#ORE_BEARING_GROUND_STONE]) which could logically use that as recipe input or output.
        public static final BlockItemTagId ORES_IN_GROUND_STONE = tag("ores_in_ground/stone");
        public static final BlockItemTagId PUMPKINS = tag("pumpkins");
        /// Non-carved pumpkins.
        public static final BlockItemTagId PUMPKINS_NORMAL = tag("pumpkins/normal");
        /// Carved pumpkins which are not light sources.
        public static final BlockItemTagId PUMPKINS_CARVED = tag("pumpkins/carved");
        /// Carved pumpkins which are also light sources.
        public static final BlockItemTagId PUMPKINS_JACK_O_LANTERNS = tag("pumpkins/jack_o_lanterns");
        public static final BlockItemTagId PLAYER_WORKSTATIONS_CRAFTING_TABLES = tag("player_workstations/crafting_tables");
        public static final BlockItemTagId PLAYER_WORKSTATIONS_FURNACES = tag("player_workstations/furnaces");
        public static final BlockItemTagId ROPES = tag("ropes");

        public static final BlockItemTagId SANDS = tag("sands");
        public static final BlockItemTagId SANDS_COLORLESS = tag("sands/colorless");
        public static final BlockItemTagId SANDS_RED = tag("sands/red");

        public static final BlockItemTagId SANDSTONE_BLOCKS = tag("sandstone/blocks");
        public static final BlockItemTagId SANDSTONE_SLABS = tag("sandstone/slabs");
        public static final BlockItemTagId SANDSTONE_STAIRS = tag("sandstone/stairs");
        public static final BlockItemTagId SANDSTONE_RED_BLOCKS = tag("sandstone/red_blocks");
        public static final BlockItemTagId SANDSTONE_RED_SLABS = tag("sandstone/red_slabs");
        public static final BlockItemTagId SANDSTONE_RED_STAIRS = tag("sandstone/red_stairs");
        public static final BlockItemTagId SANDSTONE_UNCOLORED_BLOCKS = tag("sandstone/uncolored_blocks");
        public static final BlockItemTagId SANDSTONE_UNCOLORED_SLABS = tag("sandstone/uncolored_slabs");
        public static final BlockItemTagId SANDSTONE_UNCOLORED_STAIRS = tag("sandstone/uncolored_stairs");
        /// Natural stone-like blocks that can be used as a base ingredient in recipes that takes stone.
        public static final BlockItemTagId STONES = tag("stones");
        /// Resource storage blocks. These generally have a recipe to craft a bulk of one kind of resource to the storage block
        /// and a mirror recipe to reverse the crafting with no loss in resources.
        ///
        /// Honey Block is special in that the reversing recipe is not a perfect mirror of the crafting recipe.
        /// Therefore, it is a special case and not given a storage block tag.
        public static final BlockItemTagId STORAGE_BLOCKS = tag("storage_blocks");
        public static final BlockItemTagId STORAGE_BLOCKS_BONE_MEAL = tag("storage_blocks/bone_meal");
        public static final BlockItemTagId STORAGE_BLOCKS_COAL = tag("storage_blocks/coal");
        public static final BlockItemTagId STORAGE_BLOCKS_COPPER = tag("storage_blocks/copper");
        public static final BlockItemTagId STORAGE_BLOCKS_DIAMOND = tag("storage_blocks/diamond");
        public static final BlockItemTagId STORAGE_BLOCKS_DRIED_KELP = tag("storage_blocks/dried_kelp");
        public static final BlockItemTagId STORAGE_BLOCKS_EMERALD = tag("storage_blocks/emerald");
        public static final BlockItemTagId STORAGE_BLOCKS_GOLD = tag("storage_blocks/gold");
        public static final BlockItemTagId STORAGE_BLOCKS_IRON = tag("storage_blocks/iron");
        public static final BlockItemTagId STORAGE_BLOCKS_LAPIS = tag("storage_blocks/lapis");
        public static final BlockItemTagId STORAGE_BLOCKS_NETHERITE = tag("storage_blocks/netherite");
        public static final BlockItemTagId STORAGE_BLOCKS_RAW_COPPER = tag("storage_blocks/raw_copper");
        public static final BlockItemTagId STORAGE_BLOCKS_RAW_GOLD = tag("storage_blocks/raw_gold");
        public static final BlockItemTagId STORAGE_BLOCKS_RAW_IRON = tag("storage_blocks/raw_iron");
        public static final BlockItemTagId STORAGE_BLOCKS_REDSTONE = tag("storage_blocks/redstone");
        public static final BlockItemTagId STORAGE_BLOCKS_RESIN = tag("storage_blocks/resin");
        public static final BlockItemTagId STORAGE_BLOCKS_SLIME = tag("storage_blocks/slime");
        public static final BlockItemTagId STORAGE_BLOCKS_WHEAT = tag("storage_blocks/wheat");
        /// Logs found naturally in the Overworld, excluding stripped logs.
        /// Contains [BlockTags#OVERWORLD_NATURAL_LOGS] for consistency.
        public static final BlockItemTagId OVERWORLD_NATURAL_LOGS = tag("natural_logs/overworld");
        /// Logs, including stems, found naturally in the Nether, excluding stripped logs.
        public static final BlockItemTagId NETHER_NATURAL_LOGS = tag("natural_logs/nether");
        /// Logs, including stems, found naturally that have not been stripped.
        public static final BlockItemTagId NATURAL_LOGS = tag("natural_logs");
        /// Six-sided wood blocks, including hyphae, found naturally that have not been stripped.
        public static final BlockItemTagId NATURAL_WOODS = tag("natural_woods");
        /// Logs, including stems, found naturally that have been stripped of their bark.
        public static final BlockItemTagId STRIPPED_LOGS = tag("stripped_logs");
        /// Six-sided wood blocks, including hyphae, found naturally that have been stripped of their bark.
        public static final BlockItemTagId STRIPPED_WOODS = tag("stripped_woods");

        /// Creates a shared block-item tag that has identical names.
        private static BlockItemTagId tag(String name) {
            return BlockItemTagId.create(Identifier.fromNamespaceAndPath("c", name), Identifier.fromNamespaceAndPath("c", name));
        }
    }

    public static class EntityTypes {
        public static final TagKey<EntityType<?>> BOSSES = tag("bosses");
        public static final TagKey<EntityType<?>> MINECARTS = tag("minecarts");
        public static final TagKey<EntityType<?>> BOATS = tag("boats");

        /// Tag containing entity types, generally extending {@link ItemFrame},
        /// that can be placed on the surfaces of blocks to display an item.
        public static final TagKey<EntityType<?>> ITEM_FRAMES = tag("item_frames");

        /**
         * Entities should be included in this tag if they are not allowed to be picked up by items or grabbed in a way
         * that a player can easily move the entity to anywhere they want. Ideal for special entities that should not
         * be able to be put into a mob jar for example.
         */
        public static final TagKey<EntityType<?>> CAPTURING_NOT_SUPPORTED = tag("capturing_not_supported");

        /**
         * Entities should be included in this tag if they are not allowed to be teleported in any way.
         * This is more for mods that allow teleporting entities within the same dimension. Any mod that is
         * teleporting entities to new dimensions should be checking canChangeDimensions method on the entity itself.
         */
        public static final TagKey<EntityType<?>> TELEPORTING_NOT_SUPPORTED = tag("teleporting_not_supported");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("c", name));
        }
    }

    public static class Items {
        // `neoforge` tags for functional behavior provided by NeoForge
        /**
         * Controls what items can be consumed for enchanting such as Enchanting Tables.
         * This tag defaults to {@link net.minecraft.world.item.Items#LAPIS_LAZULI} when not present in any datapacks, including forge client on vanilla server
         */
        public static final TagKey<Item> ENCHANTING_FUELS = neoforgeTag("enchanting_fuels");
        /**
         * Controls what items Piglins can use as default as a valid crossbow
         * This tag defaults to {@link net.minecraft.world.item.Items#CROSSBOW} when not present in any datapacks, including forge client on vanilla server
         */
        public static final TagKey<Item> PIGLIN_USABLE_CROSSBOWS = neoforgeTag("piglin_usable_crossbows");
        /**
         * Controls what items Pillagers can use as default as a valid crossbow
         * This tag defaults to {@link net.minecraft.world.item.Items#CROSSBOW} when not present in any datapacks, including forge client on vanilla server
         */
        public static final TagKey<Item> PILLAGER_USABLE_CROSSBOWS = neoforgeTag("pillager_usable_crossbows");
        /**
         * Controls what items Skeletons can use as default as a valid bow
         * This tag defaults to {@link net.minecraft.world.item.Items#BOW} when not present in any datapacks, including forge client on vanilla server
         */
        public static final TagKey<Item> SKELETON_USABLE_BOWS = neoforgeTag("skeleton_usable_bows");

        // `c` tags for common conventions
        public static final TagKey<Item> BONES = tag("bones");
        public static final TagKey<Item> BRICKS = tag("bricks");
        public static final TagKey<Item> BRICKS_NORMAL = tag("bricks/normal");
        public static final TagKey<Item> BRICKS_NETHER = tag("bricks/nether");
        public static final TagKey<Item> BRICKS_RESIN = tag("bricks/resin");
        public static final TagKey<Item> BUCKETS = tag("buckets");
        public static final TagKey<Item> BUCKETS_EMPTY = tag("buckets/empty");
        /**
         * Does not include entity water buckets.
         * If checking for the fluid this bucket holds in code, please use {@link net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper#getFluid} instead.
         */
        public static final TagKey<Item> BUCKETS_WATER = tag("buckets/water");
        /**
         * If checking for the fluid this bucket holds in code, please use {@link net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper#getFluid} instead.
         */
        public static final TagKey<Item> BUCKETS_LAVA = tag("buckets/lava");
        public static final TagKey<Item> BUCKETS_MILK = tag("buckets/milk");
        public static final TagKey<Item> BUCKETS_POWDER_SNOW = tag("buckets/powder_snow");
        public static final TagKey<Item> BUCKETS_ENTITY_WATER = tag("buckets/entity_water");
        public static final TagKey<Item> BUCKETS_ENTITY_DRY = tag("buckets/entity_dry");
        /**
         * Block tag equivalent is {@link BlockTags#CONCRETE_POWDER}
         */
        public static final TagKey<Item> CONCRETE_POWDERS = tag("concrete_powders");
        public static final TagKey<Item> CLUMPS = tag("clumps");
        public static final TagKey<Item> CLUMPS_RESIN = tag("clumps/resin");
        /**
         * For raw materials harvested from growable plants. Crop items can be edible like carrots or
         * non-edible like wheat and cocoa beans.
         */
        public static final TagKey<Item> CROPS = tag("crops");
        public static final TagKey<Item> CROPS_BEETROOT = tag("crops/beetroot");
        public static final TagKey<Item> CROPS_CACTUS = tag("crops/cactus");
        public static final TagKey<Item> CROPS_CARROT = tag("crops/carrot");
        public static final TagKey<Item> CROPS_COCOA_BEAN = tag("crops/cocoa_bean");
        public static final TagKey<Item> CROPS_MELON = tag("crops/melon");
        public static final TagKey<Item> CROPS_NETHER_WART = tag("crops/nether_wart");
        public static final TagKey<Item> CROPS_POTATO = tag("crops/potato");
        public static final TagKey<Item> CROPS_PUMPKIN = tag("crops/pumpkin");
        public static final TagKey<Item> CROPS_SUGAR_CANE = tag("crops/sugar_cane");
        public static final TagKey<Item> CROPS_WHEAT = tag("crops/wheat");

        /**
         * Drinks are defined as (1) consumable items that (2) use the
         * {@linkplain net.minecraft.world.item.ItemUseAnimation#DRINK drink item use animation}, (3) can be consumed regardless of the
         * player's current hunger.
         *
         * <p>Drinks may provide nutrition and saturation, but are not required to do so.
         *
         * <p>More specific types of drinks, such as Water, Milk, or Juice should be placed in a sub-tag, such as
         * {@code #c:drinks/water}, {@code #c:drinks/milk}, and {@code #c:drinks/juice}.
         */
        public static final TagKey<Item> DRINKS = tag("drinks");
        /**
         * For consumable drinks that contain only water.
         */
        public static final TagKey<Item> DRINKS_WATER = tag("drinks/water");
        /**
         * For consumable drinks that are generally watery (such as potions).
         */
        public static final TagKey<Item> DRINKS_WATERY = tag("drinks/watery");
        public static final TagKey<Item> DRINKS_MILK = tag("drinks/milk");
        public static final TagKey<Item> DRINKS_HONEY = tag("drinks/honey");
        /**
         * For consumable drinks that are magic in nature and usually grant at least one
         * {@link net.minecraft.world.effect.MobEffect} when consumed.
         */
        public static final TagKey<Item> DRINKS_MAGIC = tag("drinks/magic");
        /**
         * For drinks that always grant the {@linkplain net.minecraft.world.effect.MobEffects#BAD_OMEN Bad Omen} effect.
         */
        public static final TagKey<Item> DRINKS_OMINOUS = tag("drinks/ominous");
        /**
         * Plant based fruit and vegetable juices belong in this tag, for example apple juice and carrot juice.
         *
         * <p>If tags for specific types of juices are desired, they may go in a sub-tag, using their regular name such as
         * {@code #c:drinks/apple_juice}.
         */
        public static final TagKey<Item> DRINKS_JUICE = tag("drinks/juice");

        /**
         * For non-empty bottles that are {@linkplain #DRINKS drinkable}.
         */
        public static final TagKey<Item> DRINK_CONTAINING_BOTTLE = tag("drink_containing/bottle");
        /**
         * For non-empty buckets that are {@linkplain #DRINKS drinkable}.
         */
        public static final TagKey<Item> DRINK_CONTAINING_BUCKET = tag("drink_containing/bucket");

        public static final TagKey<Item> DUSTS = tag("dusts");
        public static final TagKey<Item> DUSTS_REDSTONE = tag("dusts/redstone");
        public static final TagKey<Item> DUSTS_GLOWSTONE = tag("dusts/glowstone");

        public static final TagKey<Item> DYES = tag("dyes");
        public static final TagKey<Item> DYES_BLACK = DyeColor.BLACK.getTag();
        public static final TagKey<Item> DYES_RED = DyeColor.RED.getTag();
        public static final TagKey<Item> DYES_GREEN = DyeColor.GREEN.getTag();
        public static final TagKey<Item> DYES_BROWN = DyeColor.BROWN.getTag();
        public static final TagKey<Item> DYES_BLUE = DyeColor.BLUE.getTag();
        public static final TagKey<Item> DYES_PURPLE = DyeColor.PURPLE.getTag();
        public static final TagKey<Item> DYES_CYAN = DyeColor.CYAN.getTag();
        public static final TagKey<Item> DYES_LIGHT_GRAY = DyeColor.LIGHT_GRAY.getTag();
        public static final TagKey<Item> DYES_GRAY = DyeColor.GRAY.getTag();
        public static final TagKey<Item> DYES_PINK = DyeColor.PINK.getTag();
        public static final TagKey<Item> DYES_LIME = DyeColor.LIME.getTag();
        public static final TagKey<Item> DYES_YELLOW = DyeColor.YELLOW.getTag();
        public static final TagKey<Item> DYES_LIGHT_BLUE = DyeColor.LIGHT_BLUE.getTag();
        public static final TagKey<Item> DYES_MAGENTA = DyeColor.MAGENTA.getTag();
        public static final TagKey<Item> DYES_ORANGE = DyeColor.ORANGE.getTag();
        public static final TagKey<Item> DYES_WHITE = DyeColor.WHITE.getTag();

        /**
         * Dye color tags as a color collection, for convenience.
         */
        public static final ColorCollection<TagKey<Item>> DYE_COLORS = new ColorCollection<>(
                DYES_WHITE,
                DYES_ORANGE,
                DYES_MAGENTA,
                DYES_LIGHT_BLUE,
                DYES_YELLOW,
                DYES_LIME,
                DYES_PINK,
                DYES_GRAY,
                DYES_LIGHT_GRAY,
                DYES_CYAN,
                DYES_PURPLE,
                DYES_BLUE,
                DYES_BROWN,
                DYES_GREEN,
                DYES_RED,
                DYES_BLACK);

        /**
         * For eggs to use for culinary purposes in recipes such as baking a cake.
         */
        public static final TagKey<Item> EGGS = tag("eggs");
        public static final TagKey<Item> ENDER_PEARLS = tag("ender_pearls");
        public static final TagKey<Item> FEATHERS = tag("feathers");
        /**
         * For bonemeal-like items that can grow plants.
         * (Note: Could include durability-based modded bonemeal-like items. Check for durability {@link net.minecraft.core.component.DataComponents#DAMAGE} DataComponent to handle them properly)
         */
        public static final TagKey<Item> FERTILIZERS = tag("fertilizers");
        public static final TagKey<Item> FOODS = tag("foods");
        /**
         * Apples and other foods that are considered fruits in the culinary field belong in this tag.
         * Cherries would go here as they are considered a "stone fruit" within culinary fields.
         */
        public static final TagKey<Item> FOODS_FRUIT = tag("foods/fruit");
        /**
         * Tomatoes and other foods that are considered vegetables in the culinary field belong in this tag.
         */
        public static final TagKey<Item> FOODS_VEGETABLE = tag("foods/vegetable");
        /**
         * Strawberries, raspberries, and other berry foods belong in this tag.
         * Cherries would NOT go here as they are considered a "stone fruit" within culinary fields.
         */
        public static final TagKey<Item> FOODS_BERRY = tag("foods/berry");
        public static final TagKey<Item> FOODS_BREAD = tag("foods/bread");
        public static final TagKey<Item> FOODS_COOKIE = tag("foods/cookie");
        /**
         * For all doughs regardless of type, specific types of dough should fall under their respective sub-tag.<br/>
         * For example:<br/>
         * - Wheat dough (which generally results in bread) would go in "#c:foods/dough/wheat"<br/>
         * - Rye dough (which has rye as it's main ingredient) would go in "#c:foods/dough/rye"<br/>
         * - Sub-tags should also be added to this tag, for example: "#c:foods/dough/wheat" should be added to "#c:foods/dough"<br/>
         * <br/>
         * There are some important assumptions that should be kept in mind.<br/>
         * - It is assumed that "1 dough = result", which in the case of wheat dough would be "1 dough = 1 bread"<br/>
         * - It is assumed that this dough can be baked into another item<br/>
         * - It is *not* assumed that all doughs result in bread, there can be doughs in this tag that result in things like pizza, etc.
         * This means that this tag should *not* be used for furnace recipes, mods should add their own dough to result recipes for their respective items.
         */
        public static final TagKey<Item> FOODS_DOUGH = tag("foods/dough");
        public static final TagKey<Item> FOODS_RAW_MEAT = tag("foods/raw_meat");
        public static final TagKey<Item> FOODS_COOKED_MEAT = tag("foods/cooked_meat");
        public static final TagKey<Item> FOODS_RAW_FISH = tag("foods/raw_fish");
        public static final TagKey<Item> FOODS_COOKED_FISH = tag("foods/cooked_fish");
        /**
         * Soups, stews, and other liquid food in bowls belongs in this tag.
         */
        public static final TagKey<Item> FOODS_SOUP = tag("foods/soup");
        /**
         * Sweets and candies like lollipops or chocolate belong in this tag.
         */
        public static final TagKey<Item> FOODS_CANDY = tag("foods/candy");
        /**
         * Pies and other pie-like foods belong in this tag.
         */
        public static final TagKey<Item> FOODS_PIE = tag("foods/pie");
        /**
         * Any gold-based foods would go in this tag. Such as Golden Apples or Glistering Melon Slice.
         */
        public static final TagKey<Item> FOODS_GOLDEN = tag("foods/golden");
        /**
         * Foods like cake that can be eaten when placed in the world belong in this tag.
         */
        public static final TagKey<Item> FOODS_EDIBLE_WHEN_PLACED = tag("foods/edible_when_placed");
        /**
         * For foods that inflict food poisoning-like effects.
         * Examples are Rotten Flesh's Hunger or Pufferfish's Nausea, or Poisonous Potato's Poison.
         */
        public static final TagKey<Item> FOODS_FOOD_POISONING = tag("foods/food_poisoning");
        /**
         * All foods edible by animals excluding poisonous foods.
         * (Does not include {@link ItemTags#PARROT_POISONOUS_FOOD})
         */
        public static final TagKey<Item> ANIMAL_FOODS = tag("animal_foods");
        public static final TagKey<Item> GEMS = tag("gems");
        public static final TagKey<Item> GEMS_DIAMOND = tag("gems/diamond");
        public static final TagKey<Item> GEMS_EMERALD = tag("gems/emerald");
        public static final TagKey<Item> GEMS_AMETHYST = tag("gems/amethyst");
        public static final TagKey<Item> GEMS_LAPIS = tag("gems/lapis");
        public static final TagKey<Item> GEMS_PRISMARINE = tag("gems/prismarine");
        public static final TagKey<Item> GEMS_QUARTZ = tag("gems/quartz");

        public static final TagKey<Item> GUNPOWDERS = tag("gunpowders");
        /**
         * Tag that holds all items that recipe viewers should not show to users.
         */
        public static final TagKey<Item> HIDDEN_FROM_RECIPE_VIEWERS = tag("hidden_from_recipe_viewers");
        public static final TagKey<Item> INGOTS = tag("ingots");
        public static final TagKey<Item> INGOTS_COPPER = tag("ingots/copper");
        public static final TagKey<Item> INGOTS_GOLD = tag("ingots/gold");
        public static final TagKey<Item> INGOTS_IRON = tag("ingots/iron");
        public static final TagKey<Item> INGOTS_NETHERITE = tag("ingots/netherite");
        public static final TagKey<Item> LEATHERS = tag("leathers");
        /**
         * Small mushroom items. Not the full block forms.
         */
        public static final TagKey<Item> MUSHROOMS = tag("mushrooms");
        /**
         * For music disc-like materials to be used in recipes.
         * A pancake with a JUKEBOX_PLAYABLE component attached to play in Jukeboxes as an Easter Egg is not a music disc and would not go in this tag.
         */
        public static final TagKey<Item> MUSIC_DISCS = tag("music_discs");
        public static final TagKey<Item> NETHER_STARS = tag("nether_stars");
        public static final TagKey<Item> NUGGETS = tag("nuggets");
        public static final TagKey<Item> NUGGETS_GOLD = tag("nuggets/gold");
        public static final TagKey<Item> NUGGETS_IRON = tag("nuggets/iron");
        public static final TagKey<Item> NUGGETS_COPPER = tag("nuggets/copper");
        /**
         * Items that can hold various potion effects by making use of {@link net.minecraft.core.component.DataComponents#POTION_CONTENTS}.
         * Contents of this tag may not always be a kind of bottle. Buckets of potions could go here.
         * The subtags would be the name of the container that is holding the potion effects such as `c:potions/bucket` or `c:potions/vial` as examples.
         */
        public static final TagKey<Item> POTIONS = tag("potions");
        /**
         * Variations of the potion bottle that can hold various effects by using {@link net.minecraft.core.component.DataComponents#POTION_CONTENTS}.
         * Examples are splash and lingering potions from vanilla.
         * If a mod adds a new variant like a seeking potion that applies effect to the closest entity at impact, that would in this tag.
         */
        public static final TagKey<Item> POTIONS_BOTTLE = tag("potions/bottle");

        public static final TagKey<Item> RAW_MATERIALS = tag("raw_materials");
        public static final TagKey<Item> RAW_MATERIALS_COPPER = tag("raw_materials/copper");
        public static final TagKey<Item> RAW_MATERIALS_GOLD = tag("raw_materials/gold");
        public static final TagKey<Item> RAW_MATERIALS_IRON = tag("raw_materials/iron");
        /**
         * For rod-like materials to be used in recipes.
         */
        public static final TagKey<Item> RODS = tag("rods");
        public static final TagKey<Item> RODS_BLAZE = tag("rods/blaze");
        public static final TagKey<Item> RODS_BREEZE = tag("rods/breeze");
        /**
         * For stick-like materials to be used in recipes.
         * One example is a mod adds stick variants such as Spruce Sticks but would like stick recipes to be able to use it.
         */
        public static final TagKey<Item> RODS_WOODEN = tag("rods/wooden");

        /**
         * For items that are explicitly seeds for use cases such as refilling a bird feeder block or certain seed-based recipes.
         */
        public static final TagKey<Item> SEEDS = tag("seeds");
        public static final TagKey<Item> SEEDS_BEETROOT = tag("seeds/beetroot");
        public static final TagKey<Item> SEEDS_MELON = tag("seeds/melon");
        public static final TagKey<Item> SEEDS_PUMPKIN = tag("seeds/pumpkin");
        public static final TagKey<Item> SEEDS_TORCHFLOWER = tag("seeds/torchflower");
        public static final TagKey<Item> SEEDS_PITCHER_PLANT = tag("seeds/pitcher_plant");
        public static final TagKey<Item> SEEDS_WHEAT = tag("seeds/wheat");
        /**
         * Block tag equivalent is {@link BlockTags#SHULKER_BOXES}
         */
        public static final TagKey<Item> SHULKER_BOXES = tag("shulker_boxes");
        public static final TagKey<Item> SLIME_BALLS = tag("slime_balls");
        public static final TagKey<Item> STRINGS = tag("strings");
        public static final TagKey<Item> VILLAGER_JOB_SITES = tag("villager_job_sites");

        // Tools and Armors
        /**
         * A tag containing all existing tools. Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS = tag("tools");
        /**
         * A tag containing all existing shields. Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS_SHIELD = tag("tools/shield");
        /**
         * A tag containing all existing bows. Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS_BOW = tag("tools/bow");
        /**
         * A tag containing all existing crossbows. Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS_CROSSBOW = tag("tools/crossbow");
        /**
         * A tag containing all existing fishing rods. Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS_FISHING_ROD = tag("tools/fishing_rod");
        /**
         * A tag containing all existing throwable stick-like weapons like tridents.
         * Other tools such as throwing knives or boomerangs should not be put into
         * this tag and should be put into their own tool tags.
         * Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS_TRIDENT = tag("tools/trident");
        /**
         * A tag containing all existing shears. Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS_SHEAR = tag("tools/shear");
        /**
         * A tag containing all existing brushes. Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS_BRUSH = tag("tools/brush");
        /**
         * A tag containing all existing fire starting tools such as Flint and Steel.
         * Fire Charge is not a tool (no durability) and thus, does not go in this tag.
         * Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS_IGNITER = tag("tools/igniter");
        /**
         * A tag containing all existing maces. Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS_MACE = tag("tools/mace");
        /**
         * A tag containing all existing wrenches. Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS_WRENCH = tag("tools/wrench");
        /**
         * A tag containing melee-based weapons for recipes and loot tables.
         * Tools are considered melee if they are intentionally intended to be used for melee attack as a primary purpose.
         * (In other words, Pickaxes are not melee weapons as they are not intended to be a weapon as a primary purpose)
         * Do not use this tag for determining a tool's behavior in-code.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> MELEE_WEAPON_TOOLS = tag("tools/melee_weapon");
        /**
         * A tag containing ranged-based weapons for recipes and loot tables.
         * Tools are considered ranged if they can damage entities beyond the weapon's and player's melee attack range.
         * Do not use this tag for determining a tool's behavior in-code.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> RANGED_WEAPON_TOOLS = tag("tools/ranged_weapon");
        /**
         * A tag containing mining-based tools for recipes and loot tables.
         * Do not use this tag for determining a tool's behavior in-code.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> MINING_TOOL_TOOLS = tag("tools/mining_tool");
        /**
         * A tag containing all conventional armor tags.
         * Note that this can contain armor that does not necessarily fit on a player. For that, see {@link Tags.Items#ARMORS_HUMANOID}
         */
        public static final TagKey<Item> ARMORS = tag("armors");
        /**
         * Armor that can fit on a humanoid mob like the Player. This tag collects the 4 vanilla armor tags into one parent collection for ease.
         */
        public static final TagKey<Item> ARMORS_HUMANOID = tag("armors/humanoid");
        /**
         * A tag containing armor that can fit on a Horse.
         */
        public static final TagKey<Item> ARMORS_HORSE = tag("armors/horse");
        /**
         * A tag containing armor that can fit on a Nautilus.
         */
        public static final TagKey<Item> ARMORS_NAUTILUS = tag("armors/nautilus");
        /**
         * A tag containing armor that can fit on a Wolf.
         */
        public static final TagKey<Item> ARMORS_WOLF = tag("armors/wolf");
        /**
         * Collects the many enchantable tags into one parent collection for ease.
         */
        public static final TagKey<Item> ENCHANTABLES = tag("enchantables");

        // These fields are deprecated for removal in 26.4.
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BARRELS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> BARRELS = BlockItems.BARRELS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BARRELS_WOODEN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> BARRELS_WOODEN = BlockItems.BARRELS_WOODEN.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BARS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> BARS = BlockItems.BARS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BARS_COPPER] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> BARS_COPPER = BlockItems.BARS_COPPER.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BARS_IRON] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> BARS_IRON = BlockItems.BARS_IRON.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BOOKSHELVES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> BOOKSHELVES = BlockItems.BOOKSHELVES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BUDDING_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> BUDDING_BLOCKS = BlockItems.BUDDING_BLOCKS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#BUDS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> BUDS = BlockItems.BUDS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CHAINS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> CHAINS = BlockItems.CHAINS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CHESTS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> CHESTS = BlockItems.CHESTS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CHESTS_ENDER] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> CHESTS_ENDER = BlockItems.CHESTS_ENDER.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CHESTS_TRAPPED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> CHESTS_TRAPPED = BlockItems.CHESTS_TRAPPED.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CHESTS_WOODEN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> CHESTS_WOODEN = BlockItems.CHESTS_WOODEN.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#COBBLESTONES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> COBBLESTONES = BlockItems.COBBLESTONES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#COBBLESTONES_NORMAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> COBBLESTONES_NORMAL = BlockItems.COBBLESTONES_NORMAL.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#COBBLESTONES_INFESTED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> COBBLESTONES_INFESTED = BlockItems.COBBLESTONES_INFESTED.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#COBBLESTONES_MOSSY] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> COBBLESTONES_MOSSY = BlockItems.COBBLESTONES_MOSSY.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#COBBLESTONES_DEEPSLATE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> COBBLESTONES_DEEPSLATE = BlockItems.COBBLESTONES_DEEPSLATE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CONCRETES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> CONCRETES = BlockItems.CONCRETES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#CLUSTERS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> CLUSTERS = BlockItems.CLUSTERS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED = BlockItems.DYED.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_BLACK] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_BLACK = BlockItems.DYED_BLACK.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_BLUE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_BLUE = BlockItems.DYED_BLUE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_BROWN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_BROWN = BlockItems.DYED_BROWN.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_CYAN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_CYAN = BlockItems.DYED_CYAN.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_GRAY] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_GRAY = BlockItems.DYED_GRAY.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_GREEN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_GREEN = BlockItems.DYED_GREEN.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_LIGHT_BLUE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_LIGHT_BLUE = BlockItems.DYED_LIGHT_BLUE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_LIGHT_GRAY] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_LIGHT_GRAY = BlockItems.DYED_LIGHT_GRAY.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_LIME] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_LIME = BlockItems.DYED_LIME.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_MAGENTA] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_MAGENTA = BlockItems.DYED_MAGENTA.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_ORANGE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_ORANGE = BlockItems.DYED_ORANGE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_PINK] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_PINK = BlockItems.DYED_PINK.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_PURPLE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_PURPLE = BlockItems.DYED_PURPLE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_RED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_RED = BlockItems.DYED_RED.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_WHITE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_WHITE = BlockItems.DYED_WHITE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_YELLOW] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYED_YELLOW = BlockItems.DYED_YELLOW.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYED_COLORS] mapped to the block using [BlockItemTagId#item()] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final ColorCollection<TagKey<Item>> DYED_COLORS = BlockItems.DYED_COLORS.map(BlockItemTagId::item);
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYEABLE_UNDYED_SIMPLE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYEABLE_UNDYED_SIMPLE = BlockItems.DYEABLE_UNDYED_SIMPLE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYEABLE_REDYEABLE_SIMPLE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYEABLE_REDYEABLE_SIMPLE = BlockItems.DYEABLE_REDYEABLE_SIMPLE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYEABLE_SIMPLE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYEABLE_SIMPLE = BlockItems.DYEABLE_SIMPLE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYEABLE_DYNAMIC] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYEABLE_DYNAMIC = BlockItems.DYEABLE_DYNAMIC.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#DYEABLE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> DYEABLE = BlockItems.DYEABLE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#END_STONES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> END_STONES = BlockItems.END_STONES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FENCE_GATES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> FENCE_GATES = BlockItems.FENCE_GATES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FENCE_GATES_WOODEN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> FENCE_GATES_WOODEN = BlockItems.FENCE_GATES_WOODEN.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FENCES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> FENCES = BlockItems.FENCES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FENCES_NETHER_BRICK] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> FENCES_NETHER_BRICK = BlockItems.FENCES_NETHER_BRICK.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FENCES_WOODEN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> FENCES_WOODEN = BlockItems.FENCES_WOODEN.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FLOWERS_SMALL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> FLOWERS_SMALL = BlockItems.FLOWERS_SMALL.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FLOWERS_TALL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> FLOWERS_TALL = BlockItems.FLOWERS_TALL.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FLOWERS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> FLOWERS = BlockItems.FLOWERS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#FROGLIGHTS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> FROGLIGHTS = BlockItems.FROGLIGHTS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> GLASS_BLOCKS = BlockItems.GLASS_BLOCKS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_BLOCKS_COLORLESS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> GLASS_BLOCKS_COLORLESS = BlockItems.GLASS_BLOCKS_COLORLESS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_BLOCKS_CHEAP] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> GLASS_BLOCKS_CHEAP = BlockItems.GLASS_BLOCKS_CHEAP.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_BLOCKS_TINTED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> GLASS_BLOCKS_TINTED = BlockItems.GLASS_BLOCKS_TINTED.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_PANES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> GLASS_PANES = BlockItems.GLASS_PANES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLASS_PANES_COLORLESS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> GLASS_PANES_COLORLESS = BlockItems.GLASS_PANES_COLORLESS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GLAZED_TERRACOTTAS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> GLAZED_TERRACOTTAS = BlockItems.GLAZED_TERRACOTTAS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#GRAVELS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> GRAVELS = BlockItems.GRAVELS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#NETHERRACKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> NETHERRACKS = BlockItems.NETHERRACKS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#OBSIDIANS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> OBSIDIANS = BlockItems.OBSIDIANS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#OBSIDIANS_NORMAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> OBSIDIANS_NORMAL = BlockItems.OBSIDIANS_NORMAL.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#OBSIDIANS_CRYING] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> OBSIDIANS_CRYING = BlockItems.OBSIDIANS_CRYING.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_BEARING_GROUND_DEEPSLATE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORE_BEARING_GROUND_DEEPSLATE = BlockItems.ORE_BEARING_GROUND_DEEPSLATE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_BEARING_GROUND_NETHERRACK] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORE_BEARING_GROUND_NETHERRACK = BlockItems.ORE_BEARING_GROUND_NETHERRACK.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_BEARING_GROUND_STONE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORE_BEARING_GROUND_STONE = BlockItems.ORE_BEARING_GROUND_STONE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_RATES_DENSE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORE_RATES_DENSE = BlockItems.ORE_RATES_DENSE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_RATES_SINGULAR] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORE_RATES_SINGULAR = BlockItems.ORE_RATES_SINGULAR.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORE_RATES_SPARSE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORE_RATES_SPARSE = BlockItems.ORE_RATES_SPARSE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES = BlockItems.ORES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_COAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_COAL = BlockItems.ORES_COAL.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_COPPER] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_COPPER = BlockItems.ORES_COPPER.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_DIAMOND] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_DIAMOND = BlockItems.ORES_DIAMOND.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_EMERALD] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_EMERALD = BlockItems.ORES_EMERALD.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_GOLD] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_GOLD = BlockItems.ORES_GOLD.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_IRON] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_IRON = BlockItems.ORES_IRON.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_LAPIS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_LAPIS = BlockItems.ORES_LAPIS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_NETHERITE_SCRAP] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_NETHERITE_SCRAP = BlockItems.ORES_NETHERITE_SCRAP.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_QUARTZ] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_QUARTZ = BlockItems.ORES_QUARTZ.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_REDSTONE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_REDSTONE = BlockItems.ORES_REDSTONE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_IN_GROUND_DEEPSLATE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_IN_GROUND_DEEPSLATE = BlockItems.ORES_IN_GROUND_DEEPSLATE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_IN_GROUND_NETHERRACK] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_IN_GROUND_NETHERRACK = BlockItems.ORES_IN_GROUND_NETHERRACK.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ORES_IN_GROUND_STONE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ORES_IN_GROUND_STONE = BlockItems.ORES_IN_GROUND_STONE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PLAYER_WORKSTATIONS_CRAFTING_TABLES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> PLAYER_WORKSTATIONS_CRAFTING_TABLES = BlockItems.PLAYER_WORKSTATIONS_CRAFTING_TABLES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PLAYER_WORKSTATIONS_FURNACES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> PLAYER_WORKSTATIONS_FURNACES = BlockItems.PLAYER_WORKSTATIONS_FURNACES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PUMPKINS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> PUMPKINS = BlockItems.PUMPKINS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PUMPKINS_NORMAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> PUMPKINS_NORMAL = BlockItems.PUMPKINS_NORMAL.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PUMPKINS_CARVED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> PUMPKINS_CARVED = BlockItems.PUMPKINS_CARVED.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#PUMPKINS_JACK_O_LANTERNS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> PUMPKINS_JACK_O_LANTERNS = BlockItems.PUMPKINS_JACK_O_LANTERNS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#ROPES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> ROPES = BlockItems.ROPES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDS = BlockItems.SANDS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDS_COLORLESS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDS_COLORLESS = BlockItems.SANDS_COLORLESS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDS_RED] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDS_RED = BlockItems.SANDS_RED.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDSTONE_BLOCKS = BlockItems.SANDSTONE_BLOCKS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_SLABS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDSTONE_SLABS = BlockItems.SANDSTONE_SLABS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_STAIRS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDSTONE_STAIRS = BlockItems.SANDSTONE_STAIRS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_RED_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDSTONE_RED_BLOCKS = BlockItems.SANDSTONE_RED_BLOCKS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_RED_SLABS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDSTONE_RED_SLABS = BlockItems.SANDSTONE_RED_SLABS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_RED_STAIRS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDSTONE_RED_STAIRS = BlockItems.SANDSTONE_RED_STAIRS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_UNCOLORED_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDSTONE_UNCOLORED_BLOCKS = BlockItems.SANDSTONE_UNCOLORED_BLOCKS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_UNCOLORED_SLABS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDSTONE_UNCOLORED_SLABS = BlockItems.SANDSTONE_UNCOLORED_SLABS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#SANDSTONE_UNCOLORED_STAIRS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> SANDSTONE_UNCOLORED_STAIRS = BlockItems.SANDSTONE_UNCOLORED_STAIRS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STONES] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STONES = BlockItems.STONES.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS = BlockItems.STORAGE_BLOCKS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_BONE_MEAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_BONE_MEAL = BlockItems.STORAGE_BLOCKS_BONE_MEAL.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_COAL] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_COAL = BlockItems.STORAGE_BLOCKS_COAL.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_COPPER] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_COPPER = BlockItems.STORAGE_BLOCKS_COPPER.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_DIAMOND] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_DIAMOND = BlockItems.STORAGE_BLOCKS_DIAMOND.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_DRIED_KELP] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_DRIED_KELP = BlockItems.STORAGE_BLOCKS_DRIED_KELP.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_EMERALD] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_EMERALD = BlockItems.STORAGE_BLOCKS_EMERALD.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_GOLD] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_GOLD = BlockItems.STORAGE_BLOCKS_GOLD.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_IRON] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_IRON = BlockItems.STORAGE_BLOCKS_IRON.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_LAPIS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_LAPIS = BlockItems.STORAGE_BLOCKS_LAPIS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_NETHERITE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_NETHERITE = BlockItems.STORAGE_BLOCKS_NETHERITE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_RAW_COPPER] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_COPPER = BlockItems.STORAGE_BLOCKS_RAW_COPPER.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_RAW_GOLD] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_GOLD = BlockItems.STORAGE_BLOCKS_RAW_GOLD.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_RAW_IRON] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_IRON = BlockItems.STORAGE_BLOCKS_RAW_IRON.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_REDSTONE] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_REDSTONE = BlockItems.STORAGE_BLOCKS_REDSTONE.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_RESIN] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_RESIN = BlockItems.STORAGE_BLOCKS_RESIN.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_SLIME] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_SLIME = BlockItems.STORAGE_BLOCKS_SLIME.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STORAGE_BLOCKS_WHEAT] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STORAGE_BLOCKS_WHEAT = BlockItems.STORAGE_BLOCKS_WHEAT.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#OVERWORLD_NATURAL_LOGS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> OVERWORLD_NATURAL_LOGS = BlockItems.OVERWORLD_NATURAL_LOGS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#NETHER_NATURAL_LOGS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> NETHER_NATURAL_LOGS = BlockItems.NETHER_NATURAL_LOGS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#NATURAL_LOGS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> NATURAL_LOGS = BlockItems.NATURAL_LOGS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#NATURAL_WOODS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> NATURAL_WOODS = BlockItems.NATURAL_WOODS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STRIPPED_LOGS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STRIPPED_LOGS = BlockItems.STRIPPED_LOGS.item();
        /// @deprecated This specific field is to be removed in 26.4. The tag itself will remain. Use [BlockItems#STRIPPED_WOODS] instead.
        @Deprecated(forRemoval = true, since = "26.3")
        public static final TagKey<Item> STRIPPED_WOODS = BlockItems.STRIPPED_WOODS.item();

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(Identifier.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Item> neoforgeTag(String name) {
            return ItemTags.create(Identifier.fromNamespaceAndPath("neoforge", name));
        }
    }

    /**
     * Note, fluid tags should not be plural to match the vanilla standard.
     * This is the only tag category exempted from many-different-types plural rule.
     */
    public static class Fluids {
        /**
         * Holds all fluids related to water.<p>
         * This tag is done to help out multi-loader mods/datapacks where the vanilla water tag has attached behaviors outside Neo.
         */
        public static final TagKey<Fluid> WATER = tag("water");
        /**
         * Holds all fluids related to lava.<p>
         * This tag is done to help out multi-loader mods/datapacks where the vanilla lava tag has attached behaviors outside Neo.
         */
        public static final TagKey<Fluid> LAVA = tag("lava");
        /**
         * Holds all fluids related to milk.
         */
        public static final TagKey<Fluid> MILK = tag("milk");
        /**
         * Holds all fluids that are gaseous at room temperature.
         */
        public static final TagKey<Fluid> GASEOUS = tag("gaseous");
        /**
         * Holds all fluids related to honey.
         * <p>
         * (Standard unit for honey bottle is 250mb per bottle)
         */
        public static final TagKey<Fluid> HONEY = tag("honey");
        /**
         * Holds all fluids related to experience.
         * <p>
         * (Standard unit for experience is 20mb per 1 experience. However, extraction from Bottle o' Enchanting should yield 250mb while smashing yields less)
         */
        public static final TagKey<Fluid> EXPERIENCE = tag("experience");
        /**
         * Holds all fluids related to potions. The effects of the potion fluid should be read from DataComponents.
         * The effects and color of the potion fluid should be read from {@link net.minecraft.core.component.DataComponents#POTION_CONTENTS}
         * component that people should be attaching to the fluidstack of this fluid.
         * <p>
         * (Standard unit for potions is 250mb per bottle)
         */
        public static final TagKey<Fluid> POTION = tag("potion");
        /**
         * Holds all fluids related to Suspicious Stew.
         * The effects of the suspicious stew fluid should be read from {@link net.minecraft.core.component.DataComponents#SUSPICIOUS_STEW_EFFECTS}
         * component that people should be attaching to the fluidstack of this fluid.
         * <p>
         * (Standard unit for suspicious stew is 250mb per bowl)
         */
        public static final TagKey<Fluid> SUSPICIOUS_STEW = tag("suspicious_stew");
        /**
         * Holds all fluids related to Mushroom Stew.
         * <p>
         * (Standard unit for mushroom stew is 250mb per bowl)
         */
        public static final TagKey<Fluid> MUSHROOM_STEW = tag("mushroom_stew");
        /**
         * Holds all fluids related to Rabbit Stew.
         * <p>
         * (Standard unit for rabbit stew is 250mb per bowl)
         */
        public static final TagKey<Fluid> RABBIT_STEW = tag("rabbit_stew");
        /**
         * Holds all fluids related to Beetroot Soup.
         * <p>
         * (Standard unit for beetroot soup is 250mb per bowl)
         */
        public static final TagKey<Fluid> BEETROOT_SOUP = tag("beetroot_soup");
        /**
         * Tag that holds all fluids that recipe viewers should not show to users.
         */
        public static final TagKey<Fluid> HIDDEN_FROM_RECIPE_VIEWERS = tag("hidden_from_recipe_viewers");

        private static TagKey<Fluid> tag(String name) {
            return FluidTags.create(Identifier.fromNamespaceAndPath("c", name));
        }
    }

    public static class Enchantments {
        /**
         * A tag containing enchantments that increase the amount or
         * quality of drops from blocks, such as {@link net.minecraft.world.item.enchantment.Enchantments#FORTUNE}.
         */
        public static final TagKey<Enchantment> INCREASE_BLOCK_DROPS = tag("increase_block_drops");
        /**
         * A tag containing enchantments that increase the amount or
         * quality of drops from entities, such as {@link net.minecraft.world.item.enchantment.Enchantments#LOOTING}.
         */
        public static final TagKey<Enchantment> INCREASE_ENTITY_DROPS = tag("increase_entity_drops");
        /**
         * For enchantments that increase the damage dealt by an item.
         */
        public static final TagKey<Enchantment> WEAPON_DAMAGE_ENHANCEMENTS = tag("weapon_damage_enhancements");
        /**
         * For enchantments that increase movement speed for entity wearing armor enchanted with it.
         */
        public static final TagKey<Enchantment> ENTITY_SPEED_ENHANCEMENTS = tag("entity_speed_enhancements");
        /**
         * For enchantments that applies movement-based benefits unrelated to speed for the entity wearing armor enchanted with it.
         * Example: Reducing falling speeds ({@link net.minecraft.world.item.enchantment.Enchantments#FEATHER_FALLING}) or allowing walking on water ({@link net.minecraft.world.item.enchantment.Enchantments#FROST_WALKER})
         */
        public static final TagKey<Enchantment> ENTITY_AUXILIARY_MOVEMENT_ENHANCEMENTS = tag("entity_auxiliary_movement_enhancements");
        /**
         * For enchantments that decrease damage taken or otherwise benefit, in regard to damage, the entity wearing armor enchanted with it.
         */
        public static final TagKey<Enchantment> ENTITY_DEFENSE_ENHANCEMENTS = tag("entity_defense_enhancements");
        /**
         * Tag that holds all enchantments that recipe viewers should not show to users.
         * Recipe viewers may use this to automatically find the corresponding Enchanted Book to hide.
         */
        public static final TagKey<Enchantment> HIDDEN_FROM_RECIPE_VIEWERS = tag("hidden_from_recipe_viewers");

        private static TagKey<Enchantment> tag(String name) {
            return TagKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath("c", name));
        }
    }

    public static class Potions {
        /**
         * Tag that holds all enchantments that recipe viewers should not show to users.
         * Recipe viewers may use this to automatically find the corresponding Potion items to hide.
         */
        public static final TagKey<Potion> HIDDEN_FROM_RECIPE_VIEWERS = tag("hidden_from_recipe_viewers");

        private static TagKey<Potion> tag(String name) {
            return TagKey.create(Registries.POTION, Identifier.fromNamespaceAndPath("c", name));
        }
    }

    public static class Biomes {
        /**
         * For biomes that should not spawn monsters over time the normal way.
         * In other words, their Spawners and Spawn Cost entries have the monster category empty.
         * Example: Mushroom Biomes not having Zombies, Creepers, Skeleton, nor any other normal monsters.
         */
        public static final TagKey<Biome> NO_DEFAULT_MONSTERS = tag("no_default_monsters");
        /**
         * Biomes that should not be locatable/selectable by modded biome-locating items or abilities.
         */
        public static final TagKey<Biome> HIDDEN_FROM_LOCATOR_SELECTION = tag("hidden_from_locator_selection");

        public static final TagKey<Biome> IS_VOID = tag("is_void");

        /**
         * Biomes that are above 0.8 temperature. (Excluding 0.8)
         */
        public static final TagKey<Biome> IS_HOT = tag("is_hot");
        public static final TagKey<Biome> IS_HOT_OVERWORLD = tag("is_hot/overworld");
        public static final TagKey<Biome> IS_HOT_NETHER = tag("is_hot/nether");
        public static final TagKey<Biome> IS_HOT_END = tag("is_hot/end");

        /**
         * Biomes that are between 0.5 and 0.8 temperature range. (Including 0.5 and 0.8)
         */
        public static final TagKey<Biome> IS_TEMPERATE = tag("is_temperate");
        public static final TagKey<Biome> IS_TEMPERATE_OVERWORLD = tag("is_temperate/overworld");
        public static final TagKey<Biome> IS_TEMPERATE_NETHER = tag("is_temperate/nether");
        public static final TagKey<Biome> IS_TEMPERATE_END = tag("is_temperate/end");

        /**
         * Biomes that are below 0.5 temperature. (Excluding 0.5)
         */
        public static final TagKey<Biome> IS_COLD = tag("is_cold");
        public static final TagKey<Biome> IS_COLD_OVERWORLD = tag("is_cold/overworld");
        public static final TagKey<Biome> IS_COLD_NETHER = tag("is_cold/nether");
        public static final TagKey<Biome> IS_COLD_END = tag("is_cold/end");

        /**
         * If a biome has trees but spawn infrequently like a Savanna or Sparse Jungle, then the biome is considered having sparse vegetation. It does NOT mean no trees.
         */
        public static final TagKey<Biome> IS_SPARSE_VEGETATION = tag("is_sparse_vegetation");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION_OVERWORLD = tag("is_sparse_vegetation/overworld");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION_NETHER = tag("is_sparse_vegetation/nether");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION_END = tag("is_sparse_vegetation/end");
        /**
         * If a biome has more vegetation than a regular Forest biome, then it is considered having dense vegetation.
         * This is more subjective so simply do your best with classifying your biomes.
         */
        public static final TagKey<Biome> IS_DENSE_VEGETATION = tag("is_dense_vegetation");
        public static final TagKey<Biome> IS_DENSE_VEGETATION_OVERWORLD = tag("is_dense_vegetation/overworld");
        public static final TagKey<Biome> IS_DENSE_VEGETATION_NETHER = tag("is_dense_vegetation/nether");
        public static final TagKey<Biome> IS_DENSE_VEGETATION_END = tag("is_dense_vegetation/end");

        public static final TagKey<Biome> IS_WET = tag("is_wet");
        public static final TagKey<Biome> IS_WET_OVERWORLD = tag("is_wet/overworld");
        public static final TagKey<Biome> IS_WET_NETHER = tag("is_wet/nether");
        public static final TagKey<Biome> IS_WET_END = tag("is_wet/end");
        public static final TagKey<Biome> IS_DRY = tag("is_dry");
        public static final TagKey<Biome> IS_DRY_OVERWORLD = tag("is_dry/overworld");
        public static final TagKey<Biome> IS_DRY_NETHER = tag("is_dry/nether");
        public static final TagKey<Biome> IS_DRY_END = tag("is_dry/end");

        /**
         * Biomes that are primarily composed of a specific wood type.
         * For example, normal Forest biomes are mostly Oak Trees with a few Birch Trees. This biome would be in `c:primary_wood_type/oak` tag due to Oak dominance.
         * For biomes that are composed of multiple wood types but in equal proportions, put the biome into multiple wood type tags. Biomes with very few trees like Plains are skipped.
         *
         * <p>
         * If a mod introduces a new wood type, create a new tag under the `c:primary_wood_type/` folder.
         * Multiple mods introducing the same new wood type will both be sticking their biomes in same tag.
         * For example, two mods providing Willow wood types would add their biomes to `c:primary_wood_type/willow` as it is the same kind of wood, despite different blocks.
         */
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE = tag("primary_wood_type");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_OAK = tag("primary_wood_type/oak");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_BIRCH = tag("primary_wood_type/birch");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_SPRUCE = tag("primary_wood_type/spruce");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_JUNGLE = tag("primary_wood_type/jungle");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_ACACIA = tag("primary_wood_type/acacia");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_DARK_OAK = tag("primary_wood_type/dark_oak");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_POPLAR = tag("primary_wood_type/poplar");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_MANGROVE = tag("primary_wood_type/mangrove");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_CHERRY = tag("primary_wood_type/cherry");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_PALE_OAK = tag("primary_wood_type/pale_oak");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_BAMBOO = tag("primary_wood_type/bamboo");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_CRIMSON = tag("primary_wood_type/crimson");
        public static final TagKey<Biome> PRIMARY_WOOD_TYPE_WARPED = tag("primary_wood_type/warped");

        /**
         * Biomes that spawn in the Overworld.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_OVERWORLD}
         * <p>
         * NOTE: If you do not add to the vanilla Overworld tag, be sure to add to
         * {@link net.minecraft.tags.BiomeTags#HAS_STRONGHOLD} so some Strongholds do not go missing.)
         */
        public static final TagKey<Biome> IS_OVERWORLD = tag("is_overworld");

        /**
         * Biomes whose trees are a kind of Conifer-like tree.
         * May not necessarily be a Spruce wood type.
         */
        public static final TagKey<Biome> IS_CONIFEROUS_TREE = tag("is_tree/coniferous");
        /**
         * Biomes whose trees are a kind of Savanna-like tree.
         * May not necessarily be a Savanna wood type.
         */
        public static final TagKey<Biome> IS_SAVANNA_TREE = tag("is_tree/savanna");
        /**
         * Biomes whose trees are a kind of Jungle-like tree.
         * May not necessarily be a Jungle wood type.
         */
        public static final TagKey<Biome> IS_JUNGLE_TREE = tag("is_tree/jungle");
        /**
         * Biomes whose trees are a kind of Deciduous-like tree.
         * May not necessarily be an Oak or Birch wood type.
         */
        public static final TagKey<Biome> IS_DECIDUOUS_TREE = tag("is_tree/deciduous");

        /**
         * Biomes that spawn as part of giant mountains.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_MOUNTAIN})
         */
        public static final TagKey<Biome> IS_MOUNTAIN = tag("is_mountain");
        public static final TagKey<Biome> IS_MOUNTAIN_PEAK = tag("is_mountain/peak");
        public static final TagKey<Biome> IS_MOUNTAIN_SLOPE = tag("is_mountain/slope");

        /**
         * For temperate or warmer plains-like biomes.
         * For snowy plains-like biomes, see {@link #IS_SNOWY_PLAINS}.
         */
        public static final TagKey<Biome> IS_PLAINS = tag("is_plains");
        /**
         * For snowy plains-like biomes.
         * For warmer plains-like biomes, see {@link #IS_PLAINS}.
         */
        public static final TagKey<Biome> IS_SNOWY_PLAINS = tag("is_snowy_plains");
        /**
         * Biomes densely populated with deciduous trees.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_FOREST})
         */
        public static final TagKey<Biome> IS_FOREST = tag("is_forest");
        /**
         * For biomes that are a variant of Birch Forest (has mostly birch trees)
         */
        public static final TagKey<Biome> IS_BIRCH_FOREST = tag("is_birch_forest");
        /**
         * For biomes that are a variant of Dark Forest. (Has roofed trees that are reminiscent of Dark Forest's style)
         * Pale Gardens is included in this tag because according to Mojang's blog post, they state it is a variation of the Dark Forest biome.
         * <a href="https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-4#pale_garden:~:text=The%20Pale%20Garden%20is%20a%20biome%20variation%20of%20Dark%20Forest">...</a>.
         */
        public static final TagKey<Biome> IS_DARK_FOREST = tag("is_dark_forest");
        /**
         * For biomes that are a variant of Flower Forest (Is very dense in variety of flowers)
         */
        public static final TagKey<Biome> IS_FLOWER_FOREST = tag("is_flower_forest");
        /**
         * Biomes that spawn as a taiga.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_TAIGA})
         */
        public static final TagKey<Biome> IS_TAIGA = tag("is_taiga");
        /**
         * For biomes that are an "old growth" variant of a regular biome.
         * Usually this includes taller or different tree styles as if the biome is older.
         */
        public static final TagKey<Biome> IS_OLD_GROWTH = tag("is_old_growth");
        /**
         * Biomes that spawn as a hills biome. (Previously was called Extreme Hills biome in past)
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_HILL})
         */
        public static final TagKey<Biome> IS_HILL = tag("is_hill");
        /**
         * For biomes that are a "windswept" variant of a regular biome.
         * Usually these biomes includes fewer trees than normal and more exposed stone on hilly terrain.
         */
        public static final TagKey<Biome> IS_WINDSWEPT = tag("is_windswept");
        /**
         * Biomes that spawn as a jungle.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_JUNGLE})
         */
        public static final TagKey<Biome> IS_JUNGLE = tag("is_jungle");
        /**
         * Biomes that spawn as a savanna.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_SAVANNA})
         */
        public static final TagKey<Biome> IS_SAVANNA = tag("is_savanna");
        /**
         * For biomes that are considered a swamp such as Swamp or Mangrove Swamp.
         */
        public static final TagKey<Biome> IS_SWAMP = tag("is_swamp");
        /**
         * For biomes that are considered a regular desert.
         * Badlands have their own tag to better separate them from this tag.
         */
        public static final TagKey<Biome> IS_DESERT = tag("is_desert");
        /**
         * Biomes that spawn as a badlands.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_BADLANDS})
         */
        public static final TagKey<Biome> IS_BADLANDS = tag("is_badlands");
        /**
         * Non-stony biomes that are dedicated to spawning on the shoreline of a body of water.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_BEACH})
         */
        public static final TagKey<Biome> IS_BEACH = tag("is_beach");
        /**
         * Stony biomes that are dedicated to spawning on the shoreline of a body of water.
         */
        public static final TagKey<Biome> IS_STONY_SHORES = tag("is_stony_shores");
        /**
         * For biomes that spawn primarily mushrooms.
         */
        public static final TagKey<Biome> IS_MUSHROOM = tag("is_mushroom");

        /**
         * Biomes that spawn as a river.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_RIVER})
         */
        public static final TagKey<Biome> IS_RIVER = tag("is_river");
        /**
         * Biomes that spawn as part of the world's oceans.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_OCEAN})
         */
        public static final TagKey<Biome> IS_OCEAN = tag("is_ocean");
        /**
         * Biomes that spawn as part of the world's oceans that have low depth.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_DEEP_OCEAN})
         */
        public static final TagKey<Biome> IS_DEEP_OCEAN = tag("is_deep_ocean");
        /**
         * Biomes that spawn as part of the world's oceans that have shallow depth.
         */
        public static final TagKey<Biome> IS_SHALLOW_OCEAN = tag("is_shallow_ocean");

        /**
         * Biomes that spawn primarily underground. (Not necessarily always a cave)
         */
        public static final TagKey<Biome> IS_UNDERGROUND = tag("is_underground");
        /**
         * Biomes dedicated to decorating caves such as Lush Caves or Dripstone Caves.
         */
        public static final TagKey<Biome> IS_CAVE = tag("is_cave");

        /**
         * Biomes whose flora primarily consists of vibrant thick vegetation and pools of water. Think of Lush Caves as an example.
         */
        public static final TagKey<Biome> IS_LUSH = tag("is_lush");
        /**
         * Biomes whose theme revolves around magic. Like a forest full of fairies or plants of magical abilities.
         */
        public static final TagKey<Biome> IS_MAGICAL = tag("is_magical");
        /**
         * Intended for biomes that spawns infrequently and can be difficult to find.
         */
        public static final TagKey<Biome> IS_RARE = tag("is_rare");
        /**
         * Biomes that spawn as a flat-topped hill often.
         */
        public static final TagKey<Biome> IS_PLATEAU = tag("is_plateau");
        /**
         * For biomes that are intended to be creepy or scary. For example, see Deep Dark biome or Dark Forest biome.
         */
        public static final TagKey<Biome> IS_SPOOKY = tag("is_spooky");
        /**
         * Biomes that lack any natural life or vegetation.
         * (Example, land destroyed and sterilized by nuclear weapons)
         */
        public static final TagKey<Biome> IS_WASTELAND = tag("is_wasteland");
        /**
         * Biomes whose flora primarily consists of dead or decaying vegetation.
         */
        public static final TagKey<Biome> IS_DEAD = tag("is_dead");
        /**
         * Biomes with a large amount of flowers.
         */
        public static final TagKey<Biome> IS_FLORAL = tag("is_floral");
        /**
         * Biomes that are able to spawn sand-based blocks on the surface.
         */
        public static final TagKey<Biome> IS_SANDY = tag("is_sandy");
        /**
         * For biomes that contains lots of naturally spawned snow.
         * For biomes where lot of ice is present, see {@link #IS_ICY}.
         * Biome with lots of both snow and ice may be in both tags.
         */
        public static final TagKey<Biome> IS_SNOWY = tag("is_snowy");
        /**
         * For land biomes where ice naturally spawns.
         * For biomes where snow alone spawns, see {@link #IS_SNOWY}.
         */
        public static final TagKey<Biome> IS_ICY = tag("is_icy");
        /**
         * Biomes consisting primarily of water.
         */
        public static final TagKey<Biome> IS_AQUATIC = tag("is_aquatic");
        /**
         * For water biomes where ice naturally spawns.
         * For biomes where snow alone spawns, see {@link #IS_SNOWY}.
         */
        public static final TagKey<Biome> IS_AQUATIC_ICY = tag("is_aquatic_icy");

        /**
         * Biomes that spawn in the Nether.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_NETHER})
         */
        public static final TagKey<Biome> IS_NETHER = tag("is_nether");
        public static final TagKey<Biome> IS_NETHER_FOREST = tag("is_nether_forest");

        /**
         * Biomes that spawn in the End.
         * (This is for people who want to tag their biomes without getting
         * side effects from {@link net.minecraft.tags.BiomeTags#IS_END})
         */
        public static final TagKey<Biome> IS_END = tag("is_end");
        /**
         * Biomes that spawn as part of the large islands outside the center island in The End dimension.
         */
        public static final TagKey<Biome> IS_OUTER_END_ISLAND = tag("is_outer_end_island");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("c", name));
        }
    }

    public static class Structures {
        /**
         * Structures that should not show up on minimaps or world map views from mods/sites.
         * No effect on vanilla map items.
         */
        public static final TagKey<Structure> HIDDEN_FROM_DISPLAYERS = tag("hidden_from_displayers");

        /**
         * Structures that should not be locatable/selectable by modded structure-locating items or abilities.
         * No effect on vanilla map items.
         */
        public static final TagKey<Structure> HIDDEN_FROM_LOCATOR_SELECTION = tag("hidden_from_locator_selection");

        private static TagKey<Structure> tag(String name) {
            return TagKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath("c", name));
        }
    }

    public static class DamageTypes {
        /**
         * Damage types representing magic damage.
         */
        public static final TagKey<DamageType> IS_MAGIC = neoforgeTag("is_magic");

        /**
         * Damage types representing poison damage.
         */
        public static final TagKey<DamageType> IS_POISON = neoforgeTag("is_poison");

        /**
         * Damage types representing damage that can be attributed to withering or the wither.
         */
        public static final TagKey<DamageType> IS_WITHER = neoforgeTag("is_wither");

        /**
         * Damage types representing environmental damage, such as fire, lava, magma, cactus, lightning, etc.
         */
        public static final TagKey<DamageType> IS_ENVIRONMENT = neoforgeTag("is_environment");

        /**
         * Damage types representing physical damage.<br>
         * These are types that do not fit other #is_x tags (except #is_fall)
         * and would meet the general definition of physical damage.
         */
        public static final TagKey<DamageType> IS_PHYSICAL = neoforgeTag("is_physical");

        /**
         * Damage types representing damage from commands or other non-gameplay sources.<br>
         * Damage from these types should not be reduced, and bypasses invulnerability.
         */
        public static final TagKey<DamageType> IS_TECHNICAL = neoforgeTag("is_technical");

        /**
         * Damage types that will not cause the red flashing effect.<br>
         * This tag is empty by default.
         *
         * @see GameRenderer#bobHurt
         */
        public static final TagKey<DamageType> NO_FLINCH = neoforgeTag("no_flinch");

        private static TagKey<DamageType> neoforgeTag(String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("neoforge", name));
        }
    }

    public static class WorldClocks {
        /// [World Clocks][WorldClock] are disallowed from being paused via the [TimeCommand].
        public static final TagKey<WorldClock> IGNORES_PAUSE_COMMAND = neoforgeTag("ignores/pause_command");

        /// [World Clocks][WorldClock] with this tag are exempt from pausing when [GameRules#ADVANCE_TIME] is disabled.
        public static final TagKey<WorldClock> IGNORES_ADVANCE_TIME_RULE = neoforgeTag("ignores/advance_time_rule");

        private static TagKey<WorldClock> neoforgeTag(String name) {
            return TagKey.create(Registries.WORLD_CLOCK, Identifier.fromNamespaceAndPath(NeoForgeMod.MOD_ID, name));
        }
    }

    /**
     * Use this to get a TagKey's translation key safely on any side.
     *
     * @return the translation key for a TagKey.
     */
    public static String getTagTranslationKey(TagKey<?> tagKey) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("tag.");

        Identifier registryIdentifier = tagKey.registry().identifier();
        Identifier tagIdentifier = tagKey.location();

        stringBuilder.append(registryIdentifier.toShortLanguageKey().replace("/", "."))
                .append(".")
                .append(tagIdentifier.getNamespace())
                .append(".")
                .append(tagIdentifier.getPath().replace("/", "."));

        return stringBuilder.toString();
    }
}
