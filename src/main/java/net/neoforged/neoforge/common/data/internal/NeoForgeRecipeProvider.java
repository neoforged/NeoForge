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
import java.util.stream.Stream;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColorCollection;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import org.jspecify.annotations.Nullable;

public final class NeoForgeRecipeProvider extends VanillaRecipeProvider {
    private final InterceptingRecipeOutput output;

    public NeoForgeRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput) {
        InterceptingRecipeOutput wrappedRecipeOutput = new InterceptingRecipeOutput(recipeOutput);
        super(wrappedRecipeOutput, new BootstrapContext<>() {
            @Override
            public Holder.Reference<Advancement> register(ResourceKey<Advancement> key, Advancement value) {
                return Holder.Reference.createStandAlone(advancementOutput.lookup(Registries.ADVANCEMENT), key);
            }

            @Override
            public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
                return advancementOutput.lookup(key);
            }

            @Override
            @Deprecated
            public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
                return advancementOutput.listContextElements(key);
            }
        });
        this.output = wrappedRecipeOutput;
    }

    private void exclude(ItemLike item) {
        output.excludes.add(ResourceKey.create(Registries.RECIPE, BuiltInRegistries.ITEM.getKey(item.asItem())));
    }

    private void exclude(String name) {
        output.excludes.add(ResourceKey.create(Registries.RECIPE, Identifier.parse(name)));
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

        ColorCollection.zipApply(Items.DYE, ColorCollection.VALUES.map(DyeColor::getTag), this::replace);

        replace(Blocks.COBBLESTONE, Tags.Items.COBBLESTONES_NORMAL);
        replace(Blocks.COBBLED_DEEPSLATE, Tags.Items.COBBLESTONES_DEEPSLATE);

        replace(Items.EGG, Tags.Items.EGGS);
        replace(Items.STRING, Tags.Items.STRINGS);
        exclude(getConversionRecipeName(Blocks.WOOL.pick(DyeColor.WHITE), Items.STRING));
        replace(Items.LEATHER, Tags.Items.LEATHERS);

        exclude(Blocks.GOLD_BLOCK);
        exclude(Items.GOLD_NUGGET);
        exclude(Blocks.IRON_BLOCK);
        exclude(Items.IRON_NUGGET);
        exclude(Blocks.DIAMOND_BLOCK);
        exclude(Blocks.EMERALD_BLOCK);
        exclude(Blocks.NETHERITE_BLOCK);
        exclude(Blocks.COPPER_BLOCK.weathering().unaffected());
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

    private static class InterceptingRecipeOutput implements BootstrapContext<Recipe<?>> {
        private final HolderGetter<Item> items;
        private final BootstrapContext<Recipe<?>> output;
        private final Map<Item, TagKey<Item>> replacements = new HashMap<>();
        private final Map<Item, Ingredient> specialReplacements = new HashMap<>();
        private final Set<ResourceKey<Recipe<?>>> excludes = new HashSet<>();

        private InterceptingRecipeOutput(BootstrapContext<Recipe<?>> output) {
            this.items = output.lookup(Registries.ITEM);
            this.output = output;
        }

        @Override
        public Holder.Reference<Recipe<?>> register(ResourceKey<Recipe<?>> id, Recipe<?> recipe) {
            Recipe<?> modified = enhance(id, recipe);
            if (modified != null) {
                return output.register(id, modified);
            } else {
                return Holder.Reference.createStandAlone(output.lookup(Registries.RECIPE), id);
            }
        }

        @Override
        public <S> HolderGetter<S> lookup(ResourceKey<? extends Registry<? extends S>> key) {
            return output.lookup(key);
        }

        @Override
        @Deprecated
        public <S> Stream<Holder.Reference<S>> listContextElements(ResourceKey<? extends Registry<? extends S>> key) {
            return output.listContextElements(key);
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
}
