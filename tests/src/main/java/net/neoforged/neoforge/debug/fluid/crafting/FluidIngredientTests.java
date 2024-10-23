/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.fluid.crafting;

import com.google.gson.JsonArray;
import com.mojang.serialization.JsonOps;
import java.util.List;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.CompoundFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.DifferenceFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.IntersectionFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@ForEachTest(groups = { "fluid.crafting", "crafting.ingredient" })
public class FluidIngredientTests {

    // serialization tests
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Serialization tests for empty fluid ingredients")
    static void emptyFluidIngredientSerialization(ExtendedGameTestHelper helper) {
        // analogous to IngredientTests
        // Make sure that empty ingredients serialize to []
        var emptyResult = FluidIngredient.CODEC.encodeStart(JsonOps.INSTANCE, FluidIngredient.empty());
        var emptyJson = emptyResult.resultOrPartial(error -> helper.fail("Failed to serialize empty fluid ingredient: " + error)).orElseThrow();
        helper.assertValueEqual("[]", emptyJson.toString(), "empty fluid ingredient");

        // Make sure that [] deserializes to an empty ingredient
        var result = FluidIngredient.CODEC.parse(JsonOps.INSTANCE, new JsonArray());
        var ingredient = result.resultOrPartial(error -> helper.fail("Failed to deserialize empty fluid ingredient: " + error)).orElseThrow();
        helper.assertTrue(ingredient.isEmpty(), "empty fluid ingredient should return true from isEmpty()");
        helper.assertTrue(ingredient.hasNoFluids(), "empty fluid ingredient should return true from hasNoFluids()");
        helper.assertValueEqual(FluidIngredient.empty(), ingredient, "empty fluid ingredient");
        helper.assertTrue(FluidIngredient.empty() == ingredient, "Reference equality with FluidIngredient.empty()");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Serialization tests for single fluid and tag ingredients")
    static void basicFluidIngredientSerialization(ExtendedGameTestHelper helper) {
        var singleFluid = FluidIngredient.of(Fluids.WATER);
        var tagFluid = FluidIngredient.tag(Tags.Fluids.WATER);

        // tests that the jsons for single and tag fluids do not contain a "type" field
        var singleResult = FluidIngredient.CODEC.encodeStart(JsonOps.INSTANCE, singleFluid);
        var singleJson = singleResult.resultOrPartial(error -> helper.fail("Failed to serialize single fluid ingredient: " + error)).orElseThrow();

        var tagResult = FluidIngredient.CODEC.encodeStart(JsonOps.INSTANCE, tagFluid);
        var tagJson = tagResult.resultOrPartial(error -> helper.fail("Failed to serialize tag fluid ingredient: " + error)).orElseThrow();

        helper.assertFalse(singleJson.getAsJsonObject().has("type"), "single fluid ingredient should serialize without a 'type' field");
        helper.assertFalse(tagJson.getAsJsonObject().has("type"), "tag fluid ingredient should serialize without a 'type' field");

        helper.assertValueEqual(singleJson.toString(), "{\"fluid\":\"minecraft:water\"}", "serialized single fluid ingredient to match expected format!");
        helper.assertValueEqual(tagJson.toString(), "{\"tag\":\"c:water\"}", "serialized tag fluid ingredient to match expected format!");

        // tests that deserializing simple ingredients is reproducible and produces the desired ingredients
        var singleTwo = FluidIngredient.CODEC.parse(JsonOps.INSTANCE, singleJson)
                .resultOrPartial(error -> helper.fail("Failed to deserialize single fluid ingredient from JSON: " + error))
                .orElseThrow();
        helper.assertValueEqual(singleFluid, singleTwo, "single fluid ingredient to be the same after being serialized and deserialized!");

        var tagTwo = FluidIngredient.CODEC.parse(JsonOps.INSTANCE, tagJson)
                .resultOrPartial(error -> helper.fail("Failed to deserialize single fluid ingredient from JSON: " + error))
                .orElseThrow();
        helper.assertValueEqual(tagFluid, tagTwo, "tag fluid ingredient to be the same after being serialized and deserialized!");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that custom ingredients are not empty")
    static void testFluidIngredientEmptiness(final GameTestHelper helper) {
        FluidIngredient compoundIngredient = CompoundFluidIngredient.of(FluidIngredient.of(Fluids.WATER), FluidIngredient.tag(Tags.Fluids.LAVA));
        helper.assertFalse(compoundIngredient.isEmpty(), "CompoundFluidIngredient should not be empty");
        FluidIngredient dataComponentIngredient = DataComponentFluidIngredient.of(false, DataComponentMap.EMPTY, Fluids.WATER);
        helper.assertFalse(dataComponentIngredient.isEmpty(), "DataComponentFluidIngredient should not be empty");
        FluidIngredient differenceIngredient = DifferenceFluidIngredient.of(FluidIngredient.tag(Tags.Fluids.MILK), FluidIngredient.of(Fluids.LAVA));
        helper.assertFalse(differenceIngredient.isEmpty(), "DifferenceFluidIngredient should not be empty");
        FluidIngredient intersectionIngredient = IntersectionFluidIngredient.of(FluidIngredient.of(Fluids.WATER, Fluids.LAVA), FluidIngredient.of(Fluids.WATER));
        helper.assertFalse(intersectionIngredient.isEmpty(), "IntersectionFluidIngredient should not be empty");
        FluidIngredient emptyIngredient = FluidIngredient.empty();
        helper.assertTrue(emptyIngredient.isEmpty(), "FluidIngredient.empty() should be empty!");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that custom ingredients correctly report hasNoFluids")
    static void customFluidIngredientsHasNoFluids(final GameTestHelper helper) {
        // these can all end up accidentally empty one way or another
        FluidIngredient dataComponentIngredient = DataComponentFluidIngredient.of(false, DataComponentMap.EMPTY, new Fluid[0]);
        helper.assertFalse(dataComponentIngredient.isEmpty(), "DataComponentFluidIngredient instance should not be empty");
        helper.assertTrue(dataComponentIngredient.hasNoFluids(), "DataComponentFluidIngredient with no matching fluids should return true on hasNoFluids()");
        FluidIngredient differenceIngredient = DifferenceFluidIngredient.of(FluidIngredient.tag(Tags.Fluids.WATER), FluidIngredient.tag(Tags.Fluids.WATER));
        helper.assertFalse(differenceIngredient.isEmpty(), "DifferenceFluidIngredient instance should not be empty");
        helper.assertTrue(differenceIngredient.hasNoFluids(), "DifferenceFluidIngredient with empty difference should return true on hasNoFluids()");
        FluidIngredient intersectionIngredient = IntersectionFluidIngredient.of(FluidIngredient.of(Fluids.LAVA), FluidIngredient.of(Fluids.WATER));
        helper.assertFalse(intersectionIngredient.isEmpty(), "IntersectionFluidIngredient instance should not be empty");
        helper.assertTrue(intersectionIngredient.hasNoFluids(), "IntersectionFluidIngredient with empty intersection should return true on hasNoFluids()");

        // these classes have checks in place to make sure they aren't populated with empty values
        var emptyCompoundFailed = false;
        try {
            FluidIngredient compoundIngredient = new CompoundFluidIngredient(List.of());
        } catch (Exception ignored) {
            emptyCompoundFailed = true;
        }
        helper.assertTrue(emptyCompoundFailed, "Empty CompoundFluidIngredient should not have been able to be constructed!");

        var emptySingleFailed = false;
        try {
            FluidIngredient compoundIngredient = FluidIngredient.single(Fluids.EMPTY);
        } catch (Exception ignored) {
            emptySingleFailed = true;
        }
        helper.assertTrue(emptySingleFailed, "Empty SingleFluidIngredient should not have been able to be constructed!");

        helper.assertValueEqual(CompoundFluidIngredient.of(new FluidIngredient[0]), FluidIngredient.empty(), "calling CompoundFluidIngredient.of with no children to yield FluidIngredient.empty()");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that partial data matches work correctly on fluid ingredients")
    static void fluidIngredientDataPartialMatchWorks(final GameTestHelper helper) {
        var ingredient = DataComponentFluidIngredient.of(false, DataComponents.RARITY, Rarity.EPIC, Fluids.WATER);
        var stack = new FluidStack(Fluids.WATER, 1000);

        helper.assertFalse(ingredient.test(stack), "Fluid without custom data should not match DataComponentFluidIngredient!");

        stack.applyComponents(DataComponentPatch.builder()
                .set(DataComponents.RARITY, Rarity.UNCOMMON)
                .build());

        helper.assertFalse(ingredient.test(stack), "Fluid with incorrect data should not match DataComponentFluidIngredient!");

        stack.applyComponents(DataComponentPatch.builder()
                .set(DataComponents.RARITY, Rarity.EPIC)
                .build());

        helper.assertTrue(ingredient.test(stack), "Fluid with correct data should match DataComponentFluidIngredient!");

        var data = CustomData.EMPTY.update(tag -> tag.putFloat("abcd", helper.getLevel().random.nextFloat()));
        stack.applyComponents(DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_DATA, data)
                .build());

        helper.assertTrue(ingredient.test(stack), "Fluid with correct data should match partial DataComponentFluidIngredient regardless of extra data!");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that strict data matches work correctly on fluid ingredients")
    static void fluidIngredientDataStrictMatchWorks(final GameTestHelper helper) {
        var ingredient = DataComponentFluidIngredient.of(true, DataComponents.RARITY, Rarity.EPIC, Fluids.WATER);
        var stack = new FluidStack(Fluids.WATER, 1000);

        helper.assertFalse(ingredient.test(stack), "Fluid without custom data should not match DataComponentFluidIngredient!");

        stack.applyComponents(DataComponentPatch.builder()
                .set(DataComponents.RARITY, Rarity.UNCOMMON)
                .build());

        helper.assertFalse(ingredient.test(stack), "Fluid with incorrect data should not match DataComponentFluidIngredient!");

        stack.applyComponents(DataComponentPatch.builder()
                .set(DataComponents.RARITY, Rarity.EPIC)
                .build());

        helper.assertTrue(ingredient.test(stack), "Fluid with correct data should match DataComponentFluidIngredient!");

        var data = CustomData.EMPTY.update(tag -> tag.putFloat("abcd", helper.getLevel().random.nextFloat()));
        stack.applyComponents(DataComponentPatch.builder()
                .set(DataComponents.CUSTOM_DATA, data)
                .build());

        helper.assertFalse(ingredient.test(stack), "Fluid with extra unspecified data should not match strict DataComponentFluidIngredient!");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that size and data components do not matter when matching fluid ingredients")
    static void singleFluidIngredientIgnoresSizeAndData(final GameTestHelper helper) {
        var ingredient = FluidIngredient.of(Fluids.WATER);

        helper.assertTrue(ingredient.test(new FluidStack(Fluids.WATER, 1234)), "Single fluid ingredient should match regardless of fluid amount!");
        helper.assertTrue(ingredient.test(new FluidStack(Fluids.WATER.builtInRegistryHolder(), 1234, DataComponentPatch.builder().set(DataComponents.RARITY, Rarity.COMMON).build())), "Single fluid ingredient should match regardless of fluid data!");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests serialization format of sized fluid ingredients")
    static void sizedFluidIngredientSerialization(final GameTestHelper helper) {
        var sized = SizedFluidIngredient.of(Fluids.WATER, 1000);

        var flatResult = SizedFluidIngredient.FLAT_CODEC.encodeStart(JsonOps.INSTANCE, sized);
        var flatJson = flatResult.resultOrPartial((error) -> helper.fail("(flat) Error while encoding SizedFluidIngredient: " + error)).orElseThrow();

        helper.assertValueEqual(flatJson.toString(), "{\"fluid\":\"minecraft:water\",\"amount\":1000}", "(flat) serialized SizedFluidIngredient");

        var nestedResult = SizedFluidIngredient.NESTED_CODEC.encodeStart(JsonOps.INSTANCE, sized);
        var nestedJson = nestedResult.resultOrPartial((error) -> helper.fail("(nested) Error while encoding SizedFluidIngredient: " + error)).orElseThrow();

        helper.assertValueEqual(nestedJson.toString(), "{\"ingredient\":{\"fluid\":\"minecraft:water\"},\"amount\":1000}", "(nested) serialized SizedFluidIngredient");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests matching of sized fluid ingredients")
    static void sizedFluidIngredientMatching(final GameTestHelper helper) {
        var sized = SizedFluidIngredient.of(Fluids.WATER, 2);

        helper.assertFalse(sized.test(new FluidStack(Fluids.LAVA, 1000)), "SizedFluidIngredient should not match incorrect fluid!");

        helper.assertFalse(sized.test(new FluidStack(Fluids.WATER, 1)), "SizedFluidIngredient should not match fluid with less than required amount!");
        helper.assertTrue(sized.test(new FluidStack(Fluids.WATER, 2)), "SizedFluidIngredient should match fluid with required amount!");
        helper.assertTrue(sized.test(new FluidStack(Fluids.WATER, 3)), "SizedFluidIngredient should  match fluid with more than required amount!");

        var matches = sized.getFluids();
        helper.assertTrue(matches.length == 1 && (FluidStack.matches(matches[0], new FluidStack(Fluids.WATER, 2))), "SizedFluidIngredient matches should return all matched fluids with the correct amount!");

        helper.succeed();
    }
}
