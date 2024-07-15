/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.vehicle;

import java.util.function.Supplier;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = CustomBoatTest.GROUP)
public class CustomBoatTest {
    public static final String GROUP = "level.entity.vehicle.boat";
    private static final String BOAT_NAME = "neotests:paper";
    private static final String RAFT_NAME = "neotests:paper_raft";
    private static Supplier<Block> paperBlock;
    private static Supplier<Item> paperStickItem;
    private static Supplier<BoatItem> paperBoatItem;
    private static Supplier<BoatItem> paperChestBoatItem;
    private static Supplier<BoatItem> paperRaftItem;
    private static Supplier<BoatItem> paperChestRaftItem;

    @EmptyTemplate
    @TestHolder(description = "Tests that custom boat types work")
    static void customBoatType(final DynamicTest test, final RegistrationHelper reg) {
        paperBlock = reg.blocks().registerSimpleBlock("paper", BlockBehaviour.Properties.of()).withBlockItem().withLang("Paper");
        paperStickItem = reg.items().registerSimpleItem("paper_stick").withLang("Paper Stick");

        paperBoatItem = reg.items().registerItem("paper_boat", props -> new BoatItem(false, Boat.Type.byName(BOAT_NAME), props))
                .withLang("Paper Boat");
        paperChestBoatItem = reg.items().registerItem("paper_chest_boat", props -> new BoatItem(true, Boat.Type.byName(BOAT_NAME), props))
                .withLang("Paper Chest Boat");

        paperRaftItem = reg.items().registerItem("paper_raft", props -> new BoatItem(false, Boat.Type.byName(RAFT_NAME), props))
                .withLang("Paper Raft");
        paperChestRaftItem = reg.items().registerItem("paper_chest_raft", props -> new BoatItem(true, Boat.Type.byName(RAFT_NAME), props))
                .withLang("Paper Chest Raft");

        test.updateStatus(Test.Status.PASSED, null);
    }

    @SuppressWarnings("unused") // Referenced by enumextensions.json
    public static Object getBoatTypeParameter(int idx, Class<?> type) {
        return switch (idx) {
            case 0 -> type.cast((Supplier<Block>) () -> paperBlock.get());
            case 1 -> type.cast(BOAT_NAME);
            case 2 -> type.cast((Supplier<Item>) () -> paperBoatItem.get());
            case 3 -> type.cast((Supplier<Item>) () -> paperChestBoatItem.get());
            case 4 -> type.cast((Supplier<Item>) () -> paperStickItem.get());
            case 5 -> false;
            default -> throw new IllegalArgumentException("Unexpected parameter index: " + idx);
        };
    }

    @SuppressWarnings("unused") // Referenced by enumextensions.json
    public static Object getRaftTypeParameter(int idx, Class<?> type) {
        return switch (idx) {
            case 0 -> type.cast((Supplier<Block>) () -> paperBlock.get());
            case 1 -> type.cast(RAFT_NAME);
            case 2 -> type.cast((Supplier<Item>) () -> paperRaftItem.get());
            case 3 -> type.cast((Supplier<Item>) () -> paperChestRaftItem.get());
            case 4 -> type.cast((Supplier<Item>) () -> paperStickItem.get());
            case 5 -> true;
            default -> throw new IllegalArgumentException("Unexpected parameter index: " + idx);
        };
    }
}
