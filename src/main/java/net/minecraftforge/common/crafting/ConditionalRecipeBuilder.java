/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.conditions.ConditionalOps;
import net.minecraftforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ConditionalRecipeBuilder {
   
   private String conditionalPropertyKey = "conditions";
   private RecipeBuilder recipe;
   private final List<ICondition> conditions = new ArrayList<>();
   
   public ConditionalRecipeBuilder withConditionsIn(final String key) {
      this.conditionalPropertyKey = key;
      return this;
   }
   
   public ConditionalRecipeBuilder withRecipe(final RecipeBuilder builder) {
      this.recipe = builder;
      return this;
   }
   
   public ConditionalRecipeBuilder withCondition(ICondition condition) {
      this.conditions.add(condition);
      return this;
   }
   
   public ConditionalRecipeBuilder withConditions(ICondition... conditions) {
      return withConditions(List.of(conditions));
   }
   
   public ConditionalRecipeBuilder withConditions(Collection<ICondition> conditions) {
      this.conditions.addAll(conditions);
      return this;
   }
   
   public void save(RecipeOutput recipeOutput) {
      final FinishedRecipeCapture collector = new FinishedRecipeCapture(recipeOutput);
      if (recipe == null)
         throw new IllegalStateException("Can not save conditional recipe, without recipe!");
      
      this.recipe.save(collector);
   }
   
   public void save(RecipeOutput recipeOutput, ResourceLocation id) {
      final FinishedRecipeCapture collector = new FinishedRecipeCapture(recipeOutput);
      if (recipe == null)
         throw new IllegalStateException("Can not save conditional recipe, without recipe!");
      
      this.recipe.save(collector, id);
   }
   
   public void save(RecipeOutput recipeOutput, String id) {
      final FinishedRecipeCapture collector = new FinishedRecipeCapture(recipeOutput);
      if (recipe == null)
         throw new IllegalStateException("Can not save conditional recipe, without recipe!");
      
      this.recipe.save(collector, id);
   }
   
   
   private final class FinishedRecipeCapture implements RecipeOutput {
      
      private final RecipeOutput inner;
      
      private FinishedRecipeCapture(RecipeOutput inner) {
         this.inner = inner;
      }
      
      @Override
      public void accept(FinishedRecipe p_301033_) {
         final Completed completed = new Completed(conditionalPropertyKey, p_301033_, conditions, inner.provider());
         inner.accept(completed);
      }
      
      @Override
      public Advancement.Builder advancement() {
         return inner.advancement();
      }
      
      @Override
      public HolderLookup.Provider provider() {
         return inner.provider();
      }
   }
   
   
   private class Completed implements FinishedRecipe {
      
      private final String conditionsPropertyKey;
      private final FinishedRecipe recipe;
      private final List<ICondition> conditions;
      private final HolderLookup.Provider provider;
      
      private Completed(String conditionsPropertyKey, FinishedRecipe recipe, List<ICondition> conditions, HolderLookup.Provider provider) {
         this.conditionsPropertyKey = conditionsPropertyKey;
         this.recipe = recipe;
         this.conditions = conditions;
         this.provider = provider;
      }
      
      @Override
      public void serializeRecipeData(JsonObject p_125967_) {
         recipe.serializeRecipeData(p_125967_);
         if (conditions.isEmpty())
            return;
         
         final DynamicOps<JsonElement> dynamicOps = ConditionalOps.create(RegistryOps.create(JsonOps.INSTANCE, provider), ICondition.IContext.EMPTY);
         ICondition.LIST_CODEC.fieldOf(conditionsPropertyKey).codec().encode(this.conditions, dynamicOps, p_125967_);
      }
      
      @Override
      public ResourceLocation id() {
         return recipe.id();
      }
      
      @Override
      public RecipeSerializer<?> type() {
         return recipe.type();
      }
      
      @Nullable
      @Override
      public AdvancementHolder advancement() {
         return recipe.advancement();
      }
      
      @Nullable
      @Override
      public JsonObject serializedAdvancement() {
         final JsonObject object = FinishedRecipe.super.serializedAdvancement();
         if (object == null)
            return null;
         
         if (conditions.isEmpty())
            return object;
         
         final DynamicOps<JsonElement> dynamicOps = ConditionalOps.create(RegistryOps.create(JsonOps.INSTANCE, provider), ICondition.IContext.EMPTY);
         ICondition.LIST_CODEC.fieldOf(conditionsPropertyKey).codec().encode(this.conditions, dynamicOps, object);
         
         return object;
      }
   }
}
