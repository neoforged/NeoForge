/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.textures.DirectoryPalettedPermutations;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

public class NeoForgeSpriteSourceProvider extends SpriteSourceProvider {
    public NeoForgeSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper fileHelper) {
        super(output, lookupProvider, "neoforge", fileHelper);
    }

    @Override
    protected void gather() {
        atlas(SpriteSourceProvider.BLOCKS_ATLAS)
                .addSource(new SingleFile(ResourceLocation.fromNamespaceAndPath("neoforge", "white"), Optional.empty()))
                .addSource(new DirectoryPalettedPermutations("trims/items", ResourceLocation.withDefaultNamespace("trims/color_palettes/trim_palette"), "trims/color_palettes"));
        atlas(SpriteSourceProvider.ARMOR_TRIMS_ATLAS).addSource(new DirectoryPalettedPermutations("trims/models", ResourceLocation.withDefaultNamespace("trims/color_palettes/trim_palette"), "trims/color_palettes"));
    }
}
