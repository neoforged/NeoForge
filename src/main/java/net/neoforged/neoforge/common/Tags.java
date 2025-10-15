/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;

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
        public static final TagKey<Block> BARRELS = tag("barrels");
        public static final TagKey<Block> BARRELS_WOODEN = tag("barrels/wooden");
        public static final TagKey<Block> BOOKSHELVES = tag("bookshelves");
        /**
         * For blocks that are similar to amethyst where their budding block produces buds and cluster blocks
         */
        public static final TagKey<Block> BUDDING_BLOCKS = tag("budding_blocks");
        /**
         * For blocks that are similar to amethyst where they have buddings forming from budding blocks
         */
        public static final TagKey<Block> BUDS = tag("buds");
        public static final TagKey<Block> CHAINS = tag("chains");
        public static final TagKey<Block> CHESTS = tag("chests");
        public static final TagKey<Block> CHESTS_ENDER = tag("chests/ender");
        public static final TagKey<Block> CHESTS_TRAPPED = tag("chests/trapped");
        public static final TagKey<Block> CHESTS_WOODEN = tag("chests/wooden");
        /**
         * For blocks that are similar to amethyst where they have clusters forming from budding blocks
         */
        public static final TagKey<Block> CLUSTERS = tag("clusters");
        public static final TagKey<Block> COBBLESTONES = tag("cobblestones");
        public static final TagKey<Block> COBBLESTONES_NORMAL = tag("cobblestones/normal");
        public static final TagKey<Block> COBBLESTONES_INFESTED = tag("cobblestones/infested");
        public static final TagKey<Block> COBBLESTONES_MOSSY = tag("cobblestones/mossy");
        public static final TagKey<Block> COBBLESTONES_DEEPSLATE = tag("cobblestones/deepslate");
        public static final TagKey<Block> CONCRETES = tag("concretes");

        /**
         * Tag that holds all blocks that can be dyed a specific color.
         * (Does not include color blending blocks that would behave similar to leather armor item)
         */
        public static final TagKey<Block> DYED = tag("dyed");
        public static final TagKey<Block> DYED_BLACK = tag("dyed/black");
        public static final TagKey<Block> DYED_BLUE = tag("dyed/blue");
        public static final TagKey<Block> DYED_BROWN = tag("dyed/brown");
        public static final TagKey<Block> DYED_CYAN = tag("dyed/cyan");
        public static final TagKey<Block> DYED_GRAY = tag("dyed/gray");
        public static final TagKey<Block> DYED_GREEN = tag("dyed/green");
        public static final TagKey<Block> DYED_LIGHT_BLUE = tag("dyed/light_blue");
        public static final TagKey<Block> DYED_LIGHT_GRAY = tag("dyed/light_gray");
        public static final TagKey<Block> DYED_LIME = tag("dyed/lime");
        public static final TagKey<Block> DYED_MAGENTA = tag("dyed/magenta");
        public static final TagKey<Block> DYED_ORANGE = tag("dyed/orange");
        public static final TagKey<Block> DYED_PINK = tag("dyed/pink");
        public static final TagKey<Block> DYED_PURPLE = tag("dyed/purple");
        public static final TagKey<Block> DYED_RED = tag("dyed/red");
        public static final TagKey<Block> DYED_WHITE = tag("dyed/white");
        public static final TagKey<Block> DYED_YELLOW = tag("dyed/yellow");
        public static final TagKey<Block> END_STONES = tag("end_stones");
        public static final TagKey<Block> FENCE_GATES = tag("fence_gates");
        public static final TagKey<Block> FENCE_GATES_WOODEN = tag("fence_gates/wooden");
        public static final TagKey<Block> FENCES = tag("fences");
        public static final TagKey<Block> FENCES_NETHER_BRICK = tag("fences/nether_brick");
        public static final TagKey<Block> FENCES_WOODEN = tag("fences/wooden");
        /**
         * Contains living ground-based flowers that are 1 block tall such as Dandelions or Poppy.
         * Equivalent to the "minecraft:small_flowers" block tag.
         */
        public static final TagKey<Block> FLOWERS_SMALL = tag("flowers/small");
        /**
         * Contains living ground-based flowers that are 2 block tall such as Rose Bush or Peony.
         * Equivalent to the "minecraft:tall_flowers" block tag in past Minecraft version.
         */
        public static final TagKey<Block> FLOWERS_TALL = tag("flowers/tall");
        /**
         * Contains any living plant block that contains flowers or is a flower itself.
         * Equivalent to the "minecraft:flowers" block tag.
         */
        public static final TagKey<Block> FLOWERS = tag("flowers");

        public static final TagKey<Block> GLASS_BLOCKS = tag("glass_blocks");
        public static final TagKey<Block> GLASS_BLOCKS_COLORLESS = tag("glass_blocks/colorless");
        /**
         * Glass which is made from cheap resources like sand and only minor additional ingredients like dyes
         */
        public static final TagKey<Block> GLASS_BLOCKS_CHEAP = tag("glass_blocks/cheap");
        public static final TagKey<Block> GLASS_BLOCKS_TINTED = tag("glass_blocks/tinted");

        public static final TagKey<Block> GLASS_PANES = tag("glass_panes");
        public static final TagKey<Block> GLASS_PANES_COLORLESS = tag("glass_panes/colorless");
        public static final TagKey<Block> GLAZED_TERRACOTTAS = tag("glazed_terracottas");

        public static final TagKey<Block> GRAVELS = tag("gravels");
        /**
         * Tag that holds all blocks that recipe viewers should not show to users.
         * Recipe viewers may use this to automatically find the corresponding BlockItem to hide.
         */
        public static final TagKey<Block> HIDDEN_FROM_RECIPE_VIEWERS = tag("hidden_from_recipe_viewers");
        public static final TagKey<Block> NETHERRACKS = tag("netherracks");
        public static final TagKey<Block> OBSIDIANS = tag("obsidians");
        /**
         * For common obsidian that has no special quirks or behaviors. Ideal for recipe use.
         * Crying Obsidian, for example, is a light block and harder to obtain. So it gets its own tag instead of being under normal tag.
         */
        public static final TagKey<Block> OBSIDIANS_NORMAL = tag("obsidians/normal");
        public static final TagKey<Block> OBSIDIANS_CRYING = tag("obsidians/crying");
        /**
         * Blocks which are often replaced by deepslate ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_DEEPSLATE}, during world generation.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Block> ORE_BEARING_GROUND_DEEPSLATE = tag("ore_bearing_ground/deepslate");
        /**
         * Blocks which are often replaced by netherrack ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_NETHERRACK}, during world generation.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Block> ORE_BEARING_GROUND_NETHERRACK = tag("ore_bearing_ground/netherrack");
        /**
         * Blocks which are often replaced by stone ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_STONE}, during world generation.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Block> ORE_BEARING_GROUND_STONE = tag("ore_bearing_ground/stone");
        /**
         * Ores which on average result in more than one resource worth of materials ignoring fortune and other modifiers.
         * (example, Copper Ore)
         */
        public static final TagKey<Block> ORE_RATES_DENSE = tag("ore_rates/dense");
        /**
         * Ores which on average result in one resource worth of materials ignoring fortune and other modifiers.
         * (Example, Iron Ore)
         */
        public static final TagKey<Block> ORE_RATES_SINGULAR = tag("ore_rates/singular");
        /**
         * Ores which on average result in less than one resource worth of materials ignoring fortune and other modifiers.
         * (Example, Nether Gold Ore as it drops 2 to 6 Gold Nuggets which is less than normal Gold Ore's Raw Gold drop)
         */
        public static final TagKey<Block> ORE_RATES_SPARSE = tag("ore_rates/sparse");
        public static final TagKey<Block> ORES = tag("ores");
        public static final TagKey<Block> ORES_COAL = tag("ores/coal");
        public static final TagKey<Block> ORES_COPPER = tag("ores/copper");
        public static final TagKey<Block> ORES_DIAMOND = tag("ores/diamond");
        public static final TagKey<Block> ORES_EMERALD = tag("ores/emerald");
        public static final TagKey<Block> ORES_GOLD = tag("ores/gold");
        public static final TagKey<Block> ORES_IRON = tag("ores/iron");
        public static final TagKey<Block> ORES_LAPIS = tag("ores/lapis");
        public static final TagKey<Block> ORES_NETHERITE_SCRAP = tag("ores/netherite_scrap");
        public static final TagKey<Block> ORES_QUARTZ = tag("ores/quartz");
        public static final TagKey<Block> ORES_REDSTONE = tag("ores/redstone");
        /**
         * Ores in deepslate (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_DEEPSLATE}) which could logically use deepslate as recipe input or output.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Block> ORES_IN_GROUND_DEEPSLATE = tag("ores_in_ground/deepslate");
        /**
         * Ores in netherrack (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_NETHERRACK}) which could logically use netherrack as recipe input or output.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Block> ORES_IN_GROUND_NETHERRACK = tag("ores_in_ground/netherrack");
        /**
         * Ores in stone (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_STONE}) which could logically use stone as recipe input or output.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Block> ORES_IN_GROUND_STONE = tag("ores_in_ground/stone");
        public static final TagKey<Block> PUMPKINS = tag("pumpkins");
        /**
         * For pumpkins that are not carved.
         */
        public static final TagKey<Block> PUMPKINS_NORMAL = tag("pumpkins/normal");
        /**
         * For pumpkins that are already carved but not a light source.
         */
        public static final TagKey<Block> PUMPKINS_CARVED = tag("pumpkins/carved");

        /**
         * For pumpkins that are already carved and a light source.
         */
        public static final TagKey<Block> PUMPKINS_JACK_O_LANTERNS = tag("pumpkins/jack_o_lanterns");
        public static final TagKey<Block> PLAYER_WORKSTATIONS_CRAFTING_TABLES = tag("player_workstations/crafting_tables");
        public static final TagKey<Block> PLAYER_WORKSTATIONS_FURNACES = tag("player_workstations/furnaces");
        /**
         * Blocks should be included in this tag if their movement/relocation can cause serious issues such
         * as world corruption upon being moved or for balance reason where the block should not be able to be relocated.
         * Example: Chunk loaders or pipes where other mods that move blocks do not respect
         * {@link BlockBehaviour.BlockStateBase#getPistonPushReaction}.
         */
        public static final TagKey<Block> RELOCATION_NOT_SUPPORTED = tag("relocation_not_supported");
        public static final TagKey<Block> ROPES = tag("ropes");

        public static final TagKey<Block> SANDS = tag("sands");
        public static final TagKey<Block> SANDS_COLORLESS = tag("sands/colorless");
        public static final TagKey<Block> SANDS_RED = tag("sands/red");

        public static final TagKey<Block> SANDSTONE_BLOCKS = tag("sandstone/blocks");
        public static final TagKey<Block> SANDSTONE_SLABS = tag("sandstone/slabs");
        public static final TagKey<Block> SANDSTONE_STAIRS = tag("sandstone/stairs");
        public static final TagKey<Block> SANDSTONE_RED_BLOCKS = tag("sandstone/red_blocks");
        public static final TagKey<Block> SANDSTONE_RED_SLABS = tag("sandstone/red_slabs");
        public static final TagKey<Block> SANDSTONE_RED_STAIRS = tag("sandstone/red_stairs");
        public static final TagKey<Block> SANDSTONE_UNCOLORED_BLOCKS = tag("sandstone/uncolored_blocks");
        public static final TagKey<Block> SANDSTONE_UNCOLORED_SLABS = tag("sandstone/uncolored_slabs");
        public static final TagKey<Block> SANDSTONE_UNCOLORED_STAIRS = tag("sandstone/uncolored_stairs");
        /**
         * Tag that holds all head based blocks such as Skeleton Skull or Player Head. (Named skulls to match minecraft:skulls item tag)
         */
        public static final TagKey<Block> SKULLS = tag("skulls");
        /**
         * Natural stone-like blocks that can be used as a base ingredient in recipes that takes stone.
         */
        public static final TagKey<Block> STONES = tag("stones");
        /**
         * A storage block is generally a block that has a recipe to craft a bulk of 1 kind of resource to a block
         * and has a mirror recipe to reverse the crafting with no loss in resources.
         * <p>
         * Honey Block is special in that the reversing recipe is not a perfect mirror of the crafting recipe
         * and so, it is considered a special case and not given a storage block tag.
         */
        public static final TagKey<Block> STORAGE_BLOCKS = tag("storage_blocks");
        public static final TagKey<Block> STORAGE_BLOCKS_BONE_MEAL = tag("storage_blocks/bone_meal");
        public static final TagKey<Block> STORAGE_BLOCKS_COAL = tag("storage_blocks/coal");
        public static final TagKey<Block> STORAGE_BLOCKS_COPPER = tag("storage_blocks/copper");
        public static final TagKey<Block> STORAGE_BLOCKS_DIAMOND = tag("storage_blocks/diamond");
        public static final TagKey<Block> STORAGE_BLOCKS_DRIED_KELP = tag("storage_blocks/dried_kelp");
        public static final TagKey<Block> STORAGE_BLOCKS_EMERALD = tag("storage_blocks/emerald");
        public static final TagKey<Block> STORAGE_BLOCKS_GOLD = tag("storage_blocks/gold");
        public static final TagKey<Block> STORAGE_BLOCKS_IRON = tag("storage_blocks/iron");
        public static final TagKey<Block> STORAGE_BLOCKS_LAPIS = tag("storage_blocks/lapis");
        public static final TagKey<Block> STORAGE_BLOCKS_NETHERITE = tag("storage_blocks/netherite");
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_COPPER = tag("storage_blocks/raw_copper");
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_GOLD = tag("storage_blocks/raw_gold");
        public static final TagKey<Block> STORAGE_BLOCKS_RAW_IRON = tag("storage_blocks/raw_iron");
        public static final TagKey<Block> STORAGE_BLOCKS_REDSTONE = tag("storage_blocks/redstone");
        public static final TagKey<Block> STORAGE_BLOCKS_RESIN = tag("storage_blocks/resin");
        public static final TagKey<Block> STORAGE_BLOCKS_SLIME = tag("storage_blocks/slime");
        public static final TagKey<Block> STORAGE_BLOCKS_WHEAT = tag("storage_blocks/wheat");
        public static final TagKey<Block> STRIPPED_LOGS = tag("stripped_logs");
        public static final TagKey<Block> STRIPPED_WOODS = tag("stripped_woods");
        public static final TagKey<Block> VILLAGER_JOB_SITES = tag("villager_job_sites");

        /**
         * Blocks tagged here will be tracked by Farmer Villagers who will attempt to plant crops on top.
         */
        public static final TagKey<Block> VILLAGER_FARMLANDS = neoforgeTag("villager_farmlands");

        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Block> neoforgeTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("neoforge", name));
        }
    }

    public static class EntityTypes {
        public static final TagKey<EntityType<?>> BOSSES = tag("bosses");
        public static final TagKey<EntityType<?>> MINECARTS = tag("minecarts");
        public static final TagKey<EntityType<?>> BOATS = tag("boats");

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
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", name));
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
        public static final TagKey<Item> BARRELS = tag("barrels");
        public static final TagKey<Item> BARRELS_WOODEN = tag("barrels/wooden");
        public static final TagKey<Item> BONES = tag("bones");
        public static final TagKey<Item> BOOKSHELVES = tag("bookshelves");
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
        /**
         * For blocks that are similar to amethyst where their budding block produces buds and cluster blocks
         */
        public static final TagKey<Item> BUDDING_BLOCKS = tag("budding_blocks");
        /**
         * For blocks that are similar to amethyst where they have buddings forming from budding blocks
         */
        public static final TagKey<Item> BUDS = tag("buds");
        public static final TagKey<Item> CHAINS = tag("chains");
        public static final TagKey<Item> CHESTS = tag("chests");
        public static final TagKey<Item> CHESTS_ENDER = tag("chests/ender");
        public static final TagKey<Item> CHESTS_TRAPPED = tag("chests/trapped");
        public static final TagKey<Item> CHESTS_WOODEN = tag("chests/wooden");
        public static final TagKey<Item> COBBLESTONES = tag("cobblestones");
        public static final TagKey<Item> COBBLESTONES_NORMAL = tag("cobblestones/normal");
        public static final TagKey<Item> COBBLESTONES_INFESTED = tag("cobblestones/infested");
        public static final TagKey<Item> COBBLESTONES_MOSSY = tag("cobblestones/mossy");
        public static final TagKey<Item> COBBLESTONES_DEEPSLATE = tag("cobblestones/deepslate");
        public static final TagKey<Item> CONCRETES = tag("concretes");
        /**
         * Block tag equivalent is {@link BlockTags#CONCRETE_POWDER}
         */
        public static final TagKey<Item> CONCRETE_POWDERS = tag("concrete_powders");
        /**
         * For blocks that are similar to amethyst where they have clusters forming from budding blocks
         */
        public static final TagKey<Item> CLUSTERS = tag("clusters");
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

        /**
         * Tag that holds all blocks and items that can be dyed a specific color.
         * (Does not include color blending items like leather armor
         * Use {@link net.minecraft.tags.ItemTags#DYEABLE} tag instead for color blending items)
         * <p>
         * Note: Use custom ingredients in recipes to do tag intersections and/or tag exclusions
         * to make more powerful recipes utilizing multiple tags such as dyed tags for an ingredient.
         * See {@link net.neoforged.neoforge.common.crafting.DifferenceIngredient} and {@link net.neoforged.neoforge.common.crafting.CompoundIngredient}
         * for various custom ingredients available that can also be used in data generation.
         */
        public static final TagKey<Item> DYED = tag("dyed");
        public static final TagKey<Item> DYED_BLACK = tag("dyed/black");
        public static final TagKey<Item> DYED_BLUE = tag("dyed/blue");
        public static final TagKey<Item> DYED_BROWN = tag("dyed/brown");
        public static final TagKey<Item> DYED_CYAN = tag("dyed/cyan");
        public static final TagKey<Item> DYED_GRAY = tag("dyed/gray");
        public static final TagKey<Item> DYED_GREEN = tag("dyed/green");
        public static final TagKey<Item> DYED_LIGHT_BLUE = tag("dyed/light_blue");
        public static final TagKey<Item> DYED_LIGHT_GRAY = tag("dyed/light_gray");
        public static final TagKey<Item> DYED_LIME = tag("dyed/lime");
        public static final TagKey<Item> DYED_MAGENTA = tag("dyed/magenta");
        public static final TagKey<Item> DYED_ORANGE = tag("dyed/orange");
        public static final TagKey<Item> DYED_PINK = tag("dyed/pink");
        public static final TagKey<Item> DYED_PURPLE = tag("dyed/purple");
        public static final TagKey<Item> DYED_RED = tag("dyed/red");
        public static final TagKey<Item> DYED_WHITE = tag("dyed/white");
        public static final TagKey<Item> DYED_YELLOW = tag("dyed/yellow");

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
         * For eggs to use for culinary purposes in recipes such as baking a cake.
         */
        public static final TagKey<Item> EGGS = tag("eggs");
        public static final TagKey<Item> END_STONES = tag("end_stones");
        public static final TagKey<Item> ENDER_PEARLS = tag("ender_pearls");
        public static final TagKey<Item> FEATHERS = tag("feathers");
        public static final TagKey<Item> FENCE_GATES = tag("fence_gates");
        public static final TagKey<Item> FENCE_GATES_WOODEN = tag("fence_gates/wooden");
        public static final TagKey<Item> FENCES = tag("fences");
        public static final TagKey<Item> FENCES_NETHER_BRICK = tag("fences/nether_brick");
        public static final TagKey<Item> FENCES_WOODEN = tag("fences/wooden");
        /**
         * For bonemeal-like items that can grow plants.
         * (Note: Could include durability-based modded bonemeal-like items. Check for durability {@link net.minecraft.core.component.DataComponents#DAMAGE} DataComponent to handle them properly)
         */
        public static final TagKey<Item> FERTILIZERS = tag("fertilizers");
        /**
         * Contains living ground-based flowers that are 1 block tall such as Dandelions or Poppy.
         * Equivalent to the "minecraft:small_flowers" item tag.
         */
        public static final TagKey<Item> FLOWERS_SMALL = tag("flowers/small");
        /**
         * Contains living ground-based flowers that are 2 block tall such as Rose Bush or Peony.
         * Equivalent to the "minecraft:tall_flowers" item tag in past Minecraft version.
         */
        public static final TagKey<Item> FLOWERS_TALL = tag("flowers/tall");
        /**
         * Contains any living plant block that contains flowers or is a flower itself.
         * Equivalent to the "minecraft:flowers" item tag in past minecraft versions.
         */
        public static final TagKey<Item> FLOWERS = tag("flowers");
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

        public static final TagKey<Item> GLASS_BLOCKS = tag("glass_blocks");
        public static final TagKey<Item> GLASS_BLOCKS_COLORLESS = tag("glass_blocks/colorless");
        /**
         * Glass which is made from cheap resources like sand and only minor additional ingredients like dyes
         */
        public static final TagKey<Item> GLASS_BLOCKS_CHEAP = tag("glass_blocks/cheap");
        public static final TagKey<Item> GLASS_BLOCKS_TINTED = tag("glass_blocks/tinted");

        public static final TagKey<Item> GLASS_PANES = tag("glass_panes");
        public static final TagKey<Item> GLASS_PANES_COLORLESS = tag("glass_panes/colorless");
        public static final TagKey<Item> GLAZED_TERRACOTTAS = tag("glazed_terracottas");

        public static final TagKey<Item> GRAVELS = tag("gravels");
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
        public static final TagKey<Item> NETHERRACKS = tag("netherracks");
        public static final TagKey<Item> NUGGETS = tag("nuggets");
        public static final TagKey<Item> NUGGETS_GOLD = tag("nuggets/gold");
        public static final TagKey<Item> NUGGETS_IRON = tag("nuggets/iron");
        public static final TagKey<Item> NUGGETS_COPPER = tag("nuggets/copper");
        public static final TagKey<Item> OBSIDIANS = tag("obsidians");
        /**
         * For common obsidian that has no special quirks or behaviors. Ideal for recipe use.
         * Crying Obsidian, for example, is a light block and harder to obtain. So it gets its own tag instead of being under normal tag.
         */
        public static final TagKey<Item> OBSIDIANS_NORMAL = tag("obsidians/normal");
        public static final TagKey<Item> OBSIDIANS_CRYING = tag("obsidians/crying");
        /**
         * Blocks which are often replaced by deepslate ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_DEEPSLATE}, during world generation.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Item> ORE_BEARING_GROUND_DEEPSLATE = tag("ore_bearing_ground/deepslate");
        /**
         * Blocks which are often replaced by netherrack ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_NETHERRACK}, during world generation.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Item> ORE_BEARING_GROUND_NETHERRACK = tag("ore_bearing_ground/netherrack");
        /**
         * Blocks which are often replaced by stone ores, i.e. the ores in the tag {@link #ORES_IN_GROUND_STONE}, during world generation.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Item> ORE_BEARING_GROUND_STONE = tag("ore_bearing_ground/stone");
        /**
         * Ores which on average result in more than one resource worth of materials ignoring fortune and other modifiers.
         * (example, Copper Ore)
         */
        public static final TagKey<Item> ORE_RATES_DENSE = tag("ore_rates/dense");
        /**
         * Ores which on average result in one resource worth of materials ignoring fortune and other modifiers.
         * (Example, Iron Ore)
         */
        public static final TagKey<Item> ORE_RATES_SINGULAR = tag("ore_rates/singular");
        /**
         * Ores which on average result in less than one resource worth of materials ignoring fortune and other modifiers.
         * (Example, Nether Gold Ore as it drops 2 to 6 Gold Nuggets which is less than normal Gold Ore's Raw Gold drop)
         */
        public static final TagKey<Item> ORE_RATES_SPARSE = tag("ore_rates/sparse");
        public static final TagKey<Item> ORES = tag("ores");
        public static final TagKey<Item> ORES_COAL = tag("ores/coal");
        public static final TagKey<Item> ORES_COPPER = tag("ores/copper");
        public static final TagKey<Item> ORES_DIAMOND = tag("ores/diamond");
        public static final TagKey<Item> ORES_EMERALD = tag("ores/emerald");
        public static final TagKey<Item> ORES_GOLD = tag("ores/gold");
        public static final TagKey<Item> ORES_IRON = tag("ores/iron");
        public static final TagKey<Item> ORES_LAPIS = tag("ores/lapis");
        public static final TagKey<Item> ORES_NETHERITE_SCRAP = tag("ores/netherite_scrap");
        public static final TagKey<Item> ORES_QUARTZ = tag("ores/quartz");
        public static final TagKey<Item> ORES_REDSTONE = tag("ores/redstone");
        /**
         * Ores in deepslate (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_DEEPSLATE}) which could logically use deepslate as recipe input or output.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Item> ORES_IN_GROUND_DEEPSLATE = tag("ores_in_ground/deepslate");
        /**
         * Ores in netherrack (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_NETHERRACK}) which could logically use netherrack as recipe input or output.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Item> ORES_IN_GROUND_NETHERRACK = tag("ores_in_ground/netherrack");
        /**
         * Ores in stone (or in equivalent blocks in the tag {@link #ORE_BEARING_GROUND_STONE}) which could logically use stone as recipe input or output.
         * (The block's registry name is used as the tag name)
         */
        public static final TagKey<Item> ORES_IN_GROUND_STONE = tag("ores_in_ground/stone");
        public static final TagKey<Item> PLAYER_WORKSTATIONS_CRAFTING_TABLES = tag("player_workstations/crafting_tables");
        public static final TagKey<Item> PLAYER_WORKSTATIONS_FURNACES = tag("player_workstations/furnaces");
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
        public static final TagKey<Item> PUMPKINS = tag("pumpkins");
        /**
         * For pumpkins that are not carved.
         */
        public static final TagKey<Item> PUMPKINS_NORMAL = tag("pumpkins/normal");
        /**
         * For pumpkins that are already carved but not a light source.
         */
        public static final TagKey<Item> PUMPKINS_CARVED = tag("pumpkins/carved");

        /**
         * For pumpkins that are already carved and a light source.
         */
        public static final TagKey<Item> PUMPKINS_JACK_O_LANTERNS = tag("pumpkins/jack_o_lanterns");
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
        public static final TagKey<Item> ROPES = tag("ropes");

        public static final TagKey<Item> SANDS = tag("sands");
        public static final TagKey<Item> SANDS_COLORLESS = tag("sands/colorless");
        public static final TagKey<Item> SANDS_RED = tag("sands/red");

        public static final TagKey<Item> SANDSTONE_BLOCKS = tag("sandstone/blocks");
        public static final TagKey<Item> SANDSTONE_SLABS = tag("sandstone/slabs");
        public static final TagKey<Item> SANDSTONE_STAIRS = tag("sandstone/stairs");
        public static final TagKey<Item> SANDSTONE_RED_BLOCKS = tag("sandstone/red_blocks");
        public static final TagKey<Item> SANDSTONE_RED_SLABS = tag("sandstone/red_slabs");
        public static final TagKey<Item> SANDSTONE_RED_STAIRS = tag("sandstone/red_stairs");
        public static final TagKey<Item> SANDSTONE_UNCOLORED_BLOCKS = tag("sandstone/uncolored_blocks");
        public static final TagKey<Item> SANDSTONE_UNCOLORED_SLABS = tag("sandstone/uncolored_slabs");
        public static final TagKey<Item> SANDSTONE_UNCOLORED_STAIRS = tag("sandstone/uncolored_stairs");

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
        /**
         * Natural stone-like blocks that can be used as a base ingredient in recipes that takes stone.
         */
        public static final TagKey<Item> STONES = tag("stones");
        /**
         * A storage block is generally a block that has a recipe to craft a bulk of 1 kind of resource to a block
         * and has a mirror recipe to reverse the crafting with no loss in resources.
         * <p>
         * Honey Block is special in that the reversing recipe is not a perfect mirror of the crafting recipe
         * and so, it is considered a special case and not given a storage block tag.
         */
        public static final TagKey<Item> STORAGE_BLOCKS = tag("storage_blocks");
        public static final TagKey<Item> STORAGE_BLOCKS_BONE_MEAL = tag("storage_blocks/bone_meal");
        public static final TagKey<Item> STORAGE_BLOCKS_COAL = tag("storage_blocks/coal");
        public static final TagKey<Item> STORAGE_BLOCKS_COPPER = tag("storage_blocks/copper");
        public static final TagKey<Item> STORAGE_BLOCKS_DIAMOND = tag("storage_blocks/diamond");
        public static final TagKey<Item> STORAGE_BLOCKS_DRIED_KELP = tag("storage_blocks/dried_kelp");
        public static final TagKey<Item> STORAGE_BLOCKS_EMERALD = tag("storage_blocks/emerald");
        public static final TagKey<Item> STORAGE_BLOCKS_GOLD = tag("storage_blocks/gold");
        public static final TagKey<Item> STORAGE_BLOCKS_IRON = tag("storage_blocks/iron");
        public static final TagKey<Item> STORAGE_BLOCKS_LAPIS = tag("storage_blocks/lapis");
        public static final TagKey<Item> STORAGE_BLOCKS_NETHERITE = tag("storage_blocks/netherite");
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_COPPER = tag("storage_blocks/raw_copper");
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_GOLD = tag("storage_blocks/raw_gold");
        public static final TagKey<Item> STORAGE_BLOCKS_RAW_IRON = tag("storage_blocks/raw_iron");
        public static final TagKey<Item> STORAGE_BLOCKS_REDSTONE = tag("storage_blocks/redstone");
        public static final TagKey<Item> STORAGE_BLOCKS_RESIN = tag("storage_blocks/resin");
        public static final TagKey<Item> STORAGE_BLOCKS_SLIME = tag("storage_blocks/slime");
        public static final TagKey<Item> STORAGE_BLOCKS_WHEAT = tag("storage_blocks/wheat");
        public static final TagKey<Item> STRINGS = tag("strings");
        public static final TagKey<Item> STRIPPED_LOGS = tag("stripped_logs");
        public static final TagKey<Item> STRIPPED_WOODS = tag("stripped_woods");
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
         * A tag containing all existing spears. Other tools such as throwing knives or boomerangs
         * should not be put into this tag and should be put into their own tool tags.
         * Do not use this tag for determining a tool's behavior.
         * Please use {@link ItemAbilities} instead for what action a tool can do.
         *
         * @see ItemAbility
         * @see ItemAbilities
         */
        public static final TagKey<Item> TOOLS_SPEAR = tag("tools/spear");
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
         * Collects the 4 vanilla armor tags into one parent collection for ease.
         */
        public static final TagKey<Item> ARMORS = tag("armors");
        /**
         * Collects the many enchantable tags into one parent collection for ease.
         */
        public static final TagKey<Item> ENCHANTABLES = tag("enchantables");

        private static TagKey<Item> tag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
        }

        private static TagKey<Item> neoforgeTag(String name) {
            return ItemTags.create(ResourceLocation.fromNamespaceAndPath("neoforge", name));
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
            return FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", name));
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

        private static TagKey<Enchantment> tag(String name) {
            return TagKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath("c", name));
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
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", name));
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
            return TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("c", name));
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
            return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("neoforge", name));
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

        ResourceLocation registryIdentifier = tagKey.registry().location();
        ResourceLocation tagIdentifier = tagKey.location();

        stringBuilder.append(registryIdentifier.toShortLanguageKey().replace("/", "."))
                .append(".")
                .append(tagIdentifier.getNamespace())
                .append(".")
                .append(tagIdentifier.getPath().replace("/", "."));

        return stringBuilder.toString();
    }
}
