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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface FlagsCommand {
    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return literal("flags")
                .then(literal("list").executes(context -> {
                    listFlagsFor(context, true);
                    listFlagsFor(context, false);
                    return SINGLE_SUCCESS;
                }))
                .then(literal("enabled").executes(context -> listFlagsFor(context, true)))
                .then(literal("disabled").executes(context -> listFlagsFor(context, false)))
                .then(literal("set")
                        .then(argument("flag", ResourceLocationArgument.id())
                                .suggests((context, builder) -> {
                                    var flags = Flag.flags().map(Flag::toStringShort);
                                    return SharedSuggestionProvider.suggest(flags, builder);
                                })
                                .then(literal("enabled").executes(context -> setFlagState(context, true)))
                                .then(literal("disabled").executes(context -> setFlagState(context, false)))));
    }

    private static int setFlagState(CommandContext<CommandSourceStack> context, boolean state) throws CommandSyntaxException {
        var flag = Flag.of(ResourceLocationArgument.getId(context, "flag"));
        var src = context.getSource();
        var flagManager = src.getServer().getModdedFlagManager();
        var stateKey = state ? "enabled" : "disabled";
        var changed = flagManager.set(flag, state);

        src.sendSuccess(() -> {
            if (changed) {
                return Component.translatable("commands.neoforge.flags." + stateKey, flag.toStringShort()).withStyle(style -> {
                    var notice = Component.translatable("commands.neoforge.flags.notice");
                    var hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, notice);
                    var clickEvent = new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/reload");
                    return style.withHoverEvent(hoverEvent).withClickEvent(clickEvent);
                });
            }

            return Component.translatable("commands.neoforge.flags.already_" + stateKey, flag.toStringShort());
        }, false);

        return SINGLE_SUCCESS;
    }

    private static int listFlagsFor(CommandContext<CommandSourceStack> context, boolean state) {
        var src = context.getSource();
        var stateStr = state ? "enabled" : "disabled";
        var flagManager = src.getServer().getModdedFlagManager();
        Set<Flag> flags = flagManager.getEnabledFlags();

        if (!state) {
            var knownFlags = Flag.getFlags();
            flags = Sets.difference(knownFlags, flags);
        }

        var flagsStr = flags.stream().map(Flag::toStringShort).collect(Collectors.joining(", ", "[", "]"));
        src.sendSuccess(() -> Component.translatable("commands.neoforge.flags.list_" + stateStr, flagsStr), false);
        return SINGLE_SUCCESS;
    }
}
