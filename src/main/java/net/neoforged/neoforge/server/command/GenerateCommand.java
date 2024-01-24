/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.server.command.generation.GenerationBar;
import net.neoforged.neoforge.server.command.generation.GenerationTask;

/**
 * Special thanks to Jasmine and Gegy for allowing us to use their pregenerator mod as a model to use in NeoForge!
 * Original code: <a href="https://github.com/jaskarth/fabric-chunkpregenerator">https://github.com/jaskarth/fabric-chunkpregenerator</a>
 */
class GenerateCommand {
    private static GenerationTask activeTask;
    private static GenerationBar generationBar;

    static ArgumentBuilder<CommandSourceStack, ?> register() {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("generate").requires(cs -> cs.hasPermission(4)); //permission

        builder.then(Commands.literal("start")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(Commands.argument("chunkRadius", IntegerArgumentType.integer(1, 1250)) // 20000 block radius limit
                                .then(Commands.argument("progressBar", BoolArgumentType.bool())
                                        .executes(ctx -> executeGeneration(ctx.getSource(), BlockPosArgument.getSpawnablePos(ctx, "pos"), getInt(ctx, "chunkRadius"), getBool(ctx, "progressBar"))))
                                .executes(ctx -> executeGeneration(ctx.getSource(), BlockPosArgument.getSpawnablePos(ctx, "pos"), getInt(ctx, "chunkRadius"), true)))));

        builder.then(Commands.literal("stop")
                .executes(ctx -> stopGeneration(ctx.getSource())));

        builder.then(Commands.literal("status")
                .executes(ctx -> getGenerationStatus(ctx.getSource())));

        builder.then(Commands.literal("help")
                .executes(ctx -> getGenerationHelp(ctx.getSource())));

        return builder;
    }

    private static int getInt(CommandContext<CommandSourceStack> ctx, String name) {
        return IntegerArgumentType.getInteger(ctx, name);
    }

    private static boolean getBool(CommandContext<CommandSourceStack> ctx, String name) {
        return BoolArgumentType.getBool(ctx, name);
    }

    private static int executeGeneration(CommandSourceStack source, BlockPos pos, int chunkRadius, boolean progressBar) {
        if (activeTask != null) {
            source.sendSuccess(() -> Component.translatable("commands.neoforge.chunkgen.already_running"), true);
            return Command.SINGLE_SUCCESS;
        }

        ChunkPos origin = new ChunkPos(pos);

        activeTask = new GenerationTask(source.getLevel(), origin.x, origin.z, chunkRadius);
        int diameter = chunkRadius * 2 + 1;

        if (progressBar) {
            generationBar = new GenerationBar();

            if (source.getEntity() instanceof ServerPlayer) {
                generationBar.addPlayer(source.getPlayer());
            }
        }

        source.sendSuccess(() -> Component.translatable("commands.neoforge.chunkgen.started",
                activeTask.getTotalCount(), diameter, diameter, diameter * 16, diameter * 16), true);

        activeTask.run(createPregenListener(source));

        return Command.SINGLE_SUCCESS;
    }

    private static int stopGeneration(CommandSourceStack source) {
        if (activeTask != null) {
            activeTask.stop();

            int count = activeTask.getOkCount() + activeTask.getErrorCount() + activeTask.getSkippedCount();
            int total = activeTask.getTotalCount();

            double percent = (double) count / total * 100.0;
            source.sendSuccess(() -> Component.translatable("commands.neoforge.chunkgen.stopped", count, total, percent), true);

            generationBar.close();
            generationBar = null;
            activeTask = null;
        } else {
            source.sendSuccess(() -> Component.translatable("commands.neoforge.chunkgen.not_running"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int getGenerationStatus(CommandSourceStack source) {
        if (activeTask != null) {
            int count = activeTask.getOkCount() + activeTask.getErrorCount() + activeTask.getSkippedCount();
            int total = activeTask.getTotalCount();

            double percent = (double) count / total * 100.0;
            source.sendSuccess(() -> Component.translatable("commands.neoforge.chunkgen.status", count, total, percent), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.neoforge.chunkgen.not_running"), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int getGenerationHelp(CommandSourceStack source) {
        source.sendSuccess(() -> Component.translatable("commands.neoforge.chunkgen.help_line"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static GenerationTask.Listener createPregenListener(CommandSourceStack source) {
        return new GenerationTask.Listener() {
            @Override
            public void update(int ok, int error, int skipped, int total) {
                if (generationBar != null) {
                    generationBar.update(ok, error, skipped, total);
                }
            }

            @Override
            public void complete(int error) {
                source.sendSuccess(() -> Component.translatable("commands.neoforge.chunkgen.success"), true);

                if (error > 0) {
                    source.sendFailure(Component.translatable("commands.neoforge.chunkgen.error"));
                }

                if (generationBar != null) {
                    generationBar.close();
                    generationBar = null;
                }
                activeTask = null;
            }
        };
    }
}
