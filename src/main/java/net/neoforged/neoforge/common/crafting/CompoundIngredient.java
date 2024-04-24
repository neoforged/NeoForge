/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/** Ingredient that matches if any of the child ingredients match */
public record CompoundIngredient(List<Ingredient> children) implements ICustomIngredient {
    public static final MapCodec<CompoundIngredient> CODEC = NeoForgeExtraCodecs.aliasedFieldOf(Ingredient.LIST_CODEC_NONEMPTY, "children", "ingredients").xmap(CompoundIngredient::new, CompoundIngredient::children);
    public static final Codec<CompoundIngredient> DIRECT_CODEC = Ingredient.LIST_CODEC.xmap(CompoundIngredient::new, CompoundIngredient::children);
    public static final Codec<CompoundIngredient> DIRECT_CODEC_NONEMPTY = Ingredient.LIST_CODEC_NONEMPTY.xmap(CompoundIngredient::new, CompoundIngredient::children);

    /** Creates a compound ingredient from the given list of ingredients */
    public static Ingredient of(Ingredient... children) {
        if (children.length == 0)
            return Ingredient.EMPTY;
        if (children.length == 1)
            return children[0];

        return new CompoundIngredient(List.of(children)).toVanilla();
    }

    @Override
    public Stream<ItemStack> getItems() {
        return children.stream().flatMap(child -> Arrays.stream(child.getItems()));
    }

    @Override
    public boolean test(ItemStack stack) {
        for (var child : children) {
            if (child.test(stack)) {
                return true;
            }
        }
        return false;
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
        return NeoForgeMod.COMPOUND_INGREDIENT_TYPE.get();
    }
}
