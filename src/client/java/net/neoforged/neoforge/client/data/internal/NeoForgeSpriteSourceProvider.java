/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.data.internal;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.client.resources.model.AtlasIds;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;
import net.neoforged.neoforge.client.textures.DirectoryPalettedPermutations;

public class NeoForgeSpriteSourceProvider extends SpriteSourceProvider {
    public NeoForgeSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, "neoforge");
    }

    @Override
    protected void gather() {
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(ResourceLocation.fromNamespaceAndPath("neoforge", "white"), Optional.empty()));
        atlas(AtlasIds.ARMOR_TRIMS).addSource(new DirectoryPalettedPermutations("trims/entity", "trims/color_palettes"));
        atlas(AtlasIds.BLOCKS).addSource(new DirectoryPalettedPermutations("trims/items", "trims/color_palettes"));
    }
}
