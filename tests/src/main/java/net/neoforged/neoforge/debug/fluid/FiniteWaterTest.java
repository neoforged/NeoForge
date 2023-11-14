/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.fluid;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.level.BlockEvent;

@Mod("finite_water_test")
@Mod.EventBusSubscriber()
public class FiniteWaterTest {
    private static final boolean ENABLED = false;

    @SubscribeEvent
    public static void handleFiniteWaterSource(BlockEvent.CreateFluidSourceEvent event) {
        if (ENABLED) {
            BlockState state = event.getState();
            FluidState fluidState = state.getFluidState();
            if (fluidState.getType().isSame(Fluids.WATER)) {
                event.setResult(Event.Result.DENY);
            } else if (fluidState.getType().isSame(Fluids.LAVA)) {
                event.setResult(Event.Result.ALLOW);
            }
        }
    }
}
