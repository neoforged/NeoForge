/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.event;

import java.util.Set;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * This event is fired on the client when it has finished receiving recipe data
 * from the server. This will be the case shortly after first entering the world,
 * and whenever the server decides to reload its datapacks. Note that recipe data
 * will only be sent for recipe types requested using {@link net.neoforged.neoforge.event.OnDatapackSyncEvent#sendRecipes}
 * on the server-side.
 *
 * <p>You should clean up any data you kept from this event when the player disconnects,
 * for example when {@link ClientPlayerNetworkEvent.LoggingOut} is fired.
 *
 * <p>These events are fired on the {@linkplain NeoForge#EVENT_BUS main event bus},
 * only on the {@linkplain LogicalSide#CLIENT logical client}.</p>
 */
public class RecipesReceivedEvent extends Event {
    private final Set<RecipeType<?>> recipeTypes;
    private final RecipeMap recipeMap;

    @ApiStatus.Internal
    public RecipesReceivedEvent(Set<RecipeType<?>> recipeTypes, RecipeMap recipeMap) {
        this.recipeTypes = Set.copyOf(recipeTypes);
        this.recipeMap = recipeMap;
    }

    /**
     * @return The recipe types that were requested by mods on the server to be sent to the client. This may
     *         be a subset of available recipes types or even empty if no mods requested recipes to be sent.
     */
    public Set<RecipeType<?>> getRecipeTypes() {
        return recipeTypes;
    }

    /**
     * @return The recipes received from the server.
     */
    public RecipeMap getRecipeMap() {
        return recipeMap;
    }
}
