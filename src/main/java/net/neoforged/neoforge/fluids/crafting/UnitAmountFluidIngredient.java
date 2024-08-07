/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids.crafting;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUnit;
import net.neoforged.neoforge.fluids.FluidUnitSpec;
import net.neoforged.neoforge.fluids.FluidUnits;
import org.jetbrains.annotations.Nullable;

/**
 * Standard implementation for a FluidIngredient with an amount.
 *
 * <p>{@link FluidIngredient}, like its item counterpart, explicitly does not perform count checks,
 * so this class is used to (a) wrap a standard FluidIngredient with an amount and (b) provide a
 * standard serialization format for mods to use.
 *
 * @see net.neoforged.neoforge.common.crafting.SizedIngredient
 */
public final class UnitAmountFluidIngredient {
    /**
     * The codec for the "amount" field.
     *
     * <p>The amounts field consists of a list of maps of fluid units to integers.
     * It can be simplified to a single map, or a quantity in millibuckets.
     * For example, the following list specifies a recipe that takes 1 block and
     * 3 ingots or 3 gems' worth of a molten metal or gem fluid.
     *
     * <pre>{@code
     *  [{"c:block": 1, "c:ingot", 3},
     *   {"c:block": 1, "c:gem", 3}]
     * }</pre>
     *
     * <p>If none of the entries has all of its units defined in the fluid's unit spec,
     * the ingredient will not match the fluid.
     *
     */
    public static final Codec<List<Map<FluidUnit, Integer>>> AMOUNTS_CODEC = makeAmountsCodec();

    private static <T> DataResult<List<T>> validateNotEmpty(List<T> amounts) {
        return amounts.isEmpty() ? DataResult.error(() -> "amounts must not be empty") : DataResult.success(amounts);
    }

    private static <K, V> DataResult<Map<K, V>> validateNotEmpty(Map<K, V> amounts) {
        return amounts.isEmpty() ? DataResult.error(() -> "amount must not be empty") : DataResult.success(amounts);
    }

    private static DataResult<Integer> validatePositive(int amount) {
        return amount <= 0 ? DataResult.error(() -> "amount must be positive") : DataResult.success(amount);
    }

    private static Codec<List<Map<FluidUnit, Integer>>> makeAmountsCodec() {
        Function<Integer, Map<FluidUnit, Integer>> intToMb = v -> Map.of(FluidUnits.MB, v);
        Codec<Integer> intCodec = Codec.INT.validate(UnitAmountFluidIngredient::validatePositive);
        Codec<Map<FluidUnit, Integer>> fullMapCodec = Codec.unboundedMap(FluidUnit.CODEC, intCodec).validate(UnitAmountFluidIngredient::validateNotEmpty);
        Codec<Map<FluidUnit, Integer>> mapCodec = Codec.either(fullMapCodec, intCodec)
                .xmap(e -> e.map(Function.identity(), intToMb), Either::left);
        return Codec.either(mapCodec, mapCodec.listOf().validate(UnitAmountFluidIngredient::validateNotEmpty))
                .xmap(e1 -> e1.map(List::of, Function.identity()), l -> l.size() == 1 ? Either.left(l.getFirst()) : Either.right(l));
    }

    private static final List<Map<FluidUnit, Integer>> DEFAULT_AMOUNT = List.of(Map.of(FluidUnits.BUCKET, 1));

    /**
     * The "flat" codec for {@link UnitAmountFluidIngredient}.
     *
     * <p>The amount is serialized inline with the rest of the ingredient, for example:
     *
     * <pre>{@code
     * {
     *     "fluid": "minecraft:water",
     *     "amount": 250
     * }
     * }</pre>
     *
     * <p>
     * <p>
     * Compound fluid ingredients are always serialized using the map codec, i.e.
     *
     * <pre>{@code
     * {
     *     "type": "neoforge:compound",
     *     "ingredients": [
     *         { "fluid": "minecraft:water" },
     *         { "fluid": "minecraft:milk" }
     *     ],
     *     "amount": 500
     * }
     * }</pre>
     *
     * <p>
     */
    public static final Codec<UnitAmountFluidIngredient> FLAT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidIngredient.MAP_CODEC_NONEMPTY.forGetter(UnitAmountFluidIngredient::ingredient),
            NeoForgeExtraCodecs.optionalFieldAlwaysWrite(AMOUNTS_CODEC, "amount", DEFAULT_AMOUNT).forGetter(UnitAmountFluidIngredient::amounts))
            .apply(instance, UnitAmountFluidIngredient::new));

    /**
     * The "nested" codec for {@link UnitAmountFluidIngredient}.
     *
     * <p>With this codec, the amount is <i>always</i> serialized separately from the ingredient itself, for example:
     *
     * <pre>{@code
     * {
     *     "ingredient": {
     *         "fluid": "minecraft:lava"
     *     },
     *     "amount": 1000
     * }
     * }</pre>
     */
    public static final Codec<UnitAmountFluidIngredient> NESTED_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FluidIngredient.CODEC_NON_EMPTY.fieldOf("ingredient").forGetter(UnitAmountFluidIngredient::ingredient),
            NeoForgeExtraCodecs.optionalFieldAlwaysWrite(AMOUNTS_CODEC, "amount", DEFAULT_AMOUNT).forGetter(UnitAmountFluidIngredient::amounts))
            .apply(instance, UnitAmountFluidIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, UnitAmountFluidIngredient> STREAM_CODEC = StreamCodec.composite(
            FluidIngredient.STREAM_CODEC,
            UnitAmountFluidIngredient::ingredient,
            ByteBufCodecs.<ByteBuf, FluidUnit, Integer, Map<FluidUnit, Integer>>map(HashMap::new, FluidUnit.STREAM_CODEC, ByteBufCodecs.INT).apply(ByteBufCodecs.list()),
            UnitAmountFluidIngredient::amounts,
            UnitAmountFluidIngredient::new);

    /**
     * Helper method to create a simple unit-based ingredient that has a single unit quantity.
     */
    public static UnitAmountFluidIngredient of(FluidIngredient fluid, int amount, FluidUnit unit) {
        return new UnitAmountFluidIngredient(fluid, List.of(Map.of(unit, amount)));
    }

    /**
     * Helper method to create a simple unit-based ingredient that matches a single fluid.
     */
    public static UnitAmountFluidIngredient of(Fluid fluid, int amount, FluidUnit unit) {
        return new UnitAmountFluidIngredient(FluidIngredient.of(fluid), List.of(Map.of(unit, amount)));
    }

    /**
     * Helper method to create a simple unit-based ingredient that matches a single fluid.
     */
    public static UnitAmountFluidIngredient of(TagKey<Fluid> tag, int amount, FluidUnit unit) {
        return new UnitAmountFluidIngredient(FluidIngredient.tag(tag), List.of(Map.of(unit, amount)));
    }

    /**
     * Factory method for creating any ingredient.
     */
    @SafeVarargs
    public static UnitAmountFluidIngredient of(FluidIngredient ingredient, Map<FluidUnit, Integer>... amounts) {
        if (amounts.length == 0)
            return new UnitAmountFluidIngredient(ingredient, DEFAULT_AMOUNT);
        else
            return new UnitAmountFluidIngredient(ingredient, Arrays.stream(amounts)
                    .map(UnitAmountFluidIngredient::validateNotEmpty)
                    .map(DataResult::getOrThrow).toList());
    }

    private final FluidIngredient ingredient;
    private final List<Map<FluidUnit, Integer>> amounts;

    @Nullable
    private FluidStack[] cachedStacks;

    private UnitAmountFluidIngredient(FluidIngredient ingredient, List<Map<FluidUnit, Integer>> amounts) {
        this.ingredient = ingredient;
        this.amounts = amounts;
    }

    /**
     * The unsized fluid ingredient this ingredient is based on
     */
    public FluidIngredient ingredient() {
        return ingredient;
    }

    /**
     * The list of unit-based amounts associated with this recipe.
     * Each element of the list is checked in turn, the first one that has all its units defined
     * in the fluid's unit spec is used. If none of them qualify, the ingredient does not match.
     */
    public List<Map<FluidUnit, Integer>> amounts() {
        return amounts;
    }

    /**
     * Get the quantity in millibuckets associated with the recipe's amount
     * 
     * @return The millibucket quantity for the first amount that has all its units defined in the fluid spec
     */
    public Optional<Integer> amount(FluidStack stack) {
        return amount(stack.getFluidType().getUnits(), amounts);
    }

    /**
     * Get the quantity in millibuckets associated with a given unit-based amount
     * 
     * @param fluidUnits The unit spec of the fluid
     * @param amounts    List of amounts as specified in the recipe's amount fluid
     * @return The millibucket quantity for the first amount that has all its units defined in the fluid spec
     */
    public static Optional<Integer> amount(FluidUnitSpec fluidUnits, List<Map<FluidUnit, Integer>> amounts) {
        outer:
        for (Map<FluidUnit, Integer> amount : amounts) {
            int accumulator = 0;
            for (Map.Entry<FluidUnit, Integer> entry : amount.entrySet()) {
                Optional<Integer> value = fluidUnits.getValue(entry.getKey());
                if (value.isEmpty())
                    continue outer;
                else
                    accumulator += value.get() * entry.getValue();
            }
            return Optional.of(accumulator);
        }
        return Optional.empty();
    }

    /**
     * Performs a size-sensitive test on the given stack.
     *
     * @return {@code true} if the stack matches the ingredient and has at least the required amount.
     */
    public boolean test(FluidStack stack) {
        if (ingredient.test(stack)) {
            return amount(stack).map(value -> stack.getAmount() >= value).orElse(false);
        } else {
            return false;
        }
    }

    /**
     * Returns a list of the stacks from this {@link #ingredient}, with an updated {@link #amount}.
     *
     * @implNote the array is cached and should not be modified, just like {@link FluidIngredient#getStacks()}}.
     */
    public FluidStack[] getFluids() {
        if (cachedStacks == null) {
            cachedStacks = Stream.of(ingredient.getStacks())
                    .flatMap(input -> amount(input).map(input::copyWithAmount).stream())
                    .toArray(FluidStack[]::new);
        }
        return cachedStacks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UnitAmountFluidIngredient other)) return false;
        return amounts.equals(other.amounts) && ingredient.equals(other.ingredient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ingredient, amounts);
    }

    @Override
    public String toString() {
        return amounts.stream()
                .map(l -> l.entrySet().stream()
                        .map(e -> e.getKey() + " " + e.getValue())
                        .collect(Collectors.joining(", ")))
                .collect(Collectors.joining(" / "))
                + ingredient;
    }
}
