/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = { "level.effect.event", "event" })
public class MobEffectEventTests {
    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the MobEffectEvent.Applicable event can allow an effect the entity is immune to")
    static void mobEffectApplicableAllows(final DynamicTest test) {
        AtomicBoolean allow = new AtomicBoolean(false);
        AtomicReference<MobEffectEvent.Applicable> fired = new AtomicReference<>();
        test.eventListeners().forge().addListener((final MobEffectEvent.Applicable event) -> {
            if (event.getEntity() instanceof Spider && event.getEffectInstance().is(MobEffects.POISON)) {
                fired.set(event);
                if (allow.get()) {
                    event.setResult(MobEffectEvent.Applicable.Result.APPLY);
                }
            }
        });

        test.onGameTest(helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            Spider spider = helper.spawnWithNoFreeWill(EntityTypes.SPIDER, 1, 2, 1);

            // Spiders are immune to poison, which the default result keeps
            helper.assertFalse(spider.addEffect(new MobEffectInstance(MobEffects.POISON, 100), player), "Expected the spider to stay immune to poison with the default result");
            MobEffectEvent.Applicable event = fired.get();
            helper.assertNotNull(event, "Event should fire when an effect is added");
            helper.assertTrue(event.getEffectSource() == player, "Expected the player as effect source; source is " + event.getEffectSource());
            helper.assertValueEqual(event.getResult(), MobEffectEvent.Applicable.Result.DEFAULT, "Expected the default result; result is " + event.getResult());
            helper.assertFalse(spider.hasEffect(MobEffects.POISON), "Expected the spider not to be poisoned with the default result");

            allow.set(true);
            helper.assertTrue(spider.addEffect(new MobEffectInstance(MobEffects.POISON, 100), player), "Expected the poison to be applied when the event allows it");
            helper.assertTrue(spider.hasEffect(MobEffects.POISON), "Expected the spider to be poisoned when the event allows it");
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the MobEffectEvent.Applicable event can deny an effect")
    static void mobEffectApplicableDenies(final DynamicTest test) {
        test.eventListeners().forge().addListener((final MobEffectEvent.Applicable event) -> {
            if (event.getEntity() instanceof Pig && event.getEffectInstance().is(MobEffects.SPEED)) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        });

        test.onGameTest(helper -> {
            Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, 1, 2, 1);
            helper.assertFalse(pig.addEffect(new MobEffectInstance(MobEffects.SPEED, 100)), "Expected the speed effect to be denied by the event");
            helper.assertFalse(pig.hasEffect(MobEffects.SPEED), "Expected the pig not to have the speed effect");
            // Other effects are not affected
            helper.assertTrue(pig.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100)), "Expected regeneration to still be applicable");
            helper.assertTrue(pig.hasEffect(MobEffects.REGENERATION), "Expected the pig to have the regeneration effect");
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the MobEffectEvent.Added event fires with the previous instance when an effect is added, then upgraded")
    static void mobEffectAdded(final DynamicTest test) {
        AtomicReference<MobEffectEvent.Added> fired = new AtomicReference<>();
        test.eventListeners().forge().addListener((final MobEffectEvent.Added event) -> {
            if (event.getEntity() instanceof Pig && event.getEffectInstance().is(MobEffects.SPEED)) {
                fired.set(event);
            }
        });

        test.onGameTest(helper -> {
            Player player = helper.makeMockPlayer(GameType.SURVIVAL);
            Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, 1, 2, 1);

            MobEffectInstance first = new MobEffectInstance(MobEffects.SPEED, 100);
            pig.addEffect(first, player);
            MobEffectEvent.Added event = fired.get();
            helper.assertNotNull(event, "Event should fire when an effect is added");
            helper.assertTrue(event.getEffectInstance() == first, "Expected the added instance; instance is " + event.getEffectInstance());
            helper.assertTrue(event.getOldEffectInstance() == null, "Expected no previous instance; previous is " + event.getOldEffectInstance());
            helper.assertTrue(event.getEffectSource() == player, "Expected the player as effect source; source is " + event.getEffectSource());

            // Reset to capture the event of the upgrade
            fired.set(null);
            MobEffectInstance upgrade = new MobEffectInstance(MobEffects.SPEED, 200, 1);
            pig.addEffect(upgrade, player);
            event = fired.get();
            helper.assertNotNull(event, "Event should fire when an effect is upgraded");
            helper.assertTrue(event.getEffectInstance() == upgrade, "Expected the upgraded instance; instance is " + event.getEffectInstance());
            helper.assertTrue(event.getOldEffectInstance() == first, "Expected the previous instance; previous is " + event.getOldEffectInstance());
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the MobEffectEvent.Remove event fires for the removal of one effect and of all effects, and can prevent a removal")
    static void mobEffectRemove(final DynamicTest test) {
        AtomicBoolean cancel = new AtomicBoolean(true);
        List<MobEffectEvent.Remove> fired = new ArrayList<>();
        test.eventListeners().forge().addListener((final MobEffectEvent.Remove event) -> {
            if (event.getEntity() instanceof Pig) {
                fired.add(event);
                // Only the removal of the speed effect is prevented
                if (event.getEffectInstance().is(MobEffects.SPEED)) {
                    event.setCanceled(cancel.get());
                }
            }
        });

        test.onGameTest(helper -> {
            Pig pig = helper.spawnWithNoFreeWill(EntityTypes.PIG, 1, 2, 1);
            MobEffectInstance speed = new MobEffectInstance(MobEffects.SPEED, 100);
            pig.addEffect(speed);
            pig.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100));

            helper.assertFalse(pig.removeEffect(MobEffects.SPEED), "Expected the removal to be prevented while the event is cancelled");
            helper.assertValueEqual(fired.size(), 1, "Expected a single event for the removal of one effect; events are " + fired);
            helper.assertTrue(fired.get(0).getEffectInstance() == speed, "Expected the removed instance; instance is " + fired.get(0).getEffectInstance());
            helper.assertTrue(pig.hasEffect(MobEffects.SPEED), "Expected the pig to keep the speed effect");

            // Removing all effects fires the event for each of them, and only the cancelled one is kept
            fired.clear();
            pig.removeAllEffects();
            helper.assertValueEqual(fired.size(), 2, "Expected an event for each effect when removing all effects; events are " + fired);
            helper.assertTrue(fired.stream().anyMatch(event -> event.getEffectInstance().is(MobEffects.REGENERATION)), "Expected an event for the regeneration effect; events are " + fired);
            helper.assertTrue(pig.hasEffect(MobEffects.SPEED), "Expected the pig to keep the speed effect when removing all effects");
            helper.assertFalse(pig.hasEffect(MobEffects.REGENERATION), "Expected the regeneration effect to be removed with all effects");

            cancel.set(false);
            helper.assertTrue(pig.removeEffect(MobEffects.SPEED), "Expected the removal to succeed once the event is not cancelled");
            helper.assertFalse(pig.hasEffect(MobEffects.SPEED), "Expected the speed effect to be removed");
            helper.succeed();
        });
    }

    @GameTest
    @EmptyTemplate(floor = true)
    @TestHolder(description = "Tests if the MobEffectEvent.Expired event fires when an effect runs out, and can keep the effect")
    static void mobEffectExpired(final DynamicTest test) {
        AtomicBoolean cancel = new AtomicBoolean(true);
        AtomicInteger fireCount = new AtomicInteger();
        test.eventListeners().forge().addListener((final MobEffectEvent.Expired event) -> {
            if (event.getEntity() instanceof Pig && event.getEffectInstance().is(MobEffects.SPEED)) {
                fireCount.incrementAndGet();
                event.setCanceled(cancel.get());
            }
        });

        test.onGameTest(helper -> helper.startSequence(() -> helper.spawnWithNoFreeWill(EntityTypes.PIG, 1, 2, 1))
                .thenExecute(pig -> pig.addEffect(new MobEffectInstance(MobEffects.SPEED, 1)))
                // The effect runs out on the next tick of the pig, but the cancelled event keeps it
                .thenExecuteAfter(1, pig -> {
                    helper.assertValueEqual(fireCount.get(), 1, "Expected the event to fire once when the effect runs out; fired " + fireCount.get() + " times");
                    helper.assertTrue(pig.hasEffect(MobEffects.SPEED), "Expected the effect to be kept while the event is cancelled");
                })
                .thenExecute(() -> cancel.set(false))
                // The event fires again on each tick while the effect has no duration left, and the effect goes once it is not cancelled
                .thenExecuteAfter(1, pig -> helper.assertFalse(pig.hasEffect(MobEffects.SPEED), "Expected the effect to be removed once the event is not cancelled"))
                .thenSucceed());
    }
}
