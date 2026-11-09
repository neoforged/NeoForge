/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.entity.living;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "entity.living.armadillo")
public class ArmadilloDamageTests {
    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Rolled-up armadillos must not expose negative incoming damage for small positive hits")
    static void smallHitsOnRolledArmadillos(final DynamicTest test) {
        final Set<UUID> targets = new HashSet<>();
        test.eventListeners().forge().addListener((final LivingIncomingDamageEvent event) -> {
            if (targets.contains(event.getEntity().getUUID())) {
                // Reapplying the current amount is a no-op for valid damage,
                // but reproduces the exception when vanilla produces a negative value.
                event.setAmount(event.getAmount());
            }
        });
        test.onGameTest(helper -> {
            final float[] hits = { 0.125F, 0.5F, 1.0F, 5.0F };
            final float[] reduced = { 0.0F, 0.0F, 0.0F, 2.0F };
            for (int i = 0; i < hits.length; i++) {
                for (boolean rolled : new boolean[] { false, true }) {
                    var armadillo = helper.spawnWithNoFreeWill(EntityType.ARMADILLO, 1, 2, 1);
                    targets.add(armadillo.getUUID());
                    if (rolled) armadillo.rollUp();
                    float before = armadillo.getHealth();
                    armadillo.hurtServer(helper.getLevel(), helper.getLevel().damageSources().generic(), hits[i]);
                    float expected = rolled ? reduced[i] : hits[i];
                    helper.assertTrue(Math.abs(armadillo.getHealth() - (before - expected)) < 0.0001F,
                            "Armadillo damage changed unexpectedly for rolled=" + rolled + ", damage=" + hits[i]);
                    armadillo.discard();
                }
            }
            helper.succeed();
        });
    }
}
