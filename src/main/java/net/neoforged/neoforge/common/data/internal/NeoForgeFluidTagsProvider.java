/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags.Fluids;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public final class NeoForgeFluidTagsProvider extends FluidTagsProvider
{
    public NeoForgeFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, "neoforge", existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider)
    {
        tag(Fluids.MILK).addOptional(NeoForgeMod.MILK.getId()).addOptional(NeoForgeMod.FLOWING_MILK.getId());
    }
}
