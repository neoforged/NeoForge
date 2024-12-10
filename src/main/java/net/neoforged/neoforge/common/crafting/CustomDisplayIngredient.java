/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Ingredient that wraps another ingredient to override its {@link SlotDisplay}.
 */
public record CustomDisplayIngredient(Ingredient base, SlotDisplay display) implements ICustomIngredient {
    public static final MapCodec<CustomDisplayIngredient> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance
                    .group(
                            Ingredient.CODEC.fieldOf("base").forGetter(CustomDisplayIngredient::base),
                            SlotDisplay.CODEC.fieldOf("display").forGetter(CustomDisplayIngredient::display))
                    .apply(instance, CustomDisplayIngredient::new));

    public static Ingredient of(Ingredient base, SlotDisplay display) {
        return new CustomDisplayIngredient(base, display).toVanilla();
    }

    @Override
    public boolean test(ItemStack stack) {
        return base.test(stack);
    }

    @Override
    public Stream<Holder<Item>> items() {
        return base.items();
    }

    @Override
    public boolean isSimple() {
        return base.isSimple();
    }

    @Override
    public IngredientType<?> getType() {
        return NeoForgeMod.CUSTOM_DISPLAY_INGREDIENT.get();
    }
}
