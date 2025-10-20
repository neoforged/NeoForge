/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.unittest.transfer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Supplier;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EmptyEnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.ItemAccessEnergyHandler;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.junit.jupiter.api.Test;

class EnergyTests {
    @Test
    void testEmptyEnergyHandler() {
        try (Transaction transaction = Transaction.openRoot()) {
            ensureEmpty(EmptyEnergyHandler.INSTANCE, transaction);
        }
    }

    @Test
    void testSimpleEnergyHandler() {
        var simpleHandler = new SimpleEnergyHandler(100, 5, 7);
        assertEquals(0, simpleHandler.getAmountAsLong());

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(5, simpleHandler.insert(100, transaction));
            assertEquals(3, simpleHandler.insert(3, transaction));
            assertEquals(8, simpleHandler.getAmountAsLong());
            assertEquals(7, simpleHandler.extract(10, transaction));
            assertEquals(1, simpleHandler.getAmountAsLong());
        }

        assertEquals(0, simpleHandler.getAmountAsLong());

        try (Transaction transaction = Transaction.openRoot()) {
            assertEquals(5, simpleHandler.insert(100, transaction));
            assertEquals(3, simpleHandler.insert(3, transaction));
            assertEquals(8, simpleHandler.getAmountAsLong());
            assertEquals(7, simpleHandler.extract(10, transaction));
            assertEquals(1, simpleHandler.getAmountAsLong());

            transaction.commit();
        }

        assertEquals(1, simpleHandler.getAmountAsLong());
    }

    @Test
    void testItemAccessEnergyHandler() {
        // Starting items: 2 diamonds. Diamond will be considered the energy container here.
        var startingItems = NonNullList.of(ItemStack.EMPTY, new ItemStack(Items.DIAMOND, 2));
        var handler = new ItemStacksResourceHandler(startingItems);
        var itemAccess = ItemAccess.forHandlerIndex(handler, 0);

        // Create the energy handler
        var energyHandler = new ItemAccessEnergyHandler(itemAccess, TestMod.ENERGY.get(), 60, 50, 30);

        try (Transaction transaction = Transaction.openRoot()) {
            // Insertion of 200 should only insert 100 (50 per item).
            assertEquals(100, energyHandler.insert(200, transaction));
            assertEquals(50, itemAccess.getResource().get(TestMod.ENERGY));
            // Insertion of 200 should only insert 20 (10 per item) due to the capacity.
            assertEquals(20, energyHandler.insert(200, transaction));
            assertEquals(60, itemAccess.getResource().get(TestMod.ENERGY));
            // Extraction of 30 should extract 30 (15 per item).
            assertEquals(30, energyHandler.extract(30, transaction));
            assertEquals(45, itemAccess.getResource().get(TestMod.ENERGY));
            // Check amount and capacity.
            assertEquals(90, energyHandler.getAmountAsLong());
            assertEquals(120, energyHandler.getCapacityAsLong());

            // Now check that everything returns 0 if we change the item in the slot.
            handler.set(0, ItemResource.EMPTY, 0);
            ensureEmpty(energyHandler, transaction);
        }
    }

    private static void ensureEmpty(EnergyHandler handler, TransactionContext transaction) {
        assertEquals(0, handler.insert(Integer.MAX_VALUE, transaction));
        assertEquals(0, handler.extract(Integer.MAX_VALUE, transaction));
        assertEquals(0, handler.getAmountAsLong());
        assertEquals(0, handler.getCapacityAsLong());
    }

    @Mod(TestMod.MOD_ID)
    public static class TestMod {
        private static final String MOD_ID = "energy_test";
        private static final DeferredRegister.DataComponents components = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MOD_ID);
        private static final Supplier<DataComponentType<Integer>> ENERGY = components.registerComponentType("energy",
                b -> b.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));

        public TestMod(IEventBus modBus) {
            components.register(modBus);
        }
    }
}
