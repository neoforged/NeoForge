/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.debug;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.AlterGroundEvent;
import net.minecraftforge.event.level.AlterGroundEvent.StateProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod("alter_ground_event_test")
@Mod.EventBusSubscriber
public class AlterGroundEventTest
{
    public static final boolean ENABLE = true;

    @SubscribeEvent
    public static void onAlterGround(AlterGroundEvent event)
    {
        if (ENABLE)
        {
            StateProvider old = event.getStateProvider();
            event.setStateProvider((rand, pos) -> {
                BlockState state = old.getState(rand, pos);
                return state.is(Blocks.PODZOL) && rand.nextBoolean() ? Blocks.REDSTONE_BLOCK.defaultBlockState() : state;
            });
        }
    }
}