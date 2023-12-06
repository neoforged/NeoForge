/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.chat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
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

        // test: when the /attribute command is used with no arguments, automatically give effect
        if (nodes.size() == 1 && nodes.get(0).getNode() == dispatcher.getRoot().getChild("attribute")) {
            event.setParseResults(dispatcher.parse("effect give @s minecraft:blindness 10 2", source));
        }

        // test: when the /effect command is used with no arguments, throw a custom exception
        if (nodes.size() == 1 && nodes.get(0).getNode() == dispatcher.getRoot().getChild("effect")) {
            event.setException(new SimpleCommandExceptionType(Component.literal("You tried to use the /effect command with no arguments")).create());
            event.setCanceled(true);
        }
    }

}
