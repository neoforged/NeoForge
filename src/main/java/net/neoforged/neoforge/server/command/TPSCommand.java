/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.server.command;

import com.google.common.math.Stats;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.text.DecimalFormat;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.TickRateManager;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;

class TPSCommand {
    private static final DecimalFormat TIME_FORMATTER = new DecimalFormat("########0.000");
    private static final long[] UNLOADED = new long[] { 0 };

    static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("tps")
                .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .executes(ctx -> sendTime(ctx, DimensionArgument.getDimension(ctx, "dimension"))))
                .executes(ctx -> {
                    for (ServerLevel dimension : ctx.getSource().getServer().getAllLevels()) {
                        sendTime(ctx, dimension);
                    }

                    sendTime(ctx, null);
                    return Command.SINGLE_SUCCESS;
                });
    }

    private static int sendTime(CommandContext<CommandSourceStack> context, @Nullable ServerLevel dimension) throws CommandSyntaxException {
        var src = context.getSource();
        src.sendSuccess(() -> createComponent(src.getServer(), dimension), false);
        return Command.SINGLE_SUCCESS;
    }

    private static Component createComponent(MinecraftServer server, @Nullable ServerLevel dimension) {
        MutableComponent prefix;
        long[] times;
        TickRateManager tickRateManager;

        if (dimension == null) {
            prefix = Component.translatable("commands.neoforge.tps.summary.overall");
            times = server.getTickTimesNanos();
            tickRateManager = server.tickRateManager();
        } else {
            var dimKey = dimension.dimension();
            var dimensionTimes = server.getTickTime(dimKey);

            prefix = Component.translatable("commands.neoforge.tps.summary.dimension", createDimensionComponent(dimension));
            times = dimensionTimes == null ? UNLOADED : dimensionTimes;
            tickRateManager = dimension.tickRateManager();
        }

        var tickTime = Stats.meanOf(times) / TimeUtil.NANOSECONDS_PER_MILLISECOND;
        var tps = TimeUtil.MILLISECONDS_PER_SECOND / Math.max(tickTime, tickRateManager.millisecondsPerTick());
        var timeComponent = Component.empty().append(createTickTimeComponent(tickTime)).append(". ").append(createTpsComponent(tickRateManager, tps));
        return prefix.append(CommonComponents.SPACE).append(timeComponent);
    }

    private static Component createDimensionComponent(ServerLevel dimension) {
        var regName = dimension.dimension().location();
        var dimensionTypes = dimension.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE);
        var dimensionType = Objects.requireNonNull(dimensionTypes.getKey(dimension.dimensionType()));

        return Component.empty().append(dimension.getDescription()).withStyle(style -> style
                .withColor(ChatFormatting.GREEN)
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("commands.neoforge.tps.summary.dimension.tooltip", regName.toString(), dimensionType.toString()))));
    }

    private static Component createTickTimeComponent(double tickTime) {
        return Component.translatable("commands.neoforge.tps.mean_tick_time", Component.literal(TIME_FORMATTER.format(tickTime))
                .withColor(CommonColors.GRAY))
                .withStyle(style -> style.withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("commands.neoforge.tps.mean_tick_time.tooltip"))));
    }

    private static Component createTpsComponent(TickRateManager tickRateManager, double tps) {
        return Component.translatable("commands.neoforge.tps.mean_tps", Component.literal(TIME_FORMATTER.format(tps)).withColor(calculateTPSColor(tickRateManager, tps)))
                .withStyle(style -> style.withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("commands.neoforge.tps.mean_tps.tooltip"))));
    }

    private static int calculateTPSColor(TickRateManager tickRateManager, double tps) {
        // Improved color blending code thanks to sciwhiz12
        float maxTPS = TimeUtil.MILLISECONDS_PER_SECOND / tickRateManager.millisecondsPerTick();
        // 0 degrees (0F) is red, 120 degrees (0.33F) is green
        return Mth.hsvToRgb((float) (Mth.inverseLerp(tps, 0, maxTPS) * 0.33F), 1F, 1F);
    }
}
