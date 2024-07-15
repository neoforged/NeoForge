/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.flag.FlagCommand;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class NeoForgeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        var cmd = LiteralArgumentBuilder.<CommandSourceStack>literal("neoforge");

        cmd.then(TPSCommand.register());
        cmd.then(TrackCommand.register());
        cmd.then(EntityCommand.register());
        cmd.then(GenerateCommand.register());
        cmd.then(DimensionsCommand.register());
        cmd.then(ModListCommand.register());
        cmd.then(TagsCommand.register());
        cmd.then(DumpCommand.register());
        cmd.then(TimeSpeedCommand.register());

        if (!FMLEnvironment.production)
            cmd.then(FlagCommand.register());

        dispatcher.register(cmd);
    }
}
