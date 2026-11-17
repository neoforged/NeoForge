/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.references.BlockItemId;
import net.minecraft.references.BlockItemIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockItemTagId;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColorCollection;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;

public final class NeoForgeItemTagsProvider extends BlockTagCopyingItemTagProvider {
    public NeoForgeItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, lookupProvider, blockTags, "neoforge");
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTags(HolderLookup.Provider lookupProvider) {
        copy(Tags.BlockItems.BARRELS);
        copy(Tags.BlockItems.BARRELS_WOODEN);
        copy(Tags.BlockItems.BARS);
        copy(Tags.BlockItems.BARS_COPPER);
        copy(Tags.BlockItems.BARS_IRON);
        tag(Tags.Items.BONES).add(Items.BONE);
        copy(Tags.BlockItems.BOOKSHELVES);
        tag(Tags.Items.BRICKS).addTags(Tags.Items.BRICKS_NORMAL, Tags.Items.BRICKS_NETHER, Tags.Items.BRICKS_RESIN);
        tag(Tags.Items.BRICKS_NORMAL).add(Items.BRICK);
        tag(Tags.Items.BRICKS_NETHER).add(Items.NETHER_BRICK);
        tag(Tags.Items.BRICKS_RESIN).add(Items.RESIN_BRICK);
        tag(Tags.Items.BUCKETS_EMPTY).add(Items.BUCKET);
        tag(Tags.Items.BUCKETS_WATER).add(Items.WATER_BUCKET);
        tag(Tags.Items.BUCKETS_LAVA).add(Items.LAVA_BUCKET);
        tag(Tags.Items.BUCKETS_MILK).add(Items.MILK_BUCKET);
        tag(Tags.Items.BUCKETS_POWDER_SNOW).add(Items.POWDER_SNOW_BUCKET);
        tag(Tags.Items.BUCKETS_ENTITY_WATER).add(Items.AXOLOTL_BUCKET, Items.COD_BUCKET, Items.PUFFERFISH_BUCKET, Items.TADPOLE_BUCKET, Items.TROPICAL_FISH_BUCKET, Items.SALMON_BUCKET);
        tag(Tags.Items.BUCKETS_ENTITY_DRY).add(Items.SULFUR_CUBE_BUCKET);
        tag(Tags.Items.BUCKETS).addTags(Tags.Items.BUCKETS_EMPTY, Tags.Items.BUCKETS_WATER, Tags.Items.BUCKETS_LAVA, Tags.Items.BUCKETS_MILK, Tags.Items.BUCKETS_POWDER_SNOW, Tags.Items.BUCKETS_ENTITY_WATER, Tags.Items.BUCKETS_ENTITY_DRY);
        copy(Tags.BlockItems.BUDDING_BLOCKS);
        copy(Tags.BlockItems.BUDS);
        copy(Tags.BlockItems.CHAINS);
        copy(Tags.BlockItems.CHESTS);
        copy(Tags.BlockItems.CHESTS_ENDER);
        copy(Tags.BlockItems.CHESTS_TRAPPED);
        copy(Tags.BlockItems.CHESTS_WOODEN);
        copy(Tags.BlockItems.CLUSTERS);
        tag(Tags.Items.CLUMPS).addTags(Tags.Items.CLUMPS_RESIN);
        tag(Tags.Items.CLUMPS_RESIN).add(Items.RESIN_CLUMP);
        copy(Tags.BlockItems.COBBLESTONES);
        copy(Tags.BlockItems.COBBLESTONES_NORMAL);
        copy(Tags.BlockItems.COBBLESTONES_INFESTED);
        copy(Tags.BlockItems.COBBLESTONES_MOSSY);
        copy(Tags.BlockItems.COBBLESTONES_DEEPSLATE);
        copy(Tags.BlockItems.CONCRETES);
        var concretePowder = tag(Tags.Items.CONCRETE_POWDERS);
        Items.CONCRETE_POWDER.forEach(concretePowder::add);
        tag(Tags.Items.CROPS).addTags(
                Tags.Items.CROPS_BEETROOT, Tags.Items.CROPS_CACTUS, Tags.Items.CROPS_CARROT,
                Tags.Items.CROPS_COCOA_BEAN, Tags.Items.CROPS_MELON, Tags.Items.CROPS_NETHER_WART,
                Tags.Items.CROPS_POTATO, Tags.Items.CROPS_PUMPKIN, Tags.Items.CROPS_SUGAR_CANE,
                Tags.Items.CROPS_WHEAT);
        tag(Tags.Items.CROPS_BEETROOT).add(Items.BEETROOT);
        tag(Tags.Items.CROPS_CACTUS).add(Items.CACTUS);
        tag(Tags.Items.CROPS_CARROT).add(Items.CARROT);
        tag(Tags.Items.CROPS_COCOA_BEAN).add(Items.COCOA_BEANS);
        tag(Tags.Items.CROPS_MELON).add(Items.MELON);
        tag(Tags.Items.CROPS_NETHER_WART).add(Items.NETHER_WART);
        tag(Tags.Items.CROPS_POTATO).add(Items.POTATO);
        tag(Tags.Items.CROPS_PUMPKIN).add(Items.PUMPKIN);
        tag(Tags.Items.CROPS_SUGAR_CANE).add(Items.SUGAR_CANE);
        tag(Tags.Items.CROPS_WHEAT).add(Items.WHEAT);
        tag(Tags.Items.DRINK_CONTAINING_BOTTLE).add(Items.POTION, Items.HONEY_BOTTLE, Items.OMINOUS_BOTTLE);
        tag(Tags.Items.DRINK_CONTAINING_BUCKET).add(Items.MILK_BUCKET);
        tag(Tags.Items.DRINKS).addTags(
                Tags.Items.DRINKS_WATER, Tags.Items.DRINKS_WATERY, Tags.Items.DRINKS_MILK,
                Tags.Items.DRINKS_HONEY, Tags.Items.DRINKS_MAGIC, Tags.Items.DRINKS_OMINOUS,
                Tags.Items.DRINKS_JUICE);
        tag(Tags.Items.DRINKS_WATER);
        tag(Tags.Items.DRINKS_WATERY).add(Items.POTION).addTags(Tags.Items.DRINKS_WATER);
        tag(Tags.Items.DRINKS_MILK).add(Items.MILK_BUCKET);
        tag(Tags.Items.DRINKS_HONEY).add(Items.HONEY_BOTTLE);
        tag(Tags.Items.DRINKS_MAGIC).add(Items.POTION).addTags(Tags.Items.DRINKS_OMINOUS);
        tag(Tags.Items.DRINKS_OMINOUS).add(Items.OMINOUS_BOTTLE);
        tag(Tags.Items.DRINKS_JUICE);

        ColorCollection<NeoForgeItemTagsProvider.Appender> builders = Tags.BlockItems.DYED_COLORS.map(BlockItemTagId::item).map(this::tag);
        for (ColorCollection<BlockItemId> colorCollection : NeoForgeBlockTagsProvider.dyeableBlockItems()) {
            ColorCollection.zipApply(builders, colorCollection, NeoForgeItemTagsProvider.Appender::add);
        }
        var dyeableItems = List.of(ItemIds.DYED_BUNDLE, ItemIds.CUSHION, ItemIds.HARNESS);
        for (ColorCollection<ResourceKey<Item>> colorCollection : dyeableItems) {
            ColorCollection.zipApply(builders, colorCollection, NeoForgeItemTagsProvider.Appender::add);
        }
        addColoredTags(tag(Tags.BlockItems.DYED.item())::addTag, Tags.BlockItems.DYED.item());

        ColorCollection<NeoForgeItemTagsProvider.Appender> dyesBuilders = Tags.Items.DYE_COLORS.map(this::tag);
        ColorCollection.zipApply(dyesBuilders, ItemIds.DYE, NeoForgeItemTagsProvider.Appender::add);
        addColoredTags(tag(Tags.Items.DYES)::addTags, Tags.Items.DYES);
        tag(Tags.BlockItems.DYEABLE_UNDYED_SIMPLE)
                .add(Items.BUNDLE)
                .add(Items.CANDLE)
                .add(Items.GLASS)
                .add(Items.GLASS_PANE)
                .add(Items.SHULKER_BOX)
                .add(Items.TERRACOTTA);
        tag(Tags.BlockItems.DYEABLE_REDYEABLE_SIMPLE)
                .addOptionalTag(BlockItemTags.BEDS)
                .addAll(Items.DYED_BUNDLE)
                .addOptionalTag(ItemTags.CUSHIONS)
                .addOptionalTag(ItemTags.HARNESSES)
                .addOptionalTag(BlockItemTags.WOOL)
                .addOptionalTag(BlockItemTags.WOOL_CARPETS)
                .addOptionalTag(BlockItemTags.WOOL_SLABS)
                .addOptionalTag(BlockItemTags.WOOL_STAIRS)
                .addAll(BlockItemIds.DYED_SHULKER_BOX.map(BlockItemId::item).asList());
        tag(Tags.BlockItems.DYEABLE_SIMPLE)
                .addTag(Tags.BlockItems.DYEABLE_UNDYED_SIMPLE)
                .addTag(Tags.BlockItems.DYEABLE_REDYEABLE_SIMPLE);
        tag(Tags.BlockItems.DYEABLE_DYNAMIC)
                .add(Items.LEATHER_HELMET)
                .add(Items.LEATHER_CHESTPLATE)
                .add(Items.LEATHER_LEGGINGS)
                .add(Items.LEATHER_BOOTS)
                .add(Items.LEATHER_HORSE_ARMOR)
                .add(Items.WOLF_ARMOR)
                .add(Items.FIREWORK_STAR);
        tag(Tags.BlockItems.DYEABLE)
                .addTag(Tags.BlockItems.DYEABLE_SIMPLE)
                .addTag(Tags.BlockItems.DYEABLE_DYNAMIC);

        tag(Tags.Items.DUSTS).addTags(Tags.Items.DUSTS_GLOWSTONE, Tags.Items.DUSTS_REDSTONE);
        tag(Tags.Items.DUSTS_GLOWSTONE).add(Items.GLOWSTONE_DUST);
        tag(Tags.Items.DUSTS_REDSTONE).add(Items.REDSTONE);
        tag(Tags.Items.EGGS).add(Items.EGG, Items.BROWN_EGG, Items.BLUE_EGG);
        tag(Tags.Items.ENCHANTING_FUELS).addTag(Tags.Items.GEMS_LAPIS);
        copy(Tags.BlockItems.END_STONES);
        tag(Tags.Items.ENDER_PEARLS).add(Items.ENDER_PEARL);
        tag(Tags.Items.FEATHERS).add(Items.FEATHER);
        copy(Tags.BlockItems.FENCE_GATES);
        copy(Tags.BlockItems.FENCE_GATES_WOODEN);
        copy(Tags.BlockItems.FENCES);
        copy(Tags.BlockItems.FENCES_NETHER_BRICK);
        copy(Tags.BlockItems.FENCES_WOODEN);
        tag(Tags.Items.FERTILIZERS).add(Items.BONE_MEAL);
        copy(Tags.BlockItems.FLOWERS_SMALL);
        copy(Tags.BlockItems.FLOWERS_TALL);
        copy(Tags.BlockItems.FLOWERS);
        tag(Tags.Items.FOODS_FRUIT).add(Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, Items.CHORUS_FRUIT, Items.MELON_SLICE);
        tag(Tags.Items.FOODS_VEGETABLE).add(Items.CARROT, Items.GOLDEN_CARROT, Items.POTATO, Items.BEETROOT);
        tag(Tags.Items.FOODS_BERRY).add(Items.SWEET_BERRIES, Items.GLOW_BERRIES);
        tag(Tags.Items.FOODS_BREAD).add(Items.BREAD);
        tag(Tags.Items.FOODS_COOKIE).add(Items.COOKIE);
        tag(Tags.Items.FOODS_DOUGH);
        tag(Tags.Items.FOODS_RAW_MEAT).add(Items.BEEF, Items.PORKCHOP, Items.CHICKEN, Items.RABBIT, Items.MUTTON);
        tag(Tags.Items.FOODS_RAW_FISH).add(Items.COD, Items.SALMON, Items.TROPICAL_FISH, Items.PUFFERFISH);
        tag(Tags.Items.FOODS_COOKED_MEAT).add(Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_CHICKEN, Items.COOKED_RABBIT, Items.COOKED_MUTTON);
        tag(Tags.Items.FOODS_COOKED_FISH).add(Items.COOKED_COD, Items.COOKED_SALMON);
        tag(Tags.Items.FOODS_SOUP).add(Items.BEETROOT_SOUP, Items.MUSHROOM_STEW, Items.RABBIT_STEW, Items.SUSPICIOUS_STEW);
        tag(Tags.Items.FOODS_CANDY);
        tag(Tags.Items.FOODS_PIE).add(Items.PUMPKIN_PIE);
        tag(Tags.Items.FOODS_GOLDEN).add(Items.GOLDEN_APPLE).add(Items.ENCHANTED_GOLDEN_APPLE).add(Items.GOLDEN_CARROT);
        tag(Tags.Items.FOODS_EDIBLE_WHEN_PLACED).add(Items.CAKE);
        tag(Tags.Items.FOODS_FOOD_POISONING).add(Items.POISONOUS_POTATO, Items.PUFFERFISH, Items.SPIDER_EYE, Items.CHICKEN, Items.ROTTEN_FLESH);
        tag(Tags.Items.FOODS)
                .add(Items.BAKED_POTATO, Items.PUMPKIN_PIE, Items.HONEY_BOTTLE, Items.OMINOUS_BOTTLE, Items.DRIED_KELP)
                .addTags(Tags.Items.FOODS_FRUIT, Tags.Items.FOODS_VEGETABLE, Tags.Items.FOODS_BERRY, Tags.Items.FOODS_BREAD,
                        Tags.Items.FOODS_COOKIE, Tags.Items.FOODS_DOUGH, Tags.Items.FOODS_RAW_MEAT, Tags.Items.FOODS_RAW_FISH,
                        Tags.Items.FOODS_COOKED_MEAT, Tags.Items.FOODS_COOKED_FISH, Tags.Items.FOODS_SOUP, Tags.Items.FOODS_CANDY,
                        Tags.Items.FOODS_PIE, Tags.Items.FOODS_GOLDEN, Tags.Items.FOODS_EDIBLE_WHEN_PLACED, Tags.Items.FOODS_FOOD_POISONING);
        tag(Tags.Items.ANIMAL_FOODS)
                .addTags(ItemTags.ARMADILLO_FOOD, ItemTags.AXOLOTL_FOOD, ItemTags.BEE_FOOD, ItemTags.CAMEL_FOOD,
                        ItemTags.CAT_FOOD, ItemTags.CHICKEN_FOOD, ItemTags.COW_FOOD, ItemTags.FOX_FOOD, ItemTags.FROG_FOOD,
                        ItemTags.GOAT_FOOD, ItemTags.HOGLIN_FOOD, ItemTags.HORSE_FOOD, ItemTags.LLAMA_FOOD, ItemTags.OCELOT_FOOD,
                        ItemTags.PANDA_FOOD, ItemTags.PARROT_FOOD, ItemTags.PIG_FOOD, ItemTags.PIGLIN_FOOD, ItemTags.RABBIT_FOOD,
                        ItemTags.SHEEP_FOOD, ItemTags.SNIFFER_FOOD, ItemTags.STRIDER_FOOD, ItemTags.TURTLE_FOOD, ItemTags.WOLF_FOOD);
        copy(Tags.BlockItems.FROGLIGHTS);
        tag(Tags.Items.GEMS).addTags(Tags.Items.GEMS_AMETHYST, Tags.Items.GEMS_DIAMOND, Tags.Items.GEMS_EMERALD, Tags.Items.GEMS_LAPIS, Tags.Items.GEMS_PRISMARINE, Tags.Items.GEMS_QUARTZ);
        tag(Tags.Items.GEMS_AMETHYST).add(Items.AMETHYST_SHARD);
        tag(Tags.Items.GEMS_DIAMOND).add(Items.DIAMOND);
        tag(Tags.Items.GEMS_EMERALD).add(Items.EMERALD);
        tag(Tags.Items.GEMS_LAPIS).add(Items.LAPIS_LAZULI);
        tag(Tags.Items.GEMS_PRISMARINE).add(Items.PRISMARINE_CRYSTALS);
        tag(Tags.Items.GEMS_QUARTZ).add(Items.QUARTZ);
        copy(Tags.BlockItems.GLASS_BLOCKS);
        copy(Tags.BlockItems.GLASS_BLOCKS_COLORLESS);
        copy(Tags.BlockItems.GLASS_BLOCKS_TINTED);
        copy(Tags.BlockItems.GLASS_BLOCKS_CHEAP);
        copy(Tags.BlockItems.GLASS_PANES);
        copy(Tags.BlockItems.GLASS_PANES_COLORLESS);
        copy(Tags.BlockItems.GLAZED_TERRACOTTAS);
        copy(Tags.BlockItems.GRAVELS);
        tag(Tags.Items.GUNPOWDERS).add(Items.GUNPOWDER);
        tag(Tags.Items.HIDDEN_FROM_RECIPE_VIEWERS);
        tag(Tags.BlockItems.ICE).add(Items.ICE, Items.PACKED_ICE, Items.BLUE_ICE);
        tag(Tags.Items.INGOTS).addTags(Tags.Items.INGOTS_COPPER, Tags.Items.INGOTS_GOLD, Tags.Items.INGOTS_IRON, Tags.Items.INGOTS_NETHERITE);
        tag(Tags.Items.INGOTS_COPPER).add(Items.COPPER_INGOT);
        tag(Tags.Items.INGOTS_GOLD).add(Items.GOLD_INGOT);
        tag(Tags.Items.INGOTS_IRON).add(Items.IRON_INGOT);
        tag(Tags.Items.INGOTS_NETHERITE).add(Items.NETHERITE_INGOT);
        tag(Tags.Items.LEATHERS).add(Items.LEATHER);
        tag(Tags.Items.MUSHROOMS).addTags(ItemTags.MUSHROOMS);
        tag(Tags.Items.MUSIC_DISCS).add(
                Items.MUSIC_DISC_13, Items.MUSIC_DISC_CAT, Items.MUSIC_DISC_BLOCKS, Items.MUSIC_DISC_CHIRP, Items.MUSIC_DISC_FAR,
                Items.MUSIC_DISC_MALL, Items.MUSIC_DISC_MELLOHI, Items.MUSIC_DISC_STAL, Items.MUSIC_DISC_STRAD, Items.MUSIC_DISC_WARD,
                Items.MUSIC_DISC_11, Items.MUSIC_DISC_WAIT, Items.MUSIC_DISC_OTHERSIDE, Items.MUSIC_DISC_5, Items.MUSIC_DISC_PIGSTEP,
                Items.MUSIC_DISC_RELIC, Items.MUSIC_DISC_CREATOR, Items.MUSIC_DISC_CREATOR_MUSIC_BOX, Items.MUSIC_DISC_PRECIPICE,
                Items.MUSIC_DISC_TEARS, Items.MUSIC_DISC_LAVA_CHICKEN, Items.MUSIC_DISC_BOUNCE);
        tag(Tags.Items.NETHER_STARS).add(Items.NETHER_STAR);
        copy(Tags.BlockItems.NETHERRACKS);
        tag(Tags.Items.NUGGETS).addTags(Tags.Items.NUGGETS_COPPER, Tags.Items.NUGGETS_IRON, Tags.Items.NUGGETS_GOLD);
        tag(Tags.Items.NUGGETS_COPPER).add(Items.COPPER_NUGGET);
        tag(Tags.Items.NUGGETS_IRON).add(Items.IRON_NUGGET);
        tag(Tags.Items.NUGGETS_GOLD).add(Items.GOLD_NUGGET);
        copy(Tags.BlockItems.OBSIDIANS);
        copy(Tags.BlockItems.ORE_BEARING_GROUND_DEEPSLATE);
        copy(Tags.BlockItems.ORE_BEARING_GROUND_NETHERRACK);
        copy(Tags.BlockItems.ORE_BEARING_GROUND_STONE);
        copy(Tags.BlockItems.ORE_RATES_DENSE);
        copy(Tags.BlockItems.ORE_RATES_SINGULAR);
        copy(Tags.BlockItems.ORE_RATES_SPARSE);
        copy(Tags.BlockItems.ORES);
        copy(Tags.BlockItems.ORES_COAL);
        copy(Tags.BlockItems.ORES_COPPER);
        copy(Tags.BlockItems.ORES_DIAMOND);
        copy(Tags.BlockItems.ORES_EMERALD);
        copy(Tags.BlockItems.ORES_GOLD);
        copy(Tags.BlockItems.ORES_IRON);
        copy(Tags.BlockItems.ORES_LAPIS);
        copy(Tags.BlockItems.ORES_QUARTZ);
        copy(Tags.BlockItems.ORES_REDSTONE);
        copy(Tags.BlockItems.ORES_NETHERITE_SCRAP);
        copy(Tags.BlockItems.ORES_IN_GROUND_DEEPSLATE);
        copy(Tags.BlockItems.ORES_IN_GROUND_NETHERRACK);
        copy(Tags.BlockItems.ORES_IN_GROUND_STONE);
        tag(Tags.Items.PIGLIN_USABLE_CROSSBOWS).addTag(Tags.Items.TOOLS_CROSSBOW);
        tag(Tags.Items.PILLAGER_USABLE_CROSSBOWS).addTags(Tags.Items.TOOLS_CROSSBOW);
        copy(Tags.BlockItems.PLAYER_WORKSTATIONS_CRAFTING_TABLES);
        copy(Tags.BlockItems.PLAYER_WORKSTATIONS_FURNACES);
        tag(Tags.Items.POTIONS_BOTTLE).add(Items.POTION, Items.SPLASH_POTION, Items.LINGERING_POTION);
        tag(Tags.Items.POTIONS).addTags(Tags.Items.POTIONS_BOTTLE);
        copy(Tags.BlockItems.OBSIDIANS_NORMAL);
        copy(Tags.BlockItems.OBSIDIANS_CRYING);
        copy(Tags.BlockItems.PUMPKINS);
        copy(Tags.BlockItems.PUMPKINS_NORMAL);
        copy(Tags.BlockItems.PUMPKINS_CARVED);
        copy(Tags.BlockItems.PUMPKINS_JACK_O_LANTERNS);
        tag(Tags.Items.RAW_MATERIALS).addTags(Tags.Items.RAW_MATERIALS_COPPER, Tags.Items.RAW_MATERIALS_GOLD, Tags.Items.RAW_MATERIALS_IRON);
        tag(Tags.Items.RAW_MATERIALS_COPPER).add(Items.RAW_COPPER);
        tag(Tags.Items.RAW_MATERIALS_GOLD).add(Items.RAW_GOLD);
        tag(Tags.Items.RAW_MATERIALS_IRON).add(Items.RAW_IRON);
        tag(Tags.Items.RODS).addTags(Tags.Items.RODS_WOODEN, Tags.Items.RODS_BLAZE, Tags.Items.RODS_BREEZE);
        tag(Tags.Items.RODS_BLAZE).add(Items.BLAZE_ROD);
        tag(Tags.Items.RODS_BREEZE).add(Items.BREEZE_ROD);
        tag(Tags.Items.RODS_WOODEN).add(Items.STICK);
        copy(Tags.BlockItems.ROPES);
        copy(Tags.BlockItems.SANDS);
        copy(Tags.BlockItems.SANDS_COLORLESS);
        copy(Tags.BlockItems.SANDS_RED);
        copy(Tags.BlockItems.SANDSTONE_BLOCKS);
        copy(Tags.BlockItems.SANDSTONE_SLABS);
        copy(Tags.BlockItems.SANDSTONE_STAIRS);
        copy(Tags.BlockItems.SANDSTONE_RED_BLOCKS);
        copy(Tags.BlockItems.SANDSTONE_RED_SLABS);
        copy(Tags.BlockItems.SANDSTONE_RED_STAIRS);
        copy(Tags.BlockItems.SANDSTONE_UNCOLORED_BLOCKS);
        copy(Tags.BlockItems.SANDSTONE_UNCOLORED_SLABS);
        copy(Tags.BlockItems.SANDSTONE_UNCOLORED_STAIRS);
        tag(Tags.Items.SEEDS).addTags(Tags.Items.SEEDS_BEETROOT, Tags.Items.SEEDS_MELON, Tags.Items.SEEDS_PUMPKIN, Tags.Items.SEEDS_TORCHFLOWER, Tags.Items.SEEDS_PITCHER_PLANT, Tags.Items.SEEDS_WHEAT);
        tag(Tags.Items.SEEDS_BEETROOT).add(Items.BEETROOT_SEEDS);
        tag(Tags.Items.SEEDS_MELON).add(Items.MELON_SEEDS);
        tag(Tags.Items.SEEDS_PUMPKIN).add(Items.PUMPKIN_SEEDS);
        tag(Tags.Items.SEEDS_TORCHFLOWER).add(Items.TORCHFLOWER_SEEDS);
        tag(Tags.Items.SEEDS_PITCHER_PLANT).add(Items.PITCHER_POD);
        tag(Tags.Items.SEEDS_WHEAT).add(Items.WHEAT_SEEDS);
        tag(Tags.Items.SKELETON_USABLE_BOWS).addTag(Tags.Items.TOOLS_BOW);
        tag(Tags.Items.SLIME_BALLS).add(Items.SLIME_BALL);
        var shulker = tag(Tags.Items.SHULKER_BOXES).add(Items.SHULKER_BOX);
        Items.DYED_SHULKER_BOX.forEach(shulker::add);
        copy(Tags.BlockItems.STONES);
        copy(Tags.BlockItems.STORAGE_BLOCKS);
        copy(Tags.BlockItems.STORAGE_BLOCKS_BONE_MEAL);
        copy(Tags.BlockItems.STORAGE_BLOCKS_COAL);
        copy(Tags.BlockItems.STORAGE_BLOCKS_COPPER);
        copy(Tags.BlockItems.STORAGE_BLOCKS_DIAMOND);
        copy(Tags.BlockItems.STORAGE_BLOCKS_DRIED_KELP);
        copy(Tags.BlockItems.STORAGE_BLOCKS_EMERALD);
        copy(Tags.BlockItems.STORAGE_BLOCKS_GOLD);
        copy(Tags.BlockItems.STORAGE_BLOCKS_IRON);
        copy(Tags.BlockItems.STORAGE_BLOCKS_LAPIS);
        copy(Tags.BlockItems.STORAGE_BLOCKS_NETHERITE);
        copy(Tags.BlockItems.STORAGE_BLOCKS_RAW_COPPER);
        copy(Tags.BlockItems.STORAGE_BLOCKS_RAW_GOLD);
        copy(Tags.BlockItems.STORAGE_BLOCKS_RAW_IRON);
        copy(Tags.BlockItems.STORAGE_BLOCKS_REDSTONE);
        copy(Tags.BlockItems.STORAGE_BLOCKS_RESIN);
        copy(Tags.BlockItems.STORAGE_BLOCKS_SLIME);
        copy(Tags.BlockItems.STORAGE_BLOCKS_WHEAT);
        tag(Tags.Items.STRINGS).add(Items.STRING);
        copy(Tags.BlockItems.OVERWORLD_NATURAL_LOGS);
        copy(Tags.BlockItems.NETHER_NATURAL_LOGS);
        copy(Tags.BlockItems.NATURAL_LOGS);
        copy(Tags.BlockItems.NATURAL_WOODS);
        copy(Tags.BlockItems.STRIPPED_LOGS);
        copy(Tags.BlockItems.STRIPPED_WOODS);
        tag(Tags.Items.VILLAGER_JOB_SITES).add(
                Items.BARREL, Items.BLAST_FURNACE, Items.BREWING_STAND, Items.CARTOGRAPHY_TABLE,
                Items.CAULDRON, Items.COMPOSTER, Items.FLETCHING_TABLE, Items.GRINDSTONE,
                Items.LECTERN, Items.LOOM, Items.SMITHING_TABLE, Items.SMOKER, Items.STONECUTTER);

        // Tools and Armors
        tag(Tags.Items.TOOLS_SHIELD).add(Items.SHIELD);
        tag(Tags.Items.TOOLS_BOW).add(Items.BOW);
        tag(Tags.Items.TOOLS_BRUSH).add(Items.BRUSH);
        tag(Tags.Items.TOOLS_MACE).add(Items.MACE);
        tag(Tags.Items.TOOLS_CROSSBOW).add(Items.CROSSBOW);
        tag(Tags.Items.TOOLS_FISHING_ROD).add(Items.FISHING_ROD);
        tag(Tags.Items.TOOLS_SHEAR).add(Items.SHEARS);
        tag(Tags.Items.TOOLS_TRIDENT).add(Items.TRIDENT);
        tag(Tags.Items.TOOLS_IGNITER).add(Items.FLINT_AND_STEEL);
        tag(Tags.Items.TOOLS_WRENCH);
        tag(Tags.Items.MINING_TOOL_TOOLS).add(Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.COPPER_PICKAXE, Items.GOLDEN_PICKAXE, Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE);
        tag(Tags.Items.MELEE_WEAPON_TOOLS).add(
                Items.MACE, Items.TRIDENT,
                Items.WOODEN_SWORD, Items.STONE_SWORD, Items.COPPER_SWORD, Items.GOLDEN_SWORD, Items.IRON_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD,
                Items.WOODEN_AXE, Items.STONE_AXE, Items.COPPER_AXE, Items.GOLDEN_AXE, Items.IRON_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE,
                Items.WOODEN_SPEAR, Items.STONE_SPEAR, Items.COPPER_SPEAR, Items.IRON_SPEAR, Items.GOLDEN_SPEAR, Items.DIAMOND_SPEAR, Items.NETHERITE_SPEAR);
        tag(Tags.Items.RANGED_WEAPON_TOOLS).add(Items.BOW, Items.CROSSBOW, Items.TRIDENT);
        tag(Tags.Items.TOOLS)
                .addTags(ItemTags.AXES, ItemTags.HOES, ItemTags.PICKAXES, ItemTags.SHOVELS, ItemTags.SPEARS, ItemTags.SWORDS)
                .addTags(Tags.Items.TOOLS_BOW, Tags.Items.TOOLS_BRUSH, Tags.Items.TOOLS_CROSSBOW, Tags.Items.TOOLS_FISHING_ROD,
                        Tags.Items.TOOLS_IGNITER, Tags.Items.TOOLS_SHEAR, Tags.Items.TOOLS_SHIELD, Tags.Items.TOOLS_TRIDENT,
                        Tags.Items.TOOLS_MACE, Tags.Items.TOOLS_WRENCH,
                        Tags.Items.MINING_TOOL_TOOLS, Tags.Items.MELEE_WEAPON_TOOLS, Tags.Items.RANGED_WEAPON_TOOLS);
        tag(Tags.Items.ARMORS)
                .addOptionalTag(Tags.Items.ARMORS_HUMANOID)
                .addOptionalTag(Tags.Items.ARMORS_HORSE)
                .addOptionalTag(Tags.Items.ARMORS_NAUTILUS)
                .addOptionalTag(Tags.Items.ARMORS_WOLF);

        tag(Tags.Items.ARMORS_HORSE)
                .add(Items.LEATHER_HORSE_ARMOR)
                .add(Items.COPPER_HORSE_ARMOR)
                .add(Items.IRON_HORSE_ARMOR)
                .add(Items.GOLDEN_HORSE_ARMOR)
                .add(Items.DIAMOND_HORSE_ARMOR)
                .add(Items.NETHERITE_HORSE_ARMOR);

        tag(Tags.Items.ARMORS_NAUTILUS)
                .add(Items.COPPER_NAUTILUS_ARMOR)
                .add(Items.IRON_NAUTILUS_ARMOR)
                .add(Items.GOLDEN_NAUTILUS_ARMOR)
                .add(Items.DIAMOND_NAUTILUS_ARMOR)
                .add(Items.NETHERITE_NAUTILUS_ARMOR);

        tag(Tags.Items.ARMORS_WOLF)
                .add(Items.WOLF_ARMOR);

        tag(Tags.Items.ARMORS_HUMANOID)
                .addTags(ItemTags.HEAD_ARMOR, ItemTags.CHEST_ARMOR, ItemTags.LEG_ARMOR, ItemTags.FOOT_ARMOR);

        tag(Tags.Items.ENCHANTABLES).addTags(
                ItemTags.ARMOR_ENCHANTABLE, ItemTags.EQUIPPABLE_ENCHANTABLE, ItemTags.WEAPON_ENCHANTABLE,
                ItemTags.MELEE_WEAPON_ENCHANTABLE, ItemTags.SHARP_WEAPON_ENCHANTABLE, ItemTags.SWEEPING_ENCHANTABLE,
                ItemTags.LUNGE_ENCHANTABLE, ItemTags.MINING_ENCHANTABLE, ItemTags.MINING_LOOT_ENCHANTABLE,
                ItemTags.FISHING_ENCHANTABLE, ItemTags.TRIDENT_ENCHANTABLE, ItemTags.BOW_ENCHANTABLE,
                ItemTags.CROSSBOW_ENCHANTABLE, ItemTags.MACE_ENCHANTABLE, ItemTags.FIRE_ASPECT_ENCHANTABLE,
                ItemTags.DURABILITY_ENCHANTABLE, ItemTags.VANISHING_ENCHANTABLE);
    }

    protected record Appender(TagAppender<Item> app) implements TagAppender<Item> {
        @Override
        public Appender add(ResourceKey<Item> element) {
            app.add(element);
            return this;
        }

        @Override
        public Appender addOptional(ResourceKey<Item> element) {
            app.addOptional(element);
            return this;
        }

        @Override
        public Appender addTag(TagKey<Item> tag) {
            app.addTag(tag);
            return this;
        }

        @Override
        public Appender addOptionalTag(TagKey<Item> tag) {
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
        public Appender remove(ResourceKey<Item> element) {
            app.remove(element);
            return this;
        }

        @Override
        public Appender remove(TagKey<Item> tag) {
            app.remove(tag);
            return this;
        }

        public Appender add(Item... items) {
            for (Item item : items) {
                add(BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow(() -> new IllegalStateException("Unregistered item")));
            }
            return this;
        }

        public Appender add(BlockItemId... ids) {
            for (ResourceKey<Item> itemResourceKey : Arrays.stream(ids).map(BlockItemId::item).toList()) {
                add(itemResourceKey);
            }
            return this;
        }

        public NeoForgeItemTagsProvider.Appender addAll(final ColorCollection<Item> collection) {
            collection.forEach(this::add);
            return this;
        }

        public NeoForgeItemTagsProvider.Appender addTag(BlockItemTagId tag) {
            return this.addTag(tag.item());
        }

        public NeoForgeItemTagsProvider.Appender addOptionalTag(BlockItemTagId tag) {
            return this.addOptionalTag(tag.item());
        }

        public NeoForgeItemTagsProvider.Appender addTags(BlockItemTagId... tags) {
            for (BlockItemTagId tag : tags) {
                this.addTag(tag);
            }
            return this;
        }

        public NeoForgeItemTagsProvider.Appender addOptionalTags(BlockItemTagId... tags) {
            for (BlockItemTagId tag : tags) {
                this.addOptionalTag(tag);
            }
            return this;
        }
    }

    @Override
    protected Appender tag(TagKey<Item> tag) {
        return new Appender(super.tag(tag));
    }

    private Appender tag(BlockItemTagId tag) {
        return tag(tag.item());
    }

    private void addColoredTags(Consumer<TagKey<Item>> consumer, TagKey<Item> group) {
        String prefix = group.location().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color : DyeColor.values()) {
            TagKey<Item> tag = getForgeItemTag(prefix + color.getName());
            consumer.accept(tag);
        }
    }

    @SuppressWarnings("unchecked")
    private TagKey<Block> getForgeBlockTag(String name) {
        try {
            name = name.toUpperCase(Locale.ENGLISH);
            return (TagKey<Block>) Tags.Blocks.class.getDeclaredField(name).get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(Tags.Blocks.class.getName() + " is missing tag name: " + name);
        }
    }

    @SuppressWarnings("unchecked")
    private TagKey<Item> getForgeItemTag(String name) {
        try {
            name = name.toUpperCase(Locale.ENGLISH);
            return (TagKey<Item>) Tags.Items.class.getDeclaredField(name).get(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(Tags.Items.class.getName() + " is missing tag name: " + name);
        }
    }

    @Override
    public String getName() {
        return "NeoForge Item Tags";
    }
}
