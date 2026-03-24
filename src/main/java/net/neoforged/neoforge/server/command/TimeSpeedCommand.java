/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.gamerules.GameRules;

class TimeSpeedCommand {
    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("day")
                .then(Commands.literal("speed")
                        .then(Commands.literal("set").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)) // same as /gamerule
                                .then(Commands.literal("default").executes(context -> setDefault(context.getSource())))
                                .then(Commands.literal("realtime").executes(context -> setDaylength(context.getSource(), 1440)))
                                .then(Commands.argument("speed", FloatArgumentType.floatArg(0f, 1000f)).executes(context -> setSpeed(context.getSource(), FloatArgumentType.getFloat(context, "speed")))))
                        .executes(context -> query(context.getSource())))
                .then(Commands.literal("length")
                        .then(Commands.literal("set").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                                .then(Commands.literal("default").executes(context -> setDefault(context.getSource())))
                                .then(Commands.literal("realtime").executes(context -> setDaylength(context.getSource(), 1440)))
                                .then(Commands.argument("minutes", IntegerArgumentType.integer(1, 1440)).executes(context -> setDaylength(context.getSource(), IntegerArgumentType.getInteger(context, "minutes")))))
                        .executes(context -> query(context.getSource())))
                .executes(context -> query(context.getSource()));
    }

    private static int query(CommandSourceStack source) {
        var clockManager = source.getLevel().clockManager();
        var defaultClock = source.getLevel().dimensionType().defaultClock().orElse(null);
        if (defaultClock == null) {
            source.sendFailure(CommandUtils.makeTranslatableWithFallback("commands.neoforge.timespeed.query.no_default_clock", levelName(source)));
            return Command.SINGLE_SUCCESS;
        }

        final float speed = clockManager.getRate(defaultClock);
        if (speed == 1) {
            source.sendSuccess(() -> CommandUtils.makeTranslatableWithFallback("commands.neoforge.timespeed.query.default", levelName(source)), true);
        } else {
            source.sendSuccess(() -> CommandUtils.makeTranslatableWithFallback("commands.neoforge.timespeed.query", levelName(source), speed, minutes(speed)), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Component levelName(CommandSourceStack source) {
        return source.getLevel().getDescription();
    }

    private static float minutes(final float speed) {
        return (int) (200f / speed) / 10f;
    }

    private static int setSpeed(CommandSourceStack source, float speed) {
        var gameRules = source.getLevel().getGameRules();
        final var advanceTime = gameRules.get(GameRules.ADVANCE_TIME);
        if (!advanceTime && speed > 0) {
            gameRules.set(GameRules.ADVANCE_TIME, true, null);
            source.sendSuccess(() -> CommandUtils.makeTranslatableWithFallback("commands.gamerule.set", GameRules.ADVANCE_TIME.id(), gameRules.getAsString(GameRules.ADVANCE_TIME)), true);
        } else if (advanceTime && speed == 0) {
            gameRules.set(GameRules.ADVANCE_TIME, false, null);
            source.sendSuccess(() -> CommandUtils.makeTranslatableWithFallback("commands.gamerule.set", GameRules.ADVANCE_TIME.id(), gameRules.getAsString(GameRules.ADVANCE_TIME)), true);
            return Command.SINGLE_SUCCESS;
        }

        var clockManager = source.getLevel().clockManager();
        var defaultClock = source.getLevel().dimensionType().defaultClock().orElse(null);
        if (defaultClock == null) {
            source.sendFailure(CommandUtils.makeTranslatableWithFallback("commands.neoforge.timespeed.query.no_default_clock", levelName(source)));
            return Command.SINGLE_SUCCESS;
        }

        clockManager.setRate(defaultClock, speed);
        source.sendSuccess(() -> CommandUtils.makeTranslatableWithFallback("commands.neoforge.timespeed.set", levelName(source), speed, minutes(speed)), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int setDaylength(CommandSourceStack source, int minutes) {
        if (minutes == 20) {
            return setDefault(source);
        }
        return setSpeed(source, 20f / minutes);
    }

    private static int setDefault(CommandSourceStack source) {
        var clockManager = source.getLevel().clockManager();
        var defaultClock = source.getLevel().dimensionType().defaultClock().orElse(null);
        if (defaultClock == null) {
            source.sendFailure(CommandUtils.makeTranslatableWithFallback("commands.neoforge.timespeed.query.no_default_clock", levelName(source)));
            return Command.SINGLE_SUCCESS;
        }

        clockManager.setRate(defaultClock, 1);
        source.sendSuccess(() -> CommandUtils.makeTranslatableWithFallback("commands.neoforge.timespeed.set.default", levelName(source)), true);
        return Command.SINGLE_SUCCESS;
    }
}
