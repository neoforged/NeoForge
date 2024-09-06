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
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
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
        var enabledFlagsStr = enabledFlags.stream().map(Flag::toStringShort).collect(Collectors.joining(", ", "[", "]"));
        var disabledFlagsStr = disabledFlags.stream().map(Flag::toStringShort).collect(Collectors.joining(", ", "[", "]"));
        src.sendSuccess(() -> Component.translatable("commands.neoforge.flag.list_enabled", enabledFlagsStr), false);
        src.sendSuccess(() -> Component.translatable("commands.neoforge.flag.list_disabled", disabledFlagsStr), false);
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
        var stateKey = state ? "enabled" : "disabled";
        var changed = flagManager.set(flag, state);

        src.sendSuccess(() -> {
            if (changed) {
                return Component.translatable("commands.neoforge.flag." + stateKey, flag.toStringShort()).withStyle(style -> {
                    var notice = Component.translatable("commands.neoforge.flag.notice");
                    var hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, notice);
                    var clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/reload");
                    return style.withHoverEvent(hoverEvent).withClickEvent(clickEvent);
                });
            }

            return Component.translatable("commands.neoforge.flag.already_" + stateKey, flag.toStringShort());
        }, false);

        return SINGLE_SUCCESS;
    }
}
