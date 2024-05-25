/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    @ParameterizedTest
    @MethodSource("provideIngredientMatrix")
    void testCompoundIngredient(Ingredient a, Ingredient b) {
        final var ingredient = CompoundIngredient.of(a, b);
        Assertions.assertThat(a.getItems()).allMatch(ingredient, "first ingredient");
        Assertions.assertThat(b.getItems()).allMatch(ingredient, "second ingredient");
    }

    @Test
    void testDifferenceIngredients(MinecraftServer server) {
        final var logs = Ingredient.of(ItemTags.LOGS);
        final var acacia = Ingredient.of(Items.ACACIA_LOG);
        final var ingredient = DifferenceIngredient.of(logs, acacia);

        Assertions.assertThat(logs.getItems())
                .filteredOn(i -> !acacia.test(i))
                .containsExactlyInAnyOrder(ingredient.getItems());
    }

    @Test
    void testIntersectionIngredient(MinecraftServer server) {
        final var second = Ingredient.of(Items.BIRCH_LOG, Items.SPRUCE_LOG, Items.DISPENSER);
        final var ingredient = IntersectionIngredient.of(Ingredient.of(ItemTags.LOGS), second);

        Assertions.assertThat(Arrays.stream(ingredient.getItems()).map(ItemStack::getItem).distinct())
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
        stack.set(DataComponents.ITEM_NAME, Component.literal("name.."));
        Assertions.assertThat(ingredient.test(stack)).withFailMessage("Strictness check failed for ingredient with strict: " + strict).isEqualTo(!strict);
    }

    private static Stream<Arguments> provideIngredientMatrix(MinecraftServer server) {
        final List<Ingredient> matrix = List.of(
                Ingredient.of(Items.DISPENSER.getDefaultInstance()),
                Ingredient.of(Items.ACACIA_DOOR),
                Ingredient.of(ItemTags.ANVIL));

        return matrix.stream()
                .flatMap(i -> matrix.stream().map(i2 -> Arguments.of(i, i2)));
    }
}
