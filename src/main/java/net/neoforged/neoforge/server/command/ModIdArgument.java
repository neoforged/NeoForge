/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

public class ModIdArgument implements ArgumentType<String> {
    private static final List<String> EXAMPLES = Arrays.asList("neoforge", "inventorysorter");

    public static ModIdArgument modIdArgument() {
        return new ModIdArgument();
    }

    @Override
    public String parse(final StringReader reader) throws CommandSyntaxException {
        return reader.readUnquotedString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(ModList.get().applyForEachModContainer(ModContainer::getModId), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
