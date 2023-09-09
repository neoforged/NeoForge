/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ForgeBlockEntityTypeTagsProvider extends BlockEntityTagsProvider
{

    public ForgeBlockEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, "neoforge", existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider)
    {
        tag(Tags.BlockEntityTypes.RELOCATION_NOT_SUPPORTED);

        // Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
        // TODO: Remove backwards compat tag entries in 1.22
        tag(Tags.BlockEntityTypes.RELOCATION_NOT_SUPPORTED)
                .addOptionalTag(new ResourceLocation("forge", "relocation_not_supported"))
                .addOptionalTag(new ResourceLocation("forge", "immovable"));
    }

    @Override
    public String getName()
    {
        return "Neoforge BlockEntityType Tags";
    }
}
