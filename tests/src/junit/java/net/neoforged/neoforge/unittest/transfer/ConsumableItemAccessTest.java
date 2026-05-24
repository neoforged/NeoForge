/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.ItemAccessFluidHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

public class ConsumableItemAccessTest {
    @Test
    void testConsumableFluidAccessItem() {
        FluidResource water = FluidResource.of(Fluids.WATER);
        ItemStack containerItem = new ItemStack(Items.DIAMOND);
        containerItem.set(TestMod.STORED_FLUID, SimpleFluidContent.copyOf(new FluidStack(Fluids.WATER, 1000)));

        ConsumableFluidHandler handler = new ConsumableFluidHandler(ItemAccess.forStack(containerItem));

        try (Transaction transaction = Transaction.openRoot()) {
            handler.insert(water, 100, transaction);
            transaction.commit();
        }
        assertFalse(containerItem.isEmpty(), "fluid container item should not be empty");

        try (Transaction transaction = Transaction.openRoot()) {
            handler.extract(water, 1100, transaction);
            transaction.commit();
        }
        assertTrue(containerItem.isEmpty(), "fluid container item should be empty");
    }

    private static class ConsumableFluidHandler extends ItemAccessFluidHandler {
        public ConsumableFluidHandler(ItemAccess itemAccess) {
            super(itemAccess, TestMod.STORED_FLUID.get(), 1000);
        }

        @Override
        protected @Nullable ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
            return newResource.isEmpty() || newAmount == 0 ? null : super.update(accessResource, index, newResource, newAmount);
        }
    }

    @Mod(TestMod.MOD_ID)
    public static class TestMod {
        public static final String MOD_ID = "consumable_item_access_test";
        private static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
        public static final Supplier<DataComponentType<SimpleFluidContent>> STORED_FLUID = COMPONENTS.registerComponentType("stored_fluid", builder -> builder
                .persistent(SimpleFluidContent.CODEC)
                .networkSynchronized(SimpleFluidContent.STREAM_CODEC));

        public TestMod(IEventBus modBus) {
            COMPONENTS.register(modBus);
        }
    }
}
