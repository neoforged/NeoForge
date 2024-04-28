/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public abstract class FluidIngredient implements Predicate<FluidStack> {
    private static final MapCodec<FluidIngredient> SINGLE_OR_TAG_CODEC = singleOrTagCodec();
    public static final MapCodec<FluidIngredient> MAP_CODEC = makeMapCodec();
    private static final Codec<FluidIngredient> MAP_CODEC_CODEC = MAP_CODEC.codec();

    public static final Codec<List<FluidIngredient>> LIST_CODEC = MAP_CODEC_CODEC.listOf();
    public static final Codec<List<FluidIngredient>> LIST_CODEC_NON_EMPTY = LIST_CODEC.validate(list -> {
        if (list.isEmpty()) {
            return DataResult.error(() -> "Fluid ingredient cannot be empty, at least one item must be defined");
        }
        return DataResult.success(list);
    });

    public static final Codec<FluidIngredient> CODEC = codec(true);
    public static final Codec<FluidIngredient> CODEC_NON_EMPTY = codec(false);

    @Nullable
    private FluidStack[] stacks;

    public FluidStack[] getStacks() {
        if (stacks == null) {
            stacks = generateStacks().toArray(FluidStack[]::new);
        }

        return stacks;
    }

    @Override
    public abstract boolean test(FluidStack fluidStack);

    protected abstract Stream<FluidStack> generateStacks();

    public abstract boolean isSimple();

    public abstract FluidIngredientType<?> getType();

    public boolean isEmpty() {
        // TODO: this resolves the ingredient by default, which i'm not sure is desirable?
        return getStacks().length == 0;
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    // codecs
    private static MapCodec<FluidIngredient> singleOrTagCodec() {
        return NeoForgeExtraCodecs.xor(
                SingleFluidIngredient.CODEC,
                TagFluidIngredient.CODEC).xmap(either -> either.map(id -> id, id -> id), ingredient -> {
                    if (ingredient instanceof SingleFluidIngredient fluid) {
                        return Either.left(fluid);
                    } else if (ingredient instanceof TagFluidIngredient tag) {
                        return Either.right(tag);
                    }
                    throw new IllegalStateException("Basic fluid ingredient should be either a fluid or a tag!");
                });
    }

    private static MapCodec<FluidIngredient> makeMapCodec() {
        return NeoForgeExtraCodecs.<FluidIngredientType<?>, FluidIngredient, FluidIngredient>dispatchMapOrElse(
                NeoForgeRegistries.FLUID_INGREDIENT_TYPES.byNameCodec(),
                FluidIngredient::getType,
                FluidIngredientType::codec,
                FluidIngredient.SINGLE_OR_TAG_CODEC).xmap(either -> either.map(id -> id, id -> id), ingredient -> {
                    if (ingredient instanceof SingleFluidIngredient || ingredient instanceof TagFluidIngredient) {
                        return Either.right(ingredient);
                    }

                    return Either.left(ingredient);
                });
    }

    private static Codec<FluidIngredient> codec(boolean allowEmpty) {
        var listCodec = Codec.lazyInitialized(() -> allowEmpty ? LIST_CODEC : LIST_CODEC_NON_EMPTY);
        return Codec.either(listCodec, MAP_CODEC_CODEC)
                .xmap(either -> either.map(CompoundFluidIngredient::of, i -> i),
                        ingredient -> {
                            if (ingredient instanceof CompoundFluidIngredient compound) {
                                return Either.left(compound.children());
                            } else if (ingredient.isEmpty()) {
                                return Either.left(List.of());
                            }

                            return Either.right(ingredient);
                        });
    }

    // empty
    public static FluidIngredient empty() {
        return EmptyFluidIngredient.INSTANCE;
    }
}
