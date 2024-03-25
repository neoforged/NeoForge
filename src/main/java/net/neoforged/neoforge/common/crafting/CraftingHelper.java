/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.function.Function;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

public class CraftingHelper {
    @ApiStatus.Internal
    public static Codec<Ingredient> makeIngredientCodec(boolean allowEmpty, Codec<Ingredient> vanillaCodec) {
        var compoundIngredientCodec = ExtraCodecs.lazyInitializedCodec(() -> allowEmpty ? CompoundIngredient.DIRECT_CODEC : CompoundIngredient.DIRECT_CODEC_NONEMPTY);
        return NeoForgeExtraCodecs.withAlternative(
                // Compound ingredient handling
                compoundIngredientCodec.flatComapMap(
                        Function.identity(),
                        i -> i instanceof CompoundIngredient c ? DataResult.success(c) : DataResult.error(() -> "Not a compound ingredient")),
                // Otherwise choose between custom and vanilla
                makeIngredientCodec0(allowEmpty, vanillaCodec));
    }

    // Choose between dispatch codec for custom ingredients and vanilla codec
    private static Codec<Ingredient> makeIngredientCodec0(boolean allowEmpty, Codec<Ingredient> vanillaCodec) {
        // Dispatch codec for custom ingredient types:
        Codec<Ingredient> dispatchCodec = NeoForgeRegistries.INGREDIENT_TYPES.byNameCodec()
                .dispatch(Ingredient::getType, ingredientType -> ingredientType.codec(allowEmpty));
        // Either codec to combine with the vanilla ingredient codec:
        Codec<Either<Ingredient, Ingredient>> eitherCodec = ExtraCodecs.either(
                dispatchCodec,
                vanillaCodec);
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
