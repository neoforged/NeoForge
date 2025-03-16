/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Fires when a player joins the server or when the reload command is ran,
 * before tags and crafting recipes are sent to the client. Send datapack data
 * to clients when this event fires.
 */
public class OnDatapackSyncEvent extends Event {
    private final PlayerList playerList;
    @Nullable
    private final ServerPlayer player;

    private final ReferenceSet<RecipeType<?>> recipeTypesToSend = new ReferenceOpenHashSet<>();

    public OnDatapackSyncEvent(PlayerList playerList, @Nullable ServerPlayer player) {
        this.playerList = playerList;
        this.player = player;
    }

    /**
     * Gets the server's player list, containing all players, when the event fires.
     *
     * @return The server's player list.
     */
    public PlayerList getPlayerList() {
        return this.playerList;
    }

    /**
     * Creates a stream of players that need to receive data during this event, which is the specified player (if present) or all players.
     *
     * @return A stream of players to sync data to.
     */
    public Stream<ServerPlayer> getRelevantPlayers() {
        return this.player == null ? this.playerList.getPlayers().stream() : Stream.of(this.player);
    }

    /**
     * Gets the player that is joining the server, or null when syncing for all players, such as when the reload command runs.
     *
     * @return The player to sync datapacks to. Null when syncing for all players.
     */
    @Nullable
    public ServerPlayer getPlayer() {
        return this.player;
    }

    /**
     * Requests that all recipes of the given types should be sent to the players.
     * 
     * @see net.neoforged.neoforge.client.event.RecipesReceivedEvent
     */
    public void sendRecipes(RecipeType<?>... recipeTypes) {
        Collections.addAll(this.recipeTypesToSend, recipeTypes);
    }

    /**
     * Requests that all recipes of the given types should be sent to the players.
     * 
     * @see net.neoforged.neoforge.client.event.RecipesReceivedEvent
     */
    public void sendRecipes(Iterable<RecipeType<?>> recipeTypes) {
        for (var recipeType : recipeTypes) {
            this.recipeTypesToSend.add(recipeType);
        }
    }

    /**
     * @return The recipe types that have already been requested to be sent to the players.
     */
    public Set<RecipeType<?>> getRecipeTypesToSend() {
        return Collections.unmodifiableSet(recipeTypesToSend);
    }
}
