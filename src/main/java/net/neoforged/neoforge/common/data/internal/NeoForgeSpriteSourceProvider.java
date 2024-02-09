/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.textures.atlas.DirectoryPalettedPermutations;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

public class NeoForgeSpriteSourceProvider extends SpriteSourceProvider {
    public static final ResourceLocation ARMOR_TRIMS_ATLAS = new ResourceLocation("minecraft:armor_trims");

    public NeoForgeSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper fileHelper) {
        super(output, lookupProvider, "neoforge", fileHelper);
    }

    @Override
    protected void gather() {
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation("neoforge:white"), Optional.empty()));

        Map<String, ResourceLocation> permutations = new HashMap<>();
        permutations.put("quartz", new ResourceLocation("minecraft:trims/color_palettes/quartz"));
        permutations.put("iron", new ResourceLocation("minecraft:trims/color_palettes/iron"));
        permutations.put("gold", new ResourceLocation("minecraft:trims/color_palettes/gold"));
        permutations.put("diamond", new ResourceLocation("minecraft:trims/color_palettes/diamond"));
        permutations.put("netherite", new ResourceLocation("minecraft:trims/color_palettes/netherite"));
        permutations.put("redstone", new ResourceLocation("minecraft:trims/color_palettes/redstone"));
        permutations.put("copper", new ResourceLocation("minecraft:trims/color_palettes/copper"));
        permutations.put("emerald", new ResourceLocation("minecraft:trims/color_palettes/emerald"));
        permutations.put("lapis", new ResourceLocation("minecraft:trims/color_palettes/lapis"));
        permutations.put("amethyst", new ResourceLocation("minecraft:trims/color_palettes/amethyst"));
        permutations.put("iron_darker", new ResourceLocation("minecraft:trims/color_palettes/iron_darker"));
        permutations.put("gold_darker", new ResourceLocation("minecraft:trims/color_palettes/gold_darker"));
        permutations.put("diamond_darker", new ResourceLocation("minecraft:trims/color_palettes/diamond_darker"));
        permutations.put("netherite_darker", new ResourceLocation("minecraft:trims/color_palettes/netherite_darker"));
        atlas(ARMOR_TRIMS_ATLAS).addSource(new DirectoryPalettedPermutations("trims/models/armor", new ResourceLocation("minecraft:trims/color_palettes/trim_palette"), permutations));
    }
}
