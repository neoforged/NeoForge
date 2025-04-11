/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.io.File;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.server.command.ModIdArgument;

public class ClientConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("config").then(ShowFile.register()));
    }

    public static class ShowFile {
        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("showfileclient").requires(cs -> cs.hasPermission(0)).then(Commands.argument("mod", ModIdArgument.modIdArgument()).executes(ShowFile::showFile));
        }

        private static int showFile(final CommandContext<CommandSourceStack> context) {
            final String modId = context.getArgument("mod", String.class);
            final ModConfig.Type type = ModConfig.Type.CLIENT;
            var configFileNames = ModConfigs.getConfigFileNames(modId, type);
            for (var configFileName : configFileNames) {
                File f = new File(configFileName);
                MutableComponent fileComponent = Component.literal(f.getName()).withStyle(ChatFormatting.UNDERLINE)
                        .withStyle((style) -> style.withClickEvent(new ClickEvent.OpenFile(f)));

                context.getSource().sendSuccess(() -> Component.translatable("commands.config.getwithtype",
                        modId, type.toString(), fileComponent), true);
            }
            if (configFileNames.isEmpty()) {
                context.getSource().sendSuccess(() -> Component.translatable("commands.config.noconfig", modId, type.toString()),
                        true);
            }
            return 0;
        }
    }
}
