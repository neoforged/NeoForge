/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FluidNormalizationTest {
    @Test
    public void testFluidSourceNormalization() {
        Fluid flowingWater = Fluids.FLOWING_WATER;
        Fluid stillWater = Fluids.WATER;
        Fluid flowingLava = Fluids.FLOWING_LAVA;
        Fluid stillLava = Fluids.LAVA;
        Fluid empty = Fluids.EMPTY;

        Assertions.assertEquals(Fluids.WATER, flowingWater.getSource());
        Assertions.assertEquals(Fluids.WATER, stillWater.getSource());
        Assertions.assertEquals(Fluids.LAVA, flowingLava.getSource());
        Assertions.assertEquals(Fluids.LAVA, stillLava.getSource());

        Assertions.assertEquals(Fluids.FLOWING_WATER, flowingWater.getFlowing());
        Assertions.assertEquals(Fluids.FLOWING_WATER, stillWater.getFlowing());
        Assertions.assertEquals(Fluids.FLOWING_LAVA, flowingLava.getFlowing());
        Assertions.assertEquals(Fluids.FLOWING_LAVA, stillLava.getFlowing());

        Assertions.assertEquals(Fluids.EMPTY, empty.getSource());
        Assertions.assertNull(empty.getFlowing());
    }
}
