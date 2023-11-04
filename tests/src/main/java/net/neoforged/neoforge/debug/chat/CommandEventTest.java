/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.chat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.ParsedCommandNode;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.CommandEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("command_event_test")
@Mod.EventBusSubscriber
public class CommandEventTest {
    public final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getParseResults().getContext().getDispatcher();
        List<ParsedCommandNode<CommandSourceStack>> nodes = event.getParseResults().getContext().getNodes();
        CommandSourceStack source = event.getParseResults().getContext().getSource();

        // test: when the /time command is used with no arguments, automatically add default arguments (/time set day)
        if (nodes.size() == 1 && nodes.get(0).getNode() == dispatcher.getRoot().getChild("time")) {
            event.setParseResults(dispatcher.parse("time set day", source));
            return;
        }

        // test: whenever a player uses the /give command, let everyone on the server know
        if (nodes.size() > 0 && nodes.get(0).getNode() == dispatcher.getRoot().getChild("give")) {
            String msg = source.getTextName() + " used the give command: " + event.getParseResults().getReader().getString();
            source.getServer().getPlayerList().getPlayers().forEach(player -> player.sendSystemMessage(Component.literal(msg)));
            return;
        }

        // this is annoying so I disabled it
        // test: when the /kill command is used with no arguments, throw a custom exception
        // if (nodes.size() == 1 && nodes.get(0).getNode() == dispatcher.getRoot().getChild("kill"))
        // {
        //     event.setException(new CommandRuntimeException(new TextComponent("You tried to use the /kill command with no arguments")));
        //     event.setCanceled(true);
        //     return;
        // }
    }

}
