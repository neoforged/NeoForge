/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;

/** Ingredient that matches the given stack, performing an exact NBT match. Use {@link PartialNBTIngredient} if you need partial match. */
public class StrictNBTIngredient extends Ingredient {
    public static final Codec<StrictNBTIngredient> CODEC = NeoForgeExtraCodecs.mapWithAlternative(
            ((MapCodec.MapCodecCodec<ItemStack>) ItemStack.CODEC).codec(),
            ItemStack.CODEC.fieldOf("stack")).xmap(StrictNBTIngredient::new, StrictNBTIngredient::getStack).codec();

    protected StrictNBTIngredient(ItemStack stack) {
        super(Stream.of(new Ingredient.ItemValue(stack, StrictNBTIngredient::compareStacksWithNbt)), NeoForgeMod.STRICT_NBT_INGREDIENT_TYPE::get);
    }

    /** Creates a new ingredient matching the given stack and tag */
    public static StrictNBTIngredient of(ItemStack stack) {
        return new StrictNBTIngredient(stack);
    }

    public ItemStack getStack() {
        return getItems()[0]; // This is safe to do, since we always pass in a single
    }

    @Override
    protected boolean areStacksEqual(ItemStack left, ItemStack right) {
        return compareStacksWithNbt(left, right);
    }

    private static boolean compareStacksWithNbt(ItemStack left, ItemStack right) {
        return left.getItem() == right.getItem()
                && left.getDamageValue() == right.getDamageValue()
                && left.areShareTagsEqual(right);
    }

    @Override
    public boolean synchronizeWithContents() {
        return false;
    }

    @Override
    public boolean isSimple() {
        return false;
    }
}
