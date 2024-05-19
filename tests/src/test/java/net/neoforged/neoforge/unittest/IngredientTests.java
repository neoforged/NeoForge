/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

@ExtendWith(EphemeralTestServerProvider.class)
public class IngredientTests {
    @ParameterizedTest
    @MethodSource("provideIngredientMatrix")
    void testCompoundIngredient(Ingredient a, Ingredient b) {
        final var ingredient = CompoundIngredient.of(a, b);
        Assertions.assertThat(a.getItems()).allMatch(ingredient, "first ingredient");
        Assertions.assertThat(b.getItems()).allMatch(ingredient, "second ingredient");
    }

    private static Stream<Arguments> provideIngredientMatrix(MinecraftServer server) {
        final List<Ingredient> matrix = List.of(
                Ingredient.of(Items.DISPENSER.getDefaultInstance()),
                Ingredient.of(ItemTags.ANVIL),
                DifferenceIngredient.of(
                        Ingredient.of(ItemTags.LOGS),
                        Ingredient.of(Items.ACACIA_LOG)));

        return matrix.stream()
                .flatMap(i -> matrix.stream().map(i2 -> Arguments.of(i, i2)));
    }
}
