/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.fluids;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class FluidUnitSpec {
    public static final Codec<FluidUnitSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(FluidUnit.CODEC, UnitInfo.CODEC).validate(FluidUnitSpec::checkFixedUnits).fieldOf("units").forGetter(s -> s.map),
            FluidUnit.CODEC.optionalFieldOf("default_unit", FluidUnits.BUCKET).forGetter(FluidUnitSpec::defaultUnit),
            Codec.INT.optionalFieldOf("smallest_decimals", -1).forGetter(s -> s.smallestUnitDecimals)).apply(instance, FluidUnitSpec::new));

    private final Map<FluidUnit, UnitInfo> map;

    private final int smallestUnitDecimals;
    private final FluidUnit defaultUnit;
    private final List<FluidUnit> listForTooltip;

    /**
     * Default spec used for fluids that have no data in the data map.
     * Formats values like "1.234 buckets".
     */
    public static final FluidUnitSpec DEFAULT = new FluidUnitSpec(
            Map.of(FluidUnits.BUCKET, new UnitInfo(FluidType.BUCKET_VOLUME)),
            FluidUnits.BUCKET, 3);

    /**
     * Helper class for constructing FluidUnitSpec values for datagen purposes.
     */
    public static class Builder {
        Map<FluidUnit, UnitInfo> data = new HashMap<>();
        FluidUnit defaultUnit = FluidUnits.BUCKET;
        int smallestUnitDecimals = -1;

        /**
         * Adds a unit to the spec.
         * 
         * @param unit             the unit being added
         * @param value            the number of millibuckets per unit for this fluid
         * @param displayInTooltip If the unit should be displayed in the tooltip
         * @return
         */
        public Builder addUnit(FluidUnit unit, int value, boolean displayInTooltip) {
            data.put(unit, new UnitInfo(value, displayInTooltip));
            return this;
        }

        /**
         * Adds a unit to the spec. The unit will be displayed in the tooltip.
         * 
         * @param unit  the unit being added
         * @param value the number of millibuckets per unit for this fluid
         */
        public Builder addUnit(FluidUnit unit, int value) {
            data.put(unit, new UnitInfo(value));
            return this;
        }

        /**
         * Sets the default unit, used in contexts where a quantity needs to be displayed as a single number.
         */
        public Builder defaultUnit(FluidUnit unit) {
            defaultUnit = unit;
            return this;
        }

        /**
         * Convenience method combining addUnit and defaultUnit
         */
        public Builder defaultUnit(FluidUnit unit, int value) {
            addUnit(unit, value, true);
            defaultUnit = unit;
            return this;
        }

        /**
         * Sets the number of decimal places used to print the smallest-valued unit.
         */
        public Builder smallestUnitDecimals(int decimals) {
            smallestUnitDecimals = decimals;
            return this;
        }

        public FluidUnitSpec build() {
            return new FluidUnitSpec(checkFixedUnits(data).getOrThrow(), defaultUnit, smallestUnitDecimals);
        }
    }

    /**
     * Creates a builder for datagen purposes.
     */
    public static Builder builder() {
        return new Builder();
    }

    private FluidUnitSpec(Map<FluidUnit, UnitInfo> data,
            FluidUnit defaultUnit,
            int smallestUnitDecimals) {
        this.map = data;
        this.listForTooltip = map.entrySet().stream()
                .filter(e -> e.getValue().showInTooltip)
                .sorted(Comparator.<Map.Entry<FluidUnit, UnitInfo>, Integer>comparing(e -> e.getValue().value).reversed())
                .map(Map.Entry::getKey)
                .toList();
        this.defaultUnit = defaultUnit;
        this.smallestUnitDecimals = smallestUnitDecimals;
    }

    /**
     * Verify that if bucket or mb are present, they must have the correct values
     */
    private static DataResult<Map<FluidUnit, UnitInfo>> checkFixedUnits(Map<FluidUnit, UnitInfo> map) {
        for (Map.Entry<FluidUnit, UnitInfo> entry : map.entrySet()) {
            if (entry.getKey() == FluidUnits.BUCKET && entry.getValue().value != FluidType.BUCKET_VOLUME)
                return DataResult.error(() -> "bucket must be defined as " + FluidType.BUCKET_VOLUME);
            if (entry.getKey() == FluidUnits.MB && entry.getValue().value != 1)
                return DataResult.error(() -> "mb must be defined as 1");
        }
        return DataResult.success(map);
    }

    /**
     * Gets a list of all units in this spec..
     */
    public Set<FluidUnit> units() {
        return map.keySet();
    }

    /**
     * Gets a default unit that may be used by mods that want to display the quantity
     * in a limited amount of space as a single number.
     */
    private FluidUnit defaultUnit() {
        return this.defaultUnit;
    }

    /**
     * Gets the quantity in millibuckets associated with a given unit.
     */
    public Optional<Integer> getValue(FluidUnit unit) {
        if (unit == FluidUnits.BUCKET)
            return Optional.of(FluidType.BUCKET_VOLUME);
        else if (unit == FluidUnits.MB)
            return Optional.of(1);
        else
            return Optional.ofNullable(map.get(unit)).map(UnitInfo::value);
    }

    /**
     * Gets a list of components suitable for use in a tooltip representing the quantity,
     * e.g. "1 block", "3 ingots"
     */
    public List<Component> getTooltipComponents(long amount) {
        if (amount == 0) return List.of(Component.translatable("fluid_unit.empty"));
        List<Component> results = new ArrayList<>();
        for (int i = 0; i < listForTooltip.size(); i++) {
            FluidUnit unit = listForTooltip.get(i);
            Optional<Integer> optional = getValue(unit);
            if (optional.isEmpty()) continue; // shouldn't happen
            int unitValue = optional.get();
            long amountUnit = amount / unitValue;
            amount = amount % unitValue;
            if (i < listForTooltip.size() - 1 || amount == 0) {
                if (amountUnit != 0)
                    results.add(Component.translatable("fluid_unit.number_and_unit_format", // "%s %s"
                            amountUnit, unit.getDescriptionId(amountUnit != 1)));
                if (amount == 0) break;
            } else {
                // non-whole quantity of the smallest unit.
                results.add(Component.translatable("fluid_unit.number_and_unit_format",
                        formatFraction(unitValue, (double) amount / unitValue),
                        unit.getDescriptionId(true)));
            }
        }
        return results;
    }

    private String formatFraction(int unitValue, double amountFloat) {
        int actualDecimals = smallestUnitDecimals >= 0 ? smallestUnitDecimals : Mth.ceil(Math.log10(unitValue));
        String format = "%." + actualDecimals + "f";
        return String.format(format, amountFloat);
    }

    /**
     * Represents information about a unit for a fluid.
     *
     * @param value         The number of millibuckets per unit.
     * @param showInTooltip Whether the unit should be displayed in tooltips or other similar contexts.
     */
    public record UnitInfo(int value, boolean showInTooltip) {

        public static final Codec<UnitInfo> CODEC = Codec.<Integer, UnitInfo>either(Codec.INT, RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("value").validate(i -> i > 0 ? DataResult.success(i) : DataResult.error(() -> "unit value must be positive")).forGetter(UnitInfo::value),
                Codec.BOOL.fieldOf("show_in_tooltip").forGetter(UnitInfo::showInTooltip))
                .apply(instance, UnitInfo::new)))
                .xmap(e -> e.map(UnitInfo::new, Function.identity()), v -> v.showInTooltip ? Either.left(v.value) : Either.right(v));
        public UnitInfo(int value) {
            this(value, true);
        }
    }
}
