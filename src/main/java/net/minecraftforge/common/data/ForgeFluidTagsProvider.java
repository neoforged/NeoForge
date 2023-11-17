/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.Tags.Fluids;

import java.util.concurrent.CompletableFuture;

public final class ForgeFluidTagsProvider extends FluidTagsProvider
{
    public ForgeFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, lookupProvider, "neoforge", existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider)
    {
        tag(Fluids.WATERS).add(net.minecraft.world.level.material.Fluids.WATER).add(net.minecraft.world.level.material.Fluids.FLOWING_WATER);
        tag(Fluids.LAVAS).add(net.minecraft.world.level.material.Fluids.LAVA).add(net.minecraft.world.level.material.Fluids.FLOWING_LAVA);
        tag(Fluids.MILKS).addOptional(ForgeMod.MILK.getId()).addOptional(ForgeMod.FLOWING_MILK.getId());
        tag(Fluids.GASEOUS);
        tag(Fluids.HONEYS);
        tag(Fluids.POTIONS);
        tag(Fluids.SUSPICIOUS_STEWS);
        tag(Fluids.MUSHROOM_STEWS);
        tag(Fluids.RABBIT_STEWS);
        tag(Fluids.BEETROOT_SOUPS);

        // Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
        // TODO: Remove backwards compat tag entries in 1.22
        tagWithOptionalLegacy(Fluids.MILKS);
        tagWithOptionalLegacy(Fluids.GASEOUS);
        tagWithOptionalLegacy(Fluids.HONEYS);
        tagWithOptionalLegacy(Fluids.POTIONS);
        tagWithOptionalLegacy(Fluids.SUSPICIOUS_STEWS);
        tagWithOptionalLegacy(Fluids.MUSHROOM_STEWS);
        tagWithOptionalLegacy(Fluids.RABBIT_STEWS);
        tagWithOptionalLegacy(Fluids.BEETROOT_SOUPS);
    }

    private IntrinsicHolderTagsProvider.IntrinsicTagAppender<Fluid> tagWithOptionalLegacy(TagKey<Fluid> tag)
    {
        IntrinsicHolderTagsProvider.IntrinsicTagAppender<Fluid> tagAppender = tag(tag);
        tagAppender.addOptionalTag(new ResourceLocation("forge", tag.location().getPath()));
        return tagAppender;
    }

    @Override
    public String getName()
    {
        return "Neoforge Fluid Tags";
    }
}
