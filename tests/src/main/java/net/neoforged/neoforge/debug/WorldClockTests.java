/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.debug;

import java.util.Set;
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
            event.createProvider(output -> new KeyTagProvider<>(output, Registries.WORLD_CLOCK, moddedProviders, modId) {
                @Override
                protected void addTags(HolderLookup.Provider registries) {
                    tag(Tags.WorldClocks.IGNORES_PAUSING).add(ignoresPause);
                    tag(Tags.WorldClocks.IGNORES_ADVANCE_TIME).add(ignoresAdvanceTime);
                }

                @Override
                public String getName() {
                    return "unpause-clock-tags";
                }
            });
        });

        test.onGameTest(helper -> {
            var originalTimeIP = helper.getLevel().clockManager().getTotalTicks(helper.getHolder(ignoresPause));
            var originalTimeAT = helper.getLevel().clockManager().getTotalTicks(helper.getHolder(ignoresAdvanceTime));

            helper.startSequence()
                    // validate clock still ticks when paused
                    .thenExecute(() -> assertHasTag(helper, ignoresPause, Tags.WorldClocks.IGNORES_PAUSING))
                    .thenExecute(() -> helper.getLevel().clockManager().setPaused(helper.getHolder(ignoresPause), true))
                    .thenIdle(2)
                    .thenExecute(() -> assertClockChanged(helper, ignoresPause, originalTimeIP, "paused"))
                    .thenExecute(() -> helper.getLevel().clockManager().setPaused(helper.getHolder(ignoresPause), false))

                    // validate clock still ticks when 'advance_time' is disabled
                    .thenExecute(() -> assertHasTag(helper, ignoresAdvanceTime, Tags.WorldClocks.IGNORES_ADVANCE_TIME))
                    .thenMap(() -> helper.getLevel().getGameRules().get(GameRules.ADVANCE_TIME))
                    .thenExecute(() -> helper.getLevel().getGameRules().set(GameRules.ADVANCE_TIME, false, helper.getLevel().getServer()))
                    .thenIdle(2)
                    .thenExecute(() -> assertClockChanged(helper, ignoresAdvanceTime, originalTimeAT, "advance_time"))
                    .thenExecute(advanceTime -> helper.getLevel().getGameRules().set(GameRules.ADVANCE_TIME, advanceTime, helper.getLevel().getServer()))

                    .thenSucceed();
        });
    }

    private static void assertHasTag(ExtendedGameTestHelper helper, ResourceKey<WorldClock> key, TagKey<WorldClock> tag) {
        helper.assertTrue(helper.getHolder(key).is(tag), "Expected clock '" + key.identifier() + "' to have tag '" + tag.location() + "'");
    }

    private static void assertClockChanged(ExtendedGameTestHelper helper, ResourceKey<WorldClock> key, long originalTime, String reason) {
        var actualTime = helper.getLevel().clockManager().getTotalTicks(helper.getHolder(key));
        helper.assertFalse(originalTime == actualTime, "Expected clock '" + key.identifier() + "' totalTicks to have changed (" + originalTime + " : " + actualTime + ") [" + reason + ']');
    }
}
