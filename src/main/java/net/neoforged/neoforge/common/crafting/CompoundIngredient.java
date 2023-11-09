/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Ingredient that matches if any of the child ingredients match */
public class CompoundIngredient extends Ingredient {
    public static final Codec<CompoundIngredient> CODEC = NeoForgeExtraCodecs.aliasedFieldOf(Ingredient.LIST_CODEC, "children", "ingredients").xmap(CompoundIngredient::new, CompoundIngredient::getChildren).codec();
    public static final Codec<CompoundIngredient> DIRECT_CODEC = Ingredient.LIST_CODEC.xmap(CompoundIngredient::new, CompoundIngredient::getChildren);
    public static final Codec<CompoundIngredient> CODEC_NONEMPTY = NeoForgeExtraCodecs.aliasedFieldOf(Ingredient.LIST_CODEC_NONEMPTY, "children", "ingredients").xmap(CompoundIngredient::new, CompoundIngredient::getChildren).codec();
    public static final Codec<CompoundIngredient> DIRECT_CODEC_NONEMPTY = Ingredient.LIST_CODEC_NONEMPTY.xmap(CompoundIngredient::new, CompoundIngredient::getChildren);

    private final List<Ingredient> children;
    private final boolean isSimple;
    private final boolean synchronizeWithContents;

    protected CompoundIngredient(List<Ingredient> children) {
        super(children.stream().map(Value::new), NeoForgeMod.COMPOUND_INGREDIENT_TYPE);
        this.children = Collections.unmodifiableList(children);
        this.isSimple = children.stream().allMatch(Ingredient::isSimple);
        this.synchronizeWithContents = children.stream().anyMatch(Ingredient::synchronizeWithContents);
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
    public ItemStack[] getItems() {
        if (synchronizeWithContents())
            return super.getItems();

        return children.stream().map(Ingredient::getItems).flatMap(Arrays::stream).toArray(ItemStack[]::new);
    }

    @Override
    public boolean test(@Nullable ItemStack p_43914_) {
        if (synchronizeWithContents())
            return super.test(p_43914_);

        return children.stream().anyMatch(i -> i.test(p_43914_));
    }

    @Override
    public IntList getStackingIds() {
        if (synchronizeWithContents())
            return super.getStackingIds();

        final var list = new IntArrayList();
        children.stream().map(Ingredient::getStackingIds).forEach(list::addAll);
        list.sort(IntComparators.NATURAL_COMPARATOR);
        return list;
    }

    @Override
    public boolean isSimple() {
        return isSimple;
    }

    @Override
    public boolean synchronizeWithContents() {
        return synchronizeWithContents;
    }

    @NotNull
    public List<Ingredient> getChildren() {
        return this.children;
    }

    private record Value(Ingredient inner) implements Ingredient.Value {
        @Override
        public Collection<ItemStack> getItems() {
            return List.of(inner.getItems());
        }
    }
}
