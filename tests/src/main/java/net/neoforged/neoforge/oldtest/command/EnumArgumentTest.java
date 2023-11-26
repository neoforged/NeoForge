/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.server.command.EnumArgument;

@Mod("enum_argument_test")
public class EnumArgumentTest {
    public static final boolean ENABLE = false;

    public EnumArgumentTest() {
        if (ENABLE)
            NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("enumargumenttest")
                .then(Commands.argument("string", StringArgumentType.string())
                        .executes(context -> {
                            context.getSource().sendSuccess(() -> Component.literal("string: " + StringArgumentType.getString(context, "string")), false);

                            return 1;
                        }))
                .then(Commands.argument("enum", EnumArgument.enumArgument(ExampleEnum.class))
                        .executes(context -> {
                            context.getSource()
                                    .sendSuccess(() -> Component.literal("enum: " + context.getArgument("enum", ExampleEnum.class)), false);

                            return 1;
                        })));
    }

    public enum ExampleEnum {
        A, B
    }
}
