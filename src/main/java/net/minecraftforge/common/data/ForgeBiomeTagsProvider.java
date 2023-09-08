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
        tag(Biomes.PLAINS, Tags.Biomes.IS_PLAINS);
        tag(Biomes.DESERT, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_DRY_OVERWORLD, Tags.Biomes.IS_SANDY, Tags.Biomes.IS_DESERT);
        tag(Biomes.TAIGA, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_CONIFEROUS);
        tag(Biomes.SWAMP, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_SWAMP);
        tag(Biomes.NETHER_WASTES, Tags.Biomes.IS_HOT_NETHER, Tags.Biomes.IS_DRY_NETHER);
        tag(Biomes.THE_END, Tags.Biomes.IS_COLD_END, Tags.Biomes.IS_DRY_END);
        tag(Biomes.FROZEN_OCEAN, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY);
        tag(Biomes.FROZEN_RIVER, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY);
        tag(Biomes.SNOWY_PLAINS, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_WASTELAND, Tags.Biomes.IS_PLAINS);
        tag(Biomes.MUSHROOM_FIELDS, Tags.Biomes.IS_MUSHROOM, Tags.Biomes.IS_RARE);
        tag(Biomes.JUNGLE, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD);
        tag(Biomes.SPARSE_JUNGLE, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_RARE);
        tag(Biomes.BEACH, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_SANDY);
        tag(Biomes.SNOWY_BEACH, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY);
        tag(Biomes.DARK_FOREST, Tags.Biomes.IS_SPOOKY, Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD);
        tag(Biomes.SNOWY_TAIGA, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_CONIFEROUS, Tags.Biomes.IS_SNOWY);
        tag(Biomes.OLD_GROWTH_PINE_TAIGA, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_CONIFEROUS);
        tag(Biomes.WINDSWEPT_FOREST, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD);
        tag(Biomes.SAVANNA, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD);
        tag(Biomes.SAVANNA_PLATEAU, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_RARE, Tags.Biomes.IS_MOUNTAIN_SLOPE, Tags.Biomes.IS_PLATEAU);
        tag(Biomes.BADLANDS, Tags.Biomes.IS_SANDY, Tags.Biomes.IS_DRY_OVERWORLD);
        tag(Biomes.WOODED_BADLANDS, Tags.Biomes.IS_SANDY, Tags.Biomes.IS_DRY_OVERWORLD, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_MOUNTAIN_SLOPE, Tags.Biomes.IS_PLATEAU);
        tag(Biomes.MEADOW, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_PLATEAU, Tags.Biomes.IS_MOUNTAIN_SLOPE, Tags.Biomes.IS_FLORAL);
        tag(Biomes.GROVE, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_CONIFEROUS, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_MOUNTAIN_SLOPE);
        tag(Biomes.SNOWY_SLOPES, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_MOUNTAIN_SLOPE);
        tag(Biomes.JAGGED_PEAKS, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_MOUNTAIN_PEAK);
        tag(Biomes.FROZEN_PEAKS, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_MOUNTAIN_PEAK);
        tag(Biomes.STONY_PEAKS, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_MOUNTAIN_PEAK);
        tag(Biomes.SMALL_END_ISLANDS, Tags.Biomes.IS_COLD_END, Tags.Biomes.IS_DRY_END);
        tag(Biomes.END_MIDLANDS, Tags.Biomes.IS_COLD_END, Tags.Biomes.IS_DRY_END);
        tag(Biomes.END_HIGHLANDS, Tags.Biomes.IS_COLD_END, Tags.Biomes.IS_DRY_END);
        tag(Biomes.END_BARRENS, Tags.Biomes.IS_COLD_END, Tags.Biomes.IS_DRY_END);
        tag(Biomes.WARM_OCEAN, Tags.Biomes.IS_HOT_OVERWORLD);
        tag(Biomes.COLD_OCEAN, Tags.Biomes.IS_COLD_OVERWORLD);
        tag(Biomes.DEEP_COLD_OCEAN, Tags.Biomes.IS_COLD_OVERWORLD);
        tag(Biomes.DEEP_FROZEN_OCEAN, Tags.Biomes.IS_COLD_OVERWORLD);
        tag(Biomes.THE_VOID, Tags.Biomes.IS_VOID);
        tag(Biomes.SUNFLOWER_PLAINS, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_RARE, Tags.Biomes.IS_FLORAL);
        tag(Biomes.WINDSWEPT_GRAVELLY_HILLS, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_RARE);
        tag(Biomes.FLOWER_FOREST, Tags.Biomes.IS_RARE);
        tag(Biomes.ICE_SPIKES, Tags.Biomes.IS_COLD_OVERWORLD, Tags.Biomes.IS_SNOWY, Tags.Biomes.IS_RARE);
        tag(Biomes.OLD_GROWTH_BIRCH_FOREST, Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_RARE);
        tag(Biomes.OLD_GROWTH_SPRUCE_TAIGA, Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_RARE);
        tag(Biomes.WINDSWEPT_SAVANNA, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_DRY_OVERWORLD, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_RARE);
        tag(Biomes.ERODED_BADLANDS, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_DRY_OVERWORLD, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD, Tags.Biomes.IS_RARE);
        tag(Biomes.BAMBOO_JUNGLE, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_RARE);
        tag(Biomes.LUSH_CAVES, Tags.Biomes.IS_CAVE, Tags.Biomes.IS_LUSH, Tags.Biomes.IS_WET_OVERWORLD);
        tag(Biomes.DRIPSTONE_CAVES, Tags.Biomes.IS_CAVE, Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD);
        tag(Biomes.SOUL_SAND_VALLEY, Tags.Biomes.IS_HOT_NETHER, Tags.Biomes.IS_DRY_NETHER);
        tag(Biomes.CRIMSON_FOREST, Tags.Biomes.IS_HOT_NETHER, Tags.Biomes.IS_DRY_NETHER);
        tag(Biomes.WARPED_FOREST, Tags.Biomes.IS_HOT_NETHER, Tags.Biomes.IS_DRY_NETHER);
        tag(Biomes.BASALT_DELTAS, Tags.Biomes.IS_HOT_NETHER, Tags.Biomes.IS_DRY_NETHER);
        tag(Biomes.MANGROVE_SWAMP, Tags.Biomes.IS_WET_OVERWORLD, Tags.Biomes.IS_HOT_OVERWORLD, Tags.Biomes.IS_SWAMP);
        tag(Biomes.DEEP_DARK, Tags.Biomes.IS_CAVE, Tags.Biomes.IS_RARE, Tags.Biomes.IS_SPOOKY);
        tag(Biomes.CHERRY_GROVE, Tags.Biomes.IS_PLATEAU, Tags.Biomes.IS_MOUNTAIN_SLOPE, Tags.Biomes.IS_FLORAL);

        tag(Tags.Biomes.IS_HOT).addTag(Tags.Biomes.IS_HOT_OVERWORLD).addTag(Tags.Biomes.IS_HOT_NETHER).addOptionalTag(Tags.Biomes.IS_HOT_END.location());
        tag(Tags.Biomes.IS_COLD).addTag(Tags.Biomes.IS_COLD_OVERWORLD).addOptionalTag(Tags.Biomes.IS_COLD_NETHER.location()).addTag(Tags.Biomes.IS_COLD_END);
        tag(Tags.Biomes.IS_SPARSE_VEGETATION).addTag(Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD).addOptionalTag(Tags.Biomes.IS_SPARSE_VEGETATION_NETHER.location()).addOptionalTag(Tags.Biomes.IS_SPARSE_VEGETATION_END.location());
        tag(Tags.Biomes.IS_DENSE_VEGETATION).addTag(Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD).addOptionalTag(Tags.Biomes.IS_DENSE_VEGETATION_NETHER.location()).addOptionalTag(Tags.Biomes.IS_DENSE_VEGETATION_END.location());
        tag(Tags.Biomes.IS_WET).addTag(Tags.Biomes.IS_WET_OVERWORLD).addOptionalTag(Tags.Biomes.IS_WET_NETHER.location()).addOptionalTag(Tags.Biomes.IS_WET_END.location());
        tag(Tags.Biomes.IS_DRY).addTag(Tags.Biomes.IS_DRY_OVERWORLD).addTag(Tags.Biomes.IS_DRY_NETHER).addTag(Tags.Biomes.IS_DRY_END);

        tag(Tags.Biomes.IS_MOUNTAIN).addTag(Tags.Biomes.IS_MOUNTAIN_PEAK).addTag(Tags.Biomes.IS_MOUNTAIN_SLOPE);
        tag(Tags.Biomes.IS_WATER).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER);
        tag(Tags.Biomes.IS_UNDERGROUND).addTag(Tags.Biomes.IS_CAVE);

        // Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
        // TODO: Remove backwards compat tag entries in 1.22
        tag(Tags.Biomes.IS_MOUNTAIN_SLOPE).addOptionalTag(new ResourceLocation("forge", "is_slope"));
        tag(Tags.Biomes.IS_MOUNTAIN_PEAK).addOptionalTag(new ResourceLocation("forge", "is_peak"));
        tagWithOptionalLegacy(Tags.Biomes.IS_MOUNTAIN);
        tagWithOptionalLegacy(Tags.Biomes.IS_HOT_OVERWORLD);
        tagWithOptionalLegacy(Tags.Biomes.IS_HOT_NETHER);
        tagWithOptionalLegacy(Tags.Biomes.IS_HOT_END);
        tagWithOptionalLegacy(Tags.Biomes.IS_HOT);
        tagWithOptionalLegacy(Tags.Biomes.IS_COLD_OVERWORLD);
        tagWithOptionalLegacy(Tags.Biomes.IS_COLD_NETHER);
        tagWithOptionalLegacy(Tags.Biomes.IS_COLD_END);
        tagWithOptionalLegacy(Tags.Biomes.IS_COLD);
        tagWithOptionalLegacy(Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD);
        tagWithOptionalLegacy(Tags.Biomes.IS_SPARSE_VEGETATION_NETHER);
        tagWithOptionalLegacy(Tags.Biomes.IS_SPARSE_VEGETATION_END);
        tagWithOptionalLegacy(Tags.Biomes.IS_SPARSE_VEGETATION);
        tag(Tags.Biomes.IS_SPARSE_VEGETATION_OVERWORLD).addOptionalTag(new ResourceLocation("forge", "is_sparse/overworld"));
        tag(Tags.Biomes.IS_SPARSE_VEGETATION_NETHER).addOptionalTag(new ResourceLocation("forge", "is_sparse/nether"));
        tag(Tags.Biomes.IS_SPARSE_VEGETATION_END).addOptionalTag(new ResourceLocation("forge", "is_sparse/end"));
        tag(Tags.Biomes.IS_SPARSE_VEGETATION).addOptionalTag(new ResourceLocation("forge", "is_sparse"));
        tag(Tags.Biomes.IS_DENSE_VEGETATION_OVERWORLD).addOptionalTag(new ResourceLocation("forge", "is_dense/overworld"));
        tag(Tags.Biomes.IS_DENSE_VEGETATION_NETHER).addOptionalTag(new ResourceLocation("forge", "is_dense/nether"));
        tag(Tags.Biomes.IS_DENSE_VEGETATION_END).addOptionalTag(new ResourceLocation("forge", "is_dense/end"));
        tag(Tags.Biomes.IS_DENSE_VEGETATION).addOptionalTag(new ResourceLocation("forge", "is_dense"));
        tagWithOptionalLegacy(Tags.Biomes.IS_WET_OVERWORLD);
        tagWithOptionalLegacy(Tags.Biomes.IS_WET_NETHER);
        tagWithOptionalLegacy(Tags.Biomes.IS_WET_END);
        tagWithOptionalLegacy(Tags.Biomes.IS_WET);
        tagWithOptionalLegacy(Tags.Biomes.IS_DRY_OVERWORLD);
        tagWithOptionalLegacy(Tags.Biomes.IS_DRY_NETHER);
        tagWithOptionalLegacy(Tags.Biomes.IS_DRY_END);
        tagWithOptionalLegacy(Tags.Biomes.IS_DRY);
        tagWithOptionalLegacy(Tags.Biomes.IS_CONIFEROUS);
        tagWithOptionalLegacy(Tags.Biomes.IS_SPOOKY);
        tagWithOptionalLegacy(Tags.Biomes.IS_DEAD);
        tagWithOptionalLegacy(Tags.Biomes.IS_LUSH);
        tagWithOptionalLegacy(Tags.Biomes.IS_MUSHROOM);
        tagWithOptionalLegacy(Tags.Biomes.IS_MAGICAL);
        tagWithOptionalLegacy(Tags.Biomes.IS_RARE);
        tagWithOptionalLegacy(Tags.Biomes.IS_PLATEAU);
        tagWithOptionalLegacy(Tags.Biomes.IS_MODIFIED);
        tagWithOptionalLegacy(Tags.Biomes.IS_FLORAL);
        tagWithOptionalLegacy(Tags.Biomes.IS_WATER);
        tagWithOptionalLegacy(Tags.Biomes.IS_DESERT);
        tagWithOptionalLegacy(Tags.Biomes.IS_PLAINS);
        tagWithOptionalLegacy(Tags.Biomes.IS_SWAMP);
        tagWithOptionalLegacy(Tags.Biomes.IS_SANDY);
        tagWithOptionalLegacy(Tags.Biomes.IS_SNOWY);
        tagWithOptionalLegacy(Tags.Biomes.IS_WASTELAND);
        tagWithOptionalLegacy(Tags.Biomes.IS_VOID);
        tagWithOptionalLegacy(Tags.Biomes.IS_CAVE);
    }

    @SafeVarargs
    private void tag(ResourceKey<Biome> biome, TagKey<Biome>... tags)
    {
        for(TagKey<Biome> key : tags)
        {
            tag(key).add(biome);
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
