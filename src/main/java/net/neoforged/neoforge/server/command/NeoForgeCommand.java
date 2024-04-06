/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NeoForgeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                LiteralArgumentBuilder.<CommandSourceStack>literal("neoforge")
                        .then(TPSCommand.register())
                        .then(TrackCommand.register())
                        .then(EntityCommand.register())
                        .then(GenerateCommand.register())
                        .then(DimensionsCommand.register())
                        .then(ModListCommand.register())
                        .then(TagsCommand.register())
                        .then(RegistryDumpCommand.register()));
    }
}
