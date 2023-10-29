/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.DimensionType;

class DimensionsCommand {
    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("dimensions")
                .requires(cs -> cs.hasPermission(0)) //permission
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.neoforge.dimensions.list"), true);
                    final Registry<DimensionType> reg = ctx.getSource().registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);

                    Map<ResourceLocation, List<ResourceLocation>> types = new HashMap<>();
                    for (ServerLevel dim : ctx.getSource().getServer().getAllLevels()) {
                        types.computeIfAbsent(reg.getKey(dim.dimensionType()), k -> new ArrayList<>()).add(dim.dimension().location());
                    }

                    types.keySet().stream().sorted().forEach(key -> {
                        ctx.getSource().sendSuccess(() -> Component.literal(key + ": " + types.get(key).stream().map(ResourceLocation::toString).sorted().collect(Collectors.joining(", "))), false);
                    });
                    return 0;
                });
    }
}
