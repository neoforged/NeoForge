/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.arguments.SerializableArgument;
import net.minecraft.commands.suggestions.SuggestionContext;
import net.minecraft.network.chat.Component;
import net.neoforged.testframework.Test;

final class TestResultArgument implements SerializableArgument.ContextFree<Test.Result> {
    public static final TestResultArgument INSTANCE = new TestResultArgument();
    public static final SerializableArgument.Type<TestResultArgument> TYPE = SerializableArgument.singletonType(INSTANCE);
    private static final List<String> VALUES = Arrays.stream(Test.Result.values()).map(Test.Result::name).map(name -> name.toLowerCase(Locale.ROOT)).toList();
    private static final DynamicCommandExceptionType ERROR_INVALID_RESULT = new DynamicCommandExceptionType(
            name -> Component.translatableEscape("argument.testframework.test_result.invalid", name));

    public static ArgumentType<Test.Result> testResult() {
        return INSTANCE.toBrigadier();
    }

    public static Test.Result getTestResult(CommandContext<?> context, String name) {
        return context.getArgument(name, Test.Result.class);
    }

    @Override
    public Test.Result parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String name = reader.readUnquotedString();
        try {
            return Test.Result.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (Throwable t) {
            reader.setCursor(start);
            throw ERROR_INVALID_RESULT.createWithContext(reader, name);
        }
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(CommandContext<? extends SuggestionContext> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return SuggestionContext.suggest(VALUES, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return VALUES;
    }

    @Override
    public Type<TestResultArgument> type() {
        return TYPE;
    }
}
