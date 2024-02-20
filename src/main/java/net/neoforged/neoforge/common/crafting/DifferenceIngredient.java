/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

/** Ingredient that matches everything from the first ingredient that is not included in the second ingredient */
public class DifferenceIngredient extends ChildBasedIngredient {
    public static final Codec<DifferenceIngredient> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            Ingredient.CODEC.fieldOf("base").forGetter(DifferenceIngredient::getBase),
                            Ingredient.CODEC.fieldOf("subtracted").forGetter(DifferenceIngredient::getSubtracted))
                    .apply(builder, DifferenceIngredient::new));

    public static final Codec<DifferenceIngredient> CODEC_NONEMPTY = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(DifferenceIngredient::getBase),
                            Ingredient.CODEC_NONEMPTY.fieldOf("subtracted").forGetter(DifferenceIngredient::getSubtracted))
                    .apply(builder, DifferenceIngredient::new));

    private final Ingredient base;
    private final Ingredient subtracted;

    protected DifferenceIngredient(Ingredient base, Ingredient subtracted) {
        super(Arrays.stream(base.getValues()).map(value -> new SubtractingValue(value, subtracted)), NeoForgeMod.DIFFERENCE_INGREDIENT_TYPE, List.of(base, subtracted));

        this.base = base;
        this.subtracted = subtracted;
    }

    public Ingredient getBase() {
        return base;
    }

    public Ingredient getSubtracted() {
        return subtracted;
    }

    @Override
    protected Stream<ItemStack> generateMatchingStacks() {
        return Arrays.stream(base.getItems()).filter(subtracted.negate());
    }

    @Override
    protected boolean testNonSynchronized(@Nullable ItemStack stack) {
        return base.test(stack) && !subtracted.test(stack);
    }

    @Override
    protected IntList generateStackingIds() {
        final IntList stackingIds = new IntArrayList(base.getStackingIds());
        stackingIds.removeAll(subtracted.getStackingIds());
        return stackingIds;
    }

    /**
     * Gets the difference from the two ingredients
     * 
     * @param base       Ingredient the item must match
     * @param subtracted Ingredient the item must not match
     * @return Ingredient that {@code base} anything in base that is not in {@code subtracted}
     */
    public static DifferenceIngredient of(Ingredient base, Ingredient subtracted) {
        return new DifferenceIngredient(base, subtracted);
    }

    private record SubtractingValue(Value inner, Ingredient subtracted) implements Ingredient.Value {
        @Override
        public Collection<ItemStack> getItems() {
            return inner().getItems().stream().filter(subtracted.negate()).toList();
        }
    }
}
