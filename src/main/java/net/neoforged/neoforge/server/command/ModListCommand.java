/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import java.util.Locale;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModList;

class ModListCommand {
    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("mods")
                .requires(cs -> cs.hasPermission(0)) //permission
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.neoforge.mods.list",
                            ModList.get().applyForEachModFile(modFile ->
                    // locator - filename : firstmod (version) - numberofmods\n
                    String.format(Locale.ROOT, "%s %s : %s (%s) - %d",
                            modFile.getProvider().name().replace(' ', '_'),
                            modFile.getFileName(),
                            modFile.getModInfos().get(0).getModId(),
                            modFile.getModInfos().get(0).getVersion(),
                            modFile.getModInfos().size())).collect(Collectors.joining("\n\u2022 ", "\n\u2022 ", ""))),
                            false);
                    return 0;
                });
    }

}
