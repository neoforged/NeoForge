/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.data.internal;

import com.google.gson.JsonObject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public final class NeoForgeRecipeProvider extends VanillaRecipeProvider {
    private final Map<Item, TagKey<Item>> replacements = new HashMap<>();
    private final Set<ResourceLocation> excludes = new HashSet<>();

    public NeoForgeRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    private void exclude(ItemLike item) {
        excludes.add(ForgeRegistries.ITEMS.getKey(item.asItem()));
    }

    private void exclude(String name) {
        excludes.add(new ResourceLocation(name));
    }

    private void replace(ItemLike item, TagKey<Item> tag) {
        replacements.put(item.asItem(), tag);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        replace(Items.STICK, Tags.Items.RODS_WOODEN);
        replace(Items.GOLD_INGOT, Tags.Items.INGOTS_GOLD);
        replace(Items.IRON_INGOT, Tags.Items.INGOTS_IRON);
        replace(Items.NETHERITE_INGOT, Tags.Items.INGOTS_NETHERITE);
        replace(Items.COPPER_INGOT, Tags.Items.INGOTS_COPPER);
        replace(Items.AMETHYST_SHARD, Tags.Items.GEMS_AMETHYST);
        replace(Items.DIAMOND, Tags.Items.GEMS_DIAMOND);
        replace(Items.EMERALD, Tags.Items.GEMS_EMERALD);
        replace(Items.CHEST, Tags.Items.CHESTS_WOODEN);
        replace(Blocks.COBBLESTONE, Tags.Items.COBBLESTONE_NORMAL);
        replace(Blocks.COBBLED_DEEPSLATE, Tags.Items.COBBLESTONE_DEEPSLATE);

        replace(Items.STRING, Tags.Items.STRING);
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

        super.buildRecipes(new RecipeOutput() {
            @Override
            public void accept(FinishedRecipe p_301033_, ICondition... conditions) {
                FinishedRecipe modified = enhance(p_301033_);
                if (modified != null)
                    recipeOutput.accept(modified);
            }

            @Override
            public Advancement.Builder advancement() {
                return recipeOutput.advancement();
            }
        });
    }

    @Nullable
    private FinishedRecipe enhance(FinishedRecipe vanilla) {
        if (vanilla instanceof ShapelessRecipeBuilder.Result shapeless)
            return enhance(shapeless);
        if (vanilla instanceof ShapedRecipeBuilder.Result shaped)
            return enhance(shaped);
        return null;
    }

    @Nullable
    private FinishedRecipe enhance(ShapelessRecipeBuilder.Result vanilla) {
        List<Ingredient> ingredients = ObfuscationReflectionHelper.getPrivateValue(ShapelessRecipeBuilder.Result.class, vanilla, "ingredients");
        if (ingredients == null) throw new IllegalStateException(ShapelessRecipeBuilder.Result.class.getName() + " has no field ingredients");
        boolean modified = false;
        for (int x = 0; x < ingredients.size(); x++) {
            Ingredient ing = enhance(vanilla.id(), ingredients.get(x));
            if (ing != null) {
                ingredients.set(x, ing);
                modified = true;
            }
        }
        return modified ? vanilla : null;
    }

    @Nullable
    @Override
    protected CompletableFuture<?> saveAdvancement(CachedOutput output, FinishedRecipe recipe, JsonObject json) {
        // NOOP - We don't replace any of the advancement things yet...
        return null;
    }

    @Override
    protected CompletableFuture<?> buildAdvancement(CachedOutput p_253674_, AdvancementHolder p_301116_) {
        // NOOP - We don't replace any of the advancement things yet...
        return CompletableFuture.allOf();
    }

    @Nullable
    private FinishedRecipe enhance(ShapedRecipeBuilder.Result vanilla) {
        Map<Character, Ingredient> ingredients = ObfuscationReflectionHelper.getPrivateValue(ShapedRecipeBuilder.Result.class, vanilla, "key");
        if (ingredients == null) throw new IllegalStateException(ShapedRecipeBuilder.Result.class.getName() + " has no field key");
        boolean modified = false;
        for (Character x : ingredients.keySet()) {
            Ingredient ing = enhance(vanilla.id(), ingredients.get(x));
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
