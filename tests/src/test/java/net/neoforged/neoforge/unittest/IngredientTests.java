/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.crafting.NBTIngredient;
import net.neoforged.neoforge.junit.utils.EphemeralTestServerProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(EphemeralTestServerProvider.class)
public class IngredientTests {
    @ParameterizedTest
    @MethodSource("provideIngredientMatrix")
    void testCompoundIngredient(Ingredient a, Ingredient b) {
        final var ingredient = CompoundIngredient.of(a, b);
        Assertions.assertThat(a.getItems()).allMatch(ingredient, "first ingredient");
        Assertions.assertThat(b.getItems()).allMatch(ingredient, "second ingredient");
    }

    @ParameterizedTest
    @MethodSource("provideIngredientMatrix")
    void testDifferenceIngredient(Ingredient a, Ingredient b) {
        final var ingredient = DifferenceIngredient.of(a, b);

        // First ingredient must match all of the values - the ones in the second
        Assertions.assertThat(a.getItems())
                .filteredOn(Predicate.not(b))
                .allMatch(ingredient, "first ingredient");

        // Second ingredient must be subtracted
        Assertions.assertThat(b.getItems()).noneMatch(ingredient);
    }

    @ParameterizedTest
    @MethodSource("provideItems")
    void testPartialMatch(Item item1, Item item2) {
        final CompoundTag originalTag = new CompoundTag();
        originalTag.putInt("abcd", 1242);
        final NBTIngredient ingredient = NBTIngredient.of(false, originalTag, item1, item2);

        // Empty NBT shouldn't match
        Assertions.assertThat(List.of(item1, item2))
                .map(Item::getDefaultInstance)
                .noneMatch(ingredient);

        // Exact NBT should match
        Assertions.assertThat(List.of(item1, item2))
                .map(item -> Util.make(item.getDefaultInstance(), stack -> stack.getOrCreateTag().putInt("abcd", 1242)))
                .allMatch(ingredient);

        // Exact NBT keys and different values shouldn't match
        Assertions.assertThat(List.of(item1, item2))
                .map(item -> Util.make(item.getDefaultInstance(), stack -> stack.getOrCreateTag().putInt("abcd", 464)))
                .noneMatch(ingredient);

        // Partial NBT should also match
        Assertions.assertThat(List.of(item1, item2))
                .map(item -> Util.make(item.getDefaultInstance(), stack -> {
                    stack.getOrCreateTag().putInt("abcd", 1242);
                    stack.getOrCreateTag().putFloat("random", 4);
                }))
                .allMatch(ingredient);
    }

    @ParameterizedTest
    @MethodSource("provideItems")
    void testStrictMatch(Item item1, Item item2) {
        final CompoundTag originalTag = new CompoundTag();
        originalTag.putInt("abcd", 1242);
        final NBTIngredient ingredient = NBTIngredient.of(true, originalTag, item1, item2);

        // Empty NBT shouldn't match
        Assertions.assertThat(List.of(item1, item2))
                .map(Item::getDefaultInstance)
                .noneMatch(ingredient);

        // Exact NBT should match
        Assertions.assertThat(List.of(item1, item2))
                .map(item -> Util.make(item.getDefaultInstance(), stack -> stack.getOrCreateTag().putInt("abcd", 1242)))
                .allMatch(ingredient);

        // Exact NBT keys and different values shouldn't match
        Assertions.assertThat(List.of(item1, item2))
                .map(item -> Util.make(item.getDefaultInstance(), stack -> stack.getOrCreateTag().putInt("abcd", 464)))
                .noneMatch(ingredient);

        // Partial NBT also shouldn't match
        Assertions.assertThat(List.of(item1, item2))
                .map(item -> Util.make(item.getDefaultInstance(), stack -> {
                    stack.getOrCreateTag().putInt("abcd", 1242);
                    stack.getOrCreateTag().putFloat("random", 4);
                }))
                .noneMatch(ingredient);
    }

    private static Stream<Arguments> provideItems(MinecraftServer server) {
        final var it = Stream.concat(Stream.of(
                Items.ACACIA_LOG, Items.BIRCH_LOG),
                server.registryAccess().registryOrThrow(Registries.ITEM)
                        .getTag(ItemTags.ANVIL)
                        .orElseThrow()
                        .stream()
                        .map(Holder::value))
                .toList();
        // Test both stackable and non-stackable items, just in case
        return it.stream().flatMap(s1 -> it.stream().map(s2 -> Arguments.of(s1, s2)));
    }

    private static Stream<Arguments> provideIngredientMatrix(MinecraftServer server) {
        final List<Ingredient> matrix = List.of(
                Ingredient.of(Items.DISPENSER.getDefaultInstance()),
                Ingredient.of(ItemTags.ANVIL),
                NBTIngredient.of(true, Items.ACACIA_BOAT.getDefaultInstance()),
                NBTIngredient.of(false, Items.ACACIA_FENCE.getDefaultInstance()),
                DifferenceIngredient.of(
                        Ingredient.of(ItemTags.LOGS),
                        Ingredient.of(Items.ACACIA_LOG)));

        return matrix.stream()
                .flatMap(i -> matrix.stream().map(i2 -> Arguments.of(i, i2)));
    }
}
