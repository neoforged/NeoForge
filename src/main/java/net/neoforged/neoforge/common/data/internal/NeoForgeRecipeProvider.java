/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Ingredient.ItemValue;
import net.minecraft.world.item.crafting.Ingredient.TagValue;
import net.minecraft.world.item.crafting.Ingredient.Value;
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
    private final Map<Item, TagKey<Item>> replacements = new HashMap<>();
    private final Map<Item, Ingredient> specialReplacements = new HashMap<>();
    private final Set<ResourceLocation> excludes = new HashSet<>();

    public NeoForgeRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider) {
        super(packOutput, provider);
    }

    private void exclude(ItemLike item) {
        excludes.add(BuiltInRegistries.ITEM.getKey(item.asItem()));
    }

    private void exclude(String name) {
        excludes.add(ResourceLocation.parse(name));
    }

    private void replace(ItemLike item, TagKey<Item> tag) {
        replacements.put(item.asItem(), tag);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
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

        replace(Blocks.COBBLESTONE, Tags.Items.COBBLESTONES_NORMAL);
        replace(Blocks.COBBLED_DEEPSLATE, Tags.Items.COBBLESTONES_DEEPSLATE);

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

        specialReplacements.put(Items.CHEST, DifferenceIngredient.of(Ingredient.of(Tags.Items.CHESTS_WOODEN), Ingredient.of(Tags.Items.CHESTS_TRAPPED)));

        super.buildRecipes(new RecipeOutput() {
            @Override
            public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions) {
                Recipe<?> modified = enhance(id, recipe);
                if (modified != null)
                    recipeOutput.accept(id, modified, null, conditions);
            }

            @Override
            public Advancement.Builder advancement() {
                return recipeOutput.advancement();
            }
        });
    }

    @Nullable
    private Recipe<?> enhance(ResourceLocation id, Recipe<?> vanilla) {
        if (vanilla instanceof ShapelessRecipe shapeless)
            return enhance(id, shapeless);
        if (vanilla instanceof ShapedRecipe shaped)
            return enhance(id, shaped);
        return null;
    }

    @Nullable
    private ShapelessRecipe enhance(ResourceLocation id, ShapelessRecipe vanilla) {
        List<Ingredient> ingredients = vanilla.getIngredients();
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

    @Override
    protected CompletableFuture<?> buildAdvancement(CachedOutput p_253674_, HolderLookup.Provider p_323646_, AdvancementHolder p_301116_) {
        // NOOP - We don't replace any of the advancement things yet...
        return CompletableFuture.allOf();
    }

    @Nullable
    private ShapedRecipe enhance(ResourceLocation id, ShapedRecipe vanilla) {
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
    private Ingredient enhance(ResourceLocation name, Ingredient vanilla) {
        if (excludes.contains(name))
            return null;

        boolean modified = false;
        List<Value> items = new ArrayList<>();
        Value[] vanillaItems = vanilla.getValues();
        if (vanillaItems.length == 1 && vanillaItems[0] instanceof ItemValue itemValue) {
            Item item = itemValue.item().getItem();
            Ingredient replacement = specialReplacements.get(item);
            if (replacement != null) {
                return replacement;
            }
        }

        for (Value entry : vanillaItems) {
            if (entry instanceof ItemValue) {
                ItemStack stack = entry.getItems().stream().findFirst().orElse(ItemStack.EMPTY);
                TagKey<Item> replacement = replacements.get(stack.getItem());
                if (replacement != null) {
                    items.add(new TagValue(replacement));
                    modified = true;
                } else
                    items.add(entry);
            } else
                items.add(entry);
        }
        return modified ? Ingredient.fromValues(items.stream()) : null;
    }
}
