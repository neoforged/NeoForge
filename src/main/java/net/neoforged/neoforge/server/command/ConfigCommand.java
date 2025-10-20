/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.io.File;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.fml.loading.FMLEnvironment;

public class ConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("config").then(ShowFile.register()));
    }

    public enum ServerModConfigType {
        COMMON,
        SERVER;

        public String extension() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static class ShowFile {
        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("showfile").requires(cs -> cs.hasPermission(0)).then(Commands.argument("mod", ModIdArgument.modIdArgument()).then(Commands.argument("type", EnumArgument.enumArgument(ServerModConfigType.class)).executes(ShowFile::showFile)));
        }

        private static int showFile(final CommandContext<CommandSourceStack> context) {
            final String modId = context.getArgument("mod", String.class);
            final ModConfig.Type type = ModConfig.Type.valueOf(context.getArgument("type", ServerModConfigType.class).toString()); // Convert it back to ModConfig to grab the configs
            var configFileNames = ModConfigs.getConfigFileNames(modId, type);
            for (var configFileName : configFileNames) {
                File f = new File(configFileName);
                MutableComponent fileComponent = Component.literal(f.getName()).withStyle(ChatFormatting.UNDERLINE);

                // Click action not allowed on dedicated servers or connected LAN players as neither cannot click a link to a file on the server/LAN owner.
                // Only provide click action for single player world owners calling this command from in-game.
                ServerPlayer caller = context.getSource().getPlayer();
                if (FMLEnvironment.getDist().isClient() && caller != null && caller.connection.getConnection().isMemoryConnection()) {
                    fileComponent.withStyle((style) -> style.withClickEvent(new ClickEvent.OpenFile(f)));
                }

                context.getSource().sendSuccess(() -> CommandUtils.makeTranslatableWithFallback("commands.config.getwithtype",
                        modId, type.toString(), fileComponent), true);
            }
            if (configFileNames.isEmpty()) {
                context.getSource().sendSuccess(() -> CommandUtils.makeTranslatableWithFallback("commands.config.noconfig", modId, type.toString()),
                        true);
            }
            return 0;
        }
    }
}
