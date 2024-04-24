/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.stream.Stream;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class CraftingHelper {
    public static Codec<Ingredient> makeIngredientCodec(boolean allowEmpty) {
        var compoundIngredientCodec = Codec.lazyInitialized(() -> allowEmpty ? CompoundIngredient.DIRECT_CODEC : CompoundIngredient.DIRECT_CODEC_NONEMPTY);
        return Codec.either(compoundIngredientCodec, makeIngredientMapCodec().codec())
                .xmap(either -> either.map(custom -> {
                    // Convert empty compound ingredients back to Ingredient.EMPTY, for the isEmpty() check to return true.
                    if (custom instanceof CompoundIngredient compound && compound.children().isEmpty()) {
                        return Ingredient.EMPTY;
                    } else {
                        return custom.toVanilla();
                    }
                }, i -> i), ingredient -> {
                    if (convertToCompoundIngredient(ingredient).getCustomIngredient() instanceof CompoundIngredient compound) {
                        // Use [] syntax for vanilla array ingredients and CompoundIngredients.
                        return Either.left(compound);
                    } else {
                        // Else use {} syntax.
                        return Either.right(ingredient);
                    }
                });
    }

    /**
     * Converts vanilla "array ingredients" to {@link CompoundIngredient}s.
     */
    private static Ingredient convertToCompoundIngredient(Ingredient ingredient) {
        if (!ingredient.isCustom() && ingredient.getValues().length != 1) {
            // Convert vanilla ingredient to custom CompoundIngredient
            // Do not use CompoundIngredient.of(...) as it will convert empty ingredients back to Ingredient.EMPTY
            return new CompoundIngredient(Stream.of(ingredient.getValues()).map(v -> Ingredient.fromValues(Stream.of(v))).toList()).toVanilla();
        }
        return ingredient;
    }

    public static MapCodec<Ingredient> makeIngredientMapCodec() {
        // Dispatch codec for custom ingredient types, else fallback to vanilla ingredient codec.
        return NeoForgeExtraCodecs.<IngredientType<?>, ICustomIngredient, Ingredient.Value>dispatchMapOrElse(
                NeoForgeRegistries.INGREDIENT_TYPES.byNameCodec(),
                ICustomIngredient::getType,
                IngredientType::codec,
                Ingredient.Value.MAP_CODEC)
                .xmap(either -> either.map(ICustomIngredient::toVanilla, v -> Ingredient.fromValues(Stream.of(v))), ingredient -> {
                    var customIngredient = convertToCompoundIngredient(ingredient).getCustomIngredient();
                    if (customIngredient == null) {
                        return Either.right(ingredient.getValues()[0]);
                    } else {
                        return Either.left(customIngredient);
                    }
                })
                .validate(ingredient -> {
                    if (!ingredient.isCustom() && ingredient.getValues().length == 0) {
                        return DataResult.error(() -> "Cannot serialize empty ingredient using the map codec");
                    }
                    return DataResult.success(ingredient);
                });
    }
}
