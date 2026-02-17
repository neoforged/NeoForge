/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.common.util;

import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.clock.ClockTimeMarker;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.clock.WorldClock;

/**
 * This models how the current time of a {@linkplain net.minecraft.world.clock.ClockManager clock} is to be adjusted.
 * <p>It's used in events to express the desired adjustment as a data model so other event listeners can inspect
 * and further change it.
 */
public sealed interface ClockAdjustment {
    void apply(ServerClockManager clockManager, Holder<WorldClock> clock);

    record Absolute(long ticks) implements ClockAdjustment {
        @Override
        public void apply(ServerClockManager clockManager, Holder<WorldClock> clock) {
            clockManager.setTotalTicks(clock, ticks);
        }
    }

    record Relative(long ticks) implements ClockAdjustment {
        @Override
        public void apply(ServerClockManager clockManager, Holder<WorldClock> clock) {
            var currentTicks = clockManager.getTotalTicks(clock);
            var newTicks = currentTicks + ticks;
            clockManager.setTotalTicks(clock, newTicks);
        }
    }

    record Marker(ResourceKey<ClockTimeMarker> marker) implements ClockAdjustment {
        public Marker {
            Objects.requireNonNull(marker);
        }

        @Override
        public void apply(ServerClockManager clockManager, Holder<WorldClock> clock) {
            clockManager.moveToTimeMarker(clock, marker);
        }
    }
}
