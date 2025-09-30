/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.Nullable;

/**
 * overrides for {@link CommandSourceStack} so that the methods will run successfully client side
 */
public class ClientCommandSourceStack extends CommandSourceStack {
    public ClientCommandSourceStack(CommandSource source, Vec3 position, Vec2 rotation, int permission, String plainTextName, Component displayName,
            Entity executing) {
        super(source, position, rotation, null, permission, plainTextName, displayName, null, executing);
    }

    /**
     * {@return the current connection, used to shorten method calls and hide the nullability warnings}
     */
    private ClientPacketListener connection() {
        return Minecraft.getInstance().getConnection();
    }

    /**
     * Sends a success message without attempting to get the server side list of admins
     */
    @Override
    public void sendSuccess(Supplier<Component> message, boolean sendToAdmins) {
        //Don't send the message to admins, as that requires querying the server for the list of admins, and would cause a NPE
        super.sendSuccess(message, false);
    }

    /**
     * {@return the list of teams from the client side}
     */
    @Override
    public Collection<String> getAllTeams() {
        return getScoreboard().getTeamNames();
    }

    /**
     * {@return the list of online player names from the client side}
     */
    @Override
    public Collection<String> getOnlinePlayerNames() {
        return connection().getOnlinePlayers().stream().map(player -> player.getProfile().name()).collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<Suggestions> suggestRegistryElements(
            ResourceKey<? extends Registry<?>> registry,
            SharedSuggestionProvider.ElementSuggestionType suggestionType,
            SuggestionsBuilder suggestionsBuilder,
            CommandContext<?> context) {
        if (registry == Registries.RECIPE) {
            // TODO 1.21.2: Not sure what to do here as the client doesn't receive recipe names. Letting super get called will cause an NPE on this.server.
            return Suggestions.empty();
        } else if (registry == Registries.ADVANCEMENT) {
            //Only suggest from advancements that are visible to the player
            return SharedSuggestionProvider.suggestResource(connection().getAdvancements().getTree().nodes().stream().map(node -> node.holder().id()), suggestionsBuilder);
        }
        return super.suggestRegistryElements(registry, suggestionType, suggestionsBuilder, context);
    }

    /**
     * {@return a set of {@link ResourceKey} for levels from the client side}
     */
    @Override
    public Set<ResourceKey<Level>> levels() {
        return connection().levels();
    }

    /**
     * {@return the {@link RegistryAccess} from the client side}
     */
    @Override
    public RegistryAccess registryAccess() {
        return connection().registryAccess();
    }

    /**
     * {@return the {@link FeatureFlagSet} from the client side}
     */
    @Override
    public FeatureFlagSet enabledFeatures() {
        return connection().enabledFeatures();
    }

    /**
     * {@return the scoreboard from the client side}
     */
    @Override
    public Scoreboard getScoreboard() {
        return connection().scoreboard();
    }

    /**
     * {@return the advancement from the id from the client side where the advancement needs to be visible to the player}
     */
    @Override
    @Nullable
    public AdvancementHolder getAdvancement(ResourceLocation id) {
        return connection().getAdvancements().get(id);
    }

    /**
     * {@return the level from the client side}
     */
    @Override
    public Level getUnsidedLevel() {
        return connection().getLevel();
    }

    /**
     * @throws UnsupportedOperationException
     *                                       because the server isn't available on the client
     */
    @Override
    public MinecraftServer getServer() {
        throw new UnsupportedOperationException("Attempted to get server in client command");
    }

    /**
     * @throws UnsupportedOperationException
     *                                       because the server side level isn't available on the client side
     */
    @Override
    public ServerLevel getLevel() {
        throw new UnsupportedOperationException("Attempted to get server level in client command");
    }
}
