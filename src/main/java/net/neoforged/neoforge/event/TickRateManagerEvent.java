/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.event;

import net.minecraft.world.TickRateManager;
import net.neoforged.bus.api.Event;

public abstract sealed class TickRateManagerEvent extends Event {
    private final TickRateManager tickRateManager;

    protected TickRateManagerEvent(TickRateManager tickRateManager) {
        this.tickRateManager = tickRateManager;
    }

    public TickRateManager tickRateManager() {
        return this.tickRateManager;
    }

    public static final class Updated extends TickRateManagerEvent {
        public Updated(TickRateManager tickRateManager) {
            super(tickRateManager);
        }

        public float tickRate() {
            return this.tickRateManager().tickrate();
        }
    }

    public static final class Freeze extends TickRateManagerEvent {
        public Freeze(TickRateManager tickRateManager) {
            super(tickRateManager);
        }

        public boolean isFrozen() {
            return this.tickRateManager().isFrozen();
        }
    }

    public static final class Step extends TickRateManagerEvent {
        public Step(TickRateManager tickRateManager) {
            super(tickRateManager);
        }

        public int frozenTicksToRun() {
            return this.tickRateManager().frozenTicksToRun();
        }
    }
}
