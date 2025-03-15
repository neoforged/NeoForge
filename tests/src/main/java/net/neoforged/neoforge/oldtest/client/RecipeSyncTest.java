/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.client;

import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

@Mod(value = RecipeSyncTest.MOD_ID, dist = Dist.CLIENT)
public class RecipeSyncTest {
    private static final boolean ENABLED = true;
    static final String MOD_ID = "recipe_sync_test";

    // request two particular recipe types but not the others
    private static final Set<RecipeType<?>> RECIPE_TYPES = Set.of(
            RecipeType.BLASTING, RecipeType.SMOKING);

    public RecipeSyncTest() {
        if (ENABLED) {
            // This handler would obviously go on the server-side
            NeoForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> {
                event.sendRecipes(RECIPE_TYPES);
            });
            NeoForge.EVENT_BUS.addListener((RecipesReceivedEvent event) -> {
                if (!event.getRecipeTypes().equals(RECIPE_TYPES)) {
                    throw new AssertionError("Expected to receive recipe types " + RECIPE_TYPES + " but got: " + event.getRecipeTypes());
                }

                // This assumes there actually are any recipes for BLASTING and SMOKING, which is the case in Vanilla
                var recipesForTypes = event.getRecipeMap().values().stream().map(r -> r.value().getType()).collect(Collectors.toSet());
                if (!recipesForTypes.equals(RECIPE_TYPES)) {
                    throw new AssertionError("Expected to receive recipes only for " + RECIPE_TYPES + ", but got recipes for: " + recipesForTypes);
                }
                Minecraft.getInstance().gui.getChat().addMessage(Component.literal("Received " + event.getRecipeMap().values().size() + " recipes from server"));
            });
        }
    }
}
