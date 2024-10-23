/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.display.FluidSlotDisplay;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * This class serves as the fluid analogue of an item {@link Ingredient},
 * that is, a representation of both a {@linkplain #test predicate} to test
 * {@link FluidStack}s against, and a {@linkplain #fluids list} of matching stacks
 * for e.g. display purposes.
 * <p>
 * The most common use for fluid ingredients is found in modded recipe inputs,
 * for example crafting mechanics accepting a list of different fluids;
 * since those mechanics even rely on a certain <em>amount</em> of a fluid being present,
 * and fluid ingredients inherently do not hold any information with respect to fluid amount;
 * you may also want to take a look at {@link SizedFluidIngredient}!
 */
public abstract class FluidIngredient implements Predicate<FluidStack> {
    public static final Codec<FluidIngredient> CODEC = FluidIngredientCodecs.codec();

    public static final StreamCodec<RegistryFriendlyByteBuf, FluidIngredient> STREAM_CODEC = FluidIngredientCodecs.streamCodec();

    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<FluidIngredient>> OPTIONAL_STREAM_CODEC = ByteBufCodecs.optional(STREAM_CODEC);

    @Nullable
    private List<Holder<Fluid>> fluids;

    /**
     * {@return a cached list of all Fluid holders that this ingredient accepts}
     * This list is immutable and thus <b>can and should not</b> be modified by the caller!
     *
     * @see #generateFluids()
     */
    public final List<Holder<Fluid>> fluids() {
        if (fluids == null) {
            fluids = generateFluids().toList();
        }

        return fluids;
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
     * {@return a stream of fluids accepted by this ingredient}
     * <p>
     * For compatibility reasons, implementations should follow the same guidelines
     * as for custom item ingredients, i.e.:
     * <ul>
     * <li>Returned fluids are generally used for display purposes, and need not be exhaustive or perfectly accurate,
     * as ingredients may additionally filter by e.g. data component values.</li>
     * <li>An exception is ingredients that {@linkplain #isSimple() are simple},
     * for which it is important that this stream corresponds exactly all fluids accepted by {@link #test(FluidStack)}!</li>
     * <li>At least one stack should always be returned, so that the ingredient is not considered empty. <b>Empty ingredients may invalidate recipes!</b></li>
     * </ul>
     *
     * <p>Note: no caching needs to be done by the implementation, this is already handled by {@link #fluids}!
     *
     * @return a stream of all fluid stacks this ingredient accepts.
     *         <p>
     *         Note: No guarantees are made as to the amount of the fluid,
     *         as FluidIngredients are generally not meant to match by amount
     *         and these stacks are mostly used for display.
     *         <p>
     * @see ICustomIngredient#items()
     */
    @ApiStatus.OverrideOnly
    protected abstract Stream<Holder<Fluid>> generateFluids();

    /**
     * {@return a slot display for this ingredient, used for display on the client-side}
     *
     * @implNote The default implementation just constructs a list of stacks from {@link #fluids()}.
     *           This is generally suitable for {@link #isSimple() simple} ingredients.
     *           Non-simple ingredients can either override this method to provide a more customized display,
     *           or let data pack writers use {@link CustomDisplayFluidIngredient} to override the display of an ingredient.
     *
     * @see Ingredient#display()
     * @see FluidSlotDisplay
     */
    public SlotDisplay display() {
        return new SlotDisplay.Composite(fluids()
                .stream()
                .map(FluidIngredient::displayForSingleFluid)
                .toList());
    }

    /**
     * Returns whether this fluid ingredient always requires {@linkplain #test direct stack testing}.
     *
     * @return {@code true} if this ingredient ignores NBT data when matching stacks, {@code false} otherwise
     * @see ICustomIngredient#isSimple()
     */
    public abstract boolean isSimple();

    /**
     * {@return The type of this fluid ingredient.}
     *
     * <p>The type <b>must</b> be registered to {@link NeoForgeRegistries#FLUID_INGREDIENT_TYPES}.
     */
    public abstract FluidIngredientType<?> getType();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public static SlotDisplay displayForSingleFluid(Holder<Fluid> holder) {
        return new FluidSlotDisplay(holder);
    }

    public static FluidIngredient of(FluidStack... fluids) {
        return of(Arrays.stream(fluids).map(FluidStack::getFluid));
    }

    public static FluidIngredient of(Fluid... fluids) {
        return of(Arrays.stream(fluids));
    }

    public static FluidIngredient of(Stream<Fluid> fluids) {
        return of(HolderSet.direct(fluids.map(Fluid::builtInRegistryHolder).toList()));
    }

    public static FluidIngredient of(HolderSet<Fluid> fluids) {
        return new SimpleFluidIngredient(fluids);
    }
}
