/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/**
 * Standard implementation for a FluidIngredient with an amount.
 *
 * <p>{@link FluidIngredient}, like its item counterpart, explicitly does not perform count checks,
 * so this class is used to (a) wrap a standard FluidIngredient with an amount and (b) provide a
 * standard serialization format for mods to use.
 *
 * @see net.neoforged.neoforge.common.crafting.SizedIngredient
 */
public final class SizedFluidIngredient {
    /**
     * The codec for {@link SizedFluidIngredient}.
     *
     * <p>With this codec, the amount is serialized separately from the ingredient itself, for example:
     *
     * <pre>{@code
     * {
     *     "ingredient": "minecraft:lava",
     *     "amount": 1000
     * }
     * }</pre>
     *
     * <p>
     * or for custom ingredients:
     *
     * <pre>{@code
     * {
     *     "ingredient": {
     *         "neoforge:type": "neoforge:intersection",
     *         "children": [
     *              "#example:tag1",
     *              "#example:tag2"
     *         ],
     *     },
     *     "amount": 4711
     * }
     * }</pre>
     */
    public static final Codec<SizedFluidIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidIngredient.CODEC.fieldOf("ingredient").forGetter(SizedFluidIngredient::ingredient),
            NeoForgeExtraCodecs.optionalFieldAlwaysWrite(ExtraCodecs.POSITIVE_INT, "amount", FluidType.BUCKET_VOLUME).forGetter(SizedFluidIngredient::amount))
            .apply(instance, SizedFluidIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedFluidIngredient> STREAM_CODEC = StreamCodec.composite(
            FluidIngredient.STREAM_CODEC,
            SizedFluidIngredient::ingredient,
            ByteBufCodecs.VAR_INT,
            SizedFluidIngredient::amount,
            SizedFluidIngredient::new);

    public static SizedFluidIngredient of(Fluid fluid, int amount) {
        return new SizedFluidIngredient(FluidIngredient.of(fluid), amount);
    }

    private final FluidIngredient ingredient;
    private final int amount;

    public SizedFluidIngredient(FluidIngredient ingredient, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        this.ingredient = ingredient;
        this.amount = amount;
    }

    public FluidIngredient ingredient() {
        return ingredient;
    }

    public int amount() {
        return amount;
    }

    /**
     * Performs a size-sensitive test on the given stack.
     *
     * @return {@code true} if the stack matches the ingredient and has at least the required amount.
     */
    public boolean test(FluidStack stack) {
        return ingredient.test(stack) && stack.getAmount() >= amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SizedFluidIngredient other)) return false;
        return amount == other.amount && ingredient.equals(other.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, amount);
    }

    @Override
    public String toString() {
        return amount + "x " + ingredient;
    }
}
