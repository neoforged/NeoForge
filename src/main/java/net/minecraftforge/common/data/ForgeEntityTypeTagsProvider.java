/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ForgeEntityTypeTagsProvider extends EntityTypeTagsProvider
{

    public ForgeEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, "neoforge", existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider)
    {
        tag(Tags.EntityTypes.BOSSES).add(EntityType.ENDER_DRAGON, EntityType.WITHER);

        // Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
        // TODO: Remove backwards compat tag entries in 1.22
        tag(Tags.EntityTypes.BOSSES).addOptional(new ResourceLocation("forge", "bosses"));
    }

    @Override
    public String getName()
    {
        return "Neoforge EntityType Tags";
    }
}
