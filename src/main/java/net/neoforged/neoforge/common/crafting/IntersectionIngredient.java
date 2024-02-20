/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

/** Ingredient that matches if all child ingredients match */
public class IntersectionIngredient extends ChildBasedIngredient {
    public static final Codec<IntersectionIngredient> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            Ingredient.LIST_CODEC.fieldOf("children").forGetter(ChildBasedIngredient::getChildren))
                    .apply(builder, IntersectionIngredient::new));

    public static final Codec<IntersectionIngredient> CODEC_NONEMPTY = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            Ingredient.LIST_CODEC_NONEMPTY.fieldOf("children").forGetter(ChildBasedIngredient::getChildren))
                    .apply(builder, IntersectionIngredient::new));

    protected IntersectionIngredient(List<Ingredient> children) {
        super(children.stream().flatMap(ingredient -> Arrays.stream(ingredient.getValues()).map(value -> {
            final List<Ingredient> matchers = new ArrayList<>(children);
            matchers.remove(ingredient);

            return new IntersectionValue(value, matchers);
        })), NeoForgeMod.INTERSECTION_INGREDIENT_TYPE, children);
    }

    /**
     * Gets an intersection ingredient
     * 
     * @param ingredients List of ingredients to match
     * @return Ingredient that only matches if all the passed ingredients match
     */
    public static Ingredient of(Ingredient... ingredients) {
        if (ingredients.length == 0)
            throw new IllegalArgumentException("Cannot create an IntersectionIngredient with no children, use Ingredient.of() to create an empty ingredient");
        if (ingredients.length == 1)
            return ingredients[0];

        return new IntersectionIngredient(Arrays.asList(ingredients));
    }

    @Override
    protected Stream<ItemStack> generateMatchingStacks() {
        return children.stream()
                .flatMap(child -> Arrays.stream(child.getItems()))
                .filter(this::testNonSynchronized);
    }

    @Override
    protected boolean testNonSynchronized(@Nullable ItemStack stack) {
        return children.stream().allMatch(c -> c.test(stack));
    }

    public record IntersectionValue(Value inner, List<Ingredient> other) implements Ingredient.Value {
        @Override
        public Collection<ItemStack> getItems() {
            return inner().getItems().stream()
                    .filter(stack -> other().stream().allMatch(ingredient -> ingredient.test(stack)))
                    .toList();
        }
    }
}
