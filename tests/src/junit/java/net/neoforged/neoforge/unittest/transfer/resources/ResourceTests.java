/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer.resources;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResourceTests {
    @Test
    void basicItemResourceChecks() {
        ItemResource apple = ItemResource.of(Items.APPLE);
        Assertions.assertNotNull(apple);
        Assertions.assertFalse(apple.isEmpty(), "Apples should not be empty");
        Assertions.assertTrue(apple.is(Items.APPLE), "The item apple and the resource apple should match");
        Assertions.assertTrue(apple.isComponentsPatchEmpty(), "We didn't modify the apples from default");
    }

    @Test
    void emptyPatchItemResourcesAreCached() {
        ItemResource apple1 = ItemResource.of(Items.APPLE);
        ItemResource apple2 = ItemResource.of(Items.APPLE);
        Assertions.assertSame(apple1, apple2);
    }

    @Test
    void patchedItemResourcesAreNotCached() {
        var patch = DataComponentPatch.builder()
                .set(DataComponents.MAX_STACK_SIZE, 10)
                .build();

        ItemResource apple1 = ItemResource.of(Items.APPLE, patch);
        ItemResource apple2 = ItemResource.of(Items.APPLE, patch);
        Assertions.assertNotSame(apple1, apple2);
    }

    @Test
    void emptyPatchFluidResourcesAreCached() {
        FluidResource lava1 = FluidResource.of(Fluids.LAVA);
        FluidResource lava2 = FluidResource.of(Fluids.LAVA);
        Assertions.assertSame(lava1, lava2);
    }

    @Test
    void patchedFluidResourcesAreNotCached() {
        var patch = DataComponentPatch.builder()
                .set(DataComponents.MAX_STACK_SIZE, 10)
                .build();

        FluidResource lava1 = FluidResource.of(Fluids.LAVA, patch);
        FluidResource lava2 = FluidResource.of(Fluids.LAVA, patch);
        Assertions.assertNotSame(lava1, lava2);
    }
}
