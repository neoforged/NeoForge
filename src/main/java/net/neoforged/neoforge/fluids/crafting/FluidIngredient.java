/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
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

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = new StreamCodec<>() {
        private static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> DISPATCH_CODEC = ByteBufCodecs.registry(NeoForgeRegistries.Keys.FLUID_INGREDIENT_TYPES)
                .dispatch(FluidIngredient::getType, FluidIngredientType::streamCodec);

        private static final StreamCodec<RegistryFriendlyByteBuf, List<FluidStack>> FLUID_LIST_CODEC = FluidStack.STREAM_CODEC.apply(
                ByteBufCodecs.collection(NonNullList::createWithCapacity));

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidIngredient ingredient) {
            if (ingredient.isSimple()) {
                FLUID_LIST_CODEC.encode(buf, Arrays.asList(ingredient.getStacks()));
            } else {
                buf.writeVarInt(-1);
                DISPATCH_CODEC.encode(buf, ingredient);
            }
        }

        @Override
        public FluidIngredient decode(RegistryFriendlyByteBuf buf) {
            var size = buf.readVarInt();
            if (size == -1) {
                return DISPATCH_CODEC.decode(buf);
            }

            return CompoundFluidIngredient.of(
                    Stream.generate(() -> FluidStack.STREAM_CODEC.decode(buf))
                            .limit(size)
                            .map(FluidIngredient::single));
        }
    };

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

    // convenience methods
    public static FluidIngredient of() {
        return empty();
    }

    public static FluidIngredient of(Fluid... fluids) {
        return of(Arrays.stream(fluids).map(fluid -> new FluidStack(fluid, FluidType.BUCKET_VOLUME)));
    }

    public static FluidIngredient of(FluidStack... fluids) {
        return of(Arrays.stream(fluids));
    }

    private static FluidIngredient of(Stream<FluidStack> fluids) {
        return CompoundFluidIngredient.of(fluids.map(FluidIngredient::single));
    }

    public static FluidIngredient single(FluidStack stack) {
        return stack.isEmpty() ? empty() : new SingleFluidIngredient(stack);
    }

    public static FluidIngredient tag(TagKey<Fluid> tag) {
        return new TagFluidIngredient(tag);
    }
}
