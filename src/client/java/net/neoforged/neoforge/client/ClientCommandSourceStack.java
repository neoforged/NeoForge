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
     * Sends a success message without attempting to get the server side list of admins
     */
    @Override
    public void sendSuccess(Supplier<Component> message, boolean sendToAdmins) {
        Minecraft.getInstance().gui.getChat().addMessage(message.get());
    }

    /**
     * {@return the list of teams from the client side}
     */
    @Override
    public Collection<String> getAllTeams() {
        return Minecraft.getInstance().level.getScoreboard().getTeamNames();
    }

    /**
     * {@return the list of online player names from the client side}
     */
    @Override
    public Collection<String> getOnlinePlayerNames() {
        return Minecraft.getInstance().getConnection().getOnlinePlayers().stream().map((player) -> player.getProfile().getName()).collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<Suggestions> suggestRegistryElements(
            ResourceKey<? extends Registry<?>> p_212330_,
            SharedSuggestionProvider.ElementSuggestionType p_212331_,
            SuggestionsBuilder p_212332_,
            CommandContext<?> p_212333_) {
        // TODO 1.21.2: Not sure what to do here. Letting super get called will cause an NPE on this.server.
        if (p_212330_ == Registries.RECIPE || p_212330_ == Registries.ADVANCEMENT) {
            return Suggestions.empty();
        }
        return super.suggestRegistryElements(p_212330_, p_212331_, p_212332_, p_212333_);
    }

    /**
     * {@return a set of {@link ResourceKey} for levels from the client side}
     */
    @Override
    public Set<ResourceKey<Level>> levels() {
        return Minecraft.getInstance().getConnection().levels();
    }

    /**
     * {@return the {@link RegistryAccess} from the client side}
     */
    @Override
    public RegistryAccess registryAccess() {
        return Minecraft.getInstance().getConnection().registryAccess();
    }

    /**
     * {@return the scoreboard from the client side}
     */
    @Override
    public Scoreboard getScoreboard() {
        return Minecraft.getInstance().level.getScoreboard();
    }

    /**
     * {@return the advancement from the id from the client side where the advancement needs to be visible to the player}
     */
    @Override
    @Nullable
    public AdvancementHolder getAdvancement(ResourceLocation id) {
        return Minecraft.getInstance().getConnection().getAdvancements().get(id);
    }

    /**
     * {@return the level from the client side}
     */
    @Override
    public Level getUnsidedLevel() {
        return Minecraft.getInstance().level;
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
