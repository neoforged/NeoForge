/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.capabilities.handlers.energy;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.transfer.handlers.templates.energy.EnergyBufferComponentHandler;
import net.neoforged.testframework.TestFramework;
import net.neoforged.testframework.annotation.OnInit;
import net.neoforged.testframework.annotation.TestGroup;
import net.neoforged.testframework.registration.DeferredItems;
import net.neoforged.testframework.registration.RegistrationHelper;

public class EnergyTestsSetup {
    @TestGroup(name = "Energy Handler Group", enabledByDefault = true)
    public static final String GROUP_ID = "handlers.energy";

    static final RegistrationHelper HELPER = RegistrationHelper.create("item_energy_tests");
    static final DeferredItems ITEMS = HELPER.items();
    static final int MAX_CAPACITY = 16384;

    static final DeferredRegister<DataComponentType<?>> COMPONENTS = HELPER.registrar(Registries.DATA_COMPONENT_TYPE);

    static final Supplier<DataComponentType<Integer>> ENERGY_COMPONENT = COMPONENTS.register("test_energy", () -> DataComponentType.<Integer>builder()
            .persistent(Codec.intRange(0, MAX_CAPACITY))
            .networkSynchronized(ByteBufCodecs.INT)
            .build());

    static final DeferredItem<Item> BATTERY = ITEMS.registerItem("test_battery", props -> new Item(props.component(ENERGY_COMPONENT, MAX_CAPACITY)));

    @OnInit
    static void init(final TestFramework framework) {
        COMPONENTS.register(framework.modEventBus());
        ITEMS.register(framework.modEventBus());
        framework.modEventBus().<RegisterCapabilitiesEvent>addListener(event -> {
            //noinspection DataFlowIssue context should never be null. We just can't mark NotNull due to immaculate
            event.registerItem(Capabilities.EnergyHandler.ITEM, EnergyBufferComponentHandler.builder(MAX_CAPACITY, ENERGY_COMPONENT)::build, BATTERY);
        });
    }
}
