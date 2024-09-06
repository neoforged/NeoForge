/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.flag;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.google.common.collect.Sets;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface FlagCommand {
    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("flag")
                .executes(FlagCommand::listCommands)
                .then(stateSubCommand(true))
                .then(stateSubCommand(false));
    }

    private static int listCommands(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var src = context.getSource();
        var flagManager = src.getServer().getModdedFlagManager();
        var knownFlags = Flag.getFlags();
        var enabledFlags = flagManager.getEnabledFlags();
        var disabledFlags = Sets.difference(knownFlags, enabledFlags);
        // TODO: localize these
        var enabledFlagsStr = enabledFlags.stream().map(Flag::toStringShort).collect(Collectors.joining(", ", "[", "]"));
        var disabledFlagsStr = disabledFlags.stream().map(Flag::toStringShort).collect(Collectors.joining(", ", "[", "]"));
        src.sendSuccess(() -> Component.literal("Enabled Flags: ").append(enabledFlagsStr), false);
        src.sendSuccess(() -> Component.literal("Disabled Flags: ").append(disabledFlagsStr), false);
        return SINGLE_SUCCESS;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> stateSubCommand(boolean state) {
        return literal(state ? "enable" : "disable")
                .then(argument("flag", ResourceLocationArgument.id())
                        .suggests(FlagCommand::suggestFlag)
                        .executes(ctx -> toggleFlag(ctx, state)));
    }

    private static CompletableFuture<Suggestions> suggestFlag(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        var flags = Flag.flags().map(Flag::toStringShort);
        return SharedSuggestionProvider.suggest(flags, builder);
    }

    private static int toggleFlag(CommandContext<CommandSourceStack> context, boolean state) throws CommandSyntaxException {
        var flag = Flag.of(ResourceLocationArgument.getId(context, "flag"));
        var src = context.getSource();
        var flagManager = src.getServer().getModdedFlagManager();
        // TODO: localize these
        var stateName = Component.literal(state ? "Enabled" : "Disabled");
        var changed = flagManager.set(flag, state);

        src.sendSuccess(() -> {
            if (changed)
                return stateName.append(" Flag: ").append(flag.toStringShort());
            else
                return Component.literal("Flag ").append(flag.toStringShort()).append(" is already ").append(stateName);
        }, false);

        return SINGLE_SUCCESS;
    }
}
