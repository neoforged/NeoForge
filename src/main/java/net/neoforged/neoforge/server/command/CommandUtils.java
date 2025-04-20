/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

/**
 * Utility class for various command-related operations.
 * <p>
 * <strong>For modders and NeoForge to both use.</strong>
 */
public final class CommandUtils {
    private CommandUtils() {}

    public static CompletableFuture<Suggestions> suggestRegistries(
            final CommandContext<CommandSourceStack> ctx,
            final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(ctx.getSource().registryAccess().listRegistryKeys().map(ResourceKey::location), builder);
    }

    public static <T extends Registry<?>> SuggestionProvider<CommandSourceStack> suggestFromRegistry(
            final Function<Registry<?>, Iterable<ResourceLocation>> namesFunction,
            final String argumentString,
            final ResourceKey<Registry<T>> registryKey) {
        return (ctx, builder) -> CommandUtils.getResourceKey(ctx, argumentString, registryKey)
                .flatMap(key -> ctx.getSource().registryAccess().lookup(key).map(registry -> {
                    SharedSuggestionProvider.suggestResource(namesFunction.apply(registry), builder);
                    return builder.buildFuture();
                }))
                .orElseGet(builder::buildFuture);
    }

    @SuppressWarnings("SameParameterValue")
    public static <T> Optional<ResourceKey<T>> getResourceKey(
            final CommandContext<CommandSourceStack> ctx,
            final String name,
            final ResourceKey<Registry<T>> registryKey) {
        // Don't inline to avoid an unchecked cast warning due to raw types
        final ResourceKey<?> key = ctx.getArgument(name, ResourceKey.class);
        return key.cast(registryKey);
    }

    public static MutableComponent makeTranslatableWithFallback(String key, Object... args) {
        String fallback = Language.getInstance().getOrDefault(key);
        return Component.translatableWithFallback(key, fallback, args);
    }

    public static MutableComponent makeTranslatableWithFallback(String key) {
        String fallback = Language.getInstance().getOrDefault(key);
        return Component.translatableWithFallback(key, fallback);
    }
}
