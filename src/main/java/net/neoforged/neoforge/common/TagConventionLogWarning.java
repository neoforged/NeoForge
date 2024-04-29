/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class TagConventionLogWarning {
    private TagConventionLogWarning() {}

    protected enum LogWarningMode {
        SILENCED,
        DEV_SHORT,
        DEV_VERBOSE,
        PROD_SHORT,
        PROD_VERBOSE
    }

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Old `forge` tags that we migrated to a new tag under a new convention.
     * May also contain commonly used `forge` tags that are not following convention.
     */
    private static final Map<TagKey<?>, TagKey<?>> LEGACY_FORGE_TAGS = Map.<TagKey<?>, TagKey<?>>ofEntries(
            createMapEntry(Registries.BLOCK, "enderman_place_on_blacklist", Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST),
            createMapEntry(Registries.BLOCK, "needs_wood_tool", Tags.Blocks.NEEDS_WOOD_TOOL),
            createMapEntry(Registries.BLOCK, "needs_gold_tool", Tags.Blocks.NEEDS_GOLD_TOOL),
            createMapEntry(Registries.BLOCK, "needs_netherite_tool", Tags.Blocks.NEEDS_NETHERITE_TOOL),

            createMapEntry(Registries.BLOCK, "barrels", Tags.Blocks.BARRELS),
            createMapEntry(Registries.BLOCK, "barrels/wooden", Tags.Blocks.BARRELS_WOODEN),
            createMapEntry(Registries.BLOCK, "bookshelves", Tags.Blocks.BOOKSHELVES),
            createMapEntry(Registries.BLOCK, "chests", Tags.Blocks.CHESTS),
            createMapEntry(Registries.BLOCK, "chests/ender", Tags.Blocks.CHESTS_ENDER),
            createMapEntry(Registries.BLOCK, "chests/trapped", Tags.Blocks.CHESTS_TRAPPED),
            createMapEntry(Registries.BLOCK, "chests/wooden", Tags.Blocks.CHESTS_WOODEN),
            createMapEntry(Registries.BLOCK, "cobblestone", Tags.Blocks.COBBLESTONES),
            createMapEntry(Registries.BLOCK, "cobblestone/normal", Tags.Blocks.COBBLESTONES_NORMAL),
            createMapEntry(Registries.BLOCK, "cobblestone/infested", Tags.Blocks.COBBLESTONES_INFESTED),
            createMapEntry(Registries.BLOCK, "cobblestone/mossy", Tags.Blocks.COBBLESTONES_MOSSY),
            createMapEntry(Registries.BLOCK, "cobblestone/deepslate", Tags.Blocks.COBBLESTONES_DEEPSLATE),
            createMapEntry(Registries.BLOCK, "crafting_table", Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createMapEntry(Registries.BLOCK, "crafting_tables", Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createMapEntry(Registries.BLOCK, "workbench", Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createMapEntry(Registries.BLOCK, "workbenches", Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createMapEntry(Registries.BLOCK, "end_stones", Tags.Blocks.END_STONES),
            createMapEntry(Registries.BLOCK, "fence_gates", Tags.Blocks.FENCE_GATES),
            createMapEntry(Registries.BLOCK, "fence_gates/wooden", Tags.Blocks.FENCE_GATES_WOODEN),
            createMapEntry(Registries.BLOCK, "fences", Tags.Blocks.FENCES),
            createMapEntry(Registries.BLOCK, "fences/nether_brick", Tags.Blocks.FENCES_NETHER_BRICK),
            createMapEntry(Registries.BLOCK, "fences/wooden", Tags.Blocks.FENCES_WOODEN),
            createMapEntry(Registries.BLOCK, "furnace", Tags.Blocks.PLAYER_WORKSTATIONS_FURNACES),
            createMapEntry(Registries.BLOCK, "furnaces", Tags.Blocks.PLAYER_WORKSTATIONS_FURNACES),
            createMapEntry(Registries.BLOCK, "glass", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/black", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/blue", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/brown", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/colorless", Tags.Blocks.GLASS_BLOCKS_COLORLESS),
            createMapEntry(Registries.BLOCK, "glass/cyan", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/gray", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/green", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/light_blue", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/light_gray", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/lime", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/magenta", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/orange", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/pink", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/purple", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/red", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/silica", Tags.Blocks.GLASS_BLOCKS_CHEAP),
            createMapEntry(Registries.BLOCK, "glass/tinted", Tags.Blocks.GLASS_BLOCKS_TINTED),
            createMapEntry(Registries.BLOCK, "glass/white", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass/yellow", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "glass_panes", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/black", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/blue", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/brown", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/colorless", Tags.Blocks.GLASS_PANES_COLORLESS),
            createMapEntry(Registries.BLOCK, "glass_panes/cyan", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/gray", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/green", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/light_blue", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/light_gray", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/lime", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/magenta", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/orange", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/pink", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/purple", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/red", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/white", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "glass_panes/yellow", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "gravel", Tags.Blocks.GRAVELS),
            createMapEntry(Registries.BLOCK, "heads", Tags.Blocks.SKULLS),
            createMapEntry(Registries.BLOCK, "skulls", Tags.Blocks.SKULLS),
            createMapEntry(Registries.BLOCK, "netherrack", Tags.Blocks.NETHERRACKS),
            createMapEntry(Registries.BLOCK, "obsidian", Tags.Blocks.OBSIDIANS),
            createMapEntry(Registries.BLOCK, "ore_bearing_ground/deepslate", Tags.Blocks.ORE_BEARING_GROUND_DEEPSLATE),
            createMapEntry(Registries.BLOCK, "ore_bearing_ground/netherrack", Tags.Blocks.ORE_BEARING_GROUND_NETHERRACK),
            createMapEntry(Registries.BLOCK, "ore_bearing_ground/stone", Tags.Blocks.ORE_BEARING_GROUND_STONE),
            createMapEntry(Registries.BLOCK, "ore_rates/dense", Tags.Blocks.ORE_RATES_DENSE),
            createMapEntry(Registries.BLOCK, "ore_rates/singular", Tags.Blocks.ORE_RATES_SINGULAR),
            createMapEntry(Registries.BLOCK, "ore_rates/sparse", Tags.Blocks.ORE_RATES_SPARSE),
            createMapEntry(Registries.BLOCK, "ores", Tags.Blocks.ORES),
            createMapEntry(Registries.BLOCK, "ores/coal", Tags.Blocks.ORES_COAL),
            createMapEntry(Registries.BLOCK, "ores/copper", Tags.Blocks.ORES_COPPER),
            createMapEntry(Registries.BLOCK, "ores/diamond", Tags.Blocks.ORES_DIAMOND),
            createMapEntry(Registries.BLOCK, "ores/emerald", Tags.Blocks.ORES_EMERALD),
            createMapEntry(Registries.BLOCK, "ores/gold", Tags.Blocks.ORES_GOLD),
            createMapEntry(Registries.BLOCK, "ores/iron", Tags.Blocks.ORES_IRON),
            createMapEntry(Registries.BLOCK, "ores/lapis", Tags.Blocks.ORES_LAPIS),
            createMapEntry(Registries.BLOCK, "ores/netherite_scrap", Tags.Blocks.ORES_NETHERITE_SCRAP),
            createMapEntry(Registries.BLOCK, "ores/quartz", Tags.Blocks.ORES_QUARTZ),
            createMapEntry(Registries.BLOCK, "ores/redstone", Tags.Blocks.ORES_REDSTONE),
            createMapEntry(Registries.BLOCK, "ores_in_ground/deepslate", Tags.Blocks.ORES_IN_GROUND_DEEPSLATE),
            createMapEntry(Registries.BLOCK, "ores_in_ground/netherrack", Tags.Blocks.ORES_IN_GROUND_NETHERRACK),
            createMapEntry(Registries.BLOCK, "ores_in_ground/stone", Tags.Blocks.ORES_IN_GROUND_STONE),
            createMapEntry(Registries.BLOCK, "sand", Tags.Blocks.SANDS),
            createMapEntry(Registries.BLOCK, "sand/colorless", Tags.Blocks.SANDS_COLORLESS),
            createMapEntry(Registries.BLOCK, "sand/red", Tags.Blocks.SANDS_RED),
            createMapEntry(Registries.BLOCK, "sandstone", Tags.Blocks.SANDSTONE_BLOCKS),
            createMapEntry(Registries.BLOCK, "stained_glass", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "stained_glass_panes", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "stone", Tags.Blocks.STONES),
            createMapEntry(Registries.BLOCK, "storage_blocks", Tags.Blocks.STORAGE_BLOCKS),
            createMapEntry(Registries.BLOCK, "storage_blocks/amethyst", "storage_blocks/amethyst"),
            createMapEntry(Registries.BLOCK, "storage_blocks/coal", Tags.Blocks.STORAGE_BLOCKS_COAL),
            createMapEntry(Registries.BLOCK, "storage_blocks/copper", Tags.Blocks.STORAGE_BLOCKS_COPPER),
            createMapEntry(Registries.BLOCK, "storage_blocks/diamond", Tags.Blocks.STORAGE_BLOCKS_DIAMOND),
            createMapEntry(Registries.BLOCK, "storage_blocks/emerald", Tags.Blocks.STORAGE_BLOCKS_EMERALD),
            createMapEntry(Registries.BLOCK, "storage_blocks/gold", Tags.Blocks.STORAGE_BLOCKS_GOLD),
            createMapEntry(Registries.BLOCK, "storage_blocks/iron", Tags.Blocks.STORAGE_BLOCKS_IRON),
            createMapEntry(Registries.BLOCK, "storage_blocks/lapis", Tags.Blocks.STORAGE_BLOCKS_LAPIS),
            createMapEntry(Registries.BLOCK, "storage_blocks/netherite", Tags.Blocks.STORAGE_BLOCKS_NETHERITE),
            createMapEntry(Registries.BLOCK, "storage_blocks/quartz", "storage_blocks/quartz"),
            createMapEntry(Registries.BLOCK, "storage_blocks/raw_copper", Tags.Blocks.STORAGE_BLOCKS_RAW_COPPER),
            createMapEntry(Registries.BLOCK, "storage_blocks/raw_gold", Tags.Blocks.STORAGE_BLOCKS_RAW_GOLD),
            createMapEntry(Registries.BLOCK, "storage_blocks/raw_iron", Tags.Blocks.STORAGE_BLOCKS_RAW_IRON),
            createMapEntry(Registries.BLOCK, "storage_blocks/redstone", Tags.Blocks.STORAGE_BLOCKS_REDSTONE),

            createMapEntry(Registries.BLOCK, "relocation_not_supported", Tags.Blocks.RELOCATION_NOT_SUPPORTED),
            createMapEntry(Registries.BLOCK, "immovable", Tags.Blocks.RELOCATION_NOT_SUPPORTED),
            createMapEntry(Registries.BLOCK_PREDICATE_TYPE, "relocation_not_supported", Tags.Blocks.RELOCATION_NOT_SUPPORTED),
            createMapEntry(Registries.BLOCK_PREDICATE_TYPE, "immovable", Tags.Blocks.RELOCATION_NOT_SUPPORTED),

            createMapEntry(Registries.ENTITY_TYPE, "bosses", Tags.EntityTypes.BOSSES),

            createMapEntry(Registries.ITEM, "barrels", Tags.Items.BARRELS),
            createMapEntry(Registries.ITEM, "barrels/wooden", Tags.Items.BARRELS_WOODEN),
            createMapEntry(Registries.ITEM, "bones", Tags.Items.BONES),
            createMapEntry(Registries.ITEM, "bookshelves", Tags.Items.BOOKSHELVES),
            createMapEntry(Registries.ITEM, "bucket", Tags.Items.BUCKETS),
            createMapEntry(Registries.ITEM, "chests", Tags.Items.CHESTS),
            createMapEntry(Registries.ITEM, "chests/ender", Tags.Items.CHESTS_ENDER),
            createMapEntry(Registries.ITEM, "chests/trapped", Tags.Items.CHESTS_TRAPPED),
            createMapEntry(Registries.ITEM, "chests/wooden", Tags.Items.CHESTS_WOODEN),
            createMapEntry(Registries.ITEM, "cobblestone", Tags.Items.COBBLESTONES),
            createMapEntry(Registries.ITEM, "cobblestone/normal", Tags.Items.COBBLESTONES_NORMAL),
            createMapEntry(Registries.ITEM, "cobblestone/infested", Tags.Items.COBBLESTONES_INFESTED),
            createMapEntry(Registries.ITEM, "cobblestone/mossy", Tags.Items.COBBLESTONES_MOSSY),
            createMapEntry(Registries.ITEM, "cobblestone/deepslate", Tags.Items.COBBLESTONES_DEEPSLATE),
            createMapEntry(Registries.ITEM, "crafting_table", Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createMapEntry(Registries.ITEM, "crafting_tables", Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createMapEntry(Registries.ITEM, "workbench", Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createMapEntry(Registries.ITEM, "workbenches", Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createMapEntry(Registries.ITEM, "crops", Tags.Items.CROPS),
            createMapEntry(Registries.ITEM, "crops/beetroot", Tags.Items.CROPS_BEETROOT),
            createMapEntry(Registries.ITEM, "crops/carrot", Tags.Items.CROPS_CARROT),
            createMapEntry(Registries.ITEM, "crops/nether_wart", Tags.Items.CROPS_NETHER_WART),
            createMapEntry(Registries.ITEM, "crops/potato", Tags.Items.CROPS_POTATO),
            createMapEntry(Registries.ITEM, "crops/wheat", Tags.Items.CROPS_WHEAT),
            createMapEntry(Registries.ITEM, "dusts", Tags.Items.DUSTS),
            createMapEntry(Registries.ITEM, "dusts/redstone", Tags.Items.DUSTS_REDSTONE),
            createMapEntry(Registries.ITEM, "dusts/glowstone", Tags.Items.DUSTS_GLOWSTONE),
            createMapEntry(Registries.ITEM, "dyes", Tags.Items.DYES),
            createMapEntry(Registries.ITEM, "dyes/black", Tags.Items.DYES_BLACK),
            createMapEntry(Registries.ITEM, "dyes/red", Tags.Items.DYES_RED),
            createMapEntry(Registries.ITEM, "dyes/green", Tags.Items.DYES_GREEN),
            createMapEntry(Registries.ITEM, "dyes/brown", Tags.Items.DYES_BROWN),
            createMapEntry(Registries.ITEM, "dyes/blue", Tags.Items.DYES_BLUE),
            createMapEntry(Registries.ITEM, "dyes/purple", Tags.Items.DYES_PURPLE),
            createMapEntry(Registries.ITEM, "dyes/cyan", Tags.Items.DYES_CYAN),
            createMapEntry(Registries.ITEM, "dyes/light_gray", Tags.Items.DYES_LIGHT_GRAY),
            createMapEntry(Registries.ITEM, "dyes/gray", Tags.Items.DYES_GRAY),
            createMapEntry(Registries.ITEM, "dyes/pink", Tags.Items.DYES_PINK),
            createMapEntry(Registries.ITEM, "dyes/lime", Tags.Items.DYES_LIME),
            createMapEntry(Registries.ITEM, "dyes/yellow", Tags.Items.DYES_YELLOW),
            createMapEntry(Registries.ITEM, "dyes/light_blue", Tags.Items.DYES_LIGHT_BLUE),
            createMapEntry(Registries.ITEM, "dyes/magenta", Tags.Items.DYES_MAGENTA),
            createMapEntry(Registries.ITEM, "dyes/orange", Tags.Items.DYES_ORANGE),
            createMapEntry(Registries.ITEM, "dyes/white", Tags.Items.DYES_WHITE),
            createMapEntry(Registries.ITEM, "eggs", Tags.Items.EGGS),
            createMapEntry(Registries.ITEM, "enchanting_fuels", Tags.Items.ENCHANTING_FUELS),
            createMapEntry(Registries.ITEM, "end_stones", Tags.Items.END_STONES),
            createMapEntry(Registries.ITEM, "ender_pearls", Tags.Items.ENDER_PEARLS),
            createMapEntry(Registries.ITEM, "feathers", Tags.Items.FEATHERS),
            createMapEntry(Registries.ITEM, "fence_gates", Tags.Items.FENCE_GATES),
            createMapEntry(Registries.ITEM, "fence_gates/wooden", Tags.Items.FENCE_GATES_WOODEN),
            createMapEntry(Registries.ITEM, "fences", Tags.Items.FENCES),
            createMapEntry(Registries.ITEM, "fences/nether_brick", Tags.Items.FENCES_NETHER_BRICK),
            createMapEntry(Registries.ITEM, "fences/wooden", Tags.Items.FENCES_WOODEN),
            createMapEntry(Registries.ITEM, "furnace", Tags.Items.PLAYER_WORKSTATIONS_FURNACES),
            createMapEntry(Registries.ITEM, "furnaces", Tags.Items.PLAYER_WORKSTATIONS_FURNACES),
            createMapEntry(Registries.ITEM, "gems", Tags.Items.GEMS),
            createMapEntry(Registries.ITEM, "gems/diamond", Tags.Items.GEMS_DIAMOND),
            createMapEntry(Registries.ITEM, "gems/emerald", Tags.Items.GEMS_EMERALD),
            createMapEntry(Registries.ITEM, "gems/amethyst", Tags.Items.GEMS_AMETHYST),
            createMapEntry(Registries.ITEM, "gems/lapis", Tags.Items.GEMS_LAPIS),
            createMapEntry(Registries.ITEM, "gems/prismarine", Tags.Items.GEMS_PRISMARINE),
            createMapEntry(Registries.ITEM, "gems/quartz", Tags.Items.GEMS_QUARTZ),
            createMapEntry(Registries.ITEM, "glass", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/black", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/blue", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/brown", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/colorless", Tags.Items.GLASS_BLOCKS_COLORLESS),
            createMapEntry(Registries.ITEM, "glass/cyan", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/gray", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/green", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/light_blue", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/light_gray", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/lime", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/magenta", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/orange", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/pink", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/purple", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/red", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/silica", Tags.Items.GLASS_BLOCKS_CHEAP),
            createMapEntry(Registries.ITEM, "glass/tinted", Tags.Items.GLASS_BLOCKS_TINTED),
            createMapEntry(Registries.ITEM, "glass/white", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass/yellow", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "glass_panes", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/black", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/blue", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/brown", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/colorless", Tags.Items.GLASS_PANES_COLORLESS),
            createMapEntry(Registries.ITEM, "glass_panes/cyan", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/gray", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/green", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/light_blue", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/light_gray", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/lime", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/magenta", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/orange", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/pink", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/purple", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/red", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/white", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "glass_panes/yellow", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "gravel", Tags.Items.GRAVELS),
            createMapEntry(Registries.ITEM, "gunpowder", Tags.Items.GUNPOWDERS),
            createMapEntry(Registries.ITEM, "ingots", Tags.Items.INGOTS),
            createMapEntry(Registries.ITEM, "ingots/brick", Tags.Items.BRICKS_NORMAL),
            createMapEntry(Registries.ITEM, "ingots/copper", Tags.Items.INGOTS_COPPER),
            createMapEntry(Registries.ITEM, "ingots/gold", Tags.Items.INGOTS_GOLD),
            createMapEntry(Registries.ITEM, "ingots/iron", Tags.Items.INGOTS_IRON),
            createMapEntry(Registries.ITEM, "ingots/netherite", Tags.Items.INGOTS_NETHERITE),
            createMapEntry(Registries.ITEM, "ingots/nether_brick", Tags.Items.BRICKS_NETHER),
            createMapEntry(Registries.ITEM, "leather", Tags.Items.LEATHERS),
            createMapEntry(Registries.ITEM, "mushrooms", Tags.Items.MUSHROOMS),
            createMapEntry(Registries.ITEM, "nether_stars", Tags.Items.NETHER_STARS),
            createMapEntry(Registries.ITEM, "netherrack", Tags.Items.NETHERRACKS),
            createMapEntry(Registries.ITEM, "nuggets", Tags.Items.NUGGETS),
            createMapEntry(Registries.ITEM, "nuggets/gold", Tags.Items.NUGGETS_GOLD),
            createMapEntry(Registries.ITEM, "nuggets/iron", Tags.Items.NUGGETS_IRON),
            createMapEntry(Registries.ITEM, "obsidian", Tags.Items.OBSIDIANS),
            createMapEntry(Registries.ITEM, "ore_bearing_ground/deepslate", Tags.Items.ORE_BEARING_GROUND_DEEPSLATE),
            createMapEntry(Registries.ITEM, "ore_bearing_ground/netherrack", Tags.Items.ORE_BEARING_GROUND_NETHERRACK),
            createMapEntry(Registries.ITEM, "ore_bearing_ground/stone", Tags.Items.ORE_BEARING_GROUND_STONE),
            createMapEntry(Registries.ITEM, "ore_rates/dense", Tags.Items.ORE_RATES_DENSE),
            createMapEntry(Registries.ITEM, "ore_rates/singular", Tags.Items.ORE_RATES_SINGULAR),
            createMapEntry(Registries.ITEM, "ore_rates/sparse", Tags.Items.ORE_RATES_SPARSE),
            createMapEntry(Registries.ITEM, "ores", Tags.Items.ORES),
            createMapEntry(Registries.ITEM, "ores/coal", Tags.Items.ORES_COAL),
            createMapEntry(Registries.ITEM, "ores/copper", Tags.Items.ORES_COPPER),
            createMapEntry(Registries.ITEM, "ores/diamond", Tags.Items.ORES_DIAMOND),
            createMapEntry(Registries.ITEM, "ores/emerald", Tags.Items.ORES_EMERALD),
            createMapEntry(Registries.ITEM, "ores/gold", Tags.Items.ORES_GOLD),
            createMapEntry(Registries.ITEM, "ores/iron", Tags.Items.ORES_IRON),
            createMapEntry(Registries.ITEM, "ores/lapis", Tags.Items.ORES_LAPIS),
            createMapEntry(Registries.ITEM, "ores/netherite_scrap", Tags.Items.ORES_NETHERITE_SCRAP),
            createMapEntry(Registries.ITEM, "ores/quartz", Tags.Items.ORES_QUARTZ),
            createMapEntry(Registries.ITEM, "ores/redstone", Tags.Items.ORES_REDSTONE),
            createMapEntry(Registries.ITEM, "ores_in_ground/deepslate", Tags.Items.ORES_IN_GROUND_DEEPSLATE),
            createMapEntry(Registries.ITEM, "ores_in_ground/netherrack", Tags.Items.ORES_IN_GROUND_NETHERRACK),
            createMapEntry(Registries.ITEM, "ores_in_ground/stone", Tags.Items.ORES_IN_GROUND_STONE),
            createMapEntry(Registries.ITEM, "raw_materials", Tags.Items.RAW_MATERIALS),
            createMapEntry(Registries.ITEM, "raw_materials/copper", Tags.Items.RAW_MATERIALS_COPPER),
            createMapEntry(Registries.ITEM, "raw_materials/gold", Tags.Items.RAW_MATERIALS_GOLD),
            createMapEntry(Registries.ITEM, "raw_materials/iron", Tags.Items.RAW_MATERIALS_IRON),
            createMapEntry(Registries.ITEM, "rods", Tags.Items.RODS),
            createMapEntry(Registries.ITEM, "rods/blaze", Tags.Items.RODS_BLAZE),
            createMapEntry(Registries.ITEM, "rods/wooden", Tags.Items.RODS_WOODEN),
            createMapEntry(Registries.ITEM, "rope", Tags.Items.ROPES),
            createMapEntry(Registries.ITEM, "sand", Tags.Items.SANDS),
            createMapEntry(Registries.ITEM, "sand/colorless", Tags.Items.SANDS_COLORLESS),
            createMapEntry(Registries.ITEM, "sand/red", Tags.Items.SANDS_RED),
            createMapEntry(Registries.ITEM, "sandstone", Tags.Items.SANDSTONE_BLOCKS),
            createMapEntry(Registries.ITEM, "seeds", Tags.Items.SEEDS),
            createMapEntry(Registries.ITEM, "seeds/beetroot", Tags.Items.SEEDS_BEETROOT),
            createMapEntry(Registries.ITEM, "seeds/melon", Tags.Items.SEEDS_MELON),
            createMapEntry(Registries.ITEM, "seeds/pumpkin", Tags.Items.SEEDS_PUMPKIN),
            createMapEntry(Registries.ITEM, "seeds/wheat", Tags.Items.SEEDS_WHEAT),
            createMapEntry(Registries.ITEM, "shears", Tags.Items.TOOLS_SHEARS),
            createMapEntry(Registries.ITEM, "slimeballs", Tags.Items.SLIMEBALLS),
            createMapEntry(Registries.ITEM, "stained_glass", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "stained_glass_panes", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "stone", Tags.Items.STONES),
            createMapEntry(Registries.ITEM, "storage_blocks", Tags.Items.STORAGE_BLOCKS),
            createMapEntry(Registries.ITEM, "storage_blocks/amethyst", "storage_blocks/amethyst"),
            createMapEntry(Registries.ITEM, "storage_blocks/coal", Tags.Items.STORAGE_BLOCKS_COAL),
            createMapEntry(Registries.ITEM, "storage_blocks/copper", Tags.Items.STORAGE_BLOCKS_COPPER),
            createMapEntry(Registries.ITEM, "storage_blocks/diamond", Tags.Items.STORAGE_BLOCKS_DIAMOND),
            createMapEntry(Registries.ITEM, "storage_blocks/emerald", Tags.Items.STORAGE_BLOCKS_EMERALD),
            createMapEntry(Registries.ITEM, "storage_blocks/gold", Tags.Items.STORAGE_BLOCKS_GOLD),
            createMapEntry(Registries.ITEM, "storage_blocks/iron", Tags.Items.STORAGE_BLOCKS_IRON),
            createMapEntry(Registries.ITEM, "storage_blocks/lapis", Tags.Items.STORAGE_BLOCKS_LAPIS),
            createMapEntry(Registries.ITEM, "storage_blocks/netherite", Tags.Items.STORAGE_BLOCKS_NETHERITE),
            createMapEntry(Registries.ITEM, "storage_blocks/quartz", "storage_blocks/quartz"),
            createMapEntry(Registries.ITEM, "storage_blocks/raw_copper", Tags.Items.STORAGE_BLOCKS_RAW_COPPER),
            createMapEntry(Registries.ITEM, "storage_blocks/raw_gold", Tags.Items.STORAGE_BLOCKS_RAW_GOLD),
            createMapEntry(Registries.ITEM, "storage_blocks/raw_iron", Tags.Items.STORAGE_BLOCKS_RAW_IRON),
            createMapEntry(Registries.ITEM, "storage_blocks/redstone", Tags.Items.STORAGE_BLOCKS_REDSTONE),
            createMapEntry(Registries.ITEM, "string", Tags.Items.STRINGS),
            createMapEntry(Registries.ITEM, "tools", Tags.Items.TOOLS),
            createMapEntry(Registries.ITEM, "tools/shields", Tags.Items.TOOLS_SHIELDS),
            createMapEntry(Registries.ITEM, "tools/bows", Tags.Items.TOOLS_BOWS),
            createMapEntry(Registries.ITEM, "tools/crossbows", Tags.Items.TOOLS_CROSSBOWS),
            createMapEntry(Registries.ITEM, "tools/fishing_rods", Tags.Items.TOOLS_FISHING_RODS),
            createMapEntry(Registries.ITEM, "tools/tridents", Tags.Items.TOOLS_SPEARS),
            createMapEntry(Registries.ITEM, "tools/brushes", Tags.Items.TOOLS_BRUSHES),
            createMapEntry(Registries.ITEM, "armors", Tags.Items.ARMORS),
            createMapEntry(Registries.ITEM, "armors/helmets", ItemTags.HEAD_ARMOR),
            createMapEntry(Registries.ITEM, "armors/chestplates", ItemTags.CHEST_ARMOR),
            createMapEntry(Registries.ITEM, "armors/leggings", ItemTags.LEG_ARMOR),
            createMapEntry(Registries.ITEM, "armors/boots", ItemTags.FOOT_ARMOR),
            createMapEntry(Registries.ITEM, "wrench", "tools/wrenches"),
            createMapEntry(Registries.ITEM, "wrenches", "tools/wrenches"),
            createMapEntry(Registries.ITEM, "tools/wrench", "tools/wrenches"),
            createMapEntry(Registries.ITEM, "tools/wrenches", "tools/wrenches"),
            createMapEntry(Registries.ITEM, "food", Tags.Items.FOODS),
            createMapEntry(Registries.ITEM, "foods", Tags.Items.FOODS),
            createMapEntry(Registries.ITEM, "fruit", Tags.Items.FOODS_FRUITS),
            createMapEntry(Registries.ITEM, "fruits", Tags.Items.FOODS_FRUITS),
            createMapEntry(Registries.ITEM, "vegetable", Tags.Items.FOODS_VEGETABLES),
            createMapEntry(Registries.ITEM, "vegetables", Tags.Items.FOODS_VEGETABLES),
            createMapEntry(Registries.ITEM, "berry", Tags.Items.FOODS_BERRIES),
            createMapEntry(Registries.ITEM, "berries", Tags.Items.FOODS_BERRIES),
            createMapEntry(Registries.ITEM, "bread", Tags.Items.FOODS_BREADS),
            createMapEntry(Registries.ITEM, "breads", Tags.Items.FOODS_BREADS),
            createMapEntry(Registries.ITEM, "cookie", Tags.Items.FOODS_COOKIES),
            createMapEntry(Registries.ITEM, "cookies", Tags.Items.FOODS_COOKIES),
            createMapEntry(Registries.ITEM, "raw_meat", Tags.Items.FOODS_RAW_MEATS),
            createMapEntry(Registries.ITEM, "raw_meats", Tags.Items.FOODS_RAW_MEATS),
            createMapEntry(Registries.ITEM, "raw_fish", Tags.Items.FOODS_RAW_FISHES),
            createMapEntry(Registries.ITEM, "raw_fishes", Tags.Items.FOODS_RAW_FISHES),
            createMapEntry(Registries.ITEM, "cooked_meat", Tags.Items.FOODS_COOKED_MEATS),
            createMapEntry(Registries.ITEM, "cooked_meats", Tags.Items.FOODS_COOKED_MEATS),
            createMapEntry(Registries.ITEM, "cooked_fish", Tags.Items.FOODS_COOKED_FISHES),
            createMapEntry(Registries.ITEM, "cooked_fishes", Tags.Items.FOODS_COOKED_FISHES),
            createMapEntry(Registries.ITEM, "soup", Tags.Items.FOODS_SOUPS),
            createMapEntry(Registries.ITEM, "soups", Tags.Items.FOODS_SOUPS),
            createMapEntry(Registries.ITEM, "stew", Tags.Items.FOODS_SOUPS),
            createMapEntry(Registries.ITEM, "stews", Tags.Items.FOODS_SOUPS),
            createMapEntry(Registries.ITEM, "candy", Tags.Items.FOODS_CANDIES),
            createMapEntry(Registries.ITEM, "candies", Tags.Items.FOODS_CANDIES),

            createMapEntry(Registries.FLUID, "water", Tags.Fluids.WATER),
            createMapEntry(Registries.FLUID, "lava", Tags.Fluids.LAVA),
            createMapEntry(Registries.FLUID, "milk", Tags.Fluids.MILK),
            createMapEntry(Registries.FLUID, "gaseous", Tags.Fluids.GASEOUS),
            createMapEntry(Registries.FLUID, "honey", Tags.Fluids.HONEY),
            createMapEntry(Registries.FLUID, "potion", Tags.Fluids.POTION),
            createMapEntry(Registries.FLUID, "plantoil", "plant_oil"),

            createMapEntry(Registries.BIOME, "is_hot", Tags.Biomes.IS_HOT),
            createMapEntry(Registries.BIOME, "is_hot/overworld", Tags.Biomes.IS_HOT_OVERWORLD),
            createMapEntry(Registries.BIOME, "is_hot/nether", Tags.Biomes.IS_HOT_NETHER),
            createMapEntry(Registries.BIOME, "is_hot/end", Tags.Biomes.IS_HOT_END),
            createMapEntry(Registries.BIOME, "is_cold", Tags.Biomes.IS_COLD),
            createMapEntry(Registries.BIOME, "is_cold/overworld", Tags.Biomes.IS_COLD_OVERWORLD),
            createMapEntry(Registries.BIOME, "is_cold/nether", Tags.Biomes.IS_COLD_NETHER),
            createMapEntry(Registries.BIOME, "is_cold/end", Tags.Biomes.IS_COLD_END),
            createMapEntry(Registries.BIOME, "is_sparse", Tags.Biomes.IS_SPARSE_VEGETATION),
            createMapEntry(Registries.BIOME, "is_sparse/overworld", Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD),
            createMapEntry(Registries.BIOME, "is_sparse/nether", Tags.Biomes.IS_SPARSE_VEGETATION_NETHER),
            createMapEntry(Registries.BIOME, "is_sparse/end", Tags.Biomes.IS_SPARSE_VEGETATION_END),
            createMapEntry(Registries.BIOME, "is_dense", Tags.Biomes.IS_DENSE_VEGETATION),
            createMapEntry(Registries.BIOME, "is_dense/overworld", Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD),
            createMapEntry(Registries.BIOME, "is_dense/nether", Tags.Biomes.IS_DENSE_VEGETATION_NETHER),
            createMapEntry(Registries.BIOME, "is_dense/end", Tags.Biomes.IS_DENSE_VEGETATION_END),
            createMapEntry(Registries.BIOME, "is_wet", Tags.Biomes.IS_WET),
            createMapEntry(Registries.BIOME, "is_wet/overworld", Tags.Biomes.IS_WET_OVERWORLD),
            createMapEntry(Registries.BIOME, "is_wet/nether", Tags.Biomes.IS_WET_NETHER),
            createMapEntry(Registries.BIOME, "is_wet/end", Tags.Biomes.IS_WET_END),
            createMapEntry(Registries.BIOME, "is_dry", Tags.Biomes.IS_DRY),
            createMapEntry(Registries.BIOME, "is_dry/overworld", Tags.Biomes.IS_DRY_OVERWORLD),
            createMapEntry(Registries.BIOME, "is_dry/nether", Tags.Biomes.IS_DRY_NETHER),
            createMapEntry(Registries.BIOME, "is_dry/end", Tags.Biomes.IS_DRY_END),
            createMapEntry(Registries.BIOME, "is_coniferous", Tags.Biomes.IS_CONIFEROUS_TREE),
            createMapEntry(Registries.BIOME, "is_savanna", Tags.Biomes.IS_SAVANNA_TREE),
            createMapEntry(Registries.BIOME, "is_jungle", Tags.Biomes.IS_JUNGLE_TREE),
            createMapEntry(Registries.BIOME, "is_deciduous", Tags.Biomes.IS_DECIDUOUS_TREE),
            createMapEntry(Registries.BIOME, "is_spooky", Tags.Biomes.IS_SPOOKY),
            createMapEntry(Registries.BIOME, "is_dead", Tags.Biomes.IS_DEAD),
            createMapEntry(Registries.BIOME, "is_lush", Tags.Biomes.IS_LUSH),
            createMapEntry(Registries.BIOME, "is_mushroom", Tags.Biomes.IS_MUSHROOM),
            createMapEntry(Registries.BIOME, "is_magical", Tags.Biomes.IS_MAGICAL),
            createMapEntry(Registries.BIOME, "is_rare", Tags.Biomes.IS_RARE),
            createMapEntry(Registries.BIOME, "is_plateau", Tags.Biomes.IS_PLATEAU),
            createMapEntry(Registries.BIOME, "is_modified", Tags.Biomes.IS_MODIFIED),
            createMapEntry(Registries.BIOME, "is_water", Tags.Biomes.IS_AQUATIC),
            createMapEntry(Registries.BIOME, "is_desert", Tags.Biomes.IS_DESERT),
            createMapEntry(Registries.BIOME, "is_plains", Tags.Biomes.IS_PLAINS),
            createMapEntry(Registries.BIOME, "is_swamp", Tags.Biomes.IS_SWAMP),
            createMapEntry(Registries.BIOME, "is_sandy", Tags.Biomes.IS_SANDY),
            createMapEntry(Registries.BIOME, "is_snowy", Tags.Biomes.IS_SNOWY),
            createMapEntry(Registries.BIOME, "is_wasteland", Tags.Biomes.IS_WASTELAND),
            createMapEntry(Registries.BIOME, "is_void", Tags.Biomes.IS_VOID),
            createMapEntry(Registries.BIOME, "is_underground", Tags.Biomes.IS_UNDERGROUND),
            createMapEntry(Registries.BIOME, "is_cave", Tags.Biomes.IS_CAVE),
            createMapEntry(Registries.BIOME, "is_peak", Tags.Biomes.IS_MOUNTAIN_PEAK),
            createMapEntry(Registries.BIOME, "is_slope", Tags.Biomes.IS_MOUNTAIN_SLOPE),
            createMapEntry(Registries.BIOME, "is_mountain", Tags.Biomes.IS_MOUNTAIN),
            createMapEntry(Registries.BIOME, "is_end", Tags.Biomes.IS_END),
            createMapEntry(Registries.BIOME, "is_nether", Tags.Biomes.IS_NETHER),
            createMapEntry(Registries.BIOME, "is_overworld", Tags.Biomes.IS_OVERWORLD),
            createMapEntry(Registries.BIOME, "no_default_monsters", Tags.Biomes.NO_DEFAULT_MONSTERS));

    /*package private*/
    static void init() {
        IEventBus forgeBus = NeoForge.EVENT_BUS;

        setupLegacyTagWarning(forgeBus);
    }

    // Remove in 1.22
    private static void setupLegacyTagWarning(IEventBus forgeBus) {
        // Log tags that are still using legacy 'forge' namespace
        forgeBus.addListener((ServerStartingEvent serverStartingEvent) -> {
            // We have to wait for server start to read the server config.
            LogWarningMode legacyTagWarningMode = NeoForgeConfig.COMMON.logLegacyTagWarnings.get();
            if (legacyTagWarningMode != LogWarningMode.SILENCED) {
                boolean isConfigSetToDev = legacyTagWarningMode == LogWarningMode.DEV_SHORT ||
                        legacyTagWarningMode == LogWarningMode.DEV_VERBOSE;

                if (!FMLLoader.isProduction() == isConfigSetToDev) {
                    List<TagKey<?>> legacyTags = new ObjectArrayList<>();
                    RegistryAccess.Frozen registryAccess = serverStartingEvent.getServer().registryAccess();

                    // We only care about vanilla registries
                    registryAccess.registries().forEach(registryEntry -> {
                        if (registryEntry.key().location().getNamespace().equals("minecraft")) {
                            registryEntry.value().getTagNames().forEach(tagKey -> {
                                // Grab tags under 'forge' namespace
                                if (LEGACY_FORGE_TAGS.containsKey(tagKey) || tagKey.location().getNamespace().equals("forge")) {
                                    legacyTags.add(tagKey);
                                }
                            });
                        }
                    });

                    if (!legacyTags.isEmpty()) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("""
                                \n   Dev warning - Legacy Tags detected. Please migrate your 'forge' namespace tags to the 'c' namespace! See net.neoforged.neoforge.common.Tags.java for all tags.
                                   NOTE: Many tags have been moved around or renamed. Some new ones were added so please review the new tags.
                                    And make sure you follow tag conventions for new tags! The convention is `c` with nouns generally being plural and adjectives being singular.
                                   You can disable this message in NeoForge's common config by setting `logLegacyTagWarnings` to "SILENCED" or see individual tags with "DEV_VERBOSE".
                                """);

                        // Print out all legacy tags when desired.
                        boolean isConfigSetToVerbose = legacyTagWarningMode == LogWarningMode.DEV_VERBOSE ||
                                legacyTagWarningMode == LogWarningMode.PROD_VERBOSE;

                        if (isConfigSetToVerbose) {
                            stringBuilder.append("\nLegacy tags:");
                            for (TagKey<?> tagKey : legacyTags) {
                                if (LEGACY_FORGE_TAGS.containsKey(tagKey)) {
                                    TagKey<?> replacementTagkey = LEGACY_FORGE_TAGS.get(tagKey);
                                    stringBuilder.append("\n     ").append(tagKey).append("  ->  ").append(replacementTagkey);
                                } else {
                                    stringBuilder.append("\n     ").append(tagKey).append("  ->  ").append("See similar `c` tags in NeoForge's Tags class");
                                }
                            }
                        }

                        LOGGER.warn(stringBuilder);
                    }
                }
            }
        });
    }

    private static <T> AbstractMap.SimpleEntry<TagKey<T>, TagKey<T>> createMapEntry(ResourceKey<Registry<T>> registryKey, String tagId1, String tagId2) {
        return new AbstractMap.SimpleEntry<>(createTagKey(registryKey, "forge", tagId1), createTagKey(registryKey, "c", tagId2));
    }

    private static <T, R> AbstractMap.SimpleEntry<TagKey<T>, TagKey<R>> createMapEntry(ResourceKey<Registry<T>> registryKey, String tagId1, TagKey<R> tag2) {
        return new AbstractMap.SimpleEntry<>(createTagKey(registryKey, "forge", tagId1), tag2);
    }

    private static <T> TagKey<T> createTagKey(ResourceKey<Registry<T>> registryKey, String namespace, String tagId) {
        return TagKey.create(registryKey, new ResourceLocation(namespace, tagId));
    }
}
