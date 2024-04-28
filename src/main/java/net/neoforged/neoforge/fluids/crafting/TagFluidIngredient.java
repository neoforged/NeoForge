/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Fluid ingredient that matches all fluids within the given tag.
 * <p>
 * Unlike with ingredients, this is an explicit "type" of fluid ingredient,
 * though it may still be written without a type field, see {@link FluidIngredient#MAP_CODEC}
 */
public class TagFluidIngredient extends FluidIngredient {
    public static final MapCodec<TagFluidIngredient> CODEC = TagKey.codec(Registries.FLUID)
            .xmap(TagFluidIngredient::new, TagFluidIngredient::tag).fieldOf("tag");

    private final TagKey<Fluid> tag;

    public TagFluidIngredient(TagKey<Fluid> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(FluidStack fluidStack) {
        return fluidStack.is(tag);
    }

    @Override
    protected Stream<FluidStack> generateStacks() {
        return BuiltInRegistries.FLUID.getTag(tag)
                .stream()
                .flatMap(HolderSet::stream)
                .map(fluid -> new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }

    @Override
    public boolean isSimple() {
        return true;
    }

    @Override
    public FluidIngredientType<?> getType() {
        return NeoForgeMod.TAG_FLUID_INGREDIENT_TYPE.get();
    }

    @Override
    public int hashCode() {
        return tag.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        return obj instanceof TagFluidIngredient tag && tag.tag.equals(this.tag);
    }

    public TagKey<Fluid> tag() {
        return tag;
    }
}
