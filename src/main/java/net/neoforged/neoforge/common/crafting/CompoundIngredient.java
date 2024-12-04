/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/** Ingredient that matches if any of the child ingredients match */
public record CompoundIngredient(List<Ingredient> children) implements ICustomIngredient {

    public CompoundIngredient {
        if (children.isEmpty()) {
            // Empty ingredients are always represented as Ingredient.EMPTY.
            throw new IllegalArgumentException("Compound ingredient must have at least one child.");
        }
    }
    public static final MapCodec<CompoundIngredient> CODEC = NeoForgeExtraCodecs.aliasedFieldOf(Ingredient.CODEC.listOf(1, Integer.MAX_VALUE), "children", "ingredients").xmap(CompoundIngredient::new, CompoundIngredient::children);

    /** Creates a compound ingredient from the given list of ingredients */
    public static Ingredient of(Ingredient... children) {
        if (children.length == 1)
            return children[0];

        return new CompoundIngredient(List.of(children)).toVanilla();
    }

    @Override
    public Stream<Holder<Item>> items() {
        return children.stream().flatMap(Ingredient::items);
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

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(children.stream().map(Ingredient::display).toList());
    }
}
