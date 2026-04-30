/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.testframework.DynamicTest;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;
import net.neoforged.testframework.gametest.GameTest;

@ForEachTest(groups = "world_clock_tests")
public interface WorldClockTests {
    @GameTest
    @TestHolder(description = "Registeres a new world clock which should never be pausable", enabledByDefault = true)
    @EmptyTemplate
    static void unpauseClocksTest(DynamicTest test) {
        var modId = test.createModId();
        var ignoresPause = ResourceKey.create(Registries.WORLD_CLOCK, Identifier.fromNamespaceAndPath(modId, "ignores_pause"));
        var ignoresAdvanceTime = ResourceKey.create(Registries.WORLD_CLOCK, Identifier.fromNamespaceAndPath(modId, "ignores_advance_time"));

        test.eventListeners().mod().addListener((GatherDataEvent.Client event) -> {
            // register datapack entry generator to generate our world clock
            var moddedProviders = event.createProvider((output, lookupProvider) -> new DatapackBuiltinEntriesProvider(
                    output,
                    lookupProvider,
                    new RegistrySetBuilder().add(Registries.WORLD_CLOCK, registry -> {
                        registry.register(ignoresPause, new WorldClock());
                        registry.register(ignoresAdvanceTime, new WorldClock());
                    }),
                    Set.of(modId)) {
                @Override
                public String getName() {
                    return "unpause-clock-datapack-registries";
                }
            }).getRegistryProvider();

            // register tag generator to tag our new world clock as 'ignores_pausing'
            event.createProvider(output -> new KeyTagProvider<WorldClock>(output, Registries.WORLD_CLOCK, moddedProviders, modId) {
                @Override
                protected void addTags(HolderLookup.Provider registries) {
                    tag(Tags.WorldClocks.IGNORES_PAUSE_COMMAND).add(ignoresPause);
                    tag(Tags.WorldClocks.IGNORES_ADVANCE_TIME_RULE).add(ignoresAdvanceTime);
                }

                @Override
                public String getName() {
                    return "unpause-clock-tags";
                }
            });
        });

        test.onGameTest(helper -> helper.startSequence()
                // ensure `advance_time` game rule is enabled
                .thenExecute(() -> setAdvanceTime(helper, true))

                // 'ignores_pause' tests
                .thenSequence(s -> s
                        .thenMap(() -> new ClockAndData(helper, helper.getHolder(ignoresPause)))
                        // validate this clock has `ignores_pausing` but not `ignores_advance_time`
                        // we should only be bypassing the `paused` state
                        .thenExecute(data -> assertHasTag(helper, data.clock, Tags.WorldClocks.IGNORES_PAUSE_COMMAND))
                        .thenExecute(data -> assertDoesNotHaveTag(helper, data.clock, Tags.WorldClocks.IGNORES_ADVANCE_TIME_RULE))
                        // pause clock
                        .thenExecute(data -> setPaused(helper, data.clock, true))
                        // idle to allow clock to tick
                        .thenIdle(1)
                        // validate clock has ticked, we have `ignores_pausing` we should have ticked
                        .thenExecute(data -> assertClockTicked(helper, data.clock, data.totalTicks))
                        // unpause clock
                        .thenExecute(data -> setPaused(helper, data.clock, false))
                        // refresh clock data
                        .thenMap(data -> data.refresh(helper))
                        // disable `advance_time` game rule
                        .thenExecute(() -> setAdvanceTime(helper, false))
                        // idle to allow clock to tick
                        .thenIdle(1)
                        // validate clock did not tick, we do not have `ignores_advance_time` we should not have ticked
                        .thenExecute(clock -> assertClockDidNotTick(helper, clock.clock, clock.totalTicks))
                        // reenable `advance_time` game rule
                        .thenExecute(() -> setAdvanceTime(helper, true)))

                // 'ignores_advance_time' tests
                .thenSequence(s -> s
                        .thenMap(() -> new ClockAndData(helper, helper.getHolder(ignoresAdvanceTime)))
                        // validate this clock has `ignores_advance_time` but not `ignores_pausing`
                        // we should only be bypassing the `advance_time` game rule
                        .thenExecute(data -> assertHasTag(helper, data.clock, Tags.WorldClocks.IGNORES_ADVANCE_TIME_RULE))
                        .thenExecute(data -> assertDoesNotHaveTag(helper, data.clock, Tags.WorldClocks.IGNORES_PAUSE_COMMAND))
                        // pause clock
                        .thenExecute(data -> setPaused(helper, data.clock, true))
                        // idle to allow clock to tick
                        .thenIdle(1)
                        // validate clock did not tick, we do not have `ignores_pausing` we should not have ticked
                        .thenExecute(data -> assertClockDidNotTick(helper, data.clock, data.totalTicks))
                        // unpause clock
                        .thenExecute(data -> setPaused(helper, data.clock, false))
                        // refresh clock data
                        .thenMap(data -> data.refresh(helper))
                        // disable `advance_time` game rule
                        .thenExecute(() -> setAdvanceTime(helper, false))
                        // idle to allow clock to tick
                        .thenIdle(1)
                        // validate clock has ticked, we have `ignores_advance_time` we should have ticked
                        .thenExecute(clock -> assertClockTicked(helper, clock.clock, clock.totalTicks))
                        // reenable `advance_time` game rule
                        .thenExecute(() -> setAdvanceTime(helper, true)))

                .thenSucceed());
    }

    private static void assertHasTag(ExtendedGameTestHelper helper, Holder<WorldClock> clock, TagKey<WorldClock> tag) {
        helper.assertTrue(clock.is(tag), "Expected clock '" + clock.getRegisteredName() + "' to have tag '" + tag.location() + "'");
    }

    private static void assertDoesNotHaveTag(ExtendedGameTestHelper helper, Holder<WorldClock> clock, TagKey<WorldClock> tag) {
        helper.assertTrue(!clock.is(tag), "Expected clock '" + clock.getRegisteredName() + "' to not have tag '" + tag.location() + "'");
    }

    private static void assertClockTicked(ExtendedGameTestHelper helper, Holder<WorldClock> clock, long originalTotalTicks) {
        var actualTime = getTotalTicks(helper, clock);
        helper.assertTrue(originalTotalTicks != actualTime, "Expected clock '" + clock.getRegisteredName() + "' to have ticked (" + originalTotalTicks + " : " + actualTime + ")");
    }

    private static void assertClockDidNotTick(ExtendedGameTestHelper helper, Holder<WorldClock> clock, long originalTotalTicks) {
        var actualTime = getTotalTicks(helper, clock);
        helper.assertTrue(originalTotalTicks == actualTime, "Expected clock '" + clock.getRegisteredName() + "' to not have ticked (" + originalTotalTicks + " : " + actualTime + ")");
    }

    private static long getTotalTicks(ExtendedGameTestHelper helper, Holder<WorldClock> clock) {
        return helper.getLevel().clockManager().getTotalTicks(clock);
    }

    private static void setPaused(ExtendedGameTestHelper helper, Holder<WorldClock> clock, boolean paused) {
        helper.getLevel().clockManager().setPaused(clock, paused);
    }

    private static void setAdvanceTime(ExtendedGameTestHelper helper, boolean advanceTime) {
        helper.getLevel().getGameRules().set(GameRules.ADVANCE_TIME, advanceTime, helper.getLevel().getServer());
    }

    record ClockAndData(Holder<WorldClock> clock, long totalTicks) {
        private ClockAndData(ExtendedGameTestHelper helper, Holder<WorldClock> clock) {
            this(clock, getTotalTicks(helper, clock));
        }

        public ClockAndData refresh(ExtendedGameTestHelper helper) {
            return new ClockAndData(clock, getTotalTicks(helper, clock));
        }
    }
}
