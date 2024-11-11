/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.vehicle;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.entity.vehicle.ChestRaft;
import net.minecraft.world.entity.vehicle.Raft;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.Test;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.registration.RegistrationHelper;

@ForEachTest(groups = CustomBoatTest.GROUP)
public class CustomBoatTest {
    public static final String GROUP = "level.entity.vehicle.boat";
    private static Supplier<BoatItem> paperBoatItem;
    private static Supplier<BoatItem> paperChestBoatItem;
    private static Supplier<BoatItem> paperRaftItem;
    private static Supplier<BoatItem> paperChestRaftItem;

    @EmptyTemplate
    @TestHolder(description = "Tests that custom boat types work")
    static void customBoatType(final DynamicTest test, final RegistrationHelper reg) {
        Supplier<EntityType<Boat>> paperBoat = reg.entityTypes().registerType("paper_boat", () -> EntityType.Builder.<Boat>of((type, level) -> new Boat(type, level, (Supplier<Item>) () -> paperBoatItem.get()), MobCategory.MISC));
        Supplier<EntityType<ChestBoat>> paperChestBoat = reg.entityTypes().registerType("paper_chest_boat", () -> EntityType.Builder.<ChestBoat>of((type, level) -> new ChestBoat(type, level, (Supplier<Item>) () -> paperChestBoatItem.get()), MobCategory.MISC));
        Supplier<EntityType<Raft>> paperRaft = reg.entityTypes().registerType("paper_raft", () -> EntityType.Builder.<Raft>of((type, level) -> new Raft(type, level, (Supplier<Item>) () -> paperRaftItem.get()), MobCategory.MISC));
        Supplier<EntityType<ChestRaft>> paperChestRaft = reg.entityTypes().registerType("paper_chest_raft", () -> EntityType.Builder.<ChestRaft>of((type, level) -> new ChestRaft(type, level, (Supplier<Item>) () -> paperChestRaftItem.get()), MobCategory.MISC));

        paperBoatItem = reg.items().registerItem("paper_boat", props -> new BoatItem(paperBoat.get(), props))
                .withLang("Paper Boat");
        paperChestBoatItem = reg.items().registerItem("paper_chest_boat", props -> new BoatItem(paperChestBoat.get(), props))
                .withLang("Paper Chest Boat");

        paperRaftItem = reg.items().registerItem("paper_raft", props -> new BoatItem(paperRaft.get(), props))
                .withLang("Paper Raft");
        paperChestRaftItem = reg.items().registerItem("paper_chest_raft", props -> new BoatItem(paperChestRaft.get(), props))
                .withLang("Paper Chest Raft");

        test.updateStatus(Test.Status.PASSED, null);
    }
}
