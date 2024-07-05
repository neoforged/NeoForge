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
        var listCodec = Codec.lazyInitialized(() -> allowEmpty ? Ingredient.LIST_CODEC : Ingredient.LIST_CODEC_NONEMPTY);
        return Codec.either(listCodec, makeIngredientMapCodec().codec())
                .xmap(either -> either.map(list -> {
                    // Use CompoundIngredient.of(...) to convert empty ingredients to Ingredient.EMPTY
                    return CompoundIngredient.of(list.toArray(Ingredient[]::new));
                }, i -> i), ingredient -> {
                    if (ingredient.isCustom()) {
                        if (ingredient.getCustomIngredient() instanceof CompoundIngredient compound) {
                            // Use [] syntax for CompoundIngredients.
                            return Either.left(compound.children());
                        }
                    } else if (ingredient.getValues().length != 1) {
                        // Use [] syntax for vanilla ingredients that either 0 or 2+ values.
                        return Either.left(Stream.of(ingredient.getValues()).map(v -> Ingredient.fromValues(Stream.of(v))).toList());
                    }
                    // Else use {} syntax.
                    return Either.right(ingredient);
                });
    }

    public static MapCodec<Ingredient> makeIngredientMapCodec() {
        // Dispatch codec for custom ingredient types, else fallback to vanilla ingredient codec.
        return NeoForgeExtraCodecs.<IngredientType<?>, ICustomIngredient, Ingredient.Value>dispatchMapOrElse(
                NeoForgeRegistries.INGREDIENT_TYPES.byNameCodec(),
                ICustomIngredient::getType,
                IngredientType::codec,
                Ingredient.Value.MAP_CODEC)
                .xmap(either -> either.map(ICustomIngredient::toVanilla, v -> Ingredient.fromValues(Stream.of(v))), ingredient -> {
                    if (!ingredient.isCustom()) {
                        var values = ingredient.getValues();
                        if (values.length == 1) {
                            return Either.right(values[0]);
                        }
                        // Convert vanilla ingredients with 2+ values to a CompoundIngredient. Empty ingredients are not allowed here.
                        return Either.left(new CompoundIngredient(Stream.of(ingredient.getValues()).map(v -> Ingredient.fromValues(Stream.of(v))).toList()));
                    }
                    return Either.left(ingredient.getCustomIngredient());
                })
                .validate(ingredient -> {
                    if (!ingredient.isCustom() && ingredient.getValues().length == 0) {
                        return DataResult.error(() -> "Cannot serialize empty ingredient using the map codec");
                    }
                    return DataResult.success(ingredient);
                });
    }
}
