/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.AbstractMap;
import java.util.Arrays;
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
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class TagConventionLogWarning {
    private TagConventionLogWarning() {}

    public enum LogWarningMode {
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
    private static final Map<TagKey<?>, String> LEGACY_FORGE_TAGS = Map.<TagKey<?>, String>ofEntries(
            createForgeMapEntry(Registries.BLOCK, "enderman_place_on_blacklist", Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST),
            createForgeMapEntry(Registries.BLOCK, "needs_wood_tool", Tags.Blocks.NEEDS_WOOD_TOOL),
            createForgeMapEntry(Registries.BLOCK, "needs_gold_tool", Tags.Blocks.NEEDS_GOLD_TOOL),
            createForgeMapEntry(Registries.BLOCK, "needs_netherite_tool", Tags.Blocks.NEEDS_NETHERITE_TOOL),

            createForgeMapEntry(Registries.BLOCK, "barrels", Tags.Blocks.BARRELS),
            createForgeMapEntry(Registries.BLOCK, "barrels/wooden", Tags.Blocks.BARRELS_WOODEN),
            createForgeMapEntry(Registries.BLOCK, "bookshelves", Tags.Blocks.BOOKSHELVES),
            createForgeMapEntry(Registries.BLOCK, "chests", Tags.Blocks.CHESTS),
            createForgeMapEntry(Registries.BLOCK, "chests/ender", Tags.Blocks.CHESTS_ENDER),
            createForgeMapEntry(Registries.BLOCK, "chests/trapped", Tags.Blocks.CHESTS_TRAPPED),
            createForgeMapEntry(Registries.BLOCK, "chests/wooden", Tags.Blocks.CHESTS_WOODEN),
            createForgeMapEntry(Registries.BLOCK, "cobblestone", Tags.Blocks.COBBLESTONES),
            createForgeMapEntry(Registries.BLOCK, "cobblestone/normal", Tags.Blocks.COBBLESTONES_NORMAL),
            createForgeMapEntry(Registries.BLOCK, "cobblestone/infested", Tags.Blocks.COBBLESTONES_INFESTED),
            createForgeMapEntry(Registries.BLOCK, "cobblestone/mossy", Tags.Blocks.COBBLESTONES_MOSSY),
            createForgeMapEntry(Registries.BLOCK, "cobblestone/deepslate", Tags.Blocks.COBBLESTONES_DEEPSLATE),
            createForgeMapEntry(Registries.BLOCK, "crafting_table", Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createForgeMapEntry(Registries.BLOCK, "crafting_tables", Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createForgeMapEntry(Registries.BLOCK, "workbench", Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createForgeMapEntry(Registries.BLOCK, "workbenches", Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createForgeMapEntry(Registries.BLOCK, "end_stones", Tags.Blocks.END_STONES),
            createForgeMapEntry(Registries.BLOCK, "fence_gates", Tags.Blocks.FENCE_GATES),
            createForgeMapEntry(Registries.BLOCK, "fence_gates/wooden", Tags.Blocks.FENCE_GATES_WOODEN),
            createForgeMapEntry(Registries.BLOCK, "fences", Tags.Blocks.FENCES),
            createForgeMapEntry(Registries.BLOCK, "fences/nether_brick", Tags.Blocks.FENCES_NETHER_BRICK),
            createForgeMapEntry(Registries.BLOCK, "fences/wooden", Tags.Blocks.FENCES_WOODEN),
            createForgeMapEntry(Registries.BLOCK, "furnace", Tags.Blocks.PLAYER_WORKSTATIONS_FURNACES),
            createForgeMapEntry(Registries.BLOCK, "furnaces", Tags.Blocks.PLAYER_WORKSTATIONS_FURNACES),
            createForgeMapEntry(Registries.BLOCK, "glass", Tags.Blocks.GLASS_BLOCKS),
            createMapEntry(Registries.BLOCK, "forge", "glass/black", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_BLACK),
            createMapEntry(Registries.BLOCK, "forge", "glass/blue", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_BLUE),
            createMapEntry(Registries.BLOCK, "forge", "glass/brown", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_BROWN),
            createMapEntry(Registries.BLOCK, "forge", "glass/cyan", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_CYAN),
            createMapEntry(Registries.BLOCK, "forge", "glass/gray", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_GRAY),
            createMapEntry(Registries.BLOCK, "forge", "glass/green", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_GREEN),
            createMapEntry(Registries.BLOCK, "forge", "glass/light_blue", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_LIGHT_BLUE),
            createMapEntry(Registries.BLOCK, "forge", "glass/light_gray", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_LIGHT_GRAY),
            createMapEntry(Registries.BLOCK, "forge", "glass/lime", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_LIME),
            createMapEntry(Registries.BLOCK, "forge", "glass/magenta", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_MAGENTA),
            createMapEntry(Registries.BLOCK, "forge", "glass/orange", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_ORANGE),
            createMapEntry(Registries.BLOCK, "forge", "glass/pink", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_PINK),
            createMapEntry(Registries.BLOCK, "forge", "glass/purple", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_PURPLE),
            createMapEntry(Registries.BLOCK, "forge", "glass/red", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_RED),
            createMapEntry(Registries.BLOCK, "forge", "glass/white", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_WHITE),
            createMapEntry(Registries.BLOCK, "forge", "glass/yellow", Tags.Blocks.GLASS_BLOCKS, Tags.Blocks.DYED_YELLOW),
            createForgeMapEntry(Registries.BLOCK, "glass/colorless", Tags.Blocks.GLASS_BLOCKS_COLORLESS),
            createForgeMapEntry(Registries.BLOCK, "glass/silica", Tags.Blocks.GLASS_BLOCKS_CHEAP),
            createForgeMapEntry(Registries.BLOCK, "glass/tinted", Tags.Blocks.GLASS_BLOCKS_TINTED),
            createForgeMapEntry(Registries.BLOCK, "glass_panes", Tags.Blocks.GLASS_PANES),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/black", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_BLACK),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/blue", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_BLUE),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/brown", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_BROWN),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/cyan", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_CYAN),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/gray", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_GRAY),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/green", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_GREEN),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/light_blue", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_LIGHT_BLUE),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/light_gray", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_LIGHT_GRAY),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/lime", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_LIME),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/magenta", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_MAGENTA),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/orange", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_ORANGE),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/pink", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_PINK),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/purple", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_PURPLE),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/red", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_RED),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/white", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_WHITE),
            createMapEntry(Registries.BLOCK, "forge", "glass_panes/yellow", Tags.Blocks.GLASS_PANES, Tags.Blocks.DYED_YELLOW),
            createForgeMapEntry(Registries.BLOCK, "glass_panes/colorless", Tags.Blocks.GLASS_PANES_COLORLESS),
            createForgeMapEntry(Registries.BLOCK, "gravel", Tags.Blocks.GRAVELS),
            createMapEntry(Registries.BLOCK, "c", "gravel", Tags.Blocks.GRAVELS),
            createForgeMapEntry(Registries.BLOCK, "heads", Tags.Blocks.SKULLS),
            createForgeMapEntry(Registries.BLOCK, "skulls", Tags.Blocks.SKULLS),
            createForgeMapEntry(Registries.BLOCK, "netherrack", Tags.Blocks.NETHERRACKS),
            createMapEntry(Registries.BLOCK, "c", "netherrack", Tags.Blocks.NETHERRACKS),
            createForgeMapEntry(Registries.BLOCK, "obsidian", Tags.Blocks.OBSIDIANS),
            createForgeMapEntry(Registries.BLOCK, "ore_bearing_ground/deepslate", Tags.Blocks.ORE_BEARING_GROUND_DEEPSLATE),
            createForgeMapEntry(Registries.BLOCK, "ore_bearing_ground/netherrack", Tags.Blocks.ORE_BEARING_GROUND_NETHERRACK),
            createForgeMapEntry(Registries.BLOCK, "ore_bearing_ground/stone", Tags.Blocks.ORE_BEARING_GROUND_STONE),
            createForgeMapEntry(Registries.BLOCK, "ore_rates/dense", Tags.Blocks.ORE_RATES_DENSE),
            createForgeMapEntry(Registries.BLOCK, "ore_rates/singular", Tags.Blocks.ORE_RATES_SINGULAR),
            createForgeMapEntry(Registries.BLOCK, "ore_rates/sparse", Tags.Blocks.ORE_RATES_SPARSE),
            createForgeMapEntry(Registries.BLOCK, "ores", Tags.Blocks.ORES),
            createForgeMapEntry(Registries.BLOCK, "ores/coal", Tags.Blocks.ORES_COAL),
            createForgeMapEntry(Registries.BLOCK, "ores/copper", Tags.Blocks.ORES_COPPER),
            createForgeMapEntry(Registries.BLOCK, "ores/diamond", Tags.Blocks.ORES_DIAMOND),
            createForgeMapEntry(Registries.BLOCK, "ores/emerald", Tags.Blocks.ORES_EMERALD),
            createForgeMapEntry(Registries.BLOCK, "ores/gold", Tags.Blocks.ORES_GOLD),
            createForgeMapEntry(Registries.BLOCK, "ores/iron", Tags.Blocks.ORES_IRON),
            createForgeMapEntry(Registries.BLOCK, "ores/lapis", Tags.Blocks.ORES_LAPIS),
            createForgeMapEntry(Registries.BLOCK, "ores/netherite_scrap", Tags.Blocks.ORES_NETHERITE_SCRAP),
            createForgeMapEntry(Registries.BLOCK, "ores/quartz", Tags.Blocks.ORES_QUARTZ),
            createForgeMapEntry(Registries.BLOCK, "ores/redstone", Tags.Blocks.ORES_REDSTONE),
            createForgeMapEntry(Registries.BLOCK, "ores_in_ground/deepslate", Tags.Blocks.ORES_IN_GROUND_DEEPSLATE),
            createForgeMapEntry(Registries.BLOCK, "ores_in_ground/netherrack", Tags.Blocks.ORES_IN_GROUND_NETHERRACK),
            createForgeMapEntry(Registries.BLOCK, "ores_in_ground/stone", Tags.Blocks.ORES_IN_GROUND_STONE),
            createForgeMapEntry(Registries.BLOCK, "sand", Tags.Blocks.SANDS),
            createForgeMapEntry(Registries.BLOCK, "sand/colorless", Tags.Blocks.SANDS_COLORLESS),
            createForgeMapEntry(Registries.BLOCK, "sand/red", Tags.Blocks.SANDS_RED),
            createForgeMapEntry(Registries.BLOCK, "sandstone", Tags.Blocks.SANDSTONE_BLOCKS),
            createForgeMapEntry(Registries.BLOCK, "stained_glass", Tags.Blocks.GLASS_BLOCKS),
            createForgeMapEntry(Registries.BLOCK, "stained_glass_panes", Tags.Blocks.GLASS_PANES),
            createForgeMapEntry(Registries.BLOCK, "stone", Tags.Blocks.STONES),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks", Tags.Blocks.STORAGE_BLOCKS),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/amethyst", "storage_blocks/amethyst"),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/coal", Tags.Blocks.STORAGE_BLOCKS_COAL),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/copper", Tags.Blocks.STORAGE_BLOCKS_COPPER),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/diamond", Tags.Blocks.STORAGE_BLOCKS_DIAMOND),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/emerald", Tags.Blocks.STORAGE_BLOCKS_EMERALD),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/gold", Tags.Blocks.STORAGE_BLOCKS_GOLD),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/iron", Tags.Blocks.STORAGE_BLOCKS_IRON),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/lapis", Tags.Blocks.STORAGE_BLOCKS_LAPIS),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/netherite", Tags.Blocks.STORAGE_BLOCKS_NETHERITE),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/quartz", "storage_blocks/quartz"),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/raw_copper", Tags.Blocks.STORAGE_BLOCKS_RAW_COPPER),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/raw_gold", Tags.Blocks.STORAGE_BLOCKS_RAW_GOLD),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/raw_iron", Tags.Blocks.STORAGE_BLOCKS_RAW_IRON),
            createForgeMapEntry(Registries.BLOCK, "storage_blocks/redstone", Tags.Blocks.STORAGE_BLOCKS_REDSTONE),

            createForgeMapEntry(Registries.BLOCK, "relocation_not_supported", Tags.Blocks.RELOCATION_NOT_SUPPORTED),
            createForgeMapEntry(Registries.BLOCK, "immovable", Tags.Blocks.RELOCATION_NOT_SUPPORTED),
            createForgeMapEntry(Registries.BLOCK_PREDICATE_TYPE, "relocation_not_supported", Tags.Blocks.RELOCATION_NOT_SUPPORTED),
            createForgeMapEntry(Registries.BLOCK_PREDICATE_TYPE, "immovable", Tags.Blocks.RELOCATION_NOT_SUPPORTED),

            createForgeMapEntry(Registries.ENTITY_TYPE, "bosses", Tags.EntityTypes.BOSSES),

            createForgeMapEntry(Registries.ITEM, "barrels", Tags.Items.BARRELS),
            createForgeMapEntry(Registries.ITEM, "barrels/wooden", Tags.Items.BARRELS_WOODEN),
            createForgeMapEntry(Registries.ITEM, "bones", Tags.Items.BONES),
            createForgeMapEntry(Registries.ITEM, "bookshelves", Tags.Items.BOOKSHELVES),
            createForgeMapEntry(Registries.ITEM, "bucket", Tags.Items.BUCKETS),
            createForgeMapEntry(Registries.ITEM, "chests", Tags.Items.CHESTS),
            createForgeMapEntry(Registries.ITEM, "chests/ender", Tags.Items.CHESTS_ENDER),
            createForgeMapEntry(Registries.ITEM, "chests/trapped", Tags.Items.CHESTS_TRAPPED),
            createForgeMapEntry(Registries.ITEM, "chests/wooden", Tags.Items.CHESTS_WOODEN),
            createForgeMapEntry(Registries.ITEM, "cobblestone", Tags.Items.COBBLESTONES),
            createForgeMapEntry(Registries.ITEM, "cobblestone/normal", Tags.Items.COBBLESTONES_NORMAL),
            createForgeMapEntry(Registries.ITEM, "cobblestone/infested", Tags.Items.COBBLESTONES_INFESTED),
            createForgeMapEntry(Registries.ITEM, "cobblestone/mossy", Tags.Items.COBBLESTONES_MOSSY),
            createForgeMapEntry(Registries.ITEM, "cobblestone/deepslate", Tags.Items.COBBLESTONES_DEEPSLATE),
            createForgeMapEntry(Registries.ITEM, "crafting_table", Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createForgeMapEntry(Registries.ITEM, "crafting_tables", Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createForgeMapEntry(Registries.ITEM, "workbench", Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createForgeMapEntry(Registries.ITEM, "workbenches", Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
            createForgeMapEntry(Registries.ITEM, "crops", Tags.Items.CROPS),
            createForgeMapEntry(Registries.ITEM, "crops/beetroot", Tags.Items.CROPS_BEETROOT),
            createForgeMapEntry(Registries.ITEM, "crops/carrot", Tags.Items.CROPS_CARROT),
            createForgeMapEntry(Registries.ITEM, "crops/nether_wart", Tags.Items.CROPS_NETHER_WART),
            createForgeMapEntry(Registries.ITEM, "crops/potato", Tags.Items.CROPS_POTATO),
            createForgeMapEntry(Registries.ITEM, "crops/wheat", Tags.Items.CROPS_WHEAT),
            createForgeMapEntry(Registries.ITEM, "dusts", Tags.Items.DUSTS),
            createForgeMapEntry(Registries.ITEM, "dusts/redstone", Tags.Items.DUSTS_REDSTONE),
            createForgeMapEntry(Registries.ITEM, "dusts/glowstone", Tags.Items.DUSTS_GLOWSTONE),
            createForgeMapEntry(Registries.ITEM, "dyes", Tags.Items.DYES),
            createForgeMapEntry(Registries.ITEM, "dyes/black", Tags.Items.DYES_BLACK),
            createForgeMapEntry(Registries.ITEM, "dyes/red", Tags.Items.DYES_RED),
            createForgeMapEntry(Registries.ITEM, "dyes/green", Tags.Items.DYES_GREEN),
            createForgeMapEntry(Registries.ITEM, "dyes/brown", Tags.Items.DYES_BROWN),
            createForgeMapEntry(Registries.ITEM, "dyes/blue", Tags.Items.DYES_BLUE),
            createForgeMapEntry(Registries.ITEM, "dyes/purple", Tags.Items.DYES_PURPLE),
            createForgeMapEntry(Registries.ITEM, "dyes/cyan", Tags.Items.DYES_CYAN),
            createForgeMapEntry(Registries.ITEM, "dyes/light_gray", Tags.Items.DYES_LIGHT_GRAY),
            createForgeMapEntry(Registries.ITEM, "dyes/gray", Tags.Items.DYES_GRAY),
            createForgeMapEntry(Registries.ITEM, "dyes/pink", Tags.Items.DYES_PINK),
            createForgeMapEntry(Registries.ITEM, "dyes/lime", Tags.Items.DYES_LIME),
            createForgeMapEntry(Registries.ITEM, "dyes/yellow", Tags.Items.DYES_YELLOW),
            createForgeMapEntry(Registries.ITEM, "dyes/light_blue", Tags.Items.DYES_LIGHT_BLUE),
            createForgeMapEntry(Registries.ITEM, "dyes/magenta", Tags.Items.DYES_MAGENTA),
            createForgeMapEntry(Registries.ITEM, "dyes/orange", Tags.Items.DYES_ORANGE),
            createForgeMapEntry(Registries.ITEM, "dyes/white", Tags.Items.DYES_WHITE),
            createForgeMapEntry(Registries.ITEM, "eggs", Tags.Items.EGGS),
            createForgeMapEntry(Registries.ITEM, "enchanting_fuels", Tags.Items.ENCHANTING_FUELS),
            createForgeMapEntry(Registries.ITEM, "end_stones", Tags.Items.END_STONES),
            createForgeMapEntry(Registries.ITEM, "ender_pearls", Tags.Items.ENDER_PEARLS),
            createForgeMapEntry(Registries.ITEM, "feathers", Tags.Items.FEATHERS),
            createForgeMapEntry(Registries.ITEM, "fence_gates", Tags.Items.FENCE_GATES),
            createForgeMapEntry(Registries.ITEM, "fence_gates/wooden", Tags.Items.FENCE_GATES_WOODEN),
            createForgeMapEntry(Registries.ITEM, "fences", Tags.Items.FENCES),
            createForgeMapEntry(Registries.ITEM, "fences/nether_brick", Tags.Items.FENCES_NETHER_BRICK),
            createForgeMapEntry(Registries.ITEM, "fences/wooden", Tags.Items.FENCES_WOODEN),
            createForgeMapEntry(Registries.ITEM, "furnace", Tags.Items.PLAYER_WORKSTATIONS_FURNACES),
            createForgeMapEntry(Registries.ITEM, "furnaces", Tags.Items.PLAYER_WORKSTATIONS_FURNACES),
            createForgeMapEntry(Registries.ITEM, "gems", Tags.Items.GEMS),
            createForgeMapEntry(Registries.ITEM, "gems/diamond", Tags.Items.GEMS_DIAMOND),
            createForgeMapEntry(Registries.ITEM, "gems/emerald", Tags.Items.GEMS_EMERALD),
            createForgeMapEntry(Registries.ITEM, "gems/amethyst", Tags.Items.GEMS_AMETHYST),
            createForgeMapEntry(Registries.ITEM, "gems/lapis", Tags.Items.GEMS_LAPIS),
            createForgeMapEntry(Registries.ITEM, "gems/prismarine", Tags.Items.GEMS_PRISMARINE),
            createForgeMapEntry(Registries.ITEM, "gems/quartz", Tags.Items.GEMS_QUARTZ),
            createForgeMapEntry(Registries.ITEM, "glass", Tags.Items.GLASS_BLOCKS),
            createMapEntry(Registries.ITEM, "forge", "glass/black", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_BLACK),
            createMapEntry(Registries.ITEM, "forge", "glass/blue", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_BLUE),
            createMapEntry(Registries.ITEM, "forge", "glass/brown", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_BROWN),
            createMapEntry(Registries.ITEM, "forge", "glass/cyan", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_CYAN),
            createMapEntry(Registries.ITEM, "forge", "glass/gray", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_GRAY),
            createMapEntry(Registries.ITEM, "forge", "glass/green", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_GREEN),
            createMapEntry(Registries.ITEM, "forge", "glass/light_blue", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_LIGHT_BLUE),
            createMapEntry(Registries.ITEM, "forge", "glass/light_gray", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_LIGHT_GRAY),
            createMapEntry(Registries.ITEM, "forge", "glass/lime", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_LIME),
            createMapEntry(Registries.ITEM, "forge", "glass/magenta", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_MAGENTA),
            createMapEntry(Registries.ITEM, "forge", "glass/orange", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_ORANGE),
            createMapEntry(Registries.ITEM, "forge", "glass/pink", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_PINK),
            createMapEntry(Registries.ITEM, "forge", "glass/purple", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_PURPLE),
            createMapEntry(Registries.ITEM, "forge", "glass/red", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_RED),
            createMapEntry(Registries.ITEM, "forge", "glass/white", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_WHITE),
            createMapEntry(Registries.ITEM, "forge", "glass/yellow", Tags.Items.GLASS_BLOCKS, Tags.Items.DYED_YELLOW),
            createForgeMapEntry(Registries.ITEM, "glass/colorless", Tags.Items.GLASS_BLOCKS_COLORLESS),
            createForgeMapEntry(Registries.ITEM, "glass/silica", Tags.Items.GLASS_BLOCKS_CHEAP),
            createForgeMapEntry(Registries.ITEM, "glass/tinted", Tags.Items.GLASS_BLOCKS_TINTED),
            createForgeMapEntry(Registries.ITEM, "glass_panes", Tags.Items.GLASS_PANES),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/black", Tags.Items.GLASS_PANES, Tags.Items.DYED_BLACK),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/blue", Tags.Items.GLASS_PANES, Tags.Items.DYED_BLUE),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/brown", Tags.Items.GLASS_PANES, Tags.Items.DYED_BROWN),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/cyan", Tags.Items.GLASS_PANES, Tags.Items.DYED_CYAN),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/gray", Tags.Items.GLASS_PANES, Tags.Items.DYED_GRAY),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/green", Tags.Items.GLASS_PANES, Tags.Items.DYED_GREEN),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/light_blue", Tags.Items.GLASS_PANES, Tags.Items.DYED_LIGHT_BLUE),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/light_gray", Tags.Items.GLASS_PANES, Tags.Items.DYED_LIGHT_GRAY),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/lime", Tags.Items.GLASS_PANES, Tags.Items.DYED_LIME),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/magenta", Tags.Items.GLASS_PANES, Tags.Items.DYED_MAGENTA),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/orange", Tags.Items.GLASS_PANES, Tags.Items.DYED_ORANGE),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/pink", Tags.Items.GLASS_PANES, Tags.Items.DYED_PINK),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/purple", Tags.Items.GLASS_PANES, Tags.Items.DYED_PURPLE),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/red", Tags.Items.GLASS_PANES, Tags.Items.DYED_RED),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/white", Tags.Items.GLASS_PANES, Tags.Items.DYED_WHITE),
            createMapEntry(Registries.ITEM, "forge", "glass_panes/yellow", Tags.Items.GLASS_PANES, Tags.Items.DYED_YELLOW),
            createForgeMapEntry(Registries.ITEM, "glass_panes/colorless", Tags.Items.GLASS_PANES_COLORLESS),
            createForgeMapEntry(Registries.ITEM, "gravel", Tags.Items.GRAVELS),
            createMapEntry(Registries.ITEM, "c", "gravel", Tags.Items.GRAVELS),
            createForgeMapEntry(Registries.ITEM, "gunpowder", Tags.Items.GUNPOWDERS),
            createMapEntry(Registries.ITEM, "c", "gunpowder", Tags.Items.GUNPOWDERS),
            createForgeMapEntry(Registries.ITEM, "ingots", Tags.Items.INGOTS),
            createForgeMapEntry(Registries.ITEM, "ingots/brick", Tags.Items.BRICKS_NORMAL),
            createForgeMapEntry(Registries.ITEM, "ingots/copper", Tags.Items.INGOTS_COPPER),
            createForgeMapEntry(Registries.ITEM, "ingots/gold", Tags.Items.INGOTS_GOLD),
            createForgeMapEntry(Registries.ITEM, "ingots/iron", Tags.Items.INGOTS_IRON),
            createForgeMapEntry(Registries.ITEM, "ingots/netherite", Tags.Items.INGOTS_NETHERITE),
            createForgeMapEntry(Registries.ITEM, "ingots/nether_brick", Tags.Items.BRICKS_NETHER),
            createForgeMapEntry(Registries.ITEM, "leather", Tags.Items.LEATHERS),
            createMapEntry(Registries.ITEM, "c", "leather", Tags.Items.LEATHERS),
            createForgeMapEntry(Registries.ITEM, "mushrooms", Tags.Items.MUSHROOMS),
            createForgeMapEntry(Registries.ITEM, "nether_stars", Tags.Items.NETHER_STARS),
            createForgeMapEntry(Registries.ITEM, "netherrack", Tags.Items.NETHERRACKS),
            createMapEntry(Registries.ITEM, "c", "netherrack", Tags.Items.NETHERRACKS),
            createForgeMapEntry(Registries.ITEM, "nuggets", Tags.Items.NUGGETS),
            createForgeMapEntry(Registries.ITEM, "nuggets/gold", Tags.Items.NUGGETS_GOLD),
            createForgeMapEntry(Registries.ITEM, "nuggets/iron", Tags.Items.NUGGETS_IRON),
            createForgeMapEntry(Registries.ITEM, "obsidian", Tags.Items.OBSIDIANS),
            createForgeMapEntry(Registries.ITEM, "ore_bearing_ground/deepslate", Tags.Items.ORE_BEARING_GROUND_DEEPSLATE),
            createForgeMapEntry(Registries.ITEM, "ore_bearing_ground/netherrack", Tags.Items.ORE_BEARING_GROUND_NETHERRACK),
            createForgeMapEntry(Registries.ITEM, "ore_bearing_ground/stone", Tags.Items.ORE_BEARING_GROUND_STONE),
            createForgeMapEntry(Registries.ITEM, "ore_rates/dense", Tags.Items.ORE_RATES_DENSE),
            createForgeMapEntry(Registries.ITEM, "ore_rates/singular", Tags.Items.ORE_RATES_SINGULAR),
            createForgeMapEntry(Registries.ITEM, "ore_rates/sparse", Tags.Items.ORE_RATES_SPARSE),
            createForgeMapEntry(Registries.ITEM, "ores", Tags.Items.ORES),
            createForgeMapEntry(Registries.ITEM, "ores/coal", Tags.Items.ORES_COAL),
            createForgeMapEntry(Registries.ITEM, "ores/copper", Tags.Items.ORES_COPPER),
            createForgeMapEntry(Registries.ITEM, "ores/diamond", Tags.Items.ORES_DIAMOND),
            createForgeMapEntry(Registries.ITEM, "ores/emerald", Tags.Items.ORES_EMERALD),
            createForgeMapEntry(Registries.ITEM, "ores/gold", Tags.Items.ORES_GOLD),
            createForgeMapEntry(Registries.ITEM, "ores/iron", Tags.Items.ORES_IRON),
            createForgeMapEntry(Registries.ITEM, "ores/lapis", Tags.Items.ORES_LAPIS),
            createForgeMapEntry(Registries.ITEM, "ores/netherite_scrap", Tags.Items.ORES_NETHERITE_SCRAP),
            createForgeMapEntry(Registries.ITEM, "ores/quartz", Tags.Items.ORES_QUARTZ),
            createForgeMapEntry(Registries.ITEM, "ores/redstone", Tags.Items.ORES_REDSTONE),
            createForgeMapEntry(Registries.ITEM, "ores_in_ground/deepslate", Tags.Items.ORES_IN_GROUND_DEEPSLATE),
            createForgeMapEntry(Registries.ITEM, "ores_in_ground/netherrack", Tags.Items.ORES_IN_GROUND_NETHERRACK),
            createForgeMapEntry(Registries.ITEM, "ores_in_ground/stone", Tags.Items.ORES_IN_GROUND_STONE),
            createForgeMapEntry(Registries.ITEM, "raw_materials", Tags.Items.RAW_MATERIALS),
            createForgeMapEntry(Registries.ITEM, "raw_materials/copper", Tags.Items.RAW_MATERIALS_COPPER),
            createForgeMapEntry(Registries.ITEM, "raw_materials/gold", Tags.Items.RAW_MATERIALS_GOLD),
            createForgeMapEntry(Registries.ITEM, "raw_materials/iron", Tags.Items.RAW_MATERIALS_IRON),
            createForgeMapEntry(Registries.ITEM, "rods", Tags.Items.RODS),
            createForgeMapEntry(Registries.ITEM, "rods/blaze", Tags.Items.RODS_BLAZE),
            createForgeMapEntry(Registries.ITEM, "rods/wooden", Tags.Items.RODS_WOODEN),
            createForgeMapEntry(Registries.ITEM, "rope", Tags.Items.ROPES),
            createForgeMapEntry(Registries.ITEM, "sand", Tags.Items.SANDS),
            createForgeMapEntry(Registries.ITEM, "sand/colorless", Tags.Items.SANDS_COLORLESS),
            createForgeMapEntry(Registries.ITEM, "sand/red", Tags.Items.SANDS_RED),
            createForgeMapEntry(Registries.ITEM, "sandstone", Tags.Items.SANDSTONE_BLOCKS),
            createForgeMapEntry(Registries.ITEM, "seeds", Tags.Items.SEEDS),
            createForgeMapEntry(Registries.ITEM, "seeds/beetroot", Tags.Items.SEEDS_BEETROOT),
            createForgeMapEntry(Registries.ITEM, "seeds/melon", Tags.Items.SEEDS_MELON),
            createForgeMapEntry(Registries.ITEM, "seeds/pumpkin", Tags.Items.SEEDS_PUMPKIN),
            createForgeMapEntry(Registries.ITEM, "seeds/wheat", Tags.Items.SEEDS_WHEAT),
            createForgeMapEntry(Registries.ITEM, "shears", Tags.Items.TOOLS_SHEARS),
            createForgeMapEntry(Registries.ITEM, "slimeballs", Tags.Items.SLIMEBALLS),
            createForgeMapEntry(Registries.ITEM, "stained_glass", Tags.Items.GLASS_BLOCKS),
            createForgeMapEntry(Registries.ITEM, "stained_glass_panes", Tags.Items.GLASS_PANES),
            createForgeMapEntry(Registries.ITEM, "stone", Tags.Items.STONES),
            createForgeMapEntry(Registries.ITEM, "storage_blocks", Tags.Items.STORAGE_BLOCKS),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/amethyst", "storage_blocks/amethyst"),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/coal", Tags.Items.STORAGE_BLOCKS_COAL),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/copper", Tags.Items.STORAGE_BLOCKS_COPPER),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/diamond", Tags.Items.STORAGE_BLOCKS_DIAMOND),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/emerald", Tags.Items.STORAGE_BLOCKS_EMERALD),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/gold", Tags.Items.STORAGE_BLOCKS_GOLD),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/iron", Tags.Items.STORAGE_BLOCKS_IRON),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/lapis", Tags.Items.STORAGE_BLOCKS_LAPIS),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/netherite", Tags.Items.STORAGE_BLOCKS_NETHERITE),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/quartz", "storage_blocks/quartz"),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/raw_copper", Tags.Items.STORAGE_BLOCKS_RAW_COPPER),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/raw_gold", Tags.Items.STORAGE_BLOCKS_RAW_GOLD),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/raw_iron", Tags.Items.STORAGE_BLOCKS_RAW_IRON),
            createForgeMapEntry(Registries.ITEM, "storage_blocks/redstone", Tags.Items.STORAGE_BLOCKS_REDSTONE),
            createForgeMapEntry(Registries.ITEM, "string", Tags.Items.STRINGS),
            createForgeMapEntry(Registries.ITEM, "tools", Tags.Items.TOOLS),
            createForgeMapEntry(Registries.ITEM, "tools/shields", Tags.Items.TOOLS_SHIELDS),
            createForgeMapEntry(Registries.ITEM, "tools/bows", Tags.Items.TOOLS_BOWS),
            createForgeMapEntry(Registries.ITEM, "tools/crossbows", Tags.Items.TOOLS_CROSSBOWS),
            createForgeMapEntry(Registries.ITEM, "tools/fishing_rods", Tags.Items.TOOLS_FISHING_RODS),
            createForgeMapEntry(Registries.ITEM, "tools/tridents", Tags.Items.TOOLS_SPEARS),
            createForgeMapEntry(Registries.ITEM, "tools/brushes", Tags.Items.TOOLS_BRUSHES),
            createForgeMapEntry(Registries.ITEM, "armors", Tags.Items.ARMORS),
            createForgeMapEntry(Registries.ITEM, "armors/helmets", ItemTags.HEAD_ARMOR),
            createForgeMapEntry(Registries.ITEM, "armors/chestplates", ItemTags.CHEST_ARMOR),
            createForgeMapEntry(Registries.ITEM, "armors/leggings", ItemTags.LEG_ARMOR),
            createForgeMapEntry(Registries.ITEM, "armors/boots", ItemTags.FOOT_ARMOR),
            createForgeMapEntry(Registries.ITEM, "wrench", "tools/wrenches"),
            createForgeMapEntry(Registries.ITEM, "wrenches", "tools/wrenches"),
            createForgeMapEntry(Registries.ITEM, "tools/wrench", "tools/wrenches"),
            createForgeMapEntry(Registries.ITEM, "tools/wrenches", "tools/wrenches"),
            createForgeMapEntry(Registries.ITEM, "food", Tags.Items.FOODS),
            createForgeMapEntry(Registries.ITEM, "foods", Tags.Items.FOODS),
            createForgeMapEntry(Registries.ITEM, "fruit", Tags.Items.FOODS_FRUITS),
            createForgeMapEntry(Registries.ITEM, "fruits", Tags.Items.FOODS_FRUITS),
            createForgeMapEntry(Registries.ITEM, "vegetable", Tags.Items.FOODS_VEGETABLES),
            createForgeMapEntry(Registries.ITEM, "vegetables", Tags.Items.FOODS_VEGETABLES),
            createForgeMapEntry(Registries.ITEM, "berry", Tags.Items.FOODS_BERRIES),
            createForgeMapEntry(Registries.ITEM, "berries", Tags.Items.FOODS_BERRIES),
            createForgeMapEntry(Registries.ITEM, "bread", Tags.Items.FOODS_BREADS),
            createForgeMapEntry(Registries.ITEM, "breads", Tags.Items.FOODS_BREADS),
            createForgeMapEntry(Registries.ITEM, "cookie", Tags.Items.FOODS_COOKIES),
            createForgeMapEntry(Registries.ITEM, "cookies", Tags.Items.FOODS_COOKIES),
            createForgeMapEntry(Registries.ITEM, "raw_meat", Tags.Items.FOODS_RAW_MEATS),
            createForgeMapEntry(Registries.ITEM, "raw_meats", Tags.Items.FOODS_RAW_MEATS),
            createForgeMapEntry(Registries.ITEM, "raw_fish", Tags.Items.FOODS_RAW_FISHES),
            createForgeMapEntry(Registries.ITEM, "raw_fishes", Tags.Items.FOODS_RAW_FISHES),
            createForgeMapEntry(Registries.ITEM, "cooked_meat", Tags.Items.FOODS_COOKED_MEATS),
            createForgeMapEntry(Registries.ITEM, "cooked_meats", Tags.Items.FOODS_COOKED_MEATS),
            createForgeMapEntry(Registries.ITEM, "cooked_fish", Tags.Items.FOODS_COOKED_FISHES),
            createForgeMapEntry(Registries.ITEM, "cooked_fishes", Tags.Items.FOODS_COOKED_FISHES),
            createForgeMapEntry(Registries.ITEM, "soup", Tags.Items.FOODS_SOUPS),
            createForgeMapEntry(Registries.ITEM, "soups", Tags.Items.FOODS_SOUPS),
            createForgeMapEntry(Registries.ITEM, "stew", Tags.Items.FOODS_SOUPS),
            createForgeMapEntry(Registries.ITEM, "stews", Tags.Items.FOODS_SOUPS),
            createForgeMapEntry(Registries.ITEM, "candy", Tags.Items.FOODS_CANDIES),
            createForgeMapEntry(Registries.ITEM, "candies", Tags.Items.FOODS_CANDIES),

            createForgeMapEntry(Registries.FLUID, "water", Tags.Fluids.WATER),
            createForgeMapEntry(Registries.FLUID, "lava", Tags.Fluids.LAVA),
            createForgeMapEntry(Registries.FLUID, "milk", Tags.Fluids.MILK),
            createForgeMapEntry(Registries.FLUID, "gaseous", Tags.Fluids.GASEOUS),
            createForgeMapEntry(Registries.FLUID, "honey", Tags.Fluids.HONEY),
            createForgeMapEntry(Registries.FLUID, "potion", Tags.Fluids.POTION),
            createForgeMapEntry(Registries.FLUID, "plantoil", "plant_oil"),

            createForgeMapEntry(Registries.BIOME, "is_hot", Tags.Biomes.IS_HOT),
            createForgeMapEntry(Registries.BIOME, "is_hot/overworld", Tags.Biomes.IS_HOT_OVERWORLD),
            createForgeMapEntry(Registries.BIOME, "is_hot/nether", Tags.Biomes.IS_HOT_NETHER),
            createForgeMapEntry(Registries.BIOME, "is_hot/end", Tags.Biomes.IS_HOT_END),
            createForgeMapEntry(Registries.BIOME, "is_cold", Tags.Biomes.IS_COLD),
            createForgeMapEntry(Registries.BIOME, "is_cold/overworld", Tags.Biomes.IS_COLD_OVERWORLD),
            createForgeMapEntry(Registries.BIOME, "is_cold/nether", Tags.Biomes.IS_COLD_NETHER),
            createForgeMapEntry(Registries.BIOME, "is_cold/end", Tags.Biomes.IS_COLD_END),
            createForgeMapEntry(Registries.BIOME, "is_sparse", Tags.Biomes.IS_SPARSE_VEGETATION),
            createForgeMapEntry(Registries.BIOME, "is_sparse/overworld", Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD),
            createForgeMapEntry(Registries.BIOME, "is_sparse/nether", Tags.Biomes.IS_SPARSE_VEGETATION_NETHER),
            createForgeMapEntry(Registries.BIOME, "is_sparse/end", Tags.Biomes.IS_SPARSE_VEGETATION_END),
            createForgeMapEntry(Registries.BIOME, "is_dense", Tags.Biomes.IS_DENSE_VEGETATION),
            createForgeMapEntry(Registries.BIOME, "is_dense/overworld", Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD),
            createForgeMapEntry(Registries.BIOME, "is_dense/nether", Tags.Biomes.IS_DENSE_VEGETATION_NETHER),
            createForgeMapEntry(Registries.BIOME, "is_dense/end", Tags.Biomes.IS_DENSE_VEGETATION_END),
            createForgeMapEntry(Registries.BIOME, "is_wet", Tags.Biomes.IS_WET),
            createForgeMapEntry(Registries.BIOME, "is_wet/overworld", Tags.Biomes.IS_WET_OVERWORLD),
            createForgeMapEntry(Registries.BIOME, "is_wet/nether", Tags.Biomes.IS_WET_NETHER),
            createForgeMapEntry(Registries.BIOME, "is_wet/end", Tags.Biomes.IS_WET_END),
            createForgeMapEntry(Registries.BIOME, "is_dry", Tags.Biomes.IS_DRY),
            createForgeMapEntry(Registries.BIOME, "is_dry/overworld", Tags.Biomes.IS_DRY_OVERWORLD),
            createForgeMapEntry(Registries.BIOME, "is_dry/nether", Tags.Biomes.IS_DRY_NETHER),
            createForgeMapEntry(Registries.BIOME, "is_dry/end", Tags.Biomes.IS_DRY_END),
            createForgeMapEntry(Registries.BIOME, "is_coniferous", Tags.Biomes.IS_CONIFEROUS_TREE),
            createForgeMapEntry(Registries.BIOME, "is_savanna", Tags.Biomes.IS_SAVANNA_TREE),
            createForgeMapEntry(Registries.BIOME, "is_jungle", Tags.Biomes.IS_JUNGLE_TREE),
            createForgeMapEntry(Registries.BIOME, "is_deciduous", Tags.Biomes.IS_DECIDUOUS_TREE),
            createForgeMapEntry(Registries.BIOME, "is_spooky", Tags.Biomes.IS_SPOOKY),
            createForgeMapEntry(Registries.BIOME, "is_dead", Tags.Biomes.IS_DEAD),
            createForgeMapEntry(Registries.BIOME, "is_lush", Tags.Biomes.IS_LUSH),
            createForgeMapEntry(Registries.BIOME, "is_mushroom", Tags.Biomes.IS_MUSHROOM),
            createForgeMapEntry(Registries.BIOME, "is_magical", Tags.Biomes.IS_MAGICAL),
            createForgeMapEntry(Registries.BIOME, "is_rare", Tags.Biomes.IS_RARE),
            createForgeMapEntry(Registries.BIOME, "is_plateau", Tags.Biomes.IS_PLATEAU),
            createForgeMapEntry(Registries.BIOME, "is_modified", Tags.Biomes.IS_MODIFIED),
            createForgeMapEntry(Registries.BIOME, "is_water", Tags.Biomes.IS_AQUATIC),
            createForgeMapEntry(Registries.BIOME, "is_desert", Tags.Biomes.IS_DESERT),
            createForgeMapEntry(Registries.BIOME, "is_plains", Tags.Biomes.IS_PLAINS),
            createForgeMapEntry(Registries.BIOME, "is_swamp", Tags.Biomes.IS_SWAMP),
            createForgeMapEntry(Registries.BIOME, "is_sandy", Tags.Biomes.IS_SANDY),
            createForgeMapEntry(Registries.BIOME, "is_snowy", Tags.Biomes.IS_SNOWY),
            createForgeMapEntry(Registries.BIOME, "is_wasteland", Tags.Biomes.IS_WASTELAND),
            createForgeMapEntry(Registries.BIOME, "is_void", Tags.Biomes.IS_VOID),
            createForgeMapEntry(Registries.BIOME, "is_underground", Tags.Biomes.IS_UNDERGROUND),
            createForgeMapEntry(Registries.BIOME, "is_cave", Tags.Biomes.IS_CAVE),
            createForgeMapEntry(Registries.BIOME, "is_peak", Tags.Biomes.IS_MOUNTAIN_PEAK),
            createForgeMapEntry(Registries.BIOME, "is_slope", Tags.Biomes.IS_MOUNTAIN_SLOPE),
            createForgeMapEntry(Registries.BIOME, "is_mountain", Tags.Biomes.IS_MOUNTAIN),
            createForgeMapEntry(Registries.BIOME, "is_end", Tags.Biomes.IS_END),
            createForgeMapEntry(Registries.BIOME, "is_nether", Tags.Biomes.IS_NETHER),
            createForgeMapEntry(Registries.BIOME, "is_overworld", Tags.Biomes.IS_OVERWORLD),
            createForgeMapEntry(Registries.BIOME, "no_default_monsters", Tags.Biomes.NO_DEFAULT_MONSTERS));

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
                                    String replacementMessage = LEGACY_FORGE_TAGS.get(tagKey);
                                    stringBuilder.append("\n     ").append(tagKey).append("  ->  ").append(replacementMessage);
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

    private static <T> AbstractMap.SimpleEntry<TagKey<T>, String> createForgeMapEntry(ResourceKey<Registry<T>> registryKey, String tagId1, String tagId2) {
        return new AbstractMap.SimpleEntry<>(createTagKey(registryKey, "forge", tagId1), "c:" + tagId2);
    }

    private static <T, R> AbstractMap.SimpleEntry<TagKey<T>, String> createForgeMapEntry(ResourceKey<Registry<T>> registryKey, String tagId1, TagKey<R> tag2) {
        return new AbstractMap.SimpleEntry<>(createTagKey(registryKey, "forge", tagId1), tag2.toString());
    }

    private static <T, R> AbstractMap.SimpleEntry<TagKey<T>, String> createMapEntry(ResourceKey<Registry<T>> registryKey, String tagNamespace1, String tagId1, TagKey<R> tag2) {
        return new AbstractMap.SimpleEntry<>(createTagKey(registryKey, tagNamespace1, tagId1), tag2.toString());
    }

    private static <T, R> AbstractMap.SimpleEntry<TagKey<T>, String> createMapEntry(ResourceKey<Registry<T>> registryKey, String tagNamespace1, String tagId1, TagKey<R>... replacementTags) {
        return new AbstractMap.SimpleEntry<>(createTagKey(registryKey, tagNamespace1, tagId1), String.join(" and ", Arrays.stream(replacementTags).map(TagKey::toString).toList()));
    }

    private static <T> TagKey<T> createTagKey(ResourceKey<Registry<T>> registryKey, String namespace, String tagId) {
        return TagKey.create(registryKey, new ResourceLocation(namespace, tagId));
    }
}
