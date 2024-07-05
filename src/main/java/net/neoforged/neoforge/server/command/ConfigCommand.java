/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

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
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.StringUtils;
import net.neoforged.neoforge.client.command.ClientConfigCommand;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.internal.versions.neoforge.NeoForgeVersion;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME, modid = NeoForgeVersion.MOD_ID)
public class ConfigCommand {
    @SubscribeEvent
    public static void onClientCommandsRegister(RegisterClientCommandsEvent event) {
        ClientConfigCommand.register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("config").then(ShowFile.register()));
    }

    public enum ServerModConfigType {
        COMMON,
        SERVER;

        public String extension() {
            return StringUtils.toLowerCase(name());
        }
    }

    public static class ShowFile {
        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("showfile").requires(cs -> cs.hasPermission(0)).then(Commands.argument("mod", ModIdArgument.modIdArgument()).then(Commands.argument("type", EnumArgument.enumArgument(ServerModConfigType.class)).executes(ShowFile::showFile)));
        }

        private static int showFile(final CommandContext<CommandSourceStack> context) {
            final String modId = context.getArgument("mod", String.class);
            final ModConfig.Type type = ModConfig.Type.valueOf(context.getArgument("type", ServerModConfigType.class).toString()); // Convert it back to ModConfig to grab the configs
            final String configFileName = ConfigTracker.INSTANCE.getConfigFileName(modId, type);
            if (configFileName != null) {
                File f = new File(configFileName);
                MutableComponent fileComponent = Component.literal(f.getName()).withStyle(ChatFormatting.UNDERLINE);

                // Click action not allowed on dedicated servers or connected LAN players as neither cannot click a link to a file on the server/LAN owner.
                // Only provide click action for single player world owners calling this command from in-game.
                ServerPlayer caller = context.getSource().getPlayer();
                if (FMLLoader.getDist().isClient() && caller != null && caller.connection.getConnection().isMemoryConnection()) {
                    fileComponent.withStyle((style) -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, f.getAbsolutePath())));
                }

                context.getSource().sendSuccess(() -> Component.translatable("commands.config.getwithtype",
                        modId, type.toString(), fileComponent), true);
            } else {
                context.getSource().sendSuccess(() -> Component.translatable("commands.config.noconfig", modId, type.toString()),
                        true);
            }
            return 0;
        }
    }
}
