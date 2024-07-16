/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Objects;
import java.util.function.Predicate;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.ApiStatus;

/**
 * Command provided for easier flag testing.
 * <p>
 * Provided in development workspaces only, not to be used in production builds.
 * <p>
 * Requires op level of {@linkplain Commands#LEVEL_ADMINS}
 * <ul>
 * <li>{@code /neoforge flag list} - Lists all known flags and whether ot not they are enabled</li>
 * <li>{@code /neoforge flag [enable|disable] <flag>} - Enables/Disables the given flag</li>
 * </ul>
 */
@ApiStatus.Internal
public interface FlagCommand {
    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("flag")
                .requires(src -> src.hasPermission(Commands.LEVEL_ADMINS))
                .then(literal("list")
                        .executes(context -> {
                            var src = context.getSource();
                            src.sendSuccess(() -> {
                                var component = Component.literal("Flags: [ ");
                                var known = Flags.getFlags();
                                var i = 0;

                                for (var flag : known) {
                                    var isEnabled = Flags.isEnabled(flag);

                                    component.append(Component.literal(flag.toString())
                                            .withStyle(style -> style
                                                    .withColor(isEnabled ? CommonColors.GREEN : CommonColors.SOFT_RED)
                                                    .withItalic(true)
                                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(flag.toString())
                                                            .append(" [Enabled=")
                                                            .append(isEnabled ? "TRUE" : "FALSE")
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
                                    var disabledFlagNames = Flags.getFlags().stream().filter(Predicate.not(Flags::isEnabled)).map(Objects::toString);
                                    return SharedSuggestionProvider.suggest(disabledFlagNames, builder);
                                })
                                .executes(ctx -> toggleFlag(ctx, true))))
                .then(literal("disable")
                        .then(argument("flag", ResourceLocationArgument.id())
                                .suggests((context, builder) -> {
                                    var enabledFlagNames = Flags.getFlags().stream().filter(Flags::isEnabled).map(Objects::toString);
                                    return SharedSuggestionProvider.suggest(enabledFlagNames, builder);
                                })
                                .executes(ctx -> toggleFlag(ctx, false))));
    }

    private static int toggleFlag(CommandContext<CommandSourceStack> context, boolean enable) throws CommandSyntaxException {
        var flag = context.getArgument("flag", ResourceLocation.class);
        var src = context.getSource();
        var changed = FlagManager.INSTANCE.setEnabled(flag, enable);

        if (changed) {
            src.sendSuccess(() -> Component.literal(enable ? "Enabled flag: " : "Disabled flag: ").append(flag.toString()), false);
            FlagManager.INSTANCE.markDirty(true, true);
            return Command.SINGLE_SUCCESS;
        }

        src.sendSuccess(() -> Component.literal("Flag: ")
                .append(flag.toString())
                .append(" already ")
                .append(enable ? "Enabled" : "Disabled"), false);
        return Command.SINGLE_SUCCESS;
    }
}
