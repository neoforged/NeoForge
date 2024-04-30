/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.oldtest.fluid;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.FluidUtil;

@Mod(PotionFluidTest.ID)
public final class PotionFluidTest {
    public static final String ID = "potion_fluid_test";
    public static final boolean ENABLE = false;

    public PotionFluidTest() {
        if (!ENABLE)
            return;

        NeoForgeMod.enablePotionFluid();

        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.RightClickItem.class, event -> {
            var stack = event.getItemStack();

            if (stack.has(DataComponents.POTION_CONTENTS))
                FluidUtil.getFluidContained(stack).ifPresent(fluid -> event.getEntity().displayClientMessage(Component.literal("Contains ").append(fluid.getHoverName()), true));
        });
    }
}
