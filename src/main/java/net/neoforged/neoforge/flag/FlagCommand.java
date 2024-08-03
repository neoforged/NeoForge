/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public interface FlagCommand {
    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("flag")
                .requires(src -> src.hasPermission(Commands.LEVEL_ADMINS))
                .then(Commands.literal("enable")
                        .then(Commands.argument("flag", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    var flags = Flag.flags().map(Flag::identifier);
                                    return SharedSuggestionProvider.suggest(flags, builder);
                                })
                                .executes(ctx -> setFlag(ctx, true))))
                .then(Commands.literal("disable")
                        .then(Commands.argument("flag", StringArgumentType.word())
                                .suggests((ctx, builder) -> {
                                    var flags = ctx.getSource().getServer().getModdedFlagManager().enabledFlags().map(Flag::identifier);
                                    return SharedSuggestionProvider.suggest(flags, builder);
                                })
                                .executes(ctx -> setFlag(ctx, false))));
    }

    private static int setFlag(CommandContext<CommandSourceStack> context, boolean enable) throws CommandSyntaxException {
        var flag = Flag.of(StringArgumentType.getString(context, "flag"));
        var src = context.getSource();
        var flags = src.getServer().getModdedFlagManager();
        var state = state(enable);

        if (flags.set(flag, enable))
            src.sendSuccess(() -> Component.literal(state).append(" Flag: ").append(flag.identifier()), true);
        else
            src.sendSuccess(() -> Component.literal("Flag ").append(flag.identifier()).append(" is already ").append(state), false);

        return Command.SINGLE_SUCCESS;
    }

    private static String state(boolean enable) {
        return enable ? "Enabled" : "Disabled";
    }
}
