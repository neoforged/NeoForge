/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.common.Tags;
import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

public final class ForgeLanguageProvider extends LanguageProvider
{
    public ForgeLanguageProvider(PackOutput gen)
    {
        super(gen, "neoforge", "en_us");
    }

    @Override
    protected void addTranslations()
    {
        // Blocks
        add(Tags.Blocks.BARRELS, "Barrels");
        add(Tags.Blocks.BARRELS_WOODEN, "Wooden Barrels");
        add(Tags.Blocks.BOOKSHELVES, "Bookshelves");
        add(Tags.Blocks.CHESTS, "Chests");
        add(Tags.Blocks.CHESTS_ENDER, "Ender Chests");
        add(Tags.Blocks.CHESTS_TRAPPED, "Trapped Chests");
        add(Tags.Blocks.CHESTS_WOODEN, "Wooden Chests");
        add(Tags.Blocks.COBBLESTONE, "Cobblestone");
        add(Tags.Blocks.COBBLESTONE_NORMAL, "Normal Cobblestone");
        add(Tags.Blocks.COBBLESTONE_INFESTED, "Infested Cobblestone");
        add(Tags.Blocks.COBBLESTONE_MOSSY, "Mossy Cobblestone");
        add(Tags.Blocks.COBBLESTONE_DEEPSLATE, "Deepslate Cobblestone");
        add(Tags.Blocks.END_STONES, "End Stones");
        add(Tags.Blocks.ENDERMAN_PLACE_ON_BLACKLIST, "Enderman Place On Blacklist");
        add(Tags.Blocks.FENCE_GATES, "Fence Gates");
        add(Tags.Blocks.FENCE_GATES_WOODEN, "Wooden Fence Gates");
        add(Tags.Blocks.FENCES, "Fences");
        add(Tags.Blocks.FENCES_NETHER_BRICK, "Fences Nether Brick");
        add(Tags.Blocks.FENCES_WOODEN, "Wooden Fences");
        add(Tags.Blocks.GLASS, "Glass");
        add(Tags.Blocks.GLASS_COLORLESS, "Colorless Glass");
        add(Tags.Blocks.GLASS_SILICA, "Silica Glass");
        add(Tags.Blocks.GLASS_TINTED, "Tinted Glass");
        addColored(Tags.Blocks.STAINED_GLASS, "{color} Stained Glass");
        add(Tags.Blocks.GLASS_PANES, "Glass Panes");
        add(Tags.Blocks.GLASS_PANES_COLORLESS, "Colorless Glass Panes");
        addColored(Tags.Blocks.STAINED_GLASS_PANES, "{color} Stained Glass Panes");
        add(Tags.Blocks.GRAVEL, "Gravel");
        add(Tags.Blocks.NETHERRACK, "Netherrack");
        add(Tags.Blocks.OBSIDIAN, "Obsidian");
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
        add(Tags.Blocks.SAND, "Sand");
        add(Tags.Blocks.SAND_COLORLESS, "Colorless Sand");
        add(Tags.Blocks.SAND_RED, "Red Sand");
        add(Tags.Blocks.SANDSTONE, "Sandstone");
        add(Tags.Blocks.STONE, "Stone");
        add(Tags.Blocks.STORAGE_BLOCKS, "Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_AMETHYST, "Amethyst Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_COAL, "Coal Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_COPPER, "Copper Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_DIAMOND, "Diamond Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_EMERALD, "Emerald Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_GOLD, "Gold Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_IRON, "Iron Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_LAPIS, "Lapis Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_QUARTZ, "Quartz Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_RAW_COPPER, "Raw Copper Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_RAW_GOLD, "Raw Gold Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_RAW_IRON, "Raw Iron Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_REDSTONE, "Redstone Storage Blocks");
        add(Tags.Blocks.STORAGE_BLOCKS_NETHERITE, "Netherite Storage Blocks");

        // Items

        add(Tags.Items.BARRELS, "Barrels");
        add(Tags.Items.BARRELS_WOODEN, "Wooden Barrels");
        add(Tags.Items.BONES, "Bones");
        add(Tags.Items.BOOKSHELVES, "Bookshelves");
        add(Tags.Items.CHESTS, "Chests");
        add(Tags.Items.CHESTS_ENDER, "Ender Chests");
        add(Tags.Items.CHESTS_TRAPPED, "Trapped Chests");
        add(Tags.Items.CHESTS_WOODEN, "Wooden Chests");
        add(Tags.Items.COBBLESTONE, "Cobblestone");
        add(Tags.Items.COBBLESTONE_NORMAL, "Normal Cobblestone");
        add(Tags.Items.COBBLESTONE_INFESTED, "Infested Cobblestone");
        add(Tags.Items.COBBLESTONE_MOSSY, "Mossy Cobblestone");
        add(Tags.Items.COBBLESTONE_DEEPSLATE, "Deepslate Cobblestone");
        add(Tags.Items.CROPS, "Crops");
        add(Tags.Items.CROPS_BEETROOT, "Beetroot Crops");
        add(Tags.Items.CROPS_CARROT, "Carrot Crops");
        add(Tags.Items.CROPS_NETHER_WART, "Nether Wart Crops");
        add(Tags.Items.CROPS_POTATO, "Potato Crops");
        add(Tags.Items.CROPS_WHEAT, "Wheat Crops");
        add(Tags.Items.DUSTS, "Dusts");
        add(Tags.Items.DUSTS_GLOWSTONE, "Glowstone Dusts");
        add(Tags.Items.DUSTS_PRISMARINE, "Prismarine Dusts");
        add(Tags.Items.DUSTS_REDSTONE, "Redstone Dusts");
        addColored(Tags.Items.DYES, "{color} Dyes");
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
        add(Tags.Items.GEMS, "Gems");
        add(Tags.Items.GEMS_AMETHYST, "Amethyst Gems");
        add(Tags.Items.GEMS_DIAMOND, "Diamond Gems");
        add(Tags.Items.GEMS_EMERALD, "Emerald Gems");
        add(Tags.Items.GEMS_LAPIS, "Lapis Gems");
        add(Tags.Items.GEMS_PRISMARINE, "Prismarine Gems");
        add(Tags.Items.GEMS_QUARTZ, "Quartz Gems");
        add(Tags.Items.GLASS, "Glass");
        add(Tags.Items.GLASS_TINTED, "Tinted Glass");
        add(Tags.Items.GLASS_SILICA, "Silica Glass");
        addColored(Tags.Blocks.GLASS, "{color} Glass");
        add(Tags.Items.GLASS_PANES, "Glass Panes");
        addColored(Tags.Blocks.GLASS_PANES, "{color} Glass Panes");
        add(Tags.Items.GRAVEL, "Gravel");
        add(Tags.Items.GUNPOWDER, "Gunpowder");
        add(Tags.Items.HEADS, "Heads");
        add(Tags.Items.INGOTS, "Ingots");
        add(Tags.Items.INGOTS_BRICK, "Brick Ingots");
        add(Tags.Items.INGOTS_COPPER, "Copper Ingots");
        add(Tags.Items.INGOTS_GOLD, "Gold Ingots");
        add(Tags.Items.INGOTS_IRON, "Iron Ingots");
        add(Tags.Items.INGOTS_NETHERITE, "Netherite Ingots");
        add(Tags.Items.INGOTS_NETHER_BRICK, "Nether Brick Ingots");
        add(Tags.Items.LEATHER, "Leather");
        add(Tags.Items.MUSHROOMS, "Mushrooms");
        add(Tags.Items.NETHER_STARS, "Nether Stars");
        add(Tags.Items.NETHERRACK, "Netherrack");
        add(Tags.Items.NUGGETS, "Nuggets");
        add(Tags.Items.NUGGETS_IRON, "Iron Nuggets");
        add(Tags.Items.NUGGETS_GOLD, "Gold Nuggets");
        add(Tags.Items.OBSIDIAN, "Obsidian");
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
        add(Tags.Items.RAW_MATERIALS, "Raw Materials");
        add(Tags.Items.RAW_MATERIALS_COPPER, "Copper Raw Materials");
        add(Tags.Items.RAW_MATERIALS_GOLD, "Gold Raw Materials");
        add(Tags.Items.RAW_MATERIALS_IRON, "Iron Raw Materials");
        add(Tags.Items.RODS, "Rods");
        add(Tags.Items.RODS_BLAZE, "Blaze Rods");
        add(Tags.Items.RODS_WOODEN, "Wooden Rods");
        add(Tags.Items.SAND, "Sand");
        add(Tags.Items.SAND_COLORLESS, "Colorless Sand");
        add(Tags.Items.SAND_RED, "Red Sand");
        add(Tags.Items.SANDSTONE, "Sandstone");
        add(Tags.Items.SEEDS, "Seeds");
        add(Tags.Items.SEEDS_BEETROOT, "Beetroot Seeds");
        add(Tags.Items.SEEDS_MELON, "Melon Seeds");
        add(Tags.Items.SEEDS_PUMPKIN, "Pumpkin Seeds");
        add(Tags.Items.SEEDS_WHEAT, "Wheat Seeds");
        add(Tags.Items.SHEARS, "Shears");
        add(Tags.Items.SLIMEBALLS, "Slimeballs");
        add(Tags.Items.STAINED_GLASS, "Stained Glass");
        add(Tags.Items.STAINED_GLASS_PANES, "Stained Glass Panes");
        add(Tags.Items.STONE, "Stone");
        add(Tags.Items.STORAGE_BLOCKS, "Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_AMETHYST, "Amethyst Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_COAL, "Coal Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_COPPER, "Copper Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_DIAMOND, "Diamond Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_EMERALD, "Emerald Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_GOLD, "Gold Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_IRON, "Iron Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_LAPIS, "Lapis Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_QUARTZ, "Quartz Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_REDSTONE, "Redstone Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_RAW_COPPER, "Raw Copper Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_RAW_GOLD, "Raw Gold Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_RAW_IRON, "Raw Iron Storage Blocks");
        add(Tags.Items.STORAGE_BLOCKS_NETHERITE, "Netherite Storage Blocks");
        add(Tags.Items.STRING, "String");
        add(Tags.Items.TOOLS_SHIELDS, "Shields Tools");
        add(Tags.Items.TOOLS_BOWS, "Bows Tools");
        add(Tags.Items.TOOLS_CROSSBOWS, "Crossbows Tools");
        add(Tags.Items.TOOLS_FISHING_RODS, "Fishing Rods Tools");
        add(Tags.Items.TOOLS_TRIDENTS, "Tridents Tools");
        add(Tags.Items.TOOLS, "Tools");
        add(Tags.Items.ARMORS_HELMETS, "Helmets Armors");
        add(Tags.Items.ARMORS_CHESTPLATES, "Chestplates Armors");
        add(Tags.Items.ARMORS_LEGGINGS, "Leggings Armors");
        add(Tags.Items.ARMORS_BOOTS, "Boots Armors");
        add(Tags.Items.ARMORS, "Armors");

        // Fluids
        add(Tags.Fluids.MILK, "Milk");

        // Entities
        add(Tags.EntityTypes.BOSSES, "Bosses");

        // Biomes
        add(Tags.Biomes.IS_HOT, "Hot");
        add(Tags.Biomes.IS_HOT_OVERWORLD, "Hot Overworld");
        add(Tags.Biomes.IS_HOT_NETHER, "Hot Nether");
        add(Tags.Biomes.IS_HOT_END, "Hot End");
        add(Tags.Biomes.IS_COLD, "Cold");
        add(Tags.Biomes.IS_COLD_OVERWORLD, "Cold Overworld");
        add(Tags.Biomes.IS_COLD_NETHER, "Cold Nether");
        add(Tags.Biomes.IS_COLD_END, "Cold End");
        add(Tags.Biomes.IS_SPARSE, "Sparse");
        add(Tags.Biomes.IS_SPARSE_OVERWORLD, "Sparse Overworld");
        add(Tags.Biomes.IS_SPARSE_NETHER, "Sparse Nether");
        add(Tags.Biomes.IS_SPARSE_END, "Sparse End");
        add(Tags.Biomes.IS_DENSE, "Dense");
        add(Tags.Biomes.IS_DENSE_OVERWORLD, "Dense Overworld");
        add(Tags.Biomes.IS_DENSE_NETHER, "Dense Nether");
        add(Tags.Biomes.IS_DENSE_END, "Dense End");
        add(Tags.Biomes.IS_WET, "Wet");
        add(Tags.Biomes.IS_WET_OVERWORLD, "Wet Overworld");
        add(Tags.Biomes.IS_WET_NETHER, "Wet Nether");
        add(Tags.Biomes.IS_WET_END, "Wet End");
        add(Tags.Biomes.IS_DRY, "Dry");
        add(Tags.Biomes.IS_DRY_OVERWORLD, "Dry Overworld");
        add(Tags.Biomes.IS_DRY_NETHER, "Dry Nether");
        add(Tags.Biomes.IS_DRY_END, "Dry End");

        add(Tags.Biomes.IS_CONIFEROUS, "Coniferous");
        add(Tags.Biomes.IS_SPOOKY, "Spooky");
        add(Tags.Biomes.IS_DEAD, "Dead");
        add(Tags.Biomes.IS_LUSH, "Lush");
        add(Tags.Biomes.IS_MUSHROOM, "Mushroom");
        add(Tags.Biomes.IS_MAGICAL, "Magical");
        add(Tags.Biomes.IS_RARE, "Rare");
        add(Tags.Biomes.IS_PLATEAU, "Plateau");
        add(Tags.Biomes.IS_MODIFIED, "Modified");

        add(Tags.Biomes.IS_WATER, "Water");
        add(Tags.Biomes.IS_DESERT, "Desert");
        add(Tags.Biomes.IS_PLAINS, "Plains");
        add(Tags.Biomes.IS_SWAMP, "Swamp");
        add(Tags.Biomes.IS_SANDY, "Sandy");
        add(Tags.Biomes.IS_SNOWY, "Snowy");
        add(Tags.Biomes.IS_WASTELAND, "Wasteland");
        add(Tags.Biomes.IS_VOID, "Void");

        add(Tags.Biomes.IS_UNDERGROUND, "Underground");
        add(Tags.Biomes.IS_CAVE, "Cave");
        add(Tags.Biomes.IS_MOUNTAIN_PEAK, "Peak");
        add(Tags.Biomes.IS_MOUNTAIN_SLOPE, "Slope");
        add(Tags.Biomes.IS_MOUNTAIN, "Mountain");
    }

    private <T> void addColored(TagKey<T> baseTagKey, String pattern)
    {
        for (DyeColor color  : DyeColor.values())
        {
            TagKey<T> coloredTag = TagKey.create(
                    baseTagKey.registry(),
                    new ResourceLocation(baseTagKey.location().getNamespace(), baseTagKey.location().getPath() + "/" + color.name().toLowerCase(Locale.ROOT)));

            add(coloredTag, pattern.replace("{color}", StringUtils.capitalize(color.getName())));
        }
    }

    @Override
    public String getName()
    {
        return "Forge Translations";
    }
}
