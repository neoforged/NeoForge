/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event.level;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.common.util.ClockAdjustment;

/**
 * This event is fired to adjust the clock of a level after sleep.
 */
public class SleepFinishedTimeEvent extends LevelEvent implements ICancellableEvent {
    private ClockAdjustment adjustment;

    public SleepFinishedTimeEvent(ServerLevel level, ClockAdjustment defaultAdjustment) {
        super(level);
        this.adjustment = defaultAdjustment;
    }

    /**
     * {@return the adjustment that will be made to the clock when the event is not canceled}
     */
    public ClockAdjustment getAdjustment() {
        return adjustment;
    }

    /**
     * Sets the new time which should be set when all players wake up
     *
     * @param adjustment The adjustment that should be made to the levels clock when this event isn't canceled.
     */
    public void setAdjustment(ClockAdjustment adjustment) {
        this.adjustment = adjustment;
    }
}
