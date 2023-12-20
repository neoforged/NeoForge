/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;

@ForEachTest(groups = { EntityTests.GROUP + ".event", "event" })
public class EntityEventTests {
    @GameTest
    @EmptyTemplate(value = "15x5x15", floor = true)
    @TestHolder(description = "Tests if the entity teleport event is fired")
    static void entityTeleportEvent(final DynamicTest test) {
        test.eventListeners().forge().addListener((final EntityTeleportEvent.ChorusFruit event) -> {
            if (event.getEntity() instanceof Pig) {
                event.setCanceled(true);
            } else if (event.getEntity() instanceof Cow cow) {
                event.setTargetZ(event.getPrevZ() + 3 * (cow.getRandom().nextInt(2) + 1));
                event.setTargetX(event.getPrevX() + 3.5 * cow.getRandom().nextInt(2));
            }
            test.pass();
        });

        test.onGameTest(helper -> helper.startSequence()
                .thenSequence(seq -> seq
                        .thenMap(() -> helper.spawnWithNoFreeWill(EntityType.PIG, 7, 2, 7))
                        .thenExecute(pig -> new ItemStack(Items.CHORUS_FRUIT).finishUsingItem(helper.getLevel(), pig))
                        .thenExecute(() -> helper.assertEntityPresent(EntityType.PIG, 7, 2, 7)))

                .thenIdle(10)

                .thenSequence(seq -> seq
                        .thenMap(() -> helper.spawnWithNoFreeWill(EntityType.COW, 7, 2, 7))
                        .thenExecute(cow -> new ItemStack(Items.CHORUS_FRUIT).finishUsingItem(helper.getLevel(), cow))
                        .thenExecute(() -> helper.assertEntityNotPresent(EntityType.COW, 7, 2, 7)))

                .thenIdle(10)
                .thenExecute(helper::killAllEntities)
                .thenSucceed());
    }
}
