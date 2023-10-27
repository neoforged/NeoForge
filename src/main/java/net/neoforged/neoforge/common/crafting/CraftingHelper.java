/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.google.gson.*;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Function;

public class CraftingHelper
{
    @SuppressWarnings("unused")
    private static final Logger LOGGER = LogManager.getLogger();
    @SuppressWarnings("unused")
    private static final Marker CRAFTHELPER = MarkerManager.getMarker("CRAFTHELPER");
    private static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Recipe<?> EMPTY_RECIPE = new Recipe<>() {
        @Override
        public boolean matches(Container p_44002_, Level p_44003_) {
            return false;
        }

        @Override
        public ItemStack assemble(Container p_44001_, RegistryAccess p_267165_) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canCraftInDimensions(int p_43999_, int p_44000_) {
            return false;
        }

        @Override
        public ItemStack getResultItem(RegistryAccess p_267052_) {
            return ItemStack.EMPTY;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            throw new UnsupportedOperationException("Empty recipe has no serializer");
        }

        @Override
        public RecipeType<?> getType() {
            throw new UnsupportedOperationException("Empty recipe has no type");
        }
    };

    @ApiStatus.Internal
    public static Codec<Ingredient> makeIngredientCodec(boolean allowEmpty, Codec<Ingredient> vanillaCodec) {
        var compoundIngredientCodec = ExtraCodecs.lazyInitializedCodec(() ->
                allowEmpty ? CompoundIngredient.DIRECT_CODEC : CompoundIngredient.DIRECT_CODEC_NONEMPTY);
        return NeoForgeExtraCodecs.withAlternative(
                // Compound ingredient handling
                compoundIngredientCodec.flatComapMap(
                        Function.identity(),
                        i -> i instanceof CompoundIngredient c ?
                                DataResult.success(c) :
                                DataResult.error(() -> "Not a compound ingredient")),
                // Otherwise choose between custom and vanilla
                makeIngredientCodec0(true, vanillaCodec)
        );
    }

    // Choose between dispatch codec for custom ingredients and vanilla codec
    private static Codec<Ingredient> makeIngredientCodec0(boolean allowEmpty, Codec<Ingredient> vanillaCodec) {
        // Dispatch codec for custom ingredient types:
        Codec<Ingredient> dispatchCodec = ExtraCodecs.lazyInitializedCodec(
                // Use dispatchUnsafe to always inline the dispatched type parameters into the root ingredient object, next to the "type"
                () -> NeoForgeExtraCodecs.dispatchUnsafe(
                        ForgeRegistries.INGREDIENT_TYPES.get().getCodec(),
                        Ingredient::getType,
                        ingredientType -> ingredientType.codec(allowEmpty)));
        // Either codec to combine with the vanilla ingredient codec:
        Codec<Either<Ingredient, Ingredient>> eitherCodec = ExtraCodecs.either(
                dispatchCodec,
                vanillaCodec
        );
        return eitherCodec.xmap(either -> either.map(i -> i, i -> i), ingredient -> {
            // Prefer writing without the "type" field if possible:
            if (ingredient.getType() == NeoForgeMod.VANILLA_INGREDIENT_TYPE.get()) {
                return Either.right(ingredient);
            } else {
                return Either.left(ingredient);
            }
        });
    }
}
