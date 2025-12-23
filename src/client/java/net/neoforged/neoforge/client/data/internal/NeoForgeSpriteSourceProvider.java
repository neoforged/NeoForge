/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.data.internal;

import java.util.concurrent.CompletableFuture;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.AtlasIds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;
import net.neoforged.neoforge.client.textures.DirectoryPalettedPermutations;

public class NeoForgeSpriteSourceProvider extends SpriteSourceProvider {
    public NeoForgeSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, "neoforge");
    }

    @Override
    protected void gather() {
        atlas(AtlasIds.BLOCKS).addSource(new SingleFile(Identifier.fromNamespaceAndPath("neoforge", "white")));
        atlas(AtlasIds.ARMOR_TRIMS).addSource(new DirectoryPalettedPermutations("trims/entity", Identifier.withDefaultNamespace("trims/color_palettes/trim_palette"), "trims/color_palettes"));
        atlas(AtlasIds.ITEMS).addSource(new DirectoryPalettedPermutations("trims/items", Identifier.withDefaultNamespace("trims/color_palettes/trim_palette"), "trims/color_palettes"));
    }
}
