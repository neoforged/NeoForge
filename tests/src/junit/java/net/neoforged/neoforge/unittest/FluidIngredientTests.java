/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.CompoundFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.IntersectionFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(EphemeralTestServerProvider.class)
public class FluidIngredientTests {
    @Test
    void emptyIngredientFails(MinecraftServer server) {
        Assertions.assertThatThrownBy(() -> FluidIngredient.of(Stream.empty()))
                .withFailMessage("Empty fluid ingredient should not have been able to be constructed!")
                .isInstanceOf(UnsupportedOperationException.class);
        Assertions.assertThatThrownBy(() -> FluidIngredient.of(Fluids.WATER, Fluids.LAVA, Fluids.EMPTY))
                .withFailMessage("SimpleFluidIngredient should not have been able to be constructed with empty fluid!")
                .isInstanceOf(UnsupportedOperationException.class);

        Assertions.assertThatThrownBy(() -> new CompoundFluidIngredient(List.of()))
                .withFailMessage("Empty compound fluid ingredient should not have been able to be constructed!")
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({ "false", "true" })
    void fluidIngredientComponentMatchingWorks(boolean strict, MinecraftServer server) {
        var ingredient = DataComponentFluidIngredient.of(strict, DataComponents.RARITY, Rarity.EPIC, Fluids.WATER);
        var stack = new FluidStack(Fluids.WATER, 1000);

        Assertions.assertThat(ingredient.test(stack))
                .withFailMessage("Fluid without custom data should not match DataComponentFluidIngredient!")
                .isFalse();

        stack.applyComponents(DataComponentPatch.builder()
                .set(DataComponents.RARITY, Rarity.UNCOMMON)
                .build());

        Assertions.assertThat(ingredient.test(stack))
                .withFailMessage("Fluid with incorrect data should not match DataComponentFluidIngredient!")
                .isFalse();

        stack.applyComponents(DataComponentPatch.builder()
                .set(DataComponents.RARITY, Rarity.EPIC)
                .build());

        Assertions.assertThat(ingredient.test(stack))
                .withFailMessage("Fluid with correct data should match DataComponentFluidIngredient!")
                .isTrue();

        var data = CustomData.EMPTY.update(tag -> tag.putFloat("abcd", 0.5F));
        stack.set(DataComponents.CUSTOM_DATA, data);

        Assertions.assertThat(ingredient.test(stack))
                .withFailMessage("Strictness check failed for DataComponentFluidIngredient with strict: " + strict)
                .isEqualTo(!strict);
    }

    @Test
    void singleFluidIngredientIgnoresSizeAndData(MinecraftServer server) {
        var ingredient = FluidIngredient.of(Fluids.WATER);

        Assertions.assertThat(ingredient.test(new FluidStack(Fluids.WATER, 1234)))
                .withFailMessage("Single fluid ingredient should match regardless of fluid amount!")
                .isTrue();

        Assertions.assertThat(ingredient.test(new FluidStack(Fluids.WATER.builtInRegistryHolder(), 1234, DataComponentPatch.builder().set(DataComponents.RARITY, Rarity.COMMON).build())))
                .withFailMessage("Single fluid ingredient should match regardless of fluid data!")
                .isTrue();
    }

    @Test
    void basicFluidIngredientSerialization(MinecraftServer server) {
        var registries = server.registryAccess();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        var singleFluid = FluidIngredient.of(Fluids.WATER);
        var tagFluid = FluidIngredient.of(registries.lookupOrThrow(Registries.FLUID).getOrThrow(Tags.Fluids.WATER));

        var singleJson = FluidIngredient.CODEC.encodeStart(ops, singleFluid).getOrThrow();
        var tagJson = FluidIngredient.CODEC.encodeStart(ops, tagFluid).getOrThrow();
        Assertions.assertThat(singleJson).isEqualTo(new JsonPrimitive("minecraft:water"));
        Assertions.assertThat(tagJson).isEqualTo(new JsonPrimitive("#" + Tags.Fluids.WATER.location()));
        Assertions.assertThat(FluidIngredient.CODEC.parse(ops, singleJson).getOrThrow()).isEqualTo(singleFluid);
        Assertions.assertThat(FluidIngredient.CODEC.parse(ops, tagJson).getOrThrow()).isEqualTo(tagFluid);

        var nested = new JsonObject();
        nested.addProperty("neoforge:ingredient_type", NeoForgeMod.SIMPLE_FLUID_INGREDIENT_TYPE.getId().toString());
        nested.addProperty("fluid", "minecraft:water");
        Assertions.assertThat(FluidIngredient.CODEC.parse(ops, nested).isError()).isTrue();
    }

    @Test
    void sizedFluidIngredientSerialization(MinecraftServer server) {
        var sized = SizedFluidIngredient.of(Fluids.WATER, 1000);
        var expected = new JsonObject();
        expected.addProperty("ingredient", "minecraft:water");
        expected.addProperty("amount", 1000);

        var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var json = SizedFluidIngredient.CODEC.encodeStart(ops, sized).getOrThrow();
        Assertions.assertThat(json).isEqualTo(expected);
        Assertions.assertThat(SizedFluidIngredient.CODEC.parse(ops, json).getOrThrow()).isEqualTo(sized);
    }

    @Test
    void sizedFluidIngredientMatching(MinecraftServer server) {
        var sized = SizedFluidIngredient.of(Fluids.WATER, 2);
        Assertions.assertThat(sized.test(new FluidStack(Fluids.LAVA, 1000))).isFalse();
        Assertions.assertThat(sized.test(new FluidStack(Fluids.WATER, 1))).isFalse();
        Assertions.assertThat(sized.test(new FluidStack(Fluids.WATER, 2))).isTrue();
        Assertions.assertThat(sized.test(new FluidStack(Fluids.WATER, 3))).isTrue();

        // TODO(max): implement display wrapping for sized ingredients(?)
    }

    @Test
    void customFluidIngredientSerialization(MinecraftServer server) {
        var ingredient1 = FluidIngredient.of(Fluids.WATER, Fluids.LAVA);
        var ingredient2 = FluidIngredient.of(Fluids.LAVA);
        var intersection = IntersectionFluidIngredient.of(ingredient1, ingredient2);

        var ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        var result = FluidIngredient.CODEC.encodeStart(ops, intersection)
                .getOrThrow(error -> new RuntimeException("Failed to serialize ingredient: " + error));

        var deserialized = FluidIngredient.CODEC.decode(ops, result)
                .getOrThrow(error -> new RuntimeException("Failed to deserialize ingredient: " + error))
                .getFirst();

        if (!(deserialized instanceof IntersectionFluidIngredient)) {
            throw new AssertionError("Deserialized ingredient is not an IntersectionFluidIngredient! Got " + deserialized);
        }

        if (deserialized.test(new FluidStack(Fluids.WATER, 1))) {
            throw new AssertionError("Deserialized ingredient should not match water!");
        }
        if (!deserialized.test(new FluidStack(Fluids.LAVA, 1))) {
            throw new AssertionError("Deserialized ingredient should match lava!");
        }
    }
}
