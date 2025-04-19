/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import org.jetbrains.annotations.Nullable;

public final class NeoForgeRecipeProvider extends VanillaRecipeProvider {
    private final InterceptingRecipeOutput output;

    private NeoForgeRecipeProvider(HolderLookup.Provider provider, InterceptingRecipeOutput output) {
        super(provider, output);
        this.output = output;
    }

    private void exclude(ItemLike item) {
        output.excludes.add(ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(item.asItem())));
    }

    private void exclude(String name) {
        output.excludes.add(ResourceKey.create(Registries.RECIPE, ResourceLocation.parse(name)));
    }

    private void replace(ItemLike item, TagKey<Item> tag) {
        output.replacements.put(item.asItem(), tag);
    }

    @Override
    protected void buildRecipes() {
        replace(Items.STICK, Tags.Items.RODS_WOODEN);
        replace(Items.GOLD_INGOT, Tags.Items.INGOTS_GOLD);
        replace(Items.GOLD_NUGGET, Tags.Items.NUGGETS_GOLD);
        replace(Items.IRON_INGOT, Tags.Items.INGOTS_IRON);
        replace(Items.IRON_NUGGET, Tags.Items.NUGGETS_IRON);
        replace(Items.NETHERITE_INGOT, Tags.Items.INGOTS_NETHERITE);
        replace(Items.COPPER_INGOT, Tags.Items.INGOTS_COPPER);
        replace(Items.AMETHYST_SHARD, Tags.Items.GEMS_AMETHYST);
        replace(Items.DIAMOND, Tags.Items.GEMS_DIAMOND);
        replace(Items.EMERALD, Tags.Items.GEMS_EMERALD);

        replace(Items.WHITE_DYE, Tags.Items.DYES_WHITE);
        replace(Items.ORANGE_DYE, Tags.Items.DYES_ORANGE);
        replace(Items.MAGENTA_DYE, Tags.Items.DYES_MAGENTA);
        replace(Items.LIGHT_BLUE_DYE, Tags.Items.DYES_LIGHT_BLUE);
        replace(Items.YELLOW_DYE, Tags.Items.DYES_YELLOW);
        replace(Items.LIME_DYE, Tags.Items.DYES_LIME);
        replace(Items.PINK_DYE, Tags.Items.DYES_PINK);
        replace(Items.GRAY_DYE, Tags.Items.DYES_GRAY);
        replace(Items.LIGHT_GRAY_DYE, Tags.Items.DYES_LIGHT_GRAY);
        replace(Items.CYAN_DYE, Tags.Items.DYES_CYAN);
        replace(Items.PURPLE_DYE, Tags.Items.DYES_PURPLE);
        replace(Items.BLUE_DYE, Tags.Items.DYES_BLUE);
        replace(Items.BROWN_DYE, Tags.Items.DYES_BROWN);
        replace(Items.GREEN_DYE, Tags.Items.DYES_GREEN);
        replace(Items.RED_DYE, Tags.Items.DYES_RED);
        replace(Items.BLACK_DYE, Tags.Items.DYES_BLACK);

        replace(Blocks.COBBLESTONE, Tags.Items.COBBLESTONES_NORMAL);
        replace(Blocks.COBBLED_DEEPSLATE, Tags.Items.COBBLESTONES_DEEPSLATE);

        replace(Items.EGG, Tags.Items.EGGS);
        replace(Items.STRING, Tags.Items.STRINGS);
        exclude(getConversionRecipeName(Blocks.WHITE_WOOL, Items.STRING));

        exclude(Blocks.GOLD_BLOCK);
        exclude(Items.GOLD_NUGGET);
        exclude(Blocks.IRON_BLOCK);
        exclude(Items.IRON_NUGGET);
        exclude(Blocks.DIAMOND_BLOCK);
        exclude(Blocks.EMERALD_BLOCK);
        exclude(Blocks.NETHERITE_BLOCK);
        exclude(Blocks.COPPER_BLOCK);
        exclude(Blocks.AMETHYST_BLOCK);

        exclude(Blocks.COBBLESTONE_STAIRS);
        exclude(Blocks.COBBLESTONE_SLAB);
        exclude(Blocks.COBBLESTONE_WALL);
        exclude(Blocks.COBBLED_DEEPSLATE_STAIRS);
        exclude(Blocks.COBBLED_DEEPSLATE_SLAB);
        exclude(Blocks.COBBLED_DEEPSLATE_WALL);

        output.specialReplacements.put(Items.CHEST, DifferenceIngredient.of(tag(Tags.Items.CHESTS_WOODEN), tag(Tags.Items.CHESTS_TRAPPED)));

        super.buildRecipes();
    }

    private static class InterceptingRecipeOutput implements RecipeOutput {
        private final HolderGetter<Item> items;
        private final RecipeOutput output;
        private final Map<Item, TagKey<Item>> replacements = new HashMap<>();
        private final Map<Item, Ingredient> specialReplacements = new HashMap<>();
        private final Set<ResourceKey<Recipe<?>>> excludes = new HashSet<>();

        private InterceptingRecipeOutput(HolderGetter<Item> items, RecipeOutput output) {
            this.items = items;
            this.output = output;
        }

        @Override
        public Advancement.Builder advancement() {
            return output.advancement();
        }

        @Override
        public void includeRootAdvancement() {
            // Let's not
        }

        @Override
        public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
            Recipe<?> modified = enhance(id, recipe);
            if (modified != null)
                output.accept(id, modified, null, conditions);
        }

        @Nullable
        private Recipe<?> enhance(ResourceKey<Recipe<?>> id, Recipe<?> vanilla) {
            if (vanilla instanceof ShapelessRecipe shapeless)
                return enhance(id, shapeless);
            if (vanilla instanceof ShapedRecipe shaped)
                return enhance(id, shaped);
            return null;
        }

        @Nullable
        private ShapelessRecipe enhance(ResourceKey<Recipe<?>> id, ShapelessRecipe vanilla) {
            List<Ingredient> ingredients = ObfuscationReflectionHelper.getPrivateValue(ShapelessRecipe.class, vanilla, "ingredients");
            boolean modified = false;
            for (int x = 0; x < ingredients.size(); x++) {
                Ingredient ing = enhance(id, ingredients.get(x));
                if (ing != null) {
                    ingredients.set(x, ing);
                    modified = true;
                }
            }
            return modified ? vanilla : null;
        }

        @Nullable
        private ShapedRecipe enhance(ResourceKey<Recipe<?>> id, ShapedRecipe vanilla) {
            ShapedRecipePattern pattern = ObfuscationReflectionHelper.getPrivateValue(ShapedRecipe.class, vanilla, "pattern");
            if (pattern == null) throw new IllegalStateException(ShapedRecipe.class.getName() + " has no field pattern");
            ShapedRecipePattern.Data data = ((Optional<ShapedRecipePattern.Data>) ObfuscationReflectionHelper.getPrivateValue(ShapedRecipePattern.class, pattern, "data")).orElseThrow(() -> new IllegalArgumentException("recipe " + id + " does not have pattern data"));
            Map<Character, Ingredient> ingredients = data.key();
            boolean modified = false;
            for (Character x : ingredients.keySet()) {
                Ingredient ing = enhance(id, ingredients.get(x));
                if (ing != null) {
                    ingredients.put(x, ing);
                    modified = true;
                }
            }
            return modified ? vanilla : null;
        }

        @Nullable
        private Ingredient enhance(ResourceKey<Recipe<?>> name, Ingredient vanilla) {
            if (excludes.contains(name))
                return null;

            return vanilla.getValues().unwrap().map(
                    tagKey -> null,
                    items -> {
                        if (items.size() == 1) {
                            var specialReplacement = specialReplacements.get(items.getFirst().value());
                            if (specialReplacement != null) {
                                return specialReplacement;
                            }

                            var replacement = replacements.get(items.getFirst().value());
                            if (replacement != null) {
                                return Ingredient.of(this.items.getOrThrow(replacement));
                            }
                        }

                        for (var holder : items) {
                            if (replacements.containsKey(holder.value())) {
                                throw new IllegalArgumentException("Cannot replace '%s' which is part of a multi-item ingredient.".formatted(holder.value()));
                            }
                        }
                        return null;
                    });
        }
    }

    public static final class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider lookupProvider, RecipeOutput output) {
            return new NeoForgeRecipeProvider(lookupProvider, new InterceptingRecipeOutput(lookupProvider.lookupOrThrow(Registries.ITEM), output));
        }

        @Override
        public String getName() {
            return "NeoForge recipes";
        }
    }
}
