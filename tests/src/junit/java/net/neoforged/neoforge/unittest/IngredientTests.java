/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.crafting.IntersectionIngredient;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(EphemeralTestServerProvider.class)
public class IngredientTests {
    private static List<ItemStack> ingredientItemsAsStacks(Ingredient ingredient) {
        return ingredient.items().map(i -> i.value().getDefaultInstance()).toList();
    }

    @ParameterizedTest
    @MethodSource("provideIngredientMatrix")
    void testCompoundIngredient(Ingredient a, Ingredient b) {
        final var ingredient = CompoundIngredient.of(a, b);
        Assertions.assertThat(ingredientItemsAsStacks(a)).allMatch(ingredient, "first ingredient");
        Assertions.assertThat(ingredientItemsAsStacks(b)).allMatch(ingredient, "second ingredient");
    }

    @Test
    void testDifferenceIngredients(MinecraftServer server) {
        final var logs = Ingredient.of(server.registryAccess().lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.LOGS));
        final var acacia = Ingredient.of(Items.ACACIA_LOG);
        final var ingredient = DifferenceIngredient.of(logs, acacia);

        Assertions.assertThat(logs.items())
                .filteredOn(i -> !acacia.test(i.value().getDefaultInstance()))
                .containsExactlyInAnyOrder(ingredient.items().toArray(Holder[]::new));
    }

    @Test
    void testIntersectionIngredient(MinecraftServer server) {
        final var second = Ingredient.of(Items.BIRCH_LOG, Items.SPRUCE_LOG, Items.DISPENSER);
        final var ingredient = IntersectionIngredient.of(Ingredient.of(server.registryAccess().lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.LOGS)), second);

        Assertions.assertThat(ingredient.items().map(Holder::value).distinct())
                .containsExactlyInAnyOrder(Items.BIRCH_LOG, Items.SPRUCE_LOG);
    }

    @ParameterizedTest
    @CsvSource({ "true", "false" })
    void testComponentIngredient(boolean strict, MinecraftServer server) {
        var stack = new ItemStack(Items.DIAMOND_AXE);
        stack.set(DataComponents.DAMAGE, 1);
        var ingredient = DataComponentIngredient.of(strict, stack);
        Assertions.assertThat(ingredient.test(stack)).withFailMessage("Base ingredient doesn't match").isTrue();

        stack.set(DataComponents.DAMAGE, 2);
        Assertions.assertThat(ingredient.test(stack)).withFailMessage("Modified ingredient matches").isFalse();

        stack.set(DataComponents.DAMAGE, 1);
        stack.set(DataComponents.UNBREAKABLE, new Unbreakable(false));
        Assertions.assertThat(ingredient.test(stack)).withFailMessage("Strictness check failed for ingredient with strict: " + strict).isEqualTo(!strict);
    }

    private static Stream<Arguments> provideIngredientMatrix(MinecraftServer server) {
        final List<Ingredient> matrix = List.of(
                Ingredient.of(Items.DISPENSER),
                Ingredient.of(Items.ACACIA_DOOR),
                Ingredient.of(server.registryAccess().lookupOrThrow(Registries.ITEM).getOrThrow(ItemTags.ANVIL)));

        return matrix.stream()
                .flatMap(i -> matrix.stream().map(i2 -> Arguments.of(i, i2)));
    }
}
