/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class ForgeItemTagsProvider extends ItemTagsProvider
{
    public ForgeItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTagProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, blockTagProvider, "neoforge", existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTags(HolderLookup.Provider lookupProvider)
    {
        copy(Tags.Blocks.BARRELS, Tags.Items.BARRELS);
        copy(Tags.Blocks.BARRELS_WOODEN, Tags.Items.BARRELS_WOODEN);
        tag(Tags.Items.BONES).add(Items.BONE);
        copy(Tags.Blocks.BOOKSHELVES, Tags.Items.BOOKSHELVES);
        copy(Tags.Blocks.CHESTS, Tags.Items.CHESTS);
        copy(Tags.Blocks.CHESTS_ENDER, Tags.Items.CHESTS_ENDER);
        copy(Tags.Blocks.CHESTS_TRAPPED, Tags.Items.CHESTS_TRAPPED);
        copy(Tags.Blocks.CHESTS_WOODEN, Tags.Items.CHESTS_WOODEN);
        copy(Tags.Blocks.COBBLESTONES, Tags.Items.COBBLESTONES);
        copy(Tags.Blocks.COBBLESTONES_NORMAL, Tags.Items.COBBLESTONES_NORMAL);
        copy(Tags.Blocks.COBBLESTONES_INFESTED, Tags.Items.COBBLESTONES_INFESTED);
        copy(Tags.Blocks.COBBLESTONES_MOSSY, Tags.Items.COBBLESTONES_MOSSY);
        copy(Tags.Blocks.COBBLESTONES_DEEPSLATE, Tags.Items.COBBLESTONES_DEEPSLATE);
        tag(Tags.Items.CROPS).addTags(Tags.Items.CROPS_BEETROOT, Tags.Items.CROPS_CARROT, Tags.Items.CROPS_NETHER_WART, Tags.Items.CROPS_POTATO, Tags.Items.CROPS_WHEAT);
        tag(Tags.Items.CROPS_BEETROOT).add(Items.BEETROOT);
        tag(Tags.Items.CROPS_CARROT).add(Items.CARROT);
        tag(Tags.Items.CROPS_NETHER_WART).add(Items.NETHER_WART);
        tag(Tags.Items.CROPS_POTATO).add(Items.POTATO);
        tag(Tags.Items.CROPS_WHEAT).add(Items.WHEAT);
        addColored(Tags.Items.DYED, "{color}_banner");
        addColored(Tags.Items.DYED, "{color}_bed");
        addColored(Tags.Items.DYED, "{color}_candle");
        addColored(Tags.Items.DYED, "{color}_carpet");
        addColored(Tags.Items.DYED, "{color}_concrete");
        addColored(Tags.Items.DYED, "{color}_concrete_powder");
        addColored(Tags.Items.DYED, "{color}_glazed_terracotta");
        addColored(Tags.Items.DYED, "{color}_shulker_box");
        addColored(Tags.Items.DYED, "{color}_stained_glass");
        addColored(Tags.Items.DYED, "{color}_stained_glass_pane");
        addColored(Tags.Items.DYED, "{color}_terracotta");
        addColored(Tags.Items.DYED, "{color}_wool");
        addColoredTags(tag(Tags.Items.DYED)::addTags, Tags.Items.DYED);
        tag(Tags.Items.DUSTS).addTags(Tags.Items.DUSTS_GLOWSTONE, Tags.Items.DUSTS_PRISMARINE, Tags.Items.DUSTS_REDSTONE);
        tag(Tags.Items.DUSTS_GLOWSTONE).add(Items.GLOWSTONE_DUST);
        tag(Tags.Items.DUSTS_PRISMARINE).add(Items.PRISMARINE_SHARD);
        tag(Tags.Items.DUSTS_REDSTONE).add(Items.REDSTONE);
        addColored(Tags.Items.DYES, "{color}_dye");
        addColoredTags(tag(Tags.Items.DYES)::addTags, Tags.Items.DYES);
        tag(Tags.Items.EGGS).add(Items.EGG);
        tag(Tags.Items.ENCHANTING_FUELS).addTag(Tags.Items.GEMS_LAPIS);
        copy(Tags.Blocks.END_STONES, Tags.Items.END_STONES);
        tag(Tags.Items.ENDER_PEARLS).add(Items.ENDER_PEARL);
        tag(Tags.Items.FEATHERS).add(Items.FEATHER);
        copy(Tags.Blocks.FENCE_GATES, Tags.Items.FENCE_GATES);
        copy(Tags.Blocks.FENCE_GATES_WOODEN, Tags.Items.FENCE_GATES_WOODEN);
        copy(Tags.Blocks.FENCES, Tags.Items.FENCES);
        copy(Tags.Blocks.FENCES_NETHER_BRICK, Tags.Items.FENCES_NETHER_BRICK);
        copy(Tags.Blocks.FENCES_WOODEN, Tags.Items.FENCES_WOODEN);
        tag(Tags.Items.GEMS).addTags(Tags.Items.GEMS_AMETHYST, Tags.Items.GEMS_DIAMOND, Tags.Items.GEMS_EMERALD, Tags.Items.GEMS_LAPIS, Tags.Items.GEMS_PRISMARINE, Tags.Items.GEMS_QUARTZ);
        tag(Tags.Items.GEMS_AMETHYST).add(Items.AMETHYST_SHARD);
        tag(Tags.Items.GEMS_DIAMOND).add(Items.DIAMOND);
        tag(Tags.Items.GEMS_EMERALD).add(Items.EMERALD);
        tag(Tags.Items.GEMS_LAPIS).add(Items.LAPIS_LAZULI);
        tag(Tags.Items.GEMS_PRISMARINE).add(Items.PRISMARINE_CRYSTALS);
        tag(Tags.Items.GEMS_QUARTZ).add(Items.QUARTZ);
        copy(Tags.Blocks.GLASS_BLOCKS, Tags.Items.GLASS_BLOCKS);
        copy(Tags.Blocks.GLASS_BLOCKS_COLORLESS, Tags.Items.GLASS_BLOCKS_COLORLESS);
        copy(Tags.Blocks.GLASS_BLOCKS_TINTED, Tags.Items.GLASS_BLOCKS_TINTED);
        copy(Tags.Blocks.GLASS_BLOCKS_CHEAP, Tags.Items.GLASS_BLOCKS_CHEAP);
        copy(Tags.Blocks.GLASS_PANES, Tags.Items.GLASS_PANES);
        copy(Tags.Blocks.GLASS_PANES_COLORLESS, Tags.Items.GLASS_PANES_COLORLESS);
        copy(Tags.Blocks.GRAVEL, Tags.Items.GRAVEL);
        tag(Tags.Items.GUNPOWDER).add(Items.GUNPOWDER);
        tag(Tags.Items.HEADS).add(Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PLAYER_HEAD, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.ZOMBIE_HEAD);
        tag(Tags.Items.INGOTS).addTags(Tags.Items.INGOTS_BRICK, Tags.Items.INGOTS_COPPER, Tags.Items.INGOTS_GOLD, Tags.Items.INGOTS_IRON, Tags.Items.INGOTS_NETHERITE, Tags.Items.INGOTS_NETHER_BRICK);
        tag(Tags.Items.INGOTS_BRICK).add(Items.BRICK);
        tag(Tags.Items.INGOTS_COPPER).add(Items.COPPER_INGOT);
        tag(Tags.Items.INGOTS_GOLD).add(Items.GOLD_INGOT);
        tag(Tags.Items.INGOTS_IRON).add(Items.IRON_INGOT);
        tag(Tags.Items.INGOTS_NETHERITE).add(Items.NETHERITE_INGOT);
        tag(Tags.Items.INGOTS_NETHER_BRICK).add(Items.NETHER_BRICK);
        tag(Tags.Items.LEATHER).add(Items.LEATHER);
        tag(Tags.Items.MUSHROOMS).add(Items.BROWN_MUSHROOM, Items.RED_MUSHROOM);
        tag(Tags.Items.NETHER_STARS).add(Items.NETHER_STAR);
        copy(Tags.Blocks.NETHERRACK, Tags.Items.NETHERRACK);
        tag(Tags.Items.NUGGETS).addTags(Tags.Items.NUGGETS_GOLD, Tags.Items.NUGGETS_IRON);
        tag(Tags.Items.NUGGETS_IRON).add(Items.IRON_NUGGET);
        tag(Tags.Items.NUGGETS_GOLD).add(Items.GOLD_NUGGET);
        copy(Tags.Blocks.OBSIDIAN, Tags.Items.OBSIDIAN);
        copy(Tags.Blocks.ORE_BEARING_GROUND_DEEPSLATE, Tags.Items.ORE_BEARING_GROUND_DEEPSLATE);
        copy(Tags.Blocks.ORE_BEARING_GROUND_NETHERRACK, Tags.Items.ORE_BEARING_GROUND_NETHERRACK);
        copy(Tags.Blocks.ORE_BEARING_GROUND_STONE, Tags.Items.ORE_BEARING_GROUND_STONE);
        copy(Tags.Blocks.ORE_RATES_DENSE, Tags.Items.ORE_RATES_DENSE);
        copy(Tags.Blocks.ORE_RATES_SINGULAR, Tags.Items.ORE_RATES_SINGULAR);
        copy(Tags.Blocks.ORE_RATES_SPARSE, Tags.Items.ORE_RATES_SPARSE);
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(Tags.Blocks.ORES_COAL, Tags.Items.ORES_COAL);
        copy(Tags.Blocks.ORES_COPPER, Tags.Items.ORES_COPPER);
        copy(Tags.Blocks.ORES_DIAMOND, Tags.Items.ORES_DIAMOND);
        copy(Tags.Blocks.ORES_EMERALD, Tags.Items.ORES_EMERALD);
        copy(Tags.Blocks.ORES_GOLD, Tags.Items.ORES_GOLD);
        copy(Tags.Blocks.ORES_IRON, Tags.Items.ORES_IRON);
        copy(Tags.Blocks.ORES_LAPIS, Tags.Items.ORES_LAPIS);
        copy(Tags.Blocks.ORES_QUARTZ, Tags.Items.ORES_QUARTZ);
        copy(Tags.Blocks.ORES_REDSTONE, Tags.Items.ORES_REDSTONE);
        copy(Tags.Blocks.ORES_NETHERITE_SCRAP, Tags.Items.ORES_NETHERITE_SCRAP);
        copy(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Tags.Items.ORES_IN_GROUND_DEEPSLATE);
        copy(Tags.Blocks.ORES_IN_GROUND_NETHERRACK, Tags.Items.ORES_IN_GROUND_NETHERRACK);
        copy(Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE);
        tag(Tags.Items.RAW_BLOCKS).addTags(Tags.Items.RAW_BLOCKS_COPPER, Tags.Items.RAW_BLOCKS_GOLD, Tags.Items.RAW_BLOCKS_IRON);
        tag(Tags.Items.RAW_BLOCKS_COPPER).add(Items.RAW_COPPER_BLOCK);
        tag(Tags.Items.RAW_BLOCKS_GOLD).add(Items.RAW_GOLD_BLOCK);
        tag(Tags.Items.RAW_BLOCKS_IRON).add(Items.RAW_IRON_BLOCK);
        tag(Tags.Items.RAW_MATERIALS).addTags(Tags.Items.RAW_MATERIALS_COPPER, Tags.Items.RAW_MATERIALS_GOLD, Tags.Items.RAW_MATERIALS_IRON);
        tag(Tags.Items.RAW_MATERIALS_COPPER).add(Items.RAW_COPPER);
        tag(Tags.Items.RAW_MATERIALS_GOLD).add(Items.RAW_GOLD);
        tag(Tags.Items.RAW_MATERIALS_IRON).add(Items.RAW_IRON);
        tag(Tags.Items.RODS).addTags(Tags.Items.RODS_WOODEN, Tags.Items.RODS_BLAZE);
        tag(Tags.Items.RODS_BLAZE).add(Items.BLAZE_ROD);
        tag(Tags.Items.RODS_WOODEN).add(Items.STICK);
        copy(Tags.Blocks.SAND, Tags.Items.SAND);
        copy(Tags.Blocks.SAND_COLORLESS, Tags.Items.SAND_COLORLESS);
        copy(Tags.Blocks.SAND_RED, Tags.Items.SAND_RED);
        copy(Tags.Blocks.SANDSTONE_BLOCKS, Tags.Items.SANDSTONE_BLOCKS);
        copy(Tags.Blocks.SANDSTONE_SLABS, Tags.Items.SANDSTONE_SLABS);
        copy(Tags.Blocks.SANDSTONE_STAIRS, Tags.Items.SANDSTONE_STAIRS);
        copy(Tags.Blocks.SANDSTONE_RED_BLOCKS, Tags.Items.SANDSTONE_RED_BLOCKS);
        copy(Tags.Blocks.SANDSTONE_RED_SLABS, Tags.Items.SANDSTONE_RED_SLABS);
        copy(Tags.Blocks.SANDSTONE_RED_STAIRS, Tags.Items.SANDSTONE_RED_STAIRS);
        copy(Tags.Blocks.SANDSTONE_UNCOLORED_BLOCKS, Tags.Items.SANDSTONE_UNCOLORED_BLOCKS);
        copy(Tags.Blocks.SANDSTONE_UNCOLORED_SLABS, Tags.Items.SANDSTONE_UNCOLORED_SLABS);
        copy(Tags.Blocks.SANDSTONE_UNCOLORED_STAIRS, Tags.Items.SANDSTONE_UNCOLORED_STAIRS);
        tag(Tags.Items.SEEDS).addTags(Tags.Items.SEEDS_BEETROOT, Tags.Items.SEEDS_MELON, Tags.Items.SEEDS_PUMPKIN, Tags.Items.SEEDS_WHEAT);
        tag(Tags.Items.SEEDS_BEETROOT).add(Items.BEETROOT_SEEDS);
        tag(Tags.Items.SEEDS_MELON).add(Items.MELON_SEEDS);
        tag(Tags.Items.SEEDS_PUMPKIN).add(Items.PUMPKIN_SEEDS);
        tag(Tags.Items.SEEDS_WHEAT).add(Items.WHEAT_SEEDS);
        tag(Tags.Items.SLIMEBALLS).add(Items.SLIME_BALL);
        copy(Tags.Blocks.STONES, Tags.Items.STONES);
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
        copy(Tags.Blocks.STORAGE_BLOCKS_BONE_MEAL, Tags.Items.STORAGE_BLOCKS_BONE_MEAL);
        copy(Tags.Blocks.STORAGE_BLOCKS_COAL, Tags.Items.STORAGE_BLOCKS_COAL);
        copy(Tags.Blocks.STORAGE_BLOCKS_COPPER, Tags.Items.STORAGE_BLOCKS_COPPER);
        copy(Tags.Blocks.STORAGE_BLOCKS_DIAMOND, Tags.Items.STORAGE_BLOCKS_DIAMOND);
        copy(Tags.Blocks.STORAGE_BLOCKS_DRIED_KELP, Tags.Items.STORAGE_BLOCKS_DRIED_KELP);
        copy(Tags.Blocks.STORAGE_BLOCKS_EMERALD, Tags.Items.STORAGE_BLOCKS_EMERALD);
        copy(Tags.Blocks.STORAGE_BLOCKS_GOLD, Tags.Items.STORAGE_BLOCKS_GOLD);
        copy(Tags.Blocks.STORAGE_BLOCKS_IRON, Tags.Items.STORAGE_BLOCKS_IRON);
        copy(Tags.Blocks.STORAGE_BLOCKS_LAPIS, Tags.Items.STORAGE_BLOCKS_LAPIS);
        copy(Tags.Blocks.STORAGE_BLOCKS_NETHERITE, Tags.Items.STORAGE_BLOCKS_NETHERITE);
        copy(Tags.Blocks.STORAGE_BLOCKS_RAW_COPPER, Tags.Items.STORAGE_BLOCKS_RAW_COPPER);
        copy(Tags.Blocks.STORAGE_BLOCKS_RAW_GOLD, Tags.Items.STORAGE_BLOCKS_RAW_GOLD);
        copy(Tags.Blocks.STORAGE_BLOCKS_RAW_IRON, Tags.Items.STORAGE_BLOCKS_RAW_IRON);
        copy(Tags.Blocks.STORAGE_BLOCKS_REDSTONE, Tags.Items.STORAGE_BLOCKS_REDSTONE);
        copy(Tags.Blocks.STORAGE_BLOCKS_SLIME, Tags.Items.STORAGE_BLOCKS_SLIME);
        copy(Tags.Blocks.STORAGE_BLOCKS_WHEAT, Tags.Items.STORAGE_BLOCKS_WHEAT);
        tag(Tags.Items.STRINGS).add(Items.STRING);
        tag(Tags.Items.TOOLS_SHIELDS).add(Items.SHIELD);
        tag(Tags.Items.TOOLS_BOWS).add(Items.BOW);
        tag(Tags.Items.TOOLS_CROSSBOWS).add(Items.CROSSBOW);
        tag(Tags.Items.TOOLS_FISHING_RODS).add(Items.FISHING_ROD);
        tag(Tags.Items.TOOLS_SHEARS).add(Items.SHEARS);
        tag(Tags.Items.TOOLS_SPEARS).add(Items.TRIDENT);
        tag(Tags.Items.TOOLS)
            .addTags(ItemTags.AXES, ItemTags.HOES, ItemTags.PICKAXES, ItemTags.SHOVELS, ItemTags.SWORDS)
            .addTags(Tags.Items.TOOLS_BOWS, Tags.Items.TOOLS_CROSSBOWS, Tags.Items.TOOLS_FISHING_RODS, Tags.Items.TOOLS_SHEARS, Tags.Items.TOOLS_SHIELDS, Tags.Items.TOOLS_SPEARS);
        tag(Tags.Items.ARMORS_HELMETS).add(Items.LEATHER_HELMET, Items.CHAINMAIL_HELMET, Items.IRON_HELMET, Items.GOLDEN_HELMET, Items.DIAMOND_HELMET, Items.NETHERITE_HELMET, Items.TURTLE_HELMET);
        tag(Tags.Items.ARMORS_CHESTPLATES).add(Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE);
        tag(Tags.Items.ARMORS_LEGGINGS).add(Items.LEATHER_LEGGINGS, Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS, Items.GOLDEN_LEGGINGS, Items.DIAMOND_LEGGINGS, Items.NETHERITE_LEGGINGS);
        tag(Tags.Items.ARMORS_BOOTS).add(Items.LEATHER_BOOTS, Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.DIAMOND_BOOTS, Items.NETHERITE_BOOTS);
        tag(Tags.Items.ARMORS).addTags(Tags.Items.ARMORS_HELMETS, Tags.Items.ARMORS_CHESTPLATES, Tags.Items.ARMORS_LEGGINGS, Tags.Items.ARMORS_BOOTS);
        copy(Tags.Blocks.BUDDING_BLOCKS, Tags.Items.BUDDING_BLOCKS);
        copy(Tags.Blocks.BUDS, Tags.Items.BUDS);
        copy(Tags.Blocks.CLUSTERS, Tags.Items.CLUSTERS);

        // Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
        // TODO: Remove backwards compat tag entries in 1.22
        tagWithOptionalLegacy(Tags.Items.BONES);
        tagWithOptionalLegacy(Tags.Items.CROPS);
        tagWithOptionalLegacy(Tags.Items.CROPS_BEETROOT);
        tagWithOptionalLegacy(Tags.Items.CROPS_CARROT);
        tagWithOptionalLegacy(Tags.Items.CROPS_NETHER_WART);
        tagWithOptionalLegacy(Tags.Items.CROPS_POTATO);
        tagWithOptionalLegacy(Tags.Items.CROPS_WHEAT);
        tagWithOptionalLegacy(Tags.Items.DUSTS);
        tagWithOptionalLegacy(Tags.Items.DUSTS_GLOWSTONE);
        tagWithOptionalLegacy(Tags.Items.DUSTS_PRISMARINE);
        tagWithOptionalLegacy(Tags.Items.DUSTS_REDSTONE);
        tagColoredWithOptionalLegacy(Tags.Items.DYES);
        tag(Tags.Items.DYED_BLACK)
                .addOptional(new ResourceLocation("forge", "glass/black"))
                .addOptional(new ResourceLocation("forge", "stained_glass/black"));
        tag(Tags.Items.DYED_BLUE)
                .addOptional(new ResourceLocation("forge", "glass/blue"))
                .addOptional(new ResourceLocation("forge", "stained_glass/blue"));
        tag(Tags.Items.DYED_BROWN)
                .addOptional(new ResourceLocation("forge", "glass/brown"))
                .addOptional(new ResourceLocation("forge", "stained_glass/brown"));
        tag(Tags.Items.DYED_CYAN)
                .addOptional(new ResourceLocation("forge", "glass/cyan"))
                .addOptional(new ResourceLocation("forge", "stained_glass/cyan"));
        tag(Tags.Items.DYED_GRAY)
                .addOptional(new ResourceLocation("forge", "glass/gray"))
                .addOptional(new ResourceLocation("forge", "stained_glass/gray"));
        tag(Tags.Items.DYED_GREEN)
                .addOptional(new ResourceLocation("forge", "glass/green"))
                .addOptional(new ResourceLocation("forge", "stained_glass/green"));
        tag(Tags.Items.DYED_LIGHT_BLUE)
                .addOptional(new ResourceLocation("forge", "glass/light_blue"))
                .addOptional(new ResourceLocation("forge", "stained_glass/light_blue"));
        tag(Tags.Items.DYED_LIGHT_GRAY)
                .addOptional(new ResourceLocation("forge", "glass/light_gray"))
                .addOptional(new ResourceLocation("forge", "stained_glass/light_gray"));
        tag(Tags.Items.DYED_LIME)
                .addOptional(new ResourceLocation("forge", "glass/lime"))
                .addOptional(new ResourceLocation("forge", "stained_glass/lime"));
        tag(Tags.Items.DYED_MAGENTA)
                .addOptional(new ResourceLocation("forge", "glass/magenta"))
                .addOptional(new ResourceLocation("forge", "stained_glass/magenta"));
        tag(Tags.Items.DYED_MAGENTA)
                .addOptional(new ResourceLocation("forge", "glass/magenta"))
                .addOptional(new ResourceLocation("forge", "stained_glass/magenta"));
        tag(Tags.Items.DYED_ORANGE)
                .addOptional(new ResourceLocation("forge", "glass/orange"))
                .addOptional(new ResourceLocation("forge", "stained_glass/orange"));
        tag(Tags.Items.DYED_PINK)
                .addOptional(new ResourceLocation("forge", "glass/pink"))
                .addOptional(new ResourceLocation("forge", "stained_glass/pink"));
        tag(Tags.Items.DYED_PURPLE)
                .addOptional(new ResourceLocation("forge", "glass/purple"))
                .addOptional(new ResourceLocation("forge", "stained_glass/purple"));
        tag(Tags.Items.DYED_RED)
                .addOptional(new ResourceLocation("forge", "glass/red"))
                .addOptional(new ResourceLocation("forge", "stained_glass/red"));
        tag(Tags.Items.DYED_WHITE)
                .addOptional(new ResourceLocation("forge", "glass/white"))
                .addOptional(new ResourceLocation("forge", "stained_glass/white"));
        tag(Tags.Items.DYED_YELLOW)
                .addOptional(new ResourceLocation("forge", "glass/yellow"))
                .addOptional(new ResourceLocation("forge", "stained_glass/yellow"));
        tagWithOptionalLegacy(Tags.Items.ENDER_PEARLS);
        tagWithOptionalLegacy(Tags.Items.FEATHERS);
        tagWithOptionalLegacy(Tags.Items.GEMS);
        tagWithOptionalLegacy(Tags.Items.GEMS_AMETHYST);
        tagWithOptionalLegacy(Tags.Items.GEMS_DIAMOND);
        tagWithOptionalLegacy(Tags.Items.GEMS_EMERALD);
        tagWithOptionalLegacy(Tags.Items.GEMS_LAPIS);
        tagWithOptionalLegacy(Tags.Items.GEMS_PRISMARINE);
        tagWithOptionalLegacy(Tags.Items.GEMS_QUARTZ);
        tagWithOptionalLegacy(Tags.Items.GUNPOWDER);
        tagWithOptionalLegacy(Tags.Items.HEADS);
        tagWithOptionalLegacy(Tags.Items.INGOTS);
        tagWithOptionalLegacy(Tags.Items.INGOTS_BRICK);
        tagWithOptionalLegacy(Tags.Items.INGOTS_COPPER);
        tagWithOptionalLegacy(Tags.Items.INGOTS_GOLD);
        tagWithOptionalLegacy(Tags.Items.INGOTS_IRON);
        tagWithOptionalLegacy(Tags.Items.INGOTS_NETHERITE);
        tagWithOptionalLegacy(Tags.Items.INGOTS_NETHER_BRICK);
        tagWithOptionalLegacy(Tags.Items.LEATHER);
        tagWithOptionalLegacy(Tags.Items.MUSHROOMS);
        tagWithOptionalLegacy(Tags.Items.NETHER_STARS);
        tagWithOptionalLegacy(Tags.Items.NUGGETS);
        tagWithOptionalLegacy(Tags.Items.NUGGETS_IRON);
        tagWithOptionalLegacy(Tags.Items.NUGGETS_GOLD);
        tagWithOptionalLegacy(Tags.Items.OBSIDIAN);
        tagWithOptionalLegacy(Tags.Items.RAW_MATERIALS);
        tagWithOptionalLegacy(Tags.Items.RAW_MATERIALS_COPPER);
        tagWithOptionalLegacy(Tags.Items.RAW_MATERIALS_GOLD);
        tagWithOptionalLegacy(Tags.Items.RAW_MATERIALS_IRON);
        tagWithOptionalLegacy(Tags.Items.RODS);
        tagWithOptionalLegacy(Tags.Items.RODS_BLAZE);
        tagWithOptionalLegacy(Tags.Items.RODS_WOODEN);
        tagWithOptionalLegacy(Tags.Items.SEEDS);
        tagWithOptionalLegacy(Tags.Items.SEEDS_BEETROOT);
        tagWithOptionalLegacy(Tags.Items.SEEDS_MELON);
        tagWithOptionalLegacy(Tags.Items.SEEDS_PUMPKIN);
        tagWithOptionalLegacy(Tags.Items.SEEDS_WHEAT);
        tagWithOptionalLegacy(Tags.Items.SLIMEBALLS);
        tagWithOptionalLegacy(Tags.Items.STRINGS);
        tagWithOptionalLegacy(Tags.Items.TOOLS_SHIELDS);
        tagWithOptionalLegacy(Tags.Items.TOOLS_BOWS);
        tagWithOptionalLegacy(Tags.Items.TOOLS_CROSSBOWS);
        tagWithOptionalLegacy(Tags.Items.TOOLS_FISHING_RODS);
        tag(Tags.Items.TOOLS_SHEARS).addOptionalTag(new ResourceLocation("forge", "shears"));
        tag(Tags.Items.TOOLS_SPEARS).addOptionalTag(new ResourceLocation("forge", "tools/tridents"));
        tagWithOptionalLegacy(Tags.Items.TOOLS);
        tagWithOptionalLegacy(Tags.Items.ARMORS_HELMETS);
        tagWithOptionalLegacy(Tags.Items.ARMORS_CHESTPLATES);
        tagWithOptionalLegacy(Tags.Items.ARMORS_LEGGINGS);
        tagWithOptionalLegacy(Tags.Items.ARMORS_BOOTS);
        tagWithOptionalLegacy(Tags.Items.ARMORS);
    }

    private IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> tagWithOptionalLegacy(TagKey<Item> tag)
    {
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Item> tagAppender = tag(tag);
        tagAppender.addOptionalTag(new ResourceLocation("forge", tag.location().getPath()));
        return tagAppender;
    }

    private void tagColoredWithOptionalLegacy(TagKey<Item> group)
    {
        String prefix = group.location().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color  : DyeColor.values())
        {
            TagKey<Item> tag = getForgeItemTag(prefix + color.getName());
            tagWithOptionalLegacy(tag);
        }
    }

    private void addColored(TagKey<Item> group, String pattern)
    {
        String prefix = group.location().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color  : DyeColor.values())
        {
            ResourceLocation key = new ResourceLocation("minecraft", pattern.replace("{color}",  color.getName()));
            TagKey<Item> tag = getForgeItemTag(prefix + color.getName());
            Item item = ForgeRegistries.ITEMS.getValue(key);
            if (item == null || item  == Items.AIR)
                throw new IllegalStateException("Unknown vanilla item: " + key);
            tag(tag).add(item);
        }
    }

    private void addColoredTags(Consumer<TagKey<Item>> consumer, TagKey<Item> group)
    {
        String prefix = group.location().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color  : DyeColor.values())
        {
            TagKey<Item> tag = getForgeItemTag(prefix + color.getName());
            consumer.accept(tag);
        }
    }

    @SuppressWarnings("unchecked")
    private TagKey<Item> getForgeItemTag(String name)
    {
        try
        {
            name = name.toUpperCase(Locale.ENGLISH);
            return (TagKey<Item>)Tags.Items.class.getDeclaredField(name).get(null);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
        {
            throw new IllegalStateException(Tags.Items.class.getName() + " is missing tag name: " + name);
        }
    }

    @Override
    public String getName()
    {
        return "Neoforge Item Tags";
    }
}
