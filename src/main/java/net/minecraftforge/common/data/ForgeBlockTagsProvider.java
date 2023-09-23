/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static net.minecraftforge.common.Tags.Blocks.*;

public final class ForgeBlockTagsProvider extends BlockTagsProvider
{
    public ForgeBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, "neoforge", existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addTags(HolderLookup.Provider p_256380_)
    {
        tag(BARRELS).addTag(BARRELS_WOODEN);
        tag(BARRELS_WOODEN).add(Blocks.BARREL);
        tag(BOOKSHELVES).add(Blocks.BOOKSHELF);
        tag(CHESTS).addTags(CHESTS_ENDER, CHESTS_TRAPPED, CHESTS_WOODEN);
        tag(CHESTS_ENDER).add(Blocks.ENDER_CHEST);
        tag(CHESTS_TRAPPED).add(Blocks.TRAPPED_CHEST);
        tag(CHESTS_WOODEN).add(Blocks.CHEST, Blocks.TRAPPED_CHEST);
        tag(COBBLESTONE).addTags(COBBLESTONE_NORMAL, COBBLESTONE_INFESTED, COBBLESTONE_MOSSY, COBBLESTONE_DEEPSLATE);
        tag(COBBLESTONE_NORMAL).add(Blocks.COBBLESTONE);
        tag(COBBLESTONE_INFESTED).add(Blocks.INFESTED_COBBLESTONE);
        tag(COBBLESTONE_MOSSY).add(Blocks.MOSSY_COBBLESTONE);
        tag(COBBLESTONE_DEEPSLATE).add(Blocks.COBBLED_DEEPSLATE);
        addColored(DYED, "{color}_banner");
        addColored(DYED, "{color}_bed");
        addColored(DYED, "{color}_candle");
        addColored(DYED, "{color}_carpet");
        addColored(DYED, "{color}_concrete");
        addColored(DYED, "{color}_concrete_powder");
        addColored(DYED, "{color}_glazed_terracotta");
        addColored(DYED, "{color}_shulker_box");
        addColored(DYED, "{color}_stained_glass");
        addColored(DYED, "{color}_stained_glass_pane");
        addColored(DYED, "{color}_terracotta");
        addColored(DYED, "{color}_wall_banner");
        addColored(DYED, "{color}_wool");
        addColoredTags(tag(DYED)::addTag, DYED);
        tag(END_STONES).add(Blocks.END_STONE);
        tag(ENDERMAN_PLACE_ON_BLACKLIST);
        tag(FENCE_GATES).addTags(FENCE_GATES_WOODEN);
        tag(FENCE_GATES_WOODEN).add(Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.ACACIA_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.CRIMSON_FENCE_GATE, Blocks.WARPED_FENCE_GATE, Blocks.MANGROVE_FENCE_GATE, Blocks.BAMBOO_FENCE_GATE, Blocks.CHERRY_FENCE_GATE);
        tag(FENCES).addTags(FENCES_NETHER_BRICK, FENCES_WOODEN);
        tag(FENCES_NETHER_BRICK).add(Blocks.NETHER_BRICK_FENCE);
        tag(FENCES_WOODEN).addTag(BlockTags.WOODEN_FENCES);
        tag(GLASS_BLOCKS).addTags(GLASS_BLOCKS_COLORLESS, GLASS_BLOCKS_CHEAP, GLASS_BLOCKS_TINTED).add(Blocks.BLACK_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.PINK_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.RED_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS);
        tag(GLASS_BLOCKS_COLORLESS).add(Blocks.GLASS);
        tag(GLASS_BLOCKS_CHEAP).add(Blocks.GLASS, Blocks.BLACK_STAINED_GLASS, Blocks.BLUE_STAINED_GLASS, Blocks.BROWN_STAINED_GLASS, Blocks.CYAN_STAINED_GLASS, Blocks.GRAY_STAINED_GLASS, Blocks.GREEN_STAINED_GLASS, Blocks.LIGHT_BLUE_STAINED_GLASS, Blocks.LIGHT_GRAY_STAINED_GLASS, Blocks.LIME_STAINED_GLASS, Blocks.MAGENTA_STAINED_GLASS, Blocks.ORANGE_STAINED_GLASS, Blocks.PINK_STAINED_GLASS, Blocks.PURPLE_STAINED_GLASS, Blocks.RED_STAINED_GLASS, Blocks.WHITE_STAINED_GLASS, Blocks.YELLOW_STAINED_GLASS);
        tag(GLASS_BLOCKS_TINTED).add(Blocks.TINTED_GLASS);
        tag(GLASS_PANES).addTags(GLASS_PANES_COLORLESS).add(Blocks.BLACK_STAINED_GLASS_PANE, Blocks.BLUE_STAINED_GLASS_PANE, Blocks.BROWN_STAINED_GLASS_PANE, Blocks.CYAN_STAINED_GLASS_PANE, Blocks.GRAY_STAINED_GLASS_PANE, Blocks.GREEN_STAINED_GLASS_PANE, Blocks.LIGHT_BLUE_STAINED_GLASS_PANE, Blocks.LIGHT_GRAY_STAINED_GLASS_PANE, Blocks.LIME_STAINED_GLASS_PANE, Blocks.MAGENTA_STAINED_GLASS_PANE, Blocks.ORANGE_STAINED_GLASS_PANE, Blocks.PINK_STAINED_GLASS_PANE, Blocks.PURPLE_STAINED_GLASS_PANE, Blocks.RED_STAINED_GLASS_PANE, Blocks.WHITE_STAINED_GLASS_PANE, Blocks.YELLOW_STAINED_GLASS_PANE);
        tag(GLASS_PANES_COLORLESS).add(Blocks.GLASS_PANE);
        tag(GRAVEL).add(Blocks.GRAVEL);
        tag(NETHERRACK).add(Blocks.NETHERRACK);
        tag(OBSIDIAN).add(Blocks.OBSIDIAN);
        tag(ORE_BEARING_GROUND_DEEPSLATE).add(Blocks.DEEPSLATE);
        tag(ORE_BEARING_GROUND_NETHERRACK).add(Blocks.NETHERRACK);
        tag(ORE_BEARING_GROUND_STONE).add(Blocks.STONE);
        tag(ORE_RATES_DENSE).add(Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.REDSTONE_ORE);
        tag(ORE_RATES_SINGULAR).add(Blocks.ANCIENT_DEBRIS, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.GOLD_ORE, Blocks.IRON_ORE, Blocks.NETHER_QUARTZ_ORE);
        tag(ORE_RATES_SPARSE).add(Blocks.NETHER_GOLD_ORE);
        tag(ORES).addTags(ORES_COAL, ORES_COPPER, ORES_DIAMOND, ORES_EMERALD, ORES_GOLD, ORES_IRON, ORES_LAPIS, ORES_REDSTONE, ORES_QUARTZ, ORES_NETHERITE_SCRAP);
        tag(ORES_COAL).addTag(BlockTags.COAL_ORES);
        tag(ORES_COPPER).addTag(BlockTags.COPPER_ORES);
        tag(ORES_DIAMOND).addTag(BlockTags.DIAMOND_ORES);
        tag(ORES_EMERALD).addTag(BlockTags.EMERALD_ORES);
        tag(ORES_GOLD).addTag(BlockTags.GOLD_ORES);
        tag(ORES_IRON).addTag(BlockTags.IRON_ORES);
        tag(ORES_LAPIS).addTag(BlockTags.LAPIS_ORES);
        tag(ORES_QUARTZ).add(Blocks.NETHER_QUARTZ_ORE);
        tag(ORES_REDSTONE).addTag(BlockTags.REDSTONE_ORES);
        tag(ORES_NETHERITE_SCRAP).add(Blocks.ANCIENT_DEBRIS);
        tag(ORES_IN_GROUND_DEEPSLATE).add(Blocks.DEEPSLATE_COAL_ORE, Blocks.DEEPSLATE_COPPER_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.DEEPSLATE_IRON_ORE, Blocks.DEEPSLATE_LAPIS_ORE, Blocks.DEEPSLATE_REDSTONE_ORE);
        tag(ORES_IN_GROUND_NETHERRACK).add(Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE);
        tag(ORES_IN_GROUND_STONE).add(Blocks.COAL_ORE, Blocks.COPPER_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE, Blocks.GOLD_ORE, Blocks.IRON_ORE, Blocks.LAPIS_ORE, Blocks.REDSTONE_ORE);
        tag(SAND).addTags(SAND_COLORLESS, SAND_RED);
        tag(RELOCATION_NOT_SUPPORTED);
        tag(SAND_COLORLESS).add(Blocks.SAND);
        tag(SAND_RED).add(Blocks.RED_SAND);

        tag(SANDSTONE_RED_BLOCKS).add(Blocks.RED_SANDSTONE, Blocks.CUT_RED_SANDSTONE, Blocks.CHISELED_RED_SANDSTONE, Blocks.SMOOTH_RED_SANDSTONE);
        tag(SANDSTONE_UNCOLORED_BLOCKS).add(Blocks.SANDSTONE, Blocks.CUT_SANDSTONE, Blocks.CHISELED_SANDSTONE, Blocks.SMOOTH_SANDSTONE);
        tag(SANDSTONE_BLOCKS).addTags(SANDSTONE_RED_BLOCKS, SANDSTONE_UNCOLORED_BLOCKS);
        tag(SANDSTONE_RED_SLABS).add(Blocks.RED_SANDSTONE_SLAB, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.SMOOTH_RED_SANDSTONE_SLAB);
        tag(SANDSTONE_UNCOLORED_SLABS).add(Blocks.SANDSTONE_SLAB, Blocks.CUT_SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE_SLAB);
        tag(SANDSTONE_SLABS).addTags(SANDSTONE_RED_SLABS, SANDSTONE_UNCOLORED_SLABS);
        tag(SANDSTONE_RED_STAIRS).add(Blocks.RED_SANDSTONE_STAIRS, Blocks.SMOOTH_RED_SANDSTONE_STAIRS);
        tag(SANDSTONE_UNCOLORED_STAIRS).add(Blocks.SANDSTONE_STAIRS, Blocks.SMOOTH_SANDSTONE_STAIRS);
        tag(SANDSTONE_STAIRS).addTags(SANDSTONE_RED_STAIRS, SANDSTONE_UNCOLORED_STAIRS);

        tag(STONE).add(Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.INFESTED_STONE, Blocks.STONE, Blocks.DEEPSLATE, Blocks.INFESTED_DEEPSLATE, Blocks.TUFF);
        tag(WORLDGEN_STONE).add(Blocks.ANDESITE, Blocks.DIORITE, Blocks.GRANITE, Blocks.INFESTED_STONE, Blocks.STONE, Blocks.DEEPSLATE, Blocks.INFESTED_DEEPSLATE, Blocks.TUFF);
        tag(STORAGE_BLOCKS).addTags(STORAGE_BLOCKS_AMETHYST, STORAGE_BLOCKS_COAL, STORAGE_BLOCKS_COPPER, STORAGE_BLOCKS_DIAMOND, STORAGE_BLOCKS_EMERALD, STORAGE_BLOCKS_GOLD, STORAGE_BLOCKS_IRON, STORAGE_BLOCKS_LAPIS, STORAGE_BLOCKS_QUARTZ, STORAGE_BLOCKS_RAW_COPPER, STORAGE_BLOCKS_RAW_GOLD, STORAGE_BLOCKS_RAW_IRON, STORAGE_BLOCKS_REDSTONE, STORAGE_BLOCKS_NETHERITE);
        tag(STORAGE_BLOCKS_AMETHYST).add(Blocks.AMETHYST_BLOCK);
        tag(STORAGE_BLOCKS_COAL).add(Blocks.COAL_BLOCK);
        tag(STORAGE_BLOCKS_COPPER).add(Blocks.COPPER_BLOCK);
        tag(STORAGE_BLOCKS_DIAMOND).add(Blocks.DIAMOND_BLOCK);
        tag(STORAGE_BLOCKS_EMERALD).add(Blocks.EMERALD_BLOCK);
        tag(STORAGE_BLOCKS_GOLD).add(Blocks.GOLD_BLOCK);
        tag(STORAGE_BLOCKS_IRON).add(Blocks.IRON_BLOCK);
        tag(STORAGE_BLOCKS_LAPIS).add(Blocks.LAPIS_BLOCK);
        tag(STORAGE_BLOCKS_QUARTZ).add(Blocks.QUARTZ_BLOCK);
        tag(STORAGE_BLOCKS_RAW_COPPER).add(Blocks.RAW_COPPER_BLOCK);
        tag(STORAGE_BLOCKS_RAW_GOLD).add(Blocks.RAW_GOLD_BLOCK);
        tag(STORAGE_BLOCKS_RAW_IRON).add(Blocks.RAW_IRON_BLOCK);
        tag(STORAGE_BLOCKS_REDSTONE).add(Blocks.REDSTONE_BLOCK);
        tag(STORAGE_BLOCKS_NETHERITE).add(Blocks.NETHERITE_BLOCK);
        tag(BUDDING_BLOCKS).add(Blocks.BUDDING_AMETHYST);
        tag(BUDS).add(Blocks.SMALL_AMETHYST_BUD).add(Blocks.MEDIUM_AMETHYST_BUD).add(Blocks.LARGE_AMETHYST_BUD);
        tag(CLUSTERS).add(Blocks.AMETHYST_CLUSTER);

        // Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
        // TODO: Remove backwards compat tag entries in 1.22
        tagWithOptionalLegacy(BARRELS);
        tagWithOptionalLegacy(BARRELS_WOODEN);
        tagWithOptionalLegacy(BOOKSHELVES);
        tagWithOptionalLegacy(CHESTS);
        tagWithOptionalLegacy(CHESTS_ENDER);
        tagWithOptionalLegacy(CHESTS_TRAPPED);
        tagWithOptionalLegacy(CHESTS_WOODEN);
        tagWithOptionalLegacy(COBBLESTONE);
        tagWithOptionalLegacy(COBBLESTONE_NORMAL);
        tagWithOptionalLegacy(COBBLESTONE_INFESTED);
        tagWithOptionalLegacy(COBBLESTONE_MOSSY);
        tagWithOptionalLegacy(COBBLESTONE_DEEPSLATE);
        tagWithOptionalLegacy(END_STONES);
        tagWithOptionalLegacy(ENDERMAN_PLACE_ON_BLACKLIST);
        tagWithOptionalLegacy(FENCE_GATES);
        tagWithOptionalLegacy(FENCE_GATES_WOODEN);
        tagWithOptionalLegacy(FENCES);
        tagWithOptionalLegacy(FENCES_NETHER_BRICK);
        tagWithOptionalLegacy(FENCES_WOODEN);
        tag(DYED_BLACK)
                .addOptional(new ResourceLocation("forge", "glass/black"))
                .addOptional(new ResourceLocation("forge", "stained_glass/black"));
        tag(DYED_BLUE)
                .addOptional(new ResourceLocation("forge", "glass/blue"))
                .addOptional(new ResourceLocation("forge", "stained_glass/blue"));
        tag(DYED_BROWN)
                .addOptional(new ResourceLocation("forge", "glass/brown"))
                .addOptional(new ResourceLocation("forge", "stained_glass/brown"));
        tag(DYED_CYAN)
                .addOptional(new ResourceLocation("forge", "glass/cyan"))
                .addOptional(new ResourceLocation("forge", "stained_glass/cyan"));
        tag(DYED_GRAY)
                .addOptional(new ResourceLocation("forge", "glass/gray"))
                .addOptional(new ResourceLocation("forge", "stained_glass/gray"));
        tag(DYED_GREEN)
                .addOptional(new ResourceLocation("forge", "glass/green"))
                .addOptional(new ResourceLocation("forge", "stained_glass/green"));
        tag(DYED_LIGHT_BLUE)
                .addOptional(new ResourceLocation("forge", "glass/light_blue"))
                .addOptional(new ResourceLocation("forge", "stained_glass/light_blue"));
        tag(DYED_LIGHT_GRAY)
                .addOptional(new ResourceLocation("forge", "glass/light_gray"))
                .addOptional(new ResourceLocation("forge", "stained_glass/light_gray"));
        tag(DYED_LIME)
                .addOptional(new ResourceLocation("forge", "glass/lime"))
                .addOptional(new ResourceLocation("forge", "stained_glass/lime"));
        tag(DYED_MAGENTA)
                .addOptional(new ResourceLocation("forge", "glass/magenta"))
                .addOptional(new ResourceLocation("forge", "stained_glass/magenta"));
        tag(DYED_MAGENTA)
                .addOptional(new ResourceLocation("forge", "glass/magenta"))
                .addOptional(new ResourceLocation("forge", "stained_glass/magenta"));
        tag(DYED_ORANGE)
                .addOptional(new ResourceLocation("forge", "glass/orange"))
                .addOptional(new ResourceLocation("forge", "stained_glass/orange"));
        tag(DYED_PINK)
                .addOptional(new ResourceLocation("forge", "glass/pink"))
                .addOptional(new ResourceLocation("forge", "stained_glass/pink"));
        tag(DYED_PURPLE)
                .addOptional(new ResourceLocation("forge", "glass/purple"))
                .addOptional(new ResourceLocation("forge", "stained_glass/purple"));
        tag(DYED_RED)
                .addOptional(new ResourceLocation("forge", "glass/red"))
                .addOptional(new ResourceLocation("forge", "stained_glass/red"));
        tag(DYED_WHITE)
                .addOptional(new ResourceLocation("forge", "glass/white"))
                .addOptional(new ResourceLocation("forge", "stained_glass/white"));
        tag(DYED_YELLOW)
                .addOptional(new ResourceLocation("forge", "glass/yellow"))
                .addOptional(new ResourceLocation("forge", "stained_glass/yellow"));
        tag(GLASS_BLOCKS)
                .addOptional(new ResourceLocation("forge", "glass"));
        tag(GLASS_BLOCKS_COLORLESS).addOptional(new ResourceLocation("forge", "glass_colorless"));
        tag(GLASS_BLOCKS_CHEAP).addOptional(new ResourceLocation("forge", "glass_silica"));
        tag(GLASS_BLOCKS_TINTED).addOptional(new ResourceLocation("forge", "glass_tinted"));
        tag(GLASS_PANES_COLORLESS)
                .addOptional(new ResourceLocation("forge", "glass_panes_colorless"));
        tagWithOptionalLegacy(GRAVEL);
        tagWithOptionalLegacy(NETHERRACK);
        tagWithOptionalLegacy(OBSIDIAN);
        tagWithOptionalLegacy(ORE_BEARING_GROUND_DEEPSLATE);
        tagWithOptionalLegacy(ORE_BEARING_GROUND_NETHERRACK);
        tagWithOptionalLegacy(ORE_BEARING_GROUND_STONE);
        tagWithOptionalLegacy(ORE_RATES_DENSE);
        tagWithOptionalLegacy(ORE_RATES_SINGULAR);
        tagWithOptionalLegacy(ORE_RATES_SPARSE);
        tagWithOptionalLegacy(ORES);
        tagWithOptionalLegacy(ORES_COAL);
        tagWithOptionalLegacy(ORES_COPPER);
        tagWithOptionalLegacy(ORES_DIAMOND);
        tagWithOptionalLegacy(ORES_EMERALD);
        tagWithOptionalLegacy(ORES_GOLD);
        tagWithOptionalLegacy(ORES_IRON);
        tagWithOptionalLegacy(ORES_LAPIS);
        tagWithOptionalLegacy(ORES_QUARTZ);
        tagWithOptionalLegacy(ORES_REDSTONE);
        tagWithOptionalLegacy(ORES_NETHERITE_SCRAP);
        tagWithOptionalLegacy(ORES_IN_GROUND_DEEPSLATE);
        tagWithOptionalLegacy(ORES_IN_GROUND_NETHERRACK);
        tagWithOptionalLegacy(ORES_IN_GROUND_STONE);
        tagWithOptionalLegacy(SAND);
        tagWithOptionalLegacy(SAND_COLORLESS);
        tagWithOptionalLegacy(SAND_RED);
        tagWithOptionalLegacy(STONE);
        tagWithOptionalLegacy(STORAGE_BLOCKS);
        tagWithOptionalLegacy(STORAGE_BLOCKS_AMETHYST);
        tagWithOptionalLegacy(STORAGE_BLOCKS_COAL);
        tagWithOptionalLegacy(STORAGE_BLOCKS_COPPER);
        tagWithOptionalLegacy(STORAGE_BLOCKS_DIAMOND);
        tagWithOptionalLegacy(STORAGE_BLOCKS_EMERALD);
        tagWithOptionalLegacy(STORAGE_BLOCKS_GOLD);
        tagWithOptionalLegacy(STORAGE_BLOCKS_IRON);
        tagWithOptionalLegacy(STORAGE_BLOCKS_LAPIS);
        tagWithOptionalLegacy(STORAGE_BLOCKS_QUARTZ);
        tagWithOptionalLegacy(STORAGE_BLOCKS_RAW_COPPER);
        tagWithOptionalLegacy(STORAGE_BLOCKS_RAW_GOLD);
        tagWithOptionalLegacy(STORAGE_BLOCKS_RAW_IRON);
        tagWithOptionalLegacy(STORAGE_BLOCKS_REDSTONE);
        tagWithOptionalLegacy(STORAGE_BLOCKS_NETHERITE);
        tag(Tags.Blocks.RELOCATION_NOT_SUPPORTED)
                .addOptionalTag(new ResourceLocation("forge", "relocation_not_supported"))
                .addOptionalTag(new ResourceLocation("forge", "immovable"));
        tag(Tags.Blocks.SANDSTONE_BLOCKS).addOptionalTag(new ResourceLocation("forge", "sandstone"));
    }

    private IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tagWithOptionalLegacy(TagKey<Block> tag)
    {
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Block> tagAppender = tag(tag);
        tagAppender.addOptionalTag(new ResourceLocation("forge", tag.location().getPath()));
        return tagAppender;
    }

    private void addColored(TagKey<Block> group, String pattern)
    {
        String prefix = group.location().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color  : DyeColor.values())
        {
            ResourceLocation key = new ResourceLocation("minecraft", pattern.replace("{color}",  color.getName()));
            TagKey<Block> tag = getForgeTag(prefix + color.getName());
            Block block = ForgeRegistries.BLOCKS.getValue(key);
            if (block == null || block == Blocks.AIR)
                throw new IllegalStateException("Unknown vanilla block: " + key);
            tag(tag).add(block);
        }
    }

    private void addColoredTags(Consumer<TagKey<Block>> consumer, TagKey<Block> group)
    {
        String prefix = group.location().getPath().toUpperCase(Locale.ENGLISH) + '_';
        for (DyeColor color  : DyeColor.values())
        {
            TagKey<Block> tag = getForgeTag(prefix + color.getName());
            consumer.accept(tag);
        }
    }

    @SuppressWarnings("unchecked")
    private TagKey<Block> getForgeTag(String name)
    {
        try
        {
            name = name.toUpperCase(Locale.ENGLISH);
            return (TagKey<Block>) Tags.Blocks.class.getDeclaredField(name).get(null);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
        {
            throw new IllegalStateException(Tags.Blocks.class.getName() + " is missing tag name: " + name);
        }
    }

    @Override
    public String getName()
    {
        return "Neoforge Block Tags";
    }
}
