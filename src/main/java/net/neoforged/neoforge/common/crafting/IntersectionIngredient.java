/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForgeMod;

/** Ingredient that matches if all child ingredients match */
public record IntersectionIngredient(List<Ingredient> children) implements ICustomIngredient {

    public IntersectionIngredient {
        if (children.isEmpty()) {
            throw new IllegalArgumentException("Intersection ingredient must have at least one child.");
        }
    }
    public static final MapCodec<IntersectionIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Ingredient.CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("children").forGetter(IntersectionIngredient::children))
                    .apply(builder, IntersectionIngredient::new));

    /**
     * Gets an intersection ingredient
     * 
     * @param ingredients List of ingredients to match
     * @return Ingredient that only matches if all the passed ingredients match
     */
    public static Ingredient of(Ingredient... ingredients) {
        if (ingredients.length == 1)
            return ingredients[0];

        return new IntersectionIngredient(Arrays.asList(ingredients)).toVanilla();
    }

    @Override
    public boolean test(ItemStack stack) {
        for (var child : children) {
            if (!child.test(stack)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Stream<Holder<Item>> items() {
        return children.stream()
                .flatMap(child -> child.items())
                .filter(i -> test(i.value().getDefaultInstance()));
    }

    @Override
    public boolean isSimple() {
        for (var child : children) {
            if (!child.isSimple()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public IngredientType<?> getType() {
        return NeoForgeMod.INTERSECTION_INGREDIENT_TYPE.get();
    }
}
