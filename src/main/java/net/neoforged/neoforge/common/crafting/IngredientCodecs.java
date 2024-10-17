/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class IngredientCodecs {
    public static Codec<Ingredient> codec(Codec<Ingredient> vanillaCodec) {
        var customIngredientCodec = NeoForgeRegistries.INGREDIENT_TYPES.byNameCodec().<ICustomIngredient>dispatch(
                "neoforge:ingredient_type", ICustomIngredient::getType, IngredientType::codec);
        return Codec.xor(customIngredientCodec, vanillaCodec)
                .xmap(
                        either -> either.map(ICustomIngredient::toVanilla, i -> i),
                        ingredient -> {
                            if (ingredient.isCustom()) {
                                return Either.left(ingredient.getCustomIngredient());
                            } else {
                                return Either.right(ingredient);
                            }
                        });
    }

    private static final int CUSTOM_INGREDIENT_MARKER = -1000;
    private static final StreamCodec<RegistryFriendlyByteBuf, ICustomIngredient> CUSTOM_INGREDIENT_CODEC = ByteBufCodecs.registry(NeoForgeRegistries.Keys.INGREDIENT_TYPES)
            .dispatch(ICustomIngredient::getType, IngredientType::streamCodec);

    public static StreamCodec<RegistryFriendlyByteBuf, Ingredient> streamCodec(StreamCodec<RegistryFriendlyByteBuf, Ingredient> vanillaCodec) {
        return new StreamCodec<>() {
            @Override
            public Ingredient decode(RegistryFriendlyByteBuf buf) {
                var readerIndex = buf.readerIndex();
                var length = buf.readVarInt();
                if (length == CUSTOM_INGREDIENT_MARKER) {
                    return CUSTOM_INGREDIENT_CODEC.decode(buf).toVanilla();
                } else {
                    buf.readerIndex(readerIndex);
                    return vanillaCodec.decode(buf);
                }
            };

            @Override
            public void encode(RegistryFriendlyByteBuf buf, Ingredient ingredient) {
                if (ingredient.isCustom() && buf.getConnectionType().isNeoForge()) {
                    buf.writeVarInt(CUSTOM_INGREDIENT_MARKER);
                    CUSTOM_INGREDIENT_CODEC.encode(buf, ingredient.getCustomIngredient());
                } else {
                    vanillaCodec.encode(buf, ingredient);
                }
            }
        };
    }

    public static StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> optionalStreamCodec(StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> vanillaCodec) {
        return new StreamCodec<>() {
            @Override
            public Optional<Ingredient> decode(RegistryFriendlyByteBuf buf) {
                var readerIndex = buf.readerIndex();
                var length = buf.readVarInt();
                if (length == CUSTOM_INGREDIENT_MARKER) {
                    return Optional.of(CUSTOM_INGREDIENT_CODEC.decode(buf).toVanilla());
                } else {
                    buf.readerIndex(readerIndex);
                    return vanillaCodec.decode(buf);
                }
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, Optional<Ingredient> ingredient) {
                if (ingredient.isPresent() && ingredient.get().isCustom() && buf.getConnectionType().isNeoForge()) {
                    buf.writeVarInt(CUSTOM_INGREDIENT_MARKER);
                    CUSTOM_INGREDIENT_CODEC.encode(buf, ingredient.get().getCustomIngredient());
                } else {
                    vanillaCodec.encode(buf, ingredient);
                }
            }
        };
    }
}
