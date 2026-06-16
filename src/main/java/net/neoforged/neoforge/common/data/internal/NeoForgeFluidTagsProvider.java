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
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidIds;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags.Fluids;

public final class NeoForgeFluidTagsProvider extends FluidTagsProvider {
    public NeoForgeFluidTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, "neoforge");
    }

    @Override
    public void addTags(HolderLookup.Provider lookupProvider) {
        tag(Fluids.WATER).add(FluidIds.WATER).add(FluidIds.FLOWING_WATER);
        tag(Fluids.LAVA).add(FluidIds.LAVA).add(FluidIds.FLOWING_LAVA);
        tag(Fluids.MILK).addOptional(NeoForgeMod.MILK.getKey()).addOptional(NeoForgeMod.FLOWING_MILK.getKey());
        tag(Fluids.GASEOUS);
        tag(Fluids.HONEY);
        tag(Fluids.EXPERIENCE);
        tag(Fluids.POTION);
        tag(Fluids.SUSPICIOUS_STEW);
        tag(Fluids.MUSHROOM_STEW);
        tag(Fluids.RABBIT_STEW);
        tag(Fluids.BEETROOT_SOUP);
        tag(Fluids.HIDDEN_FROM_RECIPE_VIEWERS);
    }

    private TagAppender<Fluid> tagWithOptionalLegacy(TagKey<Fluid> tag) {
        TagAppender<Fluid> tagAppender = tag(tag);
        tagAppender.addOptionalTag(TagKey.create(Registries.FLUID, Identifier.fromNamespaceAndPath("forge", tag.location().getPath())));
        return tagAppender;
    }
}
