/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForgeMod;

/** Ingredient that matches everything from the first ingredient that is not included in the second ingredient */
public record DifferenceIngredient(Ingredient base, Ingredient subtracted) implements ICustomIngredient {
    public static final MapCodec<DifferenceIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Ingredient.CODEC.fieldOf("base").forGetter(DifferenceIngredient::base),
                            Ingredient.CODEC.fieldOf("subtracted").forGetter(DifferenceIngredient::subtracted))
                    .apply(builder, DifferenceIngredient::new));

    @Override
    public Stream<ItemStack> getItems() {
        return Stream.of(base.getItems()).filter(subtracted.negate());
    }

    @Override
    public boolean test(ItemStack stack) {
        return base.test(stack) && !subtracted.test(stack);
    }

    @Override
    public boolean isSimple() {
        return base.isSimple() && subtracted.isSimple();
    }

    @Override
    public IngredientType<?> getType() {
        return NeoForgeMod.DIFFERENCE_INGREDIENT_TYPE.get();
    }

    /**
     * Gets the difference from the two ingredients
     * 
     * @param base       Ingredient the item must match
     * @param subtracted Ingredient the item must not match
     * @return Ingredient that {@code base} anything in base that is not in {@code subtracted}
     */
    public static Ingredient of(Ingredient base, Ingredient subtracted) {
        return new DifferenceIngredient(base, subtracted).toVanilla();
    }
}
