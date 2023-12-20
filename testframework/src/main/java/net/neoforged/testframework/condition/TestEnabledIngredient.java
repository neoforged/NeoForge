/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.impl.MutableTestFramework;
import net.neoforged.testframework.impl.TestFrameworkMod;
import org.jetbrains.annotations.Nullable;

public final class TestEnabledIngredient extends Ingredient {
    public static final Codec<TestEnabledIngredient> CODEC = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            Ingredient.CODEC.fieldOf("base").forGetter(i -> i.base),
                            MutableTestFramework.REFERENCE_CODEC.fieldOf("framework").forGetter(i -> i.framework),
                            Codec.STRING.fieldOf("testId").forGetter(i -> i.testId))
                    .apply(builder, TestEnabledIngredient::new));
    public static final Codec<TestEnabledIngredient> CODEC_NONEMPTY = RecordCodecBuilder.create(
            builder -> builder
                    .group(
                            Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(i -> i.base),
                            MutableTestFramework.REFERENCE_CODEC.fieldOf("framework").forGetter(i -> i.framework),
                            Codec.STRING.fieldOf("testId").forGetter(i -> i.testId))
                    .apply(builder, TestEnabledIngredient::new));

    private final Ingredient base;
    private final TestFramework framework;
    private final String testId;

    public TestEnabledIngredient(Ingredient base, TestFramework framework, String testId) {
        super(Arrays.stream(base.getValues()), TestFrameworkMod.TEST_ENABLED_INGREDIENT);
        this.base = base;
        this.framework = framework;
        this.testId = testId;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        return base.test(stack) && framework.tests().isEnabled(testId);
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
