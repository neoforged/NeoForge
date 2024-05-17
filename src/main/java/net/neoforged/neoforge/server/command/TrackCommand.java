/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.server.timings.ObjectTimings;
import net.neoforged.neoforge.server.timings.TimeTracker;

class TrackCommand {
    private static final DecimalFormat TIME_FORMAT = new DecimalFormat("#####0.00");

    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("track")
                .then(StartTrackingCommand.register())
                .then(ResetTrackingCommand.register())
                .then(TrackResultsEntity.register())
                .then(TrackResultsBlockEntity.register())
                .then(StartTrackingCommand.register());
    }

    private static class StartTrackingCommand {
        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("start")
                    .requires(cs -> cs.hasPermission(2)) //permission
                    .then(Commands.literal("blockentity")
                            .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                    .executes(ctx -> {
                                        int duration = IntegerArgumentType.getInteger(ctx, "duration");
                                        TimeTracker.BLOCK_ENTITY_UPDATE.reset();
                                        TimeTracker.BLOCK_ENTITY_UPDATE.enable(duration);
                                        ctx.getSource().sendSuccess(() -> Component.translatable("commands.neoforge.tracking.be.enabled", duration), true);
                                        return 0;
                                    })))
                    .then(Commands.literal("entity")
                            .then(Commands.argument("duration", IntegerArgumentType.integer(1))
                                    .executes(ctx -> {
                                        int duration = IntegerArgumentType.getInteger(ctx, "duration");
                                        TimeTracker.ENTITY_UPDATE.reset();
                                        TimeTracker.ENTITY_UPDATE.enable(duration);
                                        ctx.getSource().sendSuccess(() -> Component.translatable("commands.neoforge.tracking.entity.enabled", duration), true);
                                        return 0;
                                    })));
        }
    }

    private static class ResetTrackingCommand {
        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("reset")
                    .requires(cs -> cs.hasPermission(2)) //permission
                    .then(Commands.literal("blockentity")
                            .executes(ctx -> {
                                TimeTracker.BLOCK_ENTITY_UPDATE.reset();
                                ctx.getSource().sendSuccess(() -> Component.translatable("commands.neoforge.tracking.be.reset"), true);
                                return 0;
                            }))
                    .then(Commands.literal("entity")
                            .executes(ctx -> {
                                TimeTracker.ENTITY_UPDATE.reset();
                                ctx.getSource().sendSuccess(() -> Component.translatable("commands.neoforge.tracking.entity.reset"), true);
                                return 0;
                            }));
        }
    }

    private static class TrackResults {
        /**
         * Returns the time objects recorded by the time tracker sorted by average time
         *
         * @return A list of time objects
         */
        private static <T> List<ObjectTimings<T>> getSortedTimings(TimeTracker<T> tracker) {
            ArrayList<ObjectTimings<T>> list = new ArrayList<>();

            list.addAll(tracker.getTimingData());
            list.sort(Comparator.comparingDouble(ObjectTimings::getAverageTimings));
            Collections.reverse(list);

            return list;
        }

        private static <T> int execute(CommandSourceStack source, TimeTracker<T> tracker, Function<ObjectTimings<T>, Component> toString) {
            List<ObjectTimings<T>> timingsList = getSortedTimings(tracker);
            if (timingsList.isEmpty()) {
                source.sendSuccess(() -> Component.translatable("commands.neoforge.tracking.no_data"), true);
            } else {
                timingsList.stream()
                        .filter(timings -> timings.getObject().get() != null)
                        .limit(10)
                        .forEach(timings -> source.sendSuccess(() -> toString.apply(timings), true));
            }
            return 0;
        }
    }

    private static class TrackResultsEntity {
        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("entity").executes(ctx -> TrackResults.execute(ctx.getSource(), TimeTracker.ENTITY_UPDATE, data -> {
                Entity entity = data.getObject().get();
                if (entity == null)
                    return Component.translatable("commands.neoforge.tracking.invalid");

                BlockPos pos = entity.blockPosition();
                double averageTimings = data.getAverageTimings();
                String tickTime = (averageTimings > 1000 ? TIME_FORMAT.format(averageTimings / 1000) : TIME_FORMAT.format(averageTimings)) + (averageTimings < 1000 ? "\u03bcs" : "ms");

                return Component.translatable("commands.neoforge.tracking.timing_entry", BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), entity.level().dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ(), tickTime);
            }));
        }
    }

    private static class TrackResultsBlockEntity {
        static ArgumentBuilder<CommandSourceStack, ?> register() {
            return Commands.literal("blockentity").executes(ctx -> TrackResults.execute(ctx.getSource(), TimeTracker.BLOCK_ENTITY_UPDATE, data -> {
                BlockEntity be = data.getObject().get();
                if (be == null)
                    return Component.translatable("commands.neoforge.tracking.invalid");

                BlockPos pos = be.getBlockPos();

                double averageTimings = data.getAverageTimings();
                String tickTime = (averageTimings > 1000 ? TIME_FORMAT.format(averageTimings / 1000) : TIME_FORMAT.format(averageTimings)) + (averageTimings < 1000 ? "\u03bcs" : "ms");
                return Component.translatable("commands.neoforge.tracking.timing_entry", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(be.getType()).toString(), be.getLevel().dimension().location().toString(), pos.getX(), pos.getY(), pos.getZ(), tickTime);
            }));
        }
    }
}
