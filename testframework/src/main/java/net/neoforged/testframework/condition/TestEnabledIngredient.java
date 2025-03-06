/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.impl.MutableTestFramework;
import net.neoforged.testframework.impl.TestFrameworkMod;

public record TestEnabledIngredient(Ingredient base, TestFramework framework, String testId) implements ICustomIngredient {

    public static final MapCodec<TestEnabledIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            Ingredient.CODEC.fieldOf("base").forGetter(i -> i.base),
                            MutableTestFramework.REFERENCE_CODEC.fieldOf("framework").forGetter(i -> i.framework),
                            Codec.STRING.fieldOf("testId").forGetter(i -> i.testId))
                    .apply(builder, TestEnabledIngredient::new));
    @Override
    public boolean test(ItemStack stack) {
        return base.test(stack) && framework.tests().isEnabled(testId);
    }

    @Override
    public Stream<Holder<Item>> items() {
        return base.items();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return TestFrameworkMod.TEST_ENABLED_INGREDIENT.get();
    }

    @Override
    public SlotDisplay display() {
        return base.display();
    }
}
