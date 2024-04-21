/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.Locale;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.LanguageProvider;
import org.apache.commons.lang3.StringUtils;

public final class NeoForgeLanguageProvider extends LanguageProvider {
    public NeoForgeLanguageProvider(PackOutput gen) {
        super(gen, "c", "en_us");
    }

    @Override
    protected void addTranslations() {
        // Blocks
        add(Tags.Blocks.BARRELS, "Barrels");
        add(Tags.Blocks.BARRELS_WOODEN, "Wooden Barrels");
        add(Tags.Blocks.BOOKSHELVES, "Bookshelves");
        add(Tags.Blocks.BUDDING_BLOCKS, "Budding Blocks");
        add(Tags.Blocks.BUDS, "Buds");
        add(Tags.Blocks.CHAINS, "Chains");
        add(Tags.Blocks.CHESTS, "Chests");
        add(Tags.Blocks.CHESTS_ENDER, "Ender Chests");
        add(Tags.Blocks.CHESTS_TRAPPED, "Trapped Chests");
        add(Tags.Blocks.CHESTS_WOODEN, "Wooden Chests");
        add(Tags.Blocks.CLUSTERS, "Clusters");
        add(Tags.Blocks.COBBLESTONES, "Cobblestones");
        add(Tags.Blocks.COBBLESTONES_NORMAL, "Normal Cobblestones");
        add(Tags.Blocks.COBBLESTONES_INFESTED, "Infested Cobblestones");
        add(Tags.Blocks.COBBLESTONES_MOSSY, "Mossy Cobblestones");
        add(Tags.Blocks.COBBLESTONES_DEEPSLATE, "Deepslate Cobblestones");
        add(Tags.Blocks.DYED, "Dyed Blocks");
        addColored(Tags.Blocks.DYED, "{color} Dyed Blocks");
        add(Tags.Blocks.END_STONES, "End Stones");
        add(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST, "Enderman Place On Blacklist");
        add(Tags.Blocks.FENCE_GATES, "Fence Gates");
        add(Tags.Blocks.FENCE_GATES_WOODEN, "Wooden Fence Gates");
        add(Tags.Blocks.FENCES, "Fences");
        add(Tags.Blocks.FENCES_NETHER_BRICK, "Fences Nether Brick");
        add(Tags.Blocks.FENCES_WOODEN, "Wooden Fences");
        add(Tags.Blocks.GLASS_BLOCKS, "Glass Blocks");
        add(Tags.Blocks.GLASS_BLOCKS_COLORLESS, "Colorless Glass Blocks");
        add(Tags.Blocks.GLASS_BLOCKS_CHEAP, "Silica Glass Blocks");
        add(Tags.Blocks.GLASS_BLOCKS_TINTED, "Tinted Glass Blocks");
        add(Tags.Blocks.GLASS_PANES, "Glass Panes");
        add(Tags.Blocks.GLASS_PANES_COLORLESS, "Colorless Glass Panes");
        add(Tags.Blocks.GRAVELS, "Gravels");
        add(Tags.Blocks.SKULLS, "Skulls");
        add(Tags.Blocks.HIDDEN_FROM_RECIPE_VIEWERS, "Hidden From Recipe Viewers");
        add(Tags.Blocks.NETHERRACKS, "Netherracks");
        add(Tags.Blocks.NEEDS_WOOD_TOOL, "Needs Wooden Tools");
        add(Tags.Blocks.NEEDS_GOLD_TOOL, "Needs Gold Tools");
        add(Tags.Blocks.NEEDS_NETHERITE_TOOL, "Needs Netherite Tools");
        add(Tags.Blocks.OBSIDIANS, "Obsidians");
        add(Tags.Blocks.ORE_BEARING_GROUND_DEEPSLATE, "Deepslate Ore Bearing Ground");
        add(Tags.Blocks.ORE_BEARING_GROUND_NETHERRACK, "Netherrack Ore Bearing Ground");
        add(Tags.Blocks.ORE_BEARING_GROUND_STONE, "Stone Ore Bearing Ground");
        add(Tags.Blocks.ORE_RATES_DENSE, "Dense Ore Rates");
        add(Tags.Blocks.ORE_RATES_SINGULAR, "Singular Ore Rates");
        add(Tags.Blocks.ORE_RATES_SPARSE, "Sparse Ore Rates");
        add(Tags.Blocks.ORES, "Ores");
        add(Tags.Blocks.ORES_COAL, "Coal Ores");
        add(Tags.Blocks.ORES_COPPER, "Copper Ores");
        add(Tags.Blocks.ORES_DIAMOND, "Diamond Ores");
        add(Tags.Blocks.ORES_EMERALD, "Emerald Ores");
        add(Tags.Blocks.ORES_GOLD, "Gold Ores");
        add(Tags.Blocks.ORES_IRON, "Iron Ores");
        add(Tags.Blocks.ORES_LAPIS, "Lapis Ores");
        add(Tags.Blocks.ORES_QUARTZ, "Quartz Ores");
        add(Tags.Blocks.ORES_REDSTONE, "Redstone Ores");
        add(Tags.Blocks.ORES_NETHERITE_SCRAP, "Netherite Scrap Ores");
        add(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, "Deepslate Ores In Ground");
        add(Tags.Blocks.ORES_IN_GROUND_NETHERRACK, "Netherrack Ores In Ground");
        add(Tags.Blocks.ORES_IN_GROUND_STONE, "Stone Ores In Ground");
        add(Tags.Blocks.PLAYER_WORKSTATIONS_CRAFTING_TABLES, "Crafting Tables");
        add(Tags.Blocks.PLAYER_WORKSTATIONS_FURNACES, "Furnaces");
        add(Tags.Blocks.RELOCATION_NOT_SUPPORTED, "Relocation Not Supported");
        add(Tags.Blocks.ROPES, "Ropes");
        add(Tags.Blocks.SANDS, "Sands");
        add(Tags.Blocks.SANDS_COLORLESS, "Colorless Sands");
        add(Tags.Blocks.SANDS_RED, "Red Sands");
        add(Tags.Blocks.SANDSTONE_BLOCKS, "Sandstone Blocks");
        add(Tags.Blocks.SANDSTONE_SLABS, "Sandstone Slabs");
        add(Tags.Blocks.SANDSTONE_STAIRS, "Sandstone Stairs");
        add(Tags.Blocks.SANDSTONE_RED_BLOCKS, "Red Sandstone Blocks");
        add(Tags.Blocks.SANDSTONE_RED_SLABS, "Red Sandstone Slabs");
        add(Tags.Blocks.SANDSTONE_RED_STAIRS, "Red Sandstone Stairs");
        add(Tags.Blocks.SANDSTONE_UNCOLORED_BLOCKS, "Uncolored Sandstone Blocks");
        add(Tags.Blocks.SANDSTONE_UNCOLORED_SLABS, "Uncolored Sandstone Slabs");
        add(Tags.Blocks.SANDSTONE_UNCOLORED_STAIRS, "Uncolored Sandstone Stairs");
        add(Tags.Blocks.STONES, "Stones");
        add(Tags.Blocks.STORAGE_BLOCKS, "Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_BONE_MEAL, "Bone Meal Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_COAL, "Coal Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_COPPER, "Copper Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_DIAMOND, "Diamond Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_DRIED_KELP, "Dried Kelp Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_EMERALD, "Emerald Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_GOLD, "Gold Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_IRON, "Iron Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_LAPIS, "Lapis Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_NETHERITE, "Netherite Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_RAW_COPPER, "Raw Copper Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_RAW_GOLD, "Raw Gold Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_RAW_IRON, "Raw Iron Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_REDSTONE, "Redstone Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_SLIME, "Slime Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_WHEAT, "Wheat Storage Blocks");
        add(Tags.Blocks.VILLAGER_JOB_SITES, "Villager Job Sites");

        // Items
        add(Tags.Items.BARRELS, "Barrels");
        add(Tags.Items.BARRELS_WOODEN, "Wooden Barrels");
        add(Tags.Items.BONES, "Bones");
        add(Tags.Items.BOOKSHELVES, "Bookshelves");
        add(Tags.Items.BRICKS, "Bricks");
        add(Tags.Items.BRICKS_NORMAL, "Normal Bricks");
        add(Tags.Items.BRICKS_NETHER, "Nether Bricks");
        add(Tags.Items.BUCKETS, "Buckets");
        add(Tags.Items.BUDDING_BLOCKS, "Budding Blocks");
        add(Tags.Items.BUDS, "Buds");
        add(Tags.Items.CHAINS, "Chains");
        add(Tags.Items.CHESTS, "Chests");
        add(Tags.Items.CHESTS_ENDER, "Ender Chests");
        add(Tags.Items.CHESTS_TRAPPED, "Trapped Chests");
        add(Tags.Items.CHESTS_WOODEN, "Wooden Chests");
        add(Tags.Items.CLUSTERS, "Clusters");
        add(Tags.Items.COBBLESTONES, "Cobblestones");
        add(Tags.Items.COBBLESTONES_NORMAL, "Normal Cobblestones");
        add(Tags.Items.COBBLESTONES_INFESTED, "Infested Cobblestones");
        add(Tags.Items.COBBLESTONES_MOSSY, "Mossy Cobblestones");
        add(Tags.Items.COBBLESTONES_DEEPSLATE, "Deepslate Cobblestones");
        add(Tags.Items.CROPS, "Crops");
        add(Tags.Items.CROPS_BEETROOT, "Beetroot Crops");
        add(Tags.Items.CROPS_CARROT, "Carrot Crops");
        add(Tags.Items.CROPS_NETHER_WART, "Nether Wart Crops");
        add(Tags.Items.CROPS_POTATO, "Potato Crops");
        add(Tags.Items.CROPS_WHEAT, "Wheat Crops");
        add(Tags.Items.DUSTS, "Dusts");
        add(Tags.Items.DUSTS_GLOWSTONE, "Glowstone Dusts");
        add(Tags.Items.DUSTS_REDSTONE, "Redstone Dusts");
        add(Tags.Items.DYED, "Dyed Items");
        addColored(Tags.Items.DYED, "{color} Dyed Items");
        add(Tags.Items.DYES, "Dyes");
        add(Tags.Items.DYES_BLACK, "Black Dyes");
        add(Tags.Items.DYES_RED, "Red Dyes");
        add(Tags.Items.DYES_GREEN, "Green Dyes");
        add(Tags.Items.DYES_BROWN, "Brown Dyes");
        add(Tags.Items.DYES_BLUE, "Blue Dyes");
        add(Tags.Items.DYES_PURPLE, "Purple Dyes");
        add(Tags.Items.DYES_CYAN, "Cyan Dyes");
        add(Tags.Items.DYES_LIGHT_GRAY, "Light Gray Dyes");
        add(Tags.Items.DYES_GRAY, "Gray Dyes");
        add(Tags.Items.DYES_PINK, "Pink Dyes");
        add(Tags.Items.DYES_LIME, "Lime Dyes");
        add(Tags.Items.DYES_YELLOW, "Yellow Dyes");
        add(Tags.Items.DYES_LIGHT_BLUE, "Light Blue Dyes");
        add(Tags.Items.DYES_MAGENTA, "Magenta Dyes");
        add(Tags.Items.DYES_ORANGE, "Orange Dyes");
        add(Tags.Items.DYES_WHITE, "White Dyes");
        add(Tags.Items.EGGS, "Eggs");
        add(Tags.Items.ENCHANTING_FUELS, "Enchanting Fuels");
        add(Tags.Items.END_STONES, "End Stones");
        add(Tags.Items.ENDER_PEARLS, "Ender Pearls");
        add(Tags.Items.FEATHERS, "Feathers");
        add(Tags.Items.FENCE_GATES, "Fence Gates");
        add(Tags.Items.FENCE_GATES_WOODEN, "Wooden Fence Gates");
        add(Tags.Items.FENCES, "Fences");
        add(Tags.Items.FENCES_NETHER_BRICK, "Nether Brick Fences");
        add(Tags.Items.FENCES_WOODEN, "Wooden Fences");
        add(Tags.Items.FOODS, "Foods");
        add(Tags.Items.FOODS_BERRIES, "Berries");
        add(Tags.Items.FOODS_BREADS, "Breads");
        add(Tags.Items.FOODS_CANDIES, "Candies");
        add(Tags.Items.FOODS_COOKED_FISHES, "Cooked Fishes");
        add(Tags.Items.FOODS_COOKED_MEATS, "Cooked Meats");
        add(Tags.Items.FOODS_COOKIES, "Cookies");
        add(Tags.Items.FOODS_EDIBLE_WHEN_PLACED, "Edible When Placed");
        add(Tags.Items.FOODS_FOOD_POISONING, "Food Poisoning Foods");
        add(Tags.Items.FOODS_FRUITS, "Fruits");
        add(Tags.Items.FOODS_RAW_FISHES, "Raw Fishes");
        add(Tags.Items.FOODS_RAW_MEATS, "Raw Meats");
        add(Tags.Items.FOODS_SOUPS, "Soups");
        add(Tags.Items.FOODS_VEGETABLES, "Vegetables");
        add(Tags.Items.GEMS, "Gems");
        add(Tags.Items.GEMS_AMETHYST, "Amethyst Gems");
        add(Tags.Items.GEMS_DIAMOND, "Diamond Gems");
        add(Tags.Items.GEMS_EMERALD, "Emerald Gems");
        add(Tags.Items.GEMS_LAPIS, "Lapis Gems");
        add(Tags.Items.GEMS_PRISMARINE, "Prismarine Gems");
        add(Tags.Items.GEMS_QUARTZ, "Quartz Gems");
        add(Tags.Items.GLASS_BLOCKS, "Glass Blocks");
        add(Tags.Items.GLASS_BLOCKS_TINTED, "Tinted Glass Blocks");
        add(Tags.Items.GLASS_BLOCKS_CHEAP, "Cheap Glass Blocks");
        add(Tags.Items.GLASS_BLOCKS_COLORLESS, "Colorless Glass Blocks");
        add(Tags.Items.GLASS_PANES, "Glass Panes");
        add(Tags.Items.GLASS_PANES_COLORLESS, "Colorless Glass Panes");
        add(Tags.Items.GRAVELS, "Gravels");
        add(Tags.Items.GUNPOWDERS, "Gunpowders");
        add(Tags.Items.HIDDEN_FROM_RECIPE_VIEWERS, "Hidden From Recipe Viewers");
        add(Tags.Items.INGOTS, "Ingots");
        add(Tags.Items.INGOTS_COPPER, "Copper Ingots");
        add(Tags.Items.INGOTS_GOLD, "Gold Ingots");
        add(Tags.Items.INGOTS_IRON, "Iron Ingots");
        add(Tags.Items.INGOTS_NETHERITE, "Netherite Ingots");
        add(Tags.Items.LEATHERS, "Leathers");
        add(Tags.Items.MUSHROOMS, "Mushrooms");
        add(Tags.Items.NETHER_STARS, "Nether Stars");
        add(Tags.Items.NETHERRACKS, "Netherracks");
        add(Tags.Items.NUGGETS, "Nuggets");
        add(Tags.Items.NUGGETS_IRON, "Iron Nuggets");
        add(Tags.Items.NUGGETS_GOLD, "Gold Nuggets");
        add(Tags.Items.OBSIDIANS, "Obsidians");
        add(Tags.Items.ORE_BEARING_GROUND_DEEPSLATE, "Deepslate Ore Bearing Ground");
        add(Tags.Items.ORE_BEARING_GROUND_NETHERRACK, "Netherrack Ore Bearing Ground");
        add(Tags.Items.ORE_BEARING_GROUND_STONE, "Stone Ore Bearing Ground");
        add(Tags.Items.ORE_RATES_DENSE, "Dense Ore Rates");
        add(Tags.Items.ORE_RATES_SINGULAR, "Singular Ore Rates");
        add(Tags.Items.ORE_RATES_SPARSE, "Sparse Ore Rates");
        add(Tags.Items.ORES, "Ores");
        add(Tags.Items.ORES_COAL, "Coal Ores");
        add(Tags.Items.ORES_COPPER, "Copper Ores");
        add(Tags.Items.ORES_DIAMOND, "Diamond Ores");
        add(Tags.Items.ORES_EMERALD, "Emerald Ores");
        add(Tags.Items.ORES_GOLD, "Gold Ores");
        add(Tags.Items.ORES_IRON, "Iron Ores");
        add(Tags.Items.ORES_LAPIS, "Lapis Ores");
        add(Tags.Items.ORES_QUARTZ, "Quartz Ores");
        add(Tags.Items.ORES_REDSTONE, "Redstone Ores");
        add(Tags.Items.ORES_NETHERITE_SCRAP, "Netherite Scrap Ores");
        add(Tags.Items.ORES_IN_GROUND_DEEPSLATE, "Deepslate Ores In Ground");
        add(Tags.Items.ORES_IN_GROUND_NETHERRACK, "Netherrack Ores In Ground");
        add(Tags.Items.ORES_IN_GROUND_STONE, "Stone Ores In Ground");
        add(Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES, "Crafting Tables");
        add(Tags.Items.PLAYER_WORKSTATIONS_FURNACES, "Furnaces");
        add(Tags.Items.RAW_BLOCKS, "Raw Blocks");
        add(Tags.Items.RAW_BLOCKS_COPPER, "Copper Raw Blocks");
        add(Tags.Items.RAW_BLOCKS_GOLD, "Gold Raw Blocks");
        add(Tags.Items.RAW_BLOCKS_IRON, "Iron Raw Blocks");
        add(Tags.Items.RAW_MATERIALS, "Raw Materials");
        add(Tags.Items.RAW_MATERIALS_COPPER, "Copper Raw Materials");
        add(Tags.Items.RAW_MATERIALS_GOLD, "Gold Raw Materials");
        add(Tags.Items.RAW_MATERIALS_IRON, "Iron Raw Materials");
        add(Tags.Items.RODS, "Rods");
        add(Tags.Items.RODS_BLAZE, "Blaze Rods");
        add(Tags.Items.RODS_BREEZE, "Breeze Rods");
        add(Tags.Items.RODS_WOODEN, "Wooden Rods");
        add(Tags.Items.ROPES, "Ropes");
        add(Tags.Items.SANDS, "Sands");
        add(Tags.Items.SANDS_COLORLESS, "Colorless Sands");
        add(Tags.Items.SANDS_RED, "Red Sands");
        add(Tags.Items.SANDSTONE_BLOCKS, "Sandstone Blocks");
        add(Tags.Items.SANDSTONE_SLABS, "Sandstone Slabs");
        add(Tags.Items.SANDSTONE_STAIRS, "Sandstone Stairs");
        add(Tags.Items.SANDSTONE_RED_BLOCKS, "Red Sandstone Blocks");
        add(Tags.Items.SANDSTONE_RED_SLABS, "Red Sandstone Slabs");
        add(Tags.Items.SANDSTONE_RED_STAIRS, "Red Sandstone Stairs");
        add(Tags.Items.SANDSTONE_UNCOLORED_BLOCKS, "Uncolored Sandstone Blocks");
        add(Tags.Items.SANDSTONE_UNCOLORED_SLABS, "Uncolored Sandstone Slabs");
        add(Tags.Items.SANDSTONE_UNCOLORED_STAIRS, "Uncolored Sandstone Stairs");
        add(Tags.Items.SEEDS, "Seeds");
        add(Tags.Items.SEEDS_BEETROOT, "Beetroot Seeds");
        add(Tags.Items.SEEDS_MELON, "Melon Seeds");
        add(Tags.Items.SEEDS_PUMPKIN, "Pumpkin Seeds");
        add(Tags.Items.SEEDS_WHEAT, "Wheat Seeds");
        add(Tags.Items.SLIMEBALLS, "Slimeballs");
        add(Tags.Items.STONES, "Stones");
        add(Tags.Items.STORAGE_BLOCKS, "Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_BONE_MEAL, "Bone Meal Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_COAL, "Coal Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_COPPER, "Copper Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_DIAMOND, "Diamond Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_DRIED_KELP, "Dried Kelp Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_EMERALD, "Emerald Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_GOLD, "Gold Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_IRON, "Iron Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_LAPIS, "Lapis Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_NETHERITE, "Netherite Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_RAW_COPPER, "Raw Copper Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_RAW_GOLD, "Raw Gold Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_RAW_IRON, "Raw Iron Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_REDSTONE, "Redstone Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_SLIME, "Slime Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_WHEAT, "Wheat Storage Blocks");
        add(Tags.Items.STRINGS, "Strings");
        add(Tags.Items.VILLAGER_JOB_SITES, "Villager Job Sites");
        add(Tags.Items.TOOLS_SHEARS, "Shears");
        add(Tags.Items.TOOLS_SHIELDS, "Shields");
        add(Tags.Items.TOOLS_BOWS, "Bows");
        add(Tags.Items.TOOLS_CROSSBOWS, "Crossbows");
        add(Tags.Items.TOOLS_FISHING_RODS, "Fishing Rods");
        add(Tags.Items.TOOLS_BRUSHES, "Brushes");
        add(Tags.Items.TOOLS_SPEARS, "Spears");
        add(Tags.Items.TOOLS, "Tools");
        add(Tags.Items.ARMORS, "Armors");
        add(Tags.Items.ENCHANTABLES, "Enchantables");

        // Fluids
        add(Tags.Fluids.WATER, "Water");
        add(Tags.Fluids.LAVA, "Lava");
        add(Tags.Fluids.MILK, "Milk");
        add(Tags.Fluids.GASEOUS, "Gaseous");
        add(Tags.Fluids.HONEY, "Honey");
        add(Tags.Fluids.POTION, "Potion");
        add(Tags.Fluids.SUSPICIOUS_STEW, "Suspicious Stew");
        add(Tags.Fluids.MUSHROOM_STEW, "Mushroom Stew");
        add(Tags.Fluids.RABBIT_STEW, "Rabbit Stew");
        add(Tags.Fluids.BEETROOT_SOUP, "Beetroot Soup");
        add(Tags.Fluids.HIDDEN_FROM_RECIPE_VIEWERS, "Hidden From Recipe Viewers");

        // Entities
        add(Tags.EntityTypes.BOSSES, "Bosses");
        add(Tags.EntityTypes.MINECARTS, "Minecarts");
        add(Tags.EntityTypes.BOATS, "Boats");
        add(Tags.EntityTypes.CAPTURING_NOT_SUPPORTED, "Capturing Not Supported");
        add(Tags.EntityTypes.TELEPORTING_NOT_SUPPORTED, "Teleporting Not Supported");

        // Enchantments
        add(Tags.Enchantments.INCREASE_BLOCK_DROPS, "Increase Block Drops");
        add(Tags.Enchantments.INCREASE_ENTITY_DROPS, "Increase Entity Drops");
        add(Tags.Enchantments.WEAPON_DAMAGE_ENHANCEMENTS, "Weapon Damage Enhancements");
        add(Tags.Enchantments.ENTITY_SPEED_ENHANCEMENTS, "Entity Speed Enhancements");
        add(Tags.Enchantments.ENTITY_AUXILIARY_MOVEMENT_ENHANCEMENTS, "Entity Auxiliary Movement Enhancements");
        add(Tags.Enchantments.ENTITY_DEFENSE_ENHANCEMENTS, "Entity Defense Enhancements");

        // Biomes
        add(Tags.Biomes.NO_DEFAULT_MONSTERS, "No Default Monsters");
        add(Tags.Biomes.HIDDEN_FROM_LOCATOR_SELECTION, "Hidden From Locator's Selection");
        add(Tags.Biomes.IS_VOID, "Voids");

        add(Tags.Biomes.IS_END, "Is End");
        add(Tags.Biomes.IS_NETHER, "Is Nether");
        add(Tags.Biomes.IS_OVERWORLD, "Is Overworld");

        add(Tags.Biomes.IS_HOT, "Hot");
        add(Tags.Biomes.IS_HOT_OVERWORLD, "Hot Overworld");
        add(Tags.Biomes.IS_HOT_NETHER, "Hot Nether");
        add(Tags.Biomes.IS_HOT_END, "Hot End");
        add(Tags.Biomes.IS_COLD, "Cold");
        add(Tags.Biomes.IS_COLD_OVERWORLD, "Cold Overworld");
        add(Tags.Biomes.IS_COLD_NETHER, "Cold Nether");
        add(Tags.Biomes.IS_COLD_END, "Cold End");
        add(Tags.Biomes.IS_SPARSE_VEGETATION, "Sparse Vegetation");
        add(Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD, "Sparse Overworld Vegetation");
        add(Tags.Biomes.IS_SPARSE_VEGETATION_NETHER, "Sparse Nether Vegetation");
        add(Tags.Biomes.IS_SPARSE_VEGETATION_END, "Sparse End Vegetation");
        add(Tags.Biomes.IS_DENSE_VEGETATION, "Dense Vegetation");
        add(Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD, "Dense Overworld Vegetation");
        add(Tags.Biomes.IS_DENSE_VEGETATION_NETHER, "Dense Nether Vegetation");
        add(Tags.Biomes.IS_DENSE_VEGETATION_END, "Dense End Vegetation");
        add(Tags.Biomes.IS_WET, "Wet");
        add(Tags.Biomes.IS_WET_OVERWORLD, "Wet Overworld");
        add(Tags.Biomes.IS_WET_NETHER, "Wet Nether");
        add(Tags.Biomes.IS_WET_END, "Wet End");
        add(Tags.Biomes.IS_DRY, "Dry");
        add(Tags.Biomes.IS_DRY_OVERWORLD, "Dry Overworld");
        add(Tags.Biomes.IS_DRY_NETHER, "Dry Nether");
        add(Tags.Biomes.IS_DRY_END, "Dry End");

        add(Tags.Biomes.IS_CONIFEROUS_TREE, "Coniferous Trees");
        add(Tags.Biomes.IS_SAVANNA_TREE, "Savanna Trees");
        add(Tags.Biomes.IS_JUNGLE_TREE, "Jungle Trees");
        add(Tags.Biomes.IS_DECIDUOUS_TREE, "Deciduous Trees");

        add(Tags.Biomes.IS_MOUNTAIN, "Mountains");
        add(Tags.Biomes.IS_MOUNTAIN_PEAK, "Peaks");
        add(Tags.Biomes.IS_MOUNTAIN_SLOPE, "Slopes");

        add(Tags.Biomes.IS_PLAINS, "Plains");
        add(Tags.Biomes.IS_SNOWY_PLAINS, "Snowy Plains");
        add(Tags.Biomes.IS_FOREST, "Forest");
        add(Tags.Biomes.IS_BIRCH_FOREST, "Birch Forest");
        add(Tags.Biomes.IS_FLOWER_FOREST, "Flower Forest");
        add(Tags.Biomes.IS_TAIGA, "Taiga");
        add(Tags.Biomes.IS_HILL, "Hills");
        add(Tags.Biomes.IS_WINDSWEPT, "Windswept");
        add(Tags.Biomes.IS_JUNGLE, "Jungle");
        add(Tags.Biomes.IS_FLORAL, "Floral");
        add(Tags.Biomes.IS_BEACH, "Beach");
        add(Tags.Biomes.IS_STONY_SHORES, "Stony Shores");
        add(Tags.Biomes.IS_SHALLOW_OCEAN, "Shallow Ocean");
        add(Tags.Biomes.IS_OCEAN, "Ocean");
        add(Tags.Biomes.IS_DEEP_OCEAN, "Deep Ocean");
        add(Tags.Biomes.IS_RIVER, "River");
        add(Tags.Biomes.IS_BADLANDS, "Badlands");
        add(Tags.Biomes.IS_SNOWY, "Snowy");
        add(Tags.Biomes.IS_ICY, "Icy");
        add(Tags.Biomes.IS_AQUATIC_ICY, "Aquatic Icy");
        add(Tags.Biomes.IS_SPOOKY, "Spooky");
        add(Tags.Biomes.IS_DEAD, "Dead");
        add(Tags.Biomes.IS_LUSH, "Lush");
        add(Tags.Biomes.IS_MUSHROOM, "Mushrooms");
        add(Tags.Biomes.IS_MAGICAL, "Magical");
        add(Tags.Biomes.IS_RARE, "Rare");
        add(Tags.Biomes.IS_PLATEAU, "Plateaus");
        add(Tags.Biomes.IS_MODIFIED, "Modified");
        add(Tags.Biomes.IS_OLD_GROWTH, "Old Growth");

        add(Tags.Biomes.IS_AQUATIC, "Aquatic");
        add(Tags.Biomes.IS_DESERT, "Deserts");
        add(Tags.Biomes.IS_SAVANNA, "Savanna");
        add(Tags.Biomes.IS_SWAMP, "Swamps");
        add(Tags.Biomes.IS_SANDY, "Sandy");
        add(Tags.Biomes.IS_WASTELAND, "Wastelands");

        add(Tags.Biomes.IS_UNDERGROUND, "Underground");
        add(Tags.Biomes.IS_CAVE, "Caves");

        add(Tags.Biomes.IS_NETHER_FOREST, "Nether Forest");
        add(Tags.Biomes.IS_OUTER_END_ISLAND, "Outer End Island");

        // Structures
        add(Tags.Structures.HIDDEN_FROM_DISPLAYERS, "Hidden From Displayers");
        add(Tags.Structures.HIDDEN_FROM_LOCATOR_SELECTION, "Hidden From Locator's Selection");
    }

    private <T> void addColored(TagKey<T> baseTagKey, String pattern) {
        for (DyeColor color : DyeColor.values()) {
            TagKey<T> coloredTag = TagKey.create(
                    baseTagKey.registry(),
                    new ResourceLocation(baseTagKey.location().getNamespace(), baseTagKey.location().getPath() + "/" + color.name().toLowerCase(Locale.ROOT)));

            add(coloredTag, pattern.replace("{color}", StringUtils.capitalize(color.getName())));
        }
    }

    @Override
    public String getName() {
        return "Forge Translations";
    }
}
