/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags.Fluids;

public final class NeoForgeFluidTagsProvider extends FluidTagsProvider {
    public NeoForgeFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, "neoforge");
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider) {
        tag(Fluids.WATER).add(net.minecraft.world.level.material.Fluids.WATER).add(net.minecraft.world.level.material.Fluids.FLOWING_WATER);
        tag(Fluids.LAVA).add(net.minecraft.world.level.material.Fluids.LAVA).add(net.minecraft.world.level.material.Fluids.FLOWING_LAVA);
        tag(Fluids.MILK).addOptional(NeoForgeMod.MILK.get()).addOptional(NeoForgeMod.FLOWING_MILK.get());
        tag(Fluids.GASEOUS);
        tag(Fluids.HONEY);
        tag(Fluids.EXPERIENCE);
        tag(Fluids.POTION);
        tag(Fluids.SUSPICIOUS_STEW);
        tag(Fluids.MUSHROOM_STEW);
        tag(Fluids.RABBIT_STEW);
        tag(Fluids.BEETROOT_SOUP);
        tag(Fluids.HIDDEN_FROM_RECIPE_VIEWERS);

        // Backwards compat with pre-1.21 tags. Done after so optional tag is last for better readability.
        // TODO: Remove backwards compat tag entries in 1.22
        tagWithOptionalLegacy(Fluids.MILK);
        tagWithOptionalLegacy(Fluids.GASEOUS);
        tagWithOptionalLegacy(Fluids.HONEY);
        tagWithOptionalLegacy(Fluids.POTION);
        tagWithOptionalLegacy(Fluids.SUSPICIOUS_STEW);
        tagWithOptionalLegacy(Fluids.MUSHROOM_STEW);
        tagWithOptionalLegacy(Fluids.RABBIT_STEW);
        tagWithOptionalLegacy(Fluids.BEETROOT_SOUP);
    }

    private TagAppender<Fluid, Fluid> tagWithOptionalLegacy(TagKey<Fluid> tag) {
        TagAppender<Fluid, Fluid> tagAppender = tag(tag);
        tagAppender.addOptionalTag(TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath("forge", tag.location().getPath())));
        return tagAppender;
    }
}
