/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public final class ForgeBiomeTagsProvider extends BiomeTagsProvider
{

    public ForgeBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, "neoforge", existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider)
    {
        tagWithOptionalLegacy(Biomes.PLAINS, Tags.Biomes.IS_PLAINS);
        tagWithOptionalLegacy(Biomes.DESERT, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_DRY_OVERWORLD, Tags.Biomes.IS_SANDY, Tags.Biomes.IS_DESERT);
        tagWithOptionalLegacy(Biomes.TAIGA, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_CONIFEROUS);
        tagWithOptionalLegacy(Biomes.SWAMP, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_SWAMP);
        tagWithOptionalLegacy(Biomes.NETHER_WASTES, Tags.Biomes.IS_HOT_NETHER, Tags.Biomes.IS_DRY_NETHER);
        tagWithOptionalLegacy(Biomes.THE_END, Tags.Biomes.IS_COLD_END, Tags.Biomes.IS_DRY_END);
        tagWithOptionalLegacy(Biomes.FROZEN_OCEAN, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY);
        tagWithOptionalLegacy(Biomes.FROZEN_RIVER, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY);
        tagWithOptionalLegacy(Biomes.SNOWY_PLAINS, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_WASTELAND, Tags.Biomes.IS_PLAINS);
        tagWithOptionalLegacy(Biomes.MUSHROOM_FIELDS, Tags.Biomes.IS_MUSHROOM, Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Biomes.JUNGLE, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_DENSE_OVERWORLD);
        tagWithOptionalLegacy(Biomes.SPARSE_JUNGLE, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Biomes.BEACH, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_SANDY);
        tagWithOptionalLegacy(Biomes.SNOWY_BEACH, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY);
        tagWithOptionalLegacy(Biomes.DARK_FOREST, Tags.Biomes.IS_SPOOKY, Tags.Biomes.IS_DENSE_OVERWORLD);
        tagWithOptionalLegacy(Biomes.SNOWY_TAIGA, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_CONIFEROUS, Tags.Biomes.IS_SNOWY);
        tagWithOptionalLegacy(Biomes.OLD_GROWTH_PINE_TAIGA, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_CONIFEROUS);
        tagWithOptionalLegacy(Biomes.WINDSWEPT_FOREST, Tags.Biomes.IS_SPARSE_OVERWORLD);
        tagWithOptionalLegacy(Biomes.SAVANNA, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_SPARSE_OVERWORLD);
        tagWithOptionalLegacy(Biomes.SAVANNA_PLATEAU, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_SPARSE_OVERWORLD, Tags.Biomes.IS_RARE, Tags.Biomes.IS_SLOPE, Tags.Biomes.IS_PLATEAU);
        tagWithOptionalLegacy(Biomes.BADLANDS, Tags.Biomes.IS_SANDY, Tags.Biomes.IS_DRY_OVERWORLD);
        tagWithOptionalLegacy(Biomes.WOODED_BADLANDS, Tags.Biomes.IS_SANDY, Tags.Biomes.IS_DRY_OVERWORLD, Tags.Biomes.IS_SPARSE_OVERWORLD, Tags.Biomes.IS_SLOPE, Tags.Biomes.IS_PLATEAU);
        tagWithOptionalLegacy(Biomes.MEADOW, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_PLATEAU, Tags.Biomes.IS_SLOPE);
        tagWithOptionalLegacy(Biomes.GROVE, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_CONIFEROUS, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_SLOPE);
        tagWithOptionalLegacy(Biomes.SNOWY_SLOPES, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SPARSE_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_SLOPE);
        tagWithOptionalLegacy(Biomes.JAGGED_PEAKS, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SPARSE_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_PEAK);
        tagWithOptionalLegacy(Biomes.FROZEN_PEAKS, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SPARSE_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_PEAK);
        tagWithOptionalLegacy(Biomes.STONY_PEAKS, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_PEAK);
        tagWithOptionalLegacy(Biomes.SMALL_END_ISLANDS, Tags.Biomes.IS_COLD_END, Tags.Biomes.IS_DRY_END);
        tagWithOptionalLegacy(Biomes.END_MIDLANDS, Tags.Biomes.IS_COLD_END, Tags.Biomes.IS_DRY_END);
        tagWithOptionalLegacy(Biomes.END_HIGHLANDS, Tags.Biomes.IS_COLD_END, Tags.Biomes.IS_DRY_END);
        tagWithOptionalLegacy(Biomes.END_BARRENS, Tags.Biomes.IS_COLD_END, Tags.Biomes.IS_DRY_END);
        tagWithOptionalLegacy(Biomes.WARM_OCEAN, Tags.Biomes.IS_HOT_OVERWORLD);
        tagWithOptionalLegacy(Biomes.COLD_OCEAN, Tags.Biomes.IS_COLD_OVERWORLD);
        tagWithOptionalLegacy(Biomes.DEEP_COLD_OCEAN, Tags.Biomes.IS_COLD_OVERWORLD);
        tagWithOptionalLegacy(Biomes.DEEP_FROZEN_OCEAN, Tags.Biomes.IS_COLD_OVERWORLD);
        tagWithOptionalLegacy(Biomes.THE_VOID, Tags.Biomes.IS_VOID);
        tagWithOptionalLegacy(Biomes.SUNFLOWER_PLAINS, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Biomes.WINDSWEPT_GRAVELLY_HILLS, Tags.Biomes.IS_SPARSE_OVERWORLD, Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Biomes.FLOWER_FOREST, Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Biomes.ICE_SPIKES, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Biomes.OLD_GROWTH_BIRCH_FOREST, Tags.Biomes.IS_DENSE_OVERWORLD, Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Biomes.OLD_GROWTH_SPRUCE_TAIGA, Tags.Biomes.IS_DENSE_OVERWORLD, Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Biomes.WINDSWEPT_SAVANNA, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_DRY_OVERWORLD, Tags.Biomes.IS_SPARSE_OVERWORLD, Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Biomes.ERODED_BADLANDS, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_DRY_OVERWORLD, Tags.Biomes.IS_SPARSE_OVERWORLD, Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Biomes.BAMBOO_JUNGLE, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Biomes.LUSH_CAVES, Tags.Biomes.IS_CAVE, Tags.Biomes.IS_LUSH, Tags.Biomes.IS_WET_OVERWORLD);
        tagWithOptionalLegacy(Biomes.DRIPSTONE_CAVES, Tags.Biomes.IS_CAVE, Tags.Biomes.IS_SPARSE_OVERWORLD);
        tagWithOptionalLegacy(Biomes.SOUL_SAND_VALLEY, Tags.Biomes.IS_HOT_NETHER, Tags.Biomes.IS_DRY_NETHER);
        tagWithOptionalLegacy(Biomes.CRIMSON_FOREST, Tags.Biomes.IS_HOT_NETHER, Tags.Biomes.IS_DRY_NETHER);
        tagWithOptionalLegacy(Biomes.WARPED_FOREST, Tags.Biomes.IS_HOT_NETHER, Tags.Biomes.IS_DRY_NETHER);
        tagWithOptionalLegacy(Biomes.BASALT_DELTAS, Tags.Biomes.IS_HOT_NETHER, Tags.Biomes.IS_DRY_NETHER);
        tagWithOptionalLegacy(Biomes.MANGROVE_SWAMP, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_SWAMP);
        tagWithOptionalLegacy(Biomes.DEEP_DARK, Tags.Biomes.IS_CAVE, Tags.Biomes.IS_RARE, Tags.Biomes.IS_SPOOKY);

        tag(Tags.Biomes.IS_HOT).addTag(Tags.Biomes.IS_HOT_OVERWORLD).addTag(Tags.Biomes.IS_HOT_NETHER).addOptionalTag(Tags.Biomes.IS_HOT_END.location());
        tag(Tags.Biomes.IS_COLD).addTag(Tags.Biomes.IS_COLD_OVERWORLD).addOptionalTag(Tags.Biomes.IS_COLD_NETHER.location()).addTag(Tags.Biomes.IS_COLD_END);
        tag(Tags.Biomes.IS_SPARSE).addTag(Tags.Biomes.IS_SPARSE_OVERWORLD).addOptionalTag(Tags.Biomes.IS_SPARSE_NETHER.location()).addOptionalTag(Tags.Biomes.IS_SPARSE_END.location());
        tag(Tags.Biomes.IS_DENSE).addTag(Tags.Biomes.IS_DENSE_OVERWORLD).addOptionalTag(Tags.Biomes.IS_DENSE_NETHER.location()).addOptionalTag(Tags.Biomes.IS_DENSE_END.location());
        tag(Tags.Biomes.IS_WET).addTag(Tags.Biomes.IS_WET_OVERWORLD).addOptionalTag(Tags.Biomes.IS_WET_NETHER.location()).addOptionalTag(Tags.Biomes.IS_WET_END.location());
        tag(Tags.Biomes.IS_DRY).addTag(Tags.Biomes.IS_DRY_OVERWORLD).addTag(Tags.Biomes.IS_DRY_NETHER).addTag(Tags.Biomes.IS_DRY_END);

        tag(Tags.Biomes.IS_WATER).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER);
        tag(Tags.Biomes.IS_MOUNTAIN).addTag(Tags.Biomes.IS_PEAK).addTag(Tags.Biomes.IS_SLOPE);
        tag(Tags.Biomes.IS_UNDERGROUND).addTag(Tags.Biomes.IS_CAVE);
    }

    @SafeVarargs
    private void tag(ResourceKey<Biome> biome, TagKey<Biome>... tags)
    {
        for(TagKey<Biome> key : tags)
        {
            tag(key).add(biome);
        }
    }

    @SafeVarargs
    private void tagWithOptionalLegacy(ResourceKey<Biome> biome, TagKey<Biome>... tags)
    {
        for(TagKey<Biome> key : tags)
        {
            tagWithOptionalLegacy(key).add(biome);
        }
    }

    private TagAppender<Biome> tagWithOptionalLegacy(TagKey<Biome> tag)
    {
        return tag(tag).addOptionalTag(new ResourceLocation("forge", tag.location().getPath()));
    }

    @Override
    public String getName()
    {
        return "Neoforge Biome Tags";
    }
}
