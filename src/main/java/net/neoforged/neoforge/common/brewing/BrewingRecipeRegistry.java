/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.brewing;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;
import net.neoforged.neoforge.event.brewing.PotionBrewEvent;
import org.jetbrains.annotations.Nullable;

public final class BrewingRecipeRegistry {
    private static final List<PotionBrewing.Mix<Potion>> POTION_MIXES;
    private static final List<PotionBrewing.Mix<Item>> CONTAINER_MIXES;

    static {
        List<PotionBrewing.Mix<Potion>> potionMixes = ObfuscationReflectionHelper.getPrivateValue(PotionBrewing.class, null, "POTION_MIXES");
        if (potionMixes == null) throw new IllegalStateException(PotionBrewing.class.getName() + " has no static field POTION_MIXES");
        POTION_MIXES = Collections.unmodifiableList(potionMixes);
        List<PotionBrewing.Mix<Item>> containerMixes = ObfuscationReflectionHelper.getPrivateValue(PotionBrewing.class, null, "CONTAINER_MIXES");
        if (containerMixes == null) throw new IllegalStateException(PotionBrewing.class.getName() + " has no static field CONTAINER_MIXES");
        CONTAINER_MIXES = Collections.unmodifiableList(containerMixes);
    }

    private BrewingRecipeRegistry() {}

    public static List<PotionBrewing.Mix<Potion>> getPotionMixes() {
        return POTION_MIXES;
    }

    public static List<PotionBrewing.Mix<Item>> getContainerMixes() {
        return CONTAINER_MIXES;
    }

    /**
     * Used by the brewing stand to determine if its contents can be brewed.
     * Extra parameters exist to allow modders to create bigger brewing stands
     * without much hassle
     */
    public static boolean canBrew(Level level, Container container, int catalystSlot, int[] inputSlots) {
        for (int inputSlot : inputSlots) {
            if (level.getRecipeManager().getRecipeFor(NeoForgeMod.BREWING_RECIPE_TYPE.get(), new IBrewingRecipe.IBrewingContainer.Wrapper(container, inputSlot, catalystSlot), level).isPresent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Used by the brewing stand to brew its inventory Extra parameters exist to
     * allow modders to create bigger brewing stands without much hassle
     */
    public static void brewPotions(Level level, BlockPos pos, Container container, int catalystSlot, int[] inputSlots) {
        for (int inputSlot : inputSlots) {
            brew(level, new IBrewingRecipe.IBrewingContainer.Wrapper(container, inputSlot, catalystSlot));
        }

        ItemStack catalyst = container.getItem(catalystSlot);
        if (catalyst.hasCraftingRemainingItem()) {
            ItemStack itemstack1 = catalyst.getCraftingRemainingItem();
            catalyst.shrink(1);
            if (catalyst.isEmpty()) {
                catalyst = itemstack1;
            } else {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), itemstack1);
            }
        } else {
            catalyst.shrink(1);
        }

        container.setItem(catalystSlot, catalyst);
        level.levelEvent(LevelEvent.SOUND_BREWING_STAND_BREW, pos, 0);
    }

    public static void brew(Level level, IBrewingRecipe.IBrewingContainer brewingContainer) {
        Optional<RecipeHolder<IBrewingRecipe>> recipe = level.getRecipeManager().getRecipeFor(NeoForgeMod.BREWING_RECIPE_TYPE.get(), brewingContainer, level);
        if (recipe.isEmpty()) return;
        RecipeHolder<IBrewingRecipe> brewingRecipe = recipe.get();
        if (NeoForge.EVENT_BUS.post(new PotionBrewEvent.Pre(level, brewingContainer, brewingRecipe)).isCanceled()) return;
        ItemStack result = brewingRecipe.value().assemble(brewingContainer, level.registryAccess());
        ItemStack modifiedResult = NeoForge.EVENT_BUS.post(new PotionBrewEvent.Post(level, brewingContainer, brewingRecipe, result)).getOutput();
        brewingContainer.setResult(modifiedResult);
    }

    /**
     * Returns true if the passed ItemStack is a valid ingredient for any of the
     * recipes in the registry.
     */
    public static boolean isValidIngredient(Container container, ItemStack stack) {
        if (stack.isEmpty()) return false;

        for (RecipeHolder<IBrewingRecipe> recipe : getAllBrewingRecipes(getRecipeManager(container))) {
            if (recipe.value().isCatalyst(stack)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the passed ItemStack is a valid input for any of the
     * recipes in the registry.
     */
    public static boolean isValidInput(Container container, ItemStack stack) {
        if (stack.isEmpty()) return false;

        for (RecipeHolder<IBrewingRecipe> recipe : getAllBrewingRecipes(getRecipeManager(container))) {
            if (recipe.value().isInput(stack)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns an unmodifiable list containing all the recipes in the registry
     */
    public static List<RecipeHolder<IBrewingRecipe>> getRecipes() {
        return getAllBrewingRecipes(getRecipeManager(null));
    }

    private static RecipeManager getRecipeManager(@Nullable Container container) {
        if (container instanceof BlockEntity be) {
            return be.getLevel().getRecipeManager();
        } else {
            return LogicalSidedProvider.RECIPE_MANAGER.get(EffectiveSide.get());
        }
    }

    private static List<RecipeHolder<IBrewingRecipe>> getAllBrewingRecipes(RecipeManager recipeManager) {
        return recipeManager.getAllRecipesFor(NeoForgeMod.BREWING_RECIPE_TYPE.get());
    }
}
