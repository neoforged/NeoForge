/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.fluid.crafting;

import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

// TODO(max): move rest of these to unit tests!
@ForEachTest(groups = { "fluid.crafting", "crafting.ingredient" })
public class FluidIngredientTests {

    // serialization tests
    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Serialization tests for single fluid and tag ingredients")
    static void basicFluidIngredientSerialization(ExtendedGameTestHelper helper) {
        var registryAccess = helper.getLevel().registryAccess();
        var ops = registryAccess.createSerializationContext(JsonOps.INSTANCE);

        var singleFluid = FluidIngredient.of(Fluids.WATER);
        var tagFluid = FluidIngredient.of(BuiltInRegistries.FLUID.getOrThrow(Tags.Fluids.WATER));

        // tests that the jsons for single and tag fluids do not contain a "type" field
        var singleResult = FluidIngredient.CODEC.encodeStart(ops, singleFluid);
        var singleJson = singleResult.resultOrPartial(error -> helper.fail("Failed to serialize single fluid ingredient: " + error)).orElseThrow();

        var tagResult = FluidIngredient.CODEC.encodeStart(ops, tagFluid);
        var tagJson = tagResult.resultOrPartial(error -> helper.fail("Failed to serialize tag fluid ingredient: " + error)).orElseThrow();

        helper.assertFalse(singleJson.isJsonObject(), "single fluid ingredient should not serialize as nested object!");
        helper.assertFalse(tagJson.isJsonObject(), "tag fluid ingredient should not serialize as nested object!");

        helper.assertValueEqual(singleJson.getAsString(), Fluids.WATER.builtInRegistryHolder().getRegisteredName(), "serialized single fluid ingredient to match HolderSet element format!");
        helper.assertValueEqual(tagJson.getAsString(), "#" + Tags.Fluids.WATER.location(), "serialized tag fluid ingredient to match HolderSet tag format!");

        // tests that deserializing simple ingredients is reproducible and produces the desired ingredients
        var singleTwo = FluidIngredient.CODEC.parse(ops, singleJson)
                .resultOrPartial(error -> helper.fail("Failed to deserialize single fluid ingredient from JSON: " + error))
                .orElseThrow();
        helper.assertValueEqual(singleFluid, singleTwo, "single fluid ingredient to be the same after being serialized and deserialized!");

        var tagTwo = FluidIngredient.CODEC.parse(ops, tagJson)
                .resultOrPartial(error -> helper.fail("Failed to deserialize single fluid ingredient from JSON: " + error))
                .orElseThrow();
        helper.assertValueEqual(tagFluid, tagTwo, "tag fluid ingredient to be the same after being serialized and deserialized!");

        var nestedSimpleFailed = FluidIngredient.CODEC.parse(ops, Util.make(new JsonObject(), json1 -> {
            json1.addProperty("neoforge:ingredient_type", NeoForgeMod.SIMPLE_FLUID_INGREDIENT_TYPE.getId().toString());
            json1.addProperty("fluid", "minecraft:water");
        })).isError();

        helper.assertTrue(nestedSimpleFailed, "Nested SimpleFluidIngredient should not have been deserialized from map!");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests serialization format of sized fluid ingredients")
    static void sizedFluidIngredientSerialization(final ExtendedGameTestHelper helper) {
        var sized = SizedFluidIngredient.of(Fluids.WATER, 1000);

        var nestedResult = SizedFluidIngredient.CODEC.encodeStart(JsonOps.INSTANCE, sized);
        var nestedJson = nestedResult.resultOrPartial((error) -> helper.fail("Error while encoding SizedFluidIngredient: " + error)).orElseThrow();

        helper.assertValueEqual(nestedJson.toString(), "{\"ingredient\":\"minecraft:water\",\"amount\":1000}", "(nested) serialized SizedFluidIngredient");

        helper.succeed();
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests matching of sized fluid ingredients")
    static void sizedFluidIngredientMatching(final ExtendedGameTestHelper helper) {
        var sized = SizedFluidIngredient.of(Fluids.WATER, 2);

        helper.assertFalse(sized.test(new FluidStack(Fluids.LAVA, 1000)), "SizedFluidIngredient should not match incorrect fluid!");

        helper.assertFalse(sized.test(new FluidStack(Fluids.WATER, 1)), "SizedFluidIngredient should not match fluid with less than required amount!");
        helper.assertTrue(sized.test(new FluidStack(Fluids.WATER, 2)), "SizedFluidIngredient should match fluid with required amount!");
        helper.assertTrue(sized.test(new FluidStack(Fluids.WATER, 3)), "SizedFluidIngredient should  match fluid with more than required amount!");

        // TODO(max): implement display wrapping for sized ingredients(?)
        //var matches = sized.getFluids();
        //helper.assertTrue(matches.length == 1 && (FluidStack.matches(matches[0], new FluidStack(Fluids.WATER, 2))), "SizedFluidIngredient matches should return all matched fluids with the correct amount!");

        helper.succeed();
    }
}
