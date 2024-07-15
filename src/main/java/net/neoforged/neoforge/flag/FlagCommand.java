/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;

public interface FlagCommand {
    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("flag")
                .requires(src -> src.hasPermission(Commands.LEVEL_ADMINS))
                .then(literal("list")
                        .executes(context -> {
                            var src = context.getSource();
                            src.sendSuccess(() -> {
                                var component = Component.literal("Flags: [ ");
                                var known = FlagManager.getKnownFlags();
                                var i = 0;

                                for (var flag : known) {
                                    component.append(Component.literal(flag.toString())
                                            .withStyle(style -> style
                                                    .withItalic(true)
                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(flag.toString())
                                                            .append(" [Enabled=")
                                                            .append(FlagManager.isEnabled(flag) ? "TRUE" : "FALSE")
                                                            .append("]")))));

                                    if (i + 1 < known.size())
                                        component.append(", ");

                                    i++;
                                }

                                return component.append(" ]");
                            }, false);
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(literal("enable")
                        .then(argument("flag", ResourceLocationArgument.id())
                                .suggests((context, builder) -> {
                                    var knownFlagNames = FlagManager.getKnownFlags().stream().filter(Predicate.not(FlagManager::isEnabled)).map(Objects::toString);
                                    return SharedSuggestionProvider.suggest(knownFlagNames, builder);
                                })
                                .executes(ctx -> {
                                    var flag = ctx.getArgument("flag", ResourceLocation.class);
                                    var src = ctx.getSource();
                                    FlagManager.INSTANCE.setEnabled(flag, true);
                                    src.sendSuccess(() -> Component.literal("Enabled flag: ").append(flag.toString()), false);
                                    return SINGLE_SUCCESS;
                                })))
                .then(literal("disable")
                        .then(argument("flag", ResourceLocationArgument.id())
                                .suggests((context, builder) -> {
                                    var enabledFlagNames = FlagManager.getKnownFlags().stream().filter(FlagManager::isEnabled).map(Objects::toString);
                                    return SharedSuggestionProvider.suggest(enabledFlagNames, builder);
                                })
                                .executes(ctx -> {
                                    var flag = ctx.getArgument("flag", ResourceLocation.class);
                                    var src = ctx.getSource();
                                    FlagManager.INSTANCE.setEnabled(flag, false);
                                    src.sendSuccess(() -> Component.literal("Disabled flag: ").append(flag.toString()), false);
                                    return SINGLE_SUCCESS;
                                })));
    }
}
