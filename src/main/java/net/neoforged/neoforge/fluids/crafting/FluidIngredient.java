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
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public abstract class FluidIngredient implements Predicate<FluidStack> {
    /**
     * This is a codec that is used to represent basic "single fluid" or "tag"
     * fluid ingredients directly, similar to {@link Ingredient.Value#CODEC},
     * except not using value subclasses and instead directly providing
     * the corresponding {@link FluidIngredient}.
     */
    private static final MapCodec<FluidIngredient> SINGLE_OR_TAG_CODEC = singleOrTagCodec();

    /**
     * This is a codec that represents a single {@code FluidIngredient} in map form;
     * either dispatched by type or falling back to {@link #SINGLE_OR_TAG_CODEC}
     * if no type is specified.
     *
     * @see Ingredient#MAP_CODEC_NONEMPTY
     */
    public static final MapCodec<FluidIngredient> MAP_CODEC = makeMapCodec();
    private static final Codec<FluidIngredient> MAP_CODEC_CODEC = MAP_CODEC.codec();

    public static final Codec<List<FluidIngredient>> LIST_CODEC = MAP_CODEC_CODEC.listOf();
    public static final Codec<List<FluidIngredient>> LIST_CODEC_NON_EMPTY = LIST_CODEC.validate(list -> {
        if (list.isEmpty()) {
            return DataResult.error(() -> "Fluid ingredient cannot be empty, at least one item must be defined");
        }
        return DataResult.success(list);
    });

    /**
     * Full codec representing a fluid ingredient in all possible forms.
     * <p>
     * Allows for arrays of fluid ingredients to be read as a {@link CompoundFluidIngredient},
     * as well as for the {@code type} field to be left out in case of a single fluid or tag ingredient.
     *
     * @see #codec(boolean)
     * @see #MAP_CODEC
     */
    public static final Codec<FluidIngredient> CODEC = codec(true);
    /**
     * Same as {@link #CODEC}, except not allowing for empty ingredients ({@code []})
     * to be specified.
     *
     * @see #codec(boolean)
     */
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

    /**
     * {@return an array of fluid stacks this ingredient accepts}
     * <p>
     * This implementation simply caches the results of dissolving the ingredient using
     * {@link #generateStacks()}, and should <i>not</i> be overridden unless you have good reason to.
     *
     * @see #generateStacks()
     */
    public FluidStack[] getStacks() {
        if (stacks == null) {
            stacks = generateStacks().toArray(FluidStack[]::new);
        }

        return stacks;
    }

    /**
     * Checks if a given fluid stack matches this ingredient.
     * The stack <b>must not</b> be modified in any way.
     *
     * @param fluidStack the stack to test
     * @return {@code true} if the stack matches, {@code false} otherwise
     */
    @Override
    public abstract boolean test(FluidStack fluidStack);

    /**
     * Generates a stream of all fluid stacks this ingredient matches against.
     * <p>
     * For compatibility reasons, implementations should follow the same guidelines
     * as for custom item ingredients, i.e.:
     * <ul>
     * <li>These stacks are generally used for display purposes, and need not be exhaustive or perfectly accurate.</li>
     * <li>An exception is ingredients that {@linkplain #isSimple() are simple},
     * for which it is important that the returned stacks correspond exactly to all the accepted {@link Fluid}s.</li>
     * <li>At least one stack should always be returned, otherwise the ingredient may be considered {@linkplain #hasNoFluids() accidentally empty}.</li>
     * <li>The ingredient should try to return at least one stack with each accepted {@link Fluid}.
     * This allows mods that inspect the ingredient to figure out which stacks it might accept.</li>
     * </ul>
     *
     * @return a stream of all fluid stacks this ingredient accepts.
     *         <p>
     *         Note: No guarantees are made as to the amount of the fluid,
     *         as FluidIngredients are generally not meant to match by amount
     *         and these stacks are mostly used for display.
     *         <p>
     * @see ICustomIngredient#getItems()
     */
    protected abstract Stream<FluidStack> generateStacks();

    public abstract boolean isSimple();

    /**
     * {@return The type of this fluid ingredient.}
     *
     * <p>The type <b>must</b> be registered to {@link NeoForgeRegistries#FLUID_INGREDIENT_TYPES}.
     */
    public abstract FluidIngredientType<?> getType();

    /**
     * Checks if this ingredient is <b>explicitly empty</b>, i.e.
     * equal to {@link EmptyFluidIngredient#INSTANCE}.
     * <p> Note: This does <i>not</i> return true for "accidentally empty" ingredients,
     * including compound ingredients that are explicitly constructed with no children
     * or intersection / difference ingredients that resolve to an empty set.
     *
     * @return {@code true} if this ingredient is {@link #empty()}, {@code false} otherwise
     */
    public boolean isEmpty() {
        return this == empty();
    }

    /**
     * Checks if this ingredient matches no fluids, i.e. either:
     * <ul>
     * <li>is equal to {@link #empty()},</li>
     * <li>resolves to an empty list of fluids</li>
     * </ul>
     * <p>
     * Note that this method explicitly has the potential to <b>resolve</b>
     * the ingredient; if this is not desired, you will need to check for
     * emptiness another way!
     *
     * @return {@code true} if this ingredient matches no fluids, {@code false} otherwise
     * @implNote If it is possible for your ingredient to return a "hint" on
     *           whether it is empty without resolving, you should override this method
     *           with a custom implementation.
     * @see #isEmpty()
     */
    public boolean hasNoFluids() {
        return isEmpty() || getStacks().length == 0;
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
                    // prefer serializing without a type field, if possible
                    if (ingredient instanceof SingleFluidIngredient || ingredient instanceof TagFluidIngredient) {
                        return Either.right(ingredient);
                    }

                    return Either.left(ingredient);
                });
    }

    private static Codec<FluidIngredient> codec(boolean allowEmpty) {
        var listCodec = Codec.lazyInitialized(() -> allowEmpty ? LIST_CODEC : LIST_CODEC_NON_EMPTY);
        return Codec.either(listCodec, MAP_CODEC_CODEC)
                // [{...}, {...}] is turned into a CompoundIngredient instance
                .xmap(either -> either.map(CompoundFluidIngredient::of, i -> i),
                        ingredient -> {
                            // serialize CompoundIngredient instances as an array over their children
                            if (ingredient instanceof CompoundFluidIngredient compound) {
                                return Either.left(compound.children());
                            } else if (ingredient.isEmpty()) {
                                // serialize empty ingredients as []
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

    public static FluidIngredient of(FluidStack... fluids) {
        return of(Arrays.stream(fluids).map(FluidStack::getFluid));
    }

    public static FluidIngredient of(Fluid... fluids) {
        return of(Arrays.stream(fluids));
    }

    private static FluidIngredient of(Stream<Fluid> fluids) {
        return CompoundFluidIngredient.of(fluids.map(FluidIngredient::single));
    }

    public static FluidIngredient single(FluidStack stack) {
        return single(stack.getFluid());
    }

    public static FluidIngredient single(Fluid fluid) {
        return single(fluid.builtInRegistryHolder());
    }

    public static FluidIngredient single(Holder<Fluid> holder) {
        return new SingleFluidIngredient(holder);
    }

    public static FluidIngredient tag(TagKey<Fluid> tag) {
        return new TagFluidIngredient(tag);
    }
}
