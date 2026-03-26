/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.NeoForgeMod;

/**
 * Ingredient that matches the given items, and components as defined by a {@link DataComponentPatch}.
 * <p>
 * Strict NBT ingredients will only match items that have <b>exactly</b> the provided tag, while partial ones will
 * match if the item's tags contain all of the elements of the provided one, while allowing for additional elements to exist.
 */
public class DataComponentIngredient implements ICustomIngredient {
    public static final MapCodec<DataComponentIngredient> CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder
                    .group(
                            HolderSetCodec.create(Registries.ITEM, BuiltInRegistries.ITEM.holderByNameCodec(), false).fieldOf("items").forGetter(DataComponentIngredient::itemSet),
                            DataComponentPatch.CODEC.fieldOf("components").forGetter(DataComponentIngredient::components),
                            Codec.BOOL.optionalFieldOf("strict", false).forGetter(DataComponentIngredient::componentsExhaustive))
                    .apply(builder, DataComponentIngredient::new));

    private final HolderSet<Item> items;
    private final DataComponentPatch components;
    private final boolean exhaustive;

    public DataComponentIngredient(HolderSet<Item> items, DataComponentPatch components, boolean exhaustive) {
        this.items = items;
        this.components = components;
        this.exhaustive = exhaustive;
    }

    @Override
    public boolean test(ItemStack stack) {
        if (!this.items.contains(stack.typeHolder()) || !testComponents(stack)) {
            return false;
        }

        if (exhaustive) {
            for (var type : stack.getComponents().keySet()) {
                if (components.getPatch(type) == null) {
                    return false; // Patch does not list the component
                }
            }
        }
        return true;
    }

    private boolean testComponents(DataComponentGetter getter) {
        for (var entry : components.entrySet()) {
            var type = entry.getKey();
            var value = entry.getValue();
            if (value.isEmpty() && getter.has(type) || value.isPresent() && !value.get().equals(getter.get(type))) {
                return false; // One of the patch entries doesn't match
            }
        }
        return true; // Empty patch always matches
    }

    @Override
    public Stream<Holder<Item>> items() {
        return items.stream();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return NeoForgeMod.DATA_COMPONENT_INGREDIENT_TYPE.get();
    }

    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(items.stream()
                .<SlotDisplay>map(item -> {
                    var template = new ItemStackTemplate(item, 1, components);
                    var display = new SlotDisplay.ItemStackSlotDisplay(template);
                    var remainder = item.value().getCraftingRemainder(template);
                    if (remainder != null) {
                        SlotDisplay remainderDisplay = new SlotDisplay.ItemStackSlotDisplay(remainder);
                        return new SlotDisplay.WithRemainder(display, remainderDisplay);
                    } else {
                        return display;
                    }
                })
                .toList());
    }

    public HolderSet<Item> itemSet() {
        return items;
    }

    public DataComponentPatch components() {
        return components;
    }

    /**
     * {@return true if item stacks that have any component not listed in the components of this ingredient will fail to match}
     */
    public boolean componentsExhaustive() {
        return exhaustive;
    }

    /**
     * Creates a new ingredient matching the given item, containing the given components
     */
    public static Ingredient of(boolean exhaustive, ItemStack stack) {
        return of(exhaustive, stack.getComponents(), stack.getItem());
    }

    /**
     * Creates a new ingredient matching the given item, containing the given components
     */
    public static Ingredient of(boolean exhaustive, ItemStackTemplate stack) {
        return of(exhaustive, stack.components(), stack.item());
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static <T> Ingredient of(DataComponentType<? super T> type, T value, ItemLike... items) {
        return of(false, DataComponentPatch.builder().set(type, value).build(), items);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static <T> Ingredient of(boolean exhaustive, DataComponentType<? super T> type, T value, ItemLike... items) {
        return of(exhaustive, DataComponentPatch.builder().set(type, value).build(), items);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static <T> Ingredient of(boolean exhaustive, Supplier<? extends DataComponentType<? super T>> type, T value, ItemLike... items) {
        return of(exhaustive, type.get(), value, items);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static Ingredient of(boolean exhaustive, DataComponentMap map, ItemLike... items) {
        return of(exhaustive, asPatch(map), items);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    @SafeVarargs
    public static Ingredient of(boolean exhaustive, DataComponentMap map, Holder<Item>... items) {
        return of(exhaustive, asPatch(map), items);
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static Ingredient of(boolean exhaustive, DataComponentMap map, HolderSet<Item> items) {
        return of(exhaustive, asPatch(map), items);
    }

    private static DataComponentPatch asPatch(DataComponentMap map) {
        var builder = DataComponentPatch.builder();
        for (var type : map) {
            builder.set(type);
        }
        return builder.build();
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    @SafeVarargs
    public static Ingredient of(boolean exhaustive, DataComponentPatch predicate, Holder<Item>... items) {
        return of(exhaustive, predicate, HolderSet.direct(items));
    }

    /**
     * Creates a new ingredient matching any item from the list, that contains the components set on the given patch
     * and that does <strong>not</strong> contain the components removed by the given patch.
     */
    public static Ingredient of(DataComponentPatch predicate, ItemLike... items) {
        return of(false, predicate, HolderSet.direct(Arrays.stream(items).map(ItemLike::asItem).map(Item::builtInRegistryHolder).toList()));
    }

    /**
     * Creates a new ingredient matching any item from the list, that contains the components set on the given patch
     * and that does <strong>not</strong> contain the components removed by the given patch.
     *
     * @param exhaustive If true, no other components besides the components set on the patch are allowed on an item to match.
     */
    public static Ingredient of(boolean exhaustive, DataComponentPatch predicate, ItemLike... items) {
        return of(exhaustive, predicate, HolderSet.direct(Arrays.stream(items).map(ItemLike::asItem).map(Item::builtInRegistryHolder).toList()));
    }

    /**
     * Creates a new ingredient matching any item from the list, containing the given components
     */
    public static Ingredient of(boolean exhaustive, DataComponentPatch predicate, HolderSet<Item> items) {
        return new DataComponentIngredient(items, predicate, exhaustive).toVanilla();
    }
}
