/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.EnergyHandler;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.TransferAction;
import net.neoforged.neoforge.transfer.context.IItemContext;
import net.neoforged.neoforge.transfer.context.templates.PlayerContext;
import net.neoforged.neoforge.transfer.energy.IEnergyHandler;
import net.neoforged.neoforge.transfer.energy.templates.ItemEnergyStorage;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.DeferredItems;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = "capabilities.itemenergy")
public class ItemEnergyTests {
    public static final int MAX_CAPACITY = 16384;

    private static final RegistrationHelper HELPER = RegistrationHelper.create("item_energy_tests");

    private static final DeferredRegister<DataComponentType<?>> COMPONENTS = HELPER.registrar(Registries.DATA_COMPONENT_TYPE);
    private static final Supplier<DataComponentType<Integer>> ENERGY_COMPONENT = COMPONENTS.register("test_energy", () -> DataComponentType.<Integer>builder()
            .persistent(Codec.intRange(0, MAX_CAPACITY))
            .networkSynchronized(ByteBufCodecs.INT)
            .build());

    private static final DeferredItems ITEMS = HELPER.items();
    private static final DeferredItem<Item> BATTERY = ITEMS.register("test_battery", () -> new Item(new Item.Properties().component(ENERGY_COMPONENT, MAX_CAPACITY)));

    @OnInit
    static void init(final TestFramework framework) {
        COMPONENTS.register(framework.modEventBus());
        ITEMS.register(framework.modEventBus());
        framework.modEventBus().<RegisterCapabilitiesEvent>addListener(e -> {
            e.registerItem(EnergyHandler.ITEM, (stack, ctx) -> {
                return new ItemEnergyStorage(ctx, ENERGY_COMPONENT.get(), MAX_CAPACITY);
            }, BATTERY);
        });
    }

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Tests that ComponentEnergyStorage can read and write from a data component")
    public static void testItemEnergy(DynamicTest test, RegistrationHelper reg) {
        test.onGameTest(helper -> {
            ItemStack stack = BATTERY.toStack();
            Player player = helper.makeMockPlayer();
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            IItemContext context = PlayerContext.ofHand(player, InteractionHand.MAIN_HAND);
            IEnergyHandler energy = context.getCapability(EnergyHandler.ITEM);
            helper.assertValueEqual(energy.getAmount(), MAX_CAPACITY, "Default stored energy should be equal to the max capacity.");

            helper.assertValueEqual(energy.extract(MAX_CAPACITY, TransferAction.EXECUTE), MAX_CAPACITY, "Extracted energy should be equal to the target value.");
            helper.assertValueEqual(energy.getAmount(), 0, "Post-extraction energy stored should be zero.");

            helper.assertValueEqual(energy.insert(MAX_CAPACITY, TransferAction.EXECUTE), MAX_CAPACITY, "Received energy should be equal to the target value.");
            helper.assertValueEqual(energy.getAmount(), MAX_CAPACITY, "Post-insertion energy stored should be max capacity.");

            helper.succeed();
        });
    }
}
