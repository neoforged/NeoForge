/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.Nullable;

/** Ingredient that matches if any of the child ingredients match */
public class CompoundIngredient extends ChildBasedIngredient {
    public static final MapCodec<CompoundIngredient> CODEC = NeoForgeExtraCodecs.aliasedFieldOf(Ingredient.LIST_CODEC, "children", "ingredients").xmap(CompoundIngredient::new, ChildBasedIngredient::getChildren);
    public static final Codec<CompoundIngredient> DIRECT_CODEC = Ingredient.LIST_CODEC.xmap(CompoundIngredient::new, ChildBasedIngredient::getChildren);
    public static final MapCodec<CompoundIngredient> CODEC_NONEMPTY = NeoForgeExtraCodecs.aliasedFieldOf(Ingredient.LIST_CODEC_NONEMPTY, "children", "ingredients").xmap(CompoundIngredient::new, ChildBasedIngredient::getChildren);
    public static final Codec<CompoundIngredient> DIRECT_CODEC_NONEMPTY = Ingredient.LIST_CODEC_NONEMPTY.xmap(CompoundIngredient::new, ChildBasedIngredient::getChildren);

    protected CompoundIngredient(List<Ingredient> children) {
        super(children.stream().map(Value::new), NeoForgeMod.COMPOUND_INGREDIENT_TYPE, children);
    }

    /** Creates a compound ingredient from the given list of ingredients */
    public static Ingredient of(Ingredient... children) {
        if (children.length == 0)
            return of();
        if (children.length == 1)
            return children[0];

        return new CompoundIngredient(List.of(children));
    }

    @Override
    protected Stream<ItemStack> generateMatchingStacks() {
        return children.stream().flatMap(child -> Arrays.stream(child.getItems()));
    }

    @Override
    protected boolean testComplex(@Nullable ItemStack stack) {
        return children.stream().anyMatch(i -> i.test(stack));
    }

    private record Value(Ingredient inner) implements Ingredient.Value {
        @Override
        public Collection<ItemStack> getItems() {
            return List.of(inner.getItems());
        }
    }
}
