/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.arguments.SerializableArgument;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

public class ModIdArgument implements SerializableArgument.ContextFree<String> {
    public static final ModIdArgument INSTANCE = new ModIdArgument();
    public static final SerializableArgument.Type<ModIdArgument> TYPE = new Type<>(MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));
    private static final List<String> EXAMPLES = Arrays.asList("neoforge", "inventorysorter");

    private ModIdArgument() {}

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString();
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext<? extends SuggestionContext> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return SuggestionContext.suggest(ModList.get().applyForEachModContainer(ModContainer::getModId), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public Type<ModIdArgument> type() {
        return TYPE;
    }
}
