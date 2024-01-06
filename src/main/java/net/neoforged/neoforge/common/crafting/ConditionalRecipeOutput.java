/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Wrapper around a {@link RecipeOutput} that adds conditions to all received recipes.
 * Do not construct directly, obtain via {@link RecipeOutput#withConditions(ICondition...)}.
 */
public class ConditionalRecipeOutput implements RecipeOutput {
    public static final Criterion<?> DUMMY = CriteriaTriggers.TICK.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));

    private final RecipeOutput inner;
    private final ICondition[] conditions;
    private final List<WithConditions<RecipeBuilder>> alternatives = new ArrayList<>();

    @ApiStatus.Internal
    public ConditionalRecipeOutput(RecipeOutput inner, ICondition[] conditions) {
        this.inner = inner;
        this.conditions = conditions;
    }

    public ConditionalRecipeOutput withAlternative(RecipeBuilder alternative, ICondition... conditions) {
        this.alternatives.add(new WithConditions<>(alternative, conditions));
        return this;
    }

    @Override
    public Advancement.Builder advancement() {
        return inner.advancement();
    }

    private boolean inheritAdvancements;

    /**
     * Inherit advancements in alternative recipes.
     */
    public ConditionalRecipeOutput inheritAdvancements() {
        this.inheritAdvancements = true;
        return this;
    }

    @Override
    public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder superAdvancement, List<ICondition> conditions, List<WithConditions<Pair<Recipe<?>, Advancement>>> alternatives) {
        final var actualAlternatives = Lists.newArrayList(alternatives);
        this.alternatives.forEach(alt -> {
            // If we're inheriting advancements, this will simply make sure that recipes have a criterion and don't complain
            if (inheritAdvancements) {
                alt.carrier().unlockedBy("dummy", DUMMY);
            }

            alt.carrier().save(new RecipeOutput() {
                @Override
                public Advancement.Builder advancement() {
                    return inner.advancement();
                }

                @Override
                public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, List<ICondition> conditions, List<WithConditions<Pair<Recipe<?>, Advancement>>> alternatives) {
                    conditions = Lists.newArrayList(conditions);
                    conditions.addAll(alt.conditions());
                    final Advancement actualAdvancement;
                    if (advancement == null || inheritAdvancements) {
                        actualAdvancement = superAdvancement == null ? null : superAdvancement.value();
                    } else {
                        actualAdvancement = advancement.value();
                    }
                    actualAlternatives.add(new WithConditions<>(conditions, Pair.of(recipe, actualAdvancement)));
                }
            });
        });

        conditions = Lists.newArrayList(conditions);
        conditions.addAll(List.of(this.conditions));

        inner.accept(id, recipe, superAdvancement, conditions, actualAlternatives);
    }
}
