/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.testframework.impl;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.server.command.EnumArgument;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.group.Group;
import net.neoforged.testframework.group.Groupable;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record Commands(TestFrameworkInternal framework) {
    public void register(LiteralArgumentBuilder<CommandSourceStack> node) {
        final BiFunction<LiteralArgumentBuilder<CommandSourceStack>, Boolean, LiteralArgumentBuilder<CommandSourceStack>> commandEnabling = (stack, enabling) -> stack.requires(it -> it.hasPermission(framework.configuration().commandRequiredPermission()))
                .then(argument("id", StringArgumentType.greedyString())
                        .suggests(suggestGroupable(it -> !(it instanceof Test test) || framework.tests().isEnabled(test.id()) != enabling))
                        .executes(ctx -> {
                            final String id = StringArgumentType.getString(ctx, "id");
                            parseGroupable(ctx.getSource(), id, group -> {
                                final List<Test> all = group.resolveAll();
                                if (all.stream().allMatch(it -> framework.tests().isEnabled(it.id()) == enabling)) {
                                    ctx.getSource().sendFailure(Component.literal("All tests in group are " + (enabling ? "enabled" : "disabled") + "!"));
                                } else {
                                    all.forEach(test -> framework.setEnabled(test, enabling, ctx.getSource().getEntity()));
                                    ctx.getSource().sendSuccess(() -> Component.literal((enabling ? "Enabled" : "Disabled") + " test group!"), true);
                                }
                            }, test -> {
                                if (framework.tests().isEnabled(id) == enabling) {
                                    ctx.getSource().sendFailure(Component.literal("Test is already " + (enabling ? "enabled" : "disabled") + "!"));
                                } else {
                                    framework.setEnabled(framework.tests().byId(id).orElseThrow(), enabling, ctx.getSource().getEntity());
                                    ctx.getSource().sendSuccess(() -> Component.literal((enabling ? "Enabled" : "Disabled") + " test!"), true);
                                }
                            });
                            return Command.SINGLE_SUCCESS;
                        }));

        node.then(commandEnabling.apply(literal("enable"), true));
        node.then(commandEnabling.apply(literal("disable"), false));

        node.then(literal("status")
                .then(literal("get")
                        .then(argument("id", StringArgumentType.greedyString())
                                .suggests(suggestTest(test -> framework.tests().isEnabled(test.id())))
                                .executes(ctx -> {
                                    final String id = StringArgumentType.getString(ctx, "id");
                                    parseGroupable(ctx.getSource(), id,
                                            group -> ctx.getSource().sendFailure(Component.literal("This command does not support groups!")),
                                            test -> ctx.getSource().sendSuccess(
                                                    () -> Component.literal("Status of test '").append(id).append("' is: ").append(formatStatus(framework.tests().getStatus(id))), true));
                                    return Command.SINGLE_SUCCESS;
                                })))
                .then(literal("set")
                        .requires(it -> it.hasPermission(framework.configuration().commandRequiredPermission()))
                        .then(argument("id", StringArgumentType.string())
                                .suggests(suggestTest(test -> framework.tests().isEnabled(test.id())))
                                .then(argument("result", EnumArgument.enumArgument(Test.Result.class))
                                        .executes(it -> processSetStatus(it, ""))
                                        .then(argument("message", StringArgumentType.greedyString())
                                                .executes(it -> processSetStatus(it, StringArgumentType.getString(it, "message"))))))));
    }

    private <T> SuggestionProvider<T> suggestGroupable(Predicate<Groupable> predicate) {
        return (context, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
            Stream.concat(
                    framework.tests().all().stream(),
                    framework.tests().allGroups().stream())
                    .filter(predicate)
                    .map(groupable -> groupable instanceof Test test ? test.id() : "g:" + ((Group) groupable).id())
                    .filter(it -> it.toLowerCase(Locale.ROOT).startsWith(remaining))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    private <T> SuggestionProvider<T> suggestTest(Predicate<Test> predicate) {
        return (context, builder) -> {
            String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
            framework.tests().all().stream()
                    .filter(predicate)
                    .map(Test::id)
                    .filter(it -> it.toLowerCase(Locale.ROOT).startsWith(remaining))
                    .forEach(builder::suggest);
            return builder.buildFuture();
        };
    }

    private void parseGroupable(CommandSourceStack stack, String id, Consumer<Group> isGroup, Consumer<Test> isTest) {
        if (id.startsWith("g:")) {
            final String grId = id.substring(2);
            framework.tests().maybeGetGroup(grId).ifPresentOrElse(isGroup, () -> stack.sendFailure(Component.literal("Unknown test group with id '%s'!".formatted(grId))));
        } else {
            framework.tests().byId(id).ifPresentOrElse(isTest, () -> stack.sendFailure(Component.literal("Unknown test with id '%s'!".formatted(id))));
        }
    }

    private Component formatStatus(Test.Status status) {
        final MutableComponent resultComponent = Component.literal(status.result().toString()).withStyle(style -> style.withColor(status.result().getColour()));
        if (status.message().isBlank()) {
            return resultComponent;
        } else {
            return resultComponent.append(" - ").append(Component.literal(status.message()).withStyle(ChatFormatting.AQUA));
        }
    }

    private int processSetStatus(CommandContext<CommandSourceStack> ctx, String message) {
        final String id = StringArgumentType.getString(ctx, "id");
        parseGroupable(ctx.getSource(), id,
                group -> ctx.getSource().sendFailure(Component.literal("This command does not support groups!")),
                test -> {
                    final Test.Result result = ctx.getArgument("result", Test.Result.class);
                    final Test.Status status = new Test.Status(result, message);
                    framework.changeStatus(test, status, ctx.getSource().getEntity());
                    ctx.getSource().sendSuccess(
                            () -> Component.literal("Status of test '").append(id).append("' has been changed to: ").append(formatStatus(status)), true);
                });
        return Command.SINGLE_SUCCESS;
    }
}
