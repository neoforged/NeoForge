/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.resources;

import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;

@ForEachTest(groups = ModDatapackTest.GROUP)
public class ModDatapackTest {
    public static final String GROUP = "resources";

    @TestHolder(description = "Tests that mod datapacks are loaded properly on initial load and reload", enabledByDefault = true)
    static void modDatapack(final DynamicTest test) {
        final ResourceLocation testAdvancement = new ResourceLocation(test.createModId(), "recipes/misc/test_advancement");

        test.registrationHelper().addProvider(event -> {
            List<AdvancementProvider.AdvancementGenerator> generators = List.of((registries, saver, existingFileHelper) -> Advancement.Builder.recipeAdvancement()
                    .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT)
                    .addCriterion("has_scute", CriteriaTriggers.INVENTORY_CHANGED.createCriterion(
                            new InventoryChangeTrigger.TriggerInstance(
                                    Optional.empty(), InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of(
                                            ItemPredicate.Builder.item().of(Items.TURTLE_SCUTE).build()))))
                    .rewards(AdvancementRewards.Builder.recipe(new ResourceLocation("minecraft:turtle_helmet")))
                    .save(saver, testAdvancement, existingFileHelper));
            return new AdvancementProvider(event.getGenerator().getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper(), generators);
        });

        test.eventListeners().forge().addListener((OnDatapackSyncEvent event) -> {
            if (event.getPlayerList().getServer().getAdvancements().get(testAdvancement) != null) {
                test.pass();
            } else {
                test.fail("Test advancement not loaded");
            }
        });
    }
}
