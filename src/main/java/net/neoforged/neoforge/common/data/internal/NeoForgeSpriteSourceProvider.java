/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class NeoForgeSpriteSourceProvider extends SpriteSourceProvider
{
    public NeoForgeSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper fileHelper)
    {
        super(output, lookupProvider, "neoforge", fileHelper);
    }

    @Override
    protected void gather()
    {
        atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(new ResourceLocation("forge:white"), Optional.empty()));
    }
}
