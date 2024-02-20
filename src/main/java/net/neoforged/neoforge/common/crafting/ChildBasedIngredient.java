/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

/** Intermediary class for easing handling of ingredients that make use of multiple children */
public abstract class ChildBasedIngredient extends Ingredient {
    protected final List<Ingredient> children;
    private final boolean isSimple;
    private final boolean synchronizeWithContents;

    @Nullable
    private ItemStack[] filteredMatchingStacks;

    protected ChildBasedIngredient(Stream<? extends Ingredient.Value> values, Supplier<? extends IngredientType<?>> type, List<Ingredient> children) {
        super(values, type);
        this.children = Collections.unmodifiableList(children);
        this.isSimple = children.stream().allMatch(Ingredient::isSimple);
        this.synchronizeWithContents = children.stream().anyMatch(Ingredient::synchronizeWithContents);
    }

    protected abstract Stream<ItemStack> generateMatchingStacks();

    protected abstract boolean testNonSynchronized(@Nullable ItemStack stack);

    @Override
    public final ItemStack[] getItems() {
        if (synchronizeWithContents() && isSimple()) {
            return super.getItems();
        }

        if (this.filteredMatchingStacks == null) {
            this.filteredMatchingStacks = generateMatchingStacks()
                    .distinct()//Mimic super that calls distinct on the stacks
                    .toArray(ItemStack[]::new);
        }
        return this.filteredMatchingStacks;
    }

    @Override
    public final boolean test(@Nullable ItemStack stack) {
        return synchronizeWithContents() && isSimple() ? super.test(stack) : testNonSynchronized(stack);
    }

    @Override
    public final boolean isSimple() {
        return isSimple;
    }

    @Override
    public final boolean synchronizeWithContents() {
        return synchronizeWithContents;
    }

    public final List<Ingredient> getChildren() {
        return this.children;
    }
}
